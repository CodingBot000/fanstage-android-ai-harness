package dev.fanstage.android.feature.live.chat

import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.Call
import okhttp3.Callback
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Screen-owned WebSocket and HTTP adapter. Tokens never enter logs or saved state. */
class RealChatTransport(
    baseUrl: String,
    private val token: String,
    allowInsecure: Boolean = false,
    private val initialReconnectDelayMillis: Long = 500,
) : ChatTransport {
    private class AuthenticationFailure : ChatConnectionFailure()
    private data class Attempt(val socket: WebSocket, val deferred: CompletableDeferred<Unit>)
    private val base: HttpUrl = baseUrl.toHttpUrl().also {
        require(it.username.isEmpty() && it.password.isEmpty() && it.query == null && it.fragment == null)
        require(it.scheme == "https" || (allowInsecure && it.scheme == "http"))
        require(it.encodedPath == "/")
        require(token.isNotBlank())
        require(initialReconnectDelayMillis in 1..30_000)
    }
    private val client = OkHttpClient.Builder().callTimeout(10, TimeUnit.SECONDS).build()
    private val mutableConnection = MutableStateFlow(ChatConnectionStatus.CONNECTING)
    override val connectionState = mutableConnection.asStateFlow()
    private val epoch = AtomicLong()
    private val manualReconnect = Channel<Unit>(Channel.CONFLATED)
    private val pending = ConcurrentHashMap<String, Attempt>()
    @Volatile private var active: WebSocket? = null
    @Volatile private var connecting: WebSocket? = null
    @Volatile private var activeRoom: String? = null
    @Volatile private var stopped = false

    override fun events(roomId: String): Flow<ChatEvent> = flow {
        mutableConnection.value = ChatConnectionStatus.CONNECTING
        require(roomId.matches(Regex("[a-z0-9][a-z0-9-]{0,63}")))
        var backoff = initialReconnectDelayMillis
        var reconnects = 0
        while (currentCoroutineContext().isActive && !stopped) {
            val openedAt = AtomicLong(0)
            try {
                connect(roomId, openedAt).collect { emit(it) }
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (auth: AuthenticationFailure) { throw auth }
            catch (_: IOException) { /* Preserve pending as uncertain and resubscribe. */ }
            if (stopped) break
            // A stable connection resets the consecutive-failure budget; brief
            // connect/close loops cannot reset it indefinitely.
            if (openedAt.get() != 0L && System.nanoTime() - openedAt.get() >= TimeUnit.SECONDS.toNanos(30)) {
                reconnects = 0
                backoff = initialReconnectDelayMillis
            }
            if (reconnects >= 6) {
                mutableConnection.value = ChatConnectionStatus.DISCONNECTED
                if (manualReconnect.receiveCatching().isClosed || stopped) break
                mutableConnection.value = ChatConnectionStatus.CONNECTING
                reconnects = 0
                backoff = initialReconnectDelayMillis
                continue
            }
            delay(backoff)
            reconnects++
            backoff = (backoff * 2).coerceAtMost(30_000L)
        }
    }

    override fun retryConnection() { if (!stopped && mutableConnection.value == ChatConnectionStatus.DISCONNECTED) manualReconnect.trySend(Unit) }

    private fun connect(roomId: String, openedAt: AtomicLong): Flow<ChatEvent> = callbackFlow {
        val generation = epoch.incrementAndGet()
        val request = Request.Builder().url(base.newBuilder().addPathSegments("ws/rooms/$roomId").build())
            .header("Authorization", "Bearer $token").build()
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                if (stopped || epoch.get() != generation) { webSocket.cancel(); return }
                openedAt.set(System.nanoTime())
                activeRoom = roomId
                active = webSocket
                mutableConnection.value = ChatConnectionStatus.CONNECTED
            }
            override fun onMessage(webSocket: WebSocket, text: String) {
                if (stopped || epoch.get() != generation) return
                if (text.length > 8_192) { channel.close(IOException("Frame too large")); webSocket.cancel(); return }
                try {
                    val packet = JSONObject(text)
                    when (packet.getString("type")) {
                        "subscribed" -> {
                            val head = packet.getLong("headSequence")
                            if (head < 0) throw IOException("Invalid head")
                            // The server registers the live subscription before obtaining this head.
                            // Live frames and the bounded HTTP snapshot may interleave; the VM deduplicates by sequence.
                            launch(Dispatchers.IO) {
                                try {
                                    var after = 0L
                                    while (after < head) {
                                        val page = fetchAfter(roomId, after, head, ChatLimits.RETAINED)
                                        if (page.isEmpty() || page.first().serverSequence != after + 1) throw IOException("Snapshot gap")
                                        for (m in page) {
                                            if (m.serverSequence != after + 1) throw IOException("Snapshot gap")
                                            send(ChatEvent.Message(m)); after = m.serverSequence
                                        }
                                    }
                                } catch (cancelled: CancellationException) { throw cancelled }
                                catch (e: Exception) { channel.close(IOException("Snapshot failed", e)); webSocket.cancel() }
                            }
                        }
                        "ack", "message" -> {
                            val message = parseMessage(packet.getJSONObject("message"), roomId)
                            val event = if (packet.getString("type") == "ack") ChatEvent.Ack(message) else ChatEvent.Message(message)
                            if (trySendBlocking(event).isFailure) { webSocket.cancel(); return }
                            if (event is ChatEvent.Ack && message.clientMessageId != null) pending.remove(message.clientMessageId)?.deferred?.complete(Unit)
                        }
                        "error" -> {
                            val id = packet.optString("clientMessageId")
                            val attempt = pending.remove(id)
                            if (attempt != null) {
                                if (packet.optString("code") in setOf("invalid", "conflict")) attempt.deferred.completeExceptionally(SendRejected())
                                else attempt.deferred.completeExceptionally(IOException("Server outcome unknown"))
                            }
                        }
                        else -> throw IOException("Unknown packet")
                    }
                } catch (e: Exception) { channel.close(IOException("Invalid packet", e)); webSocket.cancel() }
            }
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                if (!stopped && epoch.get() == generation) mutableConnection.value = ChatConnectionStatus.RECONNECTING
                webSocket.close(code, null)
            }
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                if (!stopped && epoch.get() == generation) mutableConnection.value = ChatConnectionStatus.RECONNECTING
                channel.close()
            }
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                if (!stopped && epoch.get() == generation) {
                    mutableConnection.value = if (response?.code == 401) ChatConnectionStatus.DISCONNECTED else ChatConnectionStatus.RECONNECTING
                }
                channel.close(if (response?.code == 401) AuthenticationFailure() else IOException("Connection lost", t))
            }
        }
        val socket = client.newWebSocket(request, listener)
        connecting = socket
        if (stopped || epoch.get() != generation) socket.cancel()
        awaitClose {
            if (connecting === socket) connecting = null
            if (active === socket) { active = null; activeRoom = null }
            pending.entries.removeIf { (_, attempt) ->
                if (attempt.socket === socket) { attempt.deferred.completeExceptionally(IOException("Connection lost")); true } else false
            }
            socket.cancel()
        }
    }.buffer(0)

    override suspend fun send(roomId: String, clientMessageId: String, text: String) {
        val socket = active ?: throw IOException("Not connected")
        if (activeRoom != roomId) throw IOException("Room changed")
        val deferred = CompletableDeferred<Unit>()
        val attempt = Attempt(socket, deferred)
        if (pending.putIfAbsent(clientMessageId, attempt) != null) throw IOException("Send already pending")
        try {
            val packet = JSONObject().put("type", "send").put("clientMessageId", clientMessageId).put("text", text)
            if (!socket.send(packet.toString())) throw IOException("Socket unavailable")
            if (withTimeoutOrNull(10_000L) { deferred.await(); true } != true) throw IOException("ACK timeout")
        } finally { pending.remove(clientMessageId, attempt) }
    }

    override suspend fun fetchAfter(roomId: String, afterSequence: Long, upperSequence: Long, limit: Int): List<ServerMessage> {
        require(roomId.matches(Regex("[a-z0-9][a-z0-9-]{0,63}")))
        require(afterSequence >= 0 && upperSequence >= afterSequence && limit in 1..ChatLimits.RETAINED)
        val url = base.newBuilder().addPathSegments("rooms/$roomId/messages")
            .addQueryParameter("afterSequence", afterSequence.toString()).addQueryParameter("upperSequence", upperSequence.toString())
            .addQueryParameter("limit", limit.toString()).build()
        val request = Request.Builder().url(url).header("Authorization", "Bearer $token").build()
        val body = execute(request)
        val json = JSONArray(body)
        if (json.length() > limit) throw IOException("Invalid page")
        return (0 until json.length()).map { parseMessage(json.getJSONObject(it), roomId) }.also { page ->
            if (page.zipWithNext().any { it.first.serverSequence >= it.second.serverSequence } || page.any { it.serverSequence <= afterSequence || it.serverSequence > upperSequence }) throw IOException("Invalid sequence page")
        }
    }

    private suspend fun execute(request: Request): String = suspendCancellableCoroutine { continuation ->
        val call = client.newCall(request)
        continuation.invokeOnCancellation { call.cancel() }
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) { if (continuation.isActive) continuation.resumeWithException(e) }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    try {
                        if (!it.isSuccessful) throw if (it.code == 401) AuthenticationFailure() else IOException("HTTP failure")
                        val source = it.body.source()
                        val buffer = okio.Buffer()
                        while (buffer.size <= 8_388_608L) {
                            val read = source.read(buffer, minOf(8_192L, 8_388_609L - buffer.size))
                            if (read == -1L) break
                        }
                        if (buffer.size > 8_388_608L) throw IOException("Page too large")
                        if (continuation.isActive) continuation.resume(buffer.readUtf8())
                    } catch (e: Exception) { if (continuation.isActive) continuation.resumeWithException(e) }
                }
            }
        })
    }

    private fun parseMessage(json: JSONObject, room: String): ServerMessage {
        val receivedRoom = json.getString("roomId")
        if (receivedRoom != room) throw IOException("Room mismatch")
        val id = if (json.isNull("clientMessageId")) null else json.getString("clientMessageId")
        return ServerMessage(room, json.getString("messageId"), id, json.getLong("serverSequence"), json.getString("text"), json.getBoolean("isOwn"), json.getString("sentAt"))
    }

    override fun close() {
        stopped = true
        manualReconnect.close()
        mutableConnection.value = ChatConnectionStatus.DISCONNECTED
        epoch.incrementAndGet()
        connecting?.cancel()
        active?.cancel()
        connecting = null
        active = null
        activeRoom = null
        pending.values.forEach { it.deferred.completeExceptionally(IOException("Closed")) }
        pending.clear()
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
    }
}
