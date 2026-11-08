package dev.fanstage.android

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextClearance
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.fanstage.android.feature.live.PlayerPanel
import dev.fanstage.android.feature.live.chat.ChatConnectionFailure
import dev.fanstage.android.feature.live.chat.ChatConnectionStatus
import dev.fanstage.android.feature.live.chat.ChatEvent
import dev.fanstage.android.feature.live.chat.ChatPanel
import dev.fanstage.android.feature.live.chat.ChatViewModel
import dev.fanstage.android.feature.live.chat.FakeChatTransport
import dev.fanstage.android.feature.live.chat.RealChatTransport
import dev.fanstage.android.feature.live.chat.SendRejected
import android.util.Base64
import android.util.Log
import android.os.SystemClock
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.security.MessageDigest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.CancellationException
import okhttp3.WebSocket
import org.junit.Assert.fail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RealChatIntegrationTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()
    private val serverPort = InstrumentationRegistry.getArguments().getString("fanServerPort")?.toIntOrNull() ?: 18765
    private val serverHost = InstrumentationRegistry.getArguments().getString("fanServerHost") ?: "10.0.2.2"
    private val base = "http://$serverHost:$serverPort"

    // Only fixed phase names, enum states and exception class names may enter CI logs.
    private inline fun <T> phase(id: String, state: () -> String = { "none" }, block: () -> T): T {
        val started = SystemClock.elapsedRealtime()
        Log.i("M8Rca", "phase=$id event=start state=${state()}")
        try {
            val result = block()
            Log.i("M8Rca", "phase=$id event=success elapsedMs=${SystemClock.elapsedRealtime() - started} state=${state()}")
            return result
        } catch (error: Throwable) {
            val details = "phase=$id event=failure elapsedMs=${SystemClock.elapsedRealtime() - started} state=${state()} type=${error.javaClass.simpleName}"
            Log.e("M8Rca", details)
            if (error is RcaDiagnostics.RcaFailure) throw error
            throw RcaDiagnostics.RcaFailure(details, error)
        }
    }

    private suspend fun <T> wait20(id: String, state: () -> String = { "none" }, block: suspend () -> T): T =
        phase(id, state) { withTimeout(20_000) { block() } }

    private fun verifyServerInitiatedGracefulClose() {
        val server = ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))
        val replied = AtomicBoolean(false)
        val done = CountDownLatch(1)
        val worker = thread(name = "synthetic-close-peer") {
            try {
                server.accept().use { socket ->
                    socket.soTimeout = 7_000
                    val input = socket.getInputStream()
                    val request = StringBuilder()
                    while (!request.endsWith("\r\n\r\n") && request.length < 16_384) {
                        val c = input.read(); if (c < 0) break; request.append(c.toChar())
                    }
                    val key = request.lines().first { it.startsWith("Sec-WebSocket-Key:", ignoreCase = true) }.substringAfter(':').trim()
                    val digest = MessageDigest.getInstance("SHA-1").digest((key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").toByteArray())
                    val accept = Base64.encodeToString(digest, Base64.NO_WRAP)
                    val output = socket.getOutputStream()
                    output.write(("HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: $accept\r\n\r\n").toByteArray())
                    val subscribed = "{\"type\":\"subscribed\",\"headSequence\":0}".toByteArray()
                    output.write(byteArrayOf(0x81.toByte(), subscribed.size.toByte())); output.write(subscribed)
                    output.write(byteArrayOf(0x88.toByte(), 2, 0x03, 0xe8.toByte())); output.flush()
                    val first = input.read()
                    replied.set(first >= 0 && first and 0x0f == 8)
                }
            } catch (_: Exception) { /* Reported by the assertion, without exposing request headers. */ }
            finally { done.countDown() }
        }
        val transport = RealChatTransport("http://127.0.0.1:${server.localPort}", "synthetic-test-only", allowInsecure = true)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val job = scope.launch { transport.events("close-test").collect() }
        try {
            assertTrue("server close reply timed out", done.await(12, TimeUnit.SECONDS))
            assertTrue("client did not acknowledge server close", replied.get())
        } finally {
            job.cancel(); transport.close(); scope.cancel(); server.close(); worker.join(1_000)
        }
    }

    /** A local frame proxy drops the first committed sender ACK and echo; the real DB is unchanged. */
    private fun verifyM5AndroidAckLoss(alice: String, bob: String) {
        val listener = ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))
        val sockets = java.util.concurrent.CopyOnWriteArrayList<java.net.Socket>()
        val droppedAck = CountDownLatch(1)
        val droppedEcho = CountDownLatch(1)
        val closing = AtomicBoolean(false)
        val proxyStage = AtomicReference("accept_wait")
        val proxyFailure = AtomicReference<String?>(null)
        val observerFailure = AtomicReference<String?>(null)
        val proxyStarted = SystemClock.elapsedRealtime()
        fun proxyEvent(stage: String) {
            proxyStage.set(stage)
            Log.i("M8Rca", "proxy=ack stage=$stage elapsedMs=${SystemClock.elapsedRealtime() - proxyStarted}")
        }
        proxyEvent("accept_wait")
        fun proxyError(error: Throwable) {
            if (!closing.get()) {
                proxyFailure.compareAndSet(null, error.javaClass.simpleName)
                Log.e("M8Rca", "proxy=ack event=failure type=${error.javaClass.simpleName}")
            }
        }
        val proxy = thread(name = "m5-ack-loss-proxy", isDaemon = true) {
            try {
                val downstream = listener.accept(); sockets.add(downstream)
                proxyEvent("accepted")
                val upstream = java.net.Socket(serverHost, serverPort); sockets.add(upstream)
                proxyEvent("upstream_connected")
                thread(name = "m5-proxy-uplink", isDaemon = true) {
                    try {
                        val source = downstream.getInputStream(); val destination = upstream.getOutputStream()
                        val handshake = StringBuilder()
                        while (!handshake.endsWith("\r\n\r\n") && handshake.length < 16_384) {
                            val byte = source.read(); if (byte < 0) return@thread; handshake.append(byte.toChar())
                        }
                        val plain = handshake.toString().split("\r\n").filterNot { it.startsWith("Sec-WebSocket-Extensions:", ignoreCase = true) }.joinToString("\r\n")
                        proxyEvent("request_handshake_received")
                        destination.write(plain.toByteArray(Charsets.US_ASCII)); destination.flush()
                        proxyEvent("request_handshake_forwarded")
                        source.copyTo(destination)
                    } catch (error: Throwable) { proxyError(error) }
                }
                val input = java.io.DataInputStream(upstream.getInputStream())
                val output = downstream.getOutputStream()
                var last = ""
                while (!last.endsWith("\r\n\r\n")) {
                    val byte = input.readUnsignedByte(); output.write(byte)
                    last = (last + byte.toChar()).takeLast(4)
                }
                output.flush()
                proxyEvent("response_handshake_forwarded")
                while (!listener.isClosed) {
                    val first = input.readUnsignedByte(); val second = input.readUnsignedByte()
                    val marker = second and 127
                    val size = when (marker) { 126 -> input.readUnsignedShort().toLong(); 127 -> input.readLong(); else -> marker.toLong() }
                    require(size in 0..1_048_576)
                    val payload = ByteArray(size.toInt()); input.readFully(payload)
                    val type = if (first and 15 == 1) org.json.JSONObject(String(payload, Charsets.UTF_8)).optString("type") else ""
                    if (type == "ack" && droppedAck.count > 0) { droppedAck.countDown(); continue }
                    if (type == "message" && droppedEcho.count > 0) { droppedEcho.countDown(); continue }
                    val data = java.io.DataOutputStream(output)
                    data.writeByte(first); data.writeByte(second)
                    when (marker) { 126 -> data.writeShort(size.toInt()); 127 -> data.writeLong(size) }
                    data.write(payload); data.flush()
                }
            } catch (error: Throwable) { proxyError(error) }
        }
        val room = "m5-ack-" + UUID.randomUUID().toString().take(18)
        val transport = RealChatTransport("http://127.0.0.1:${listener.localPort}", alice, allowInsecure = true)
        val observer = RealChatTransport(base, bob, allowInsecure = true)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val received = Channel<ChatEvent>(Channel.UNLIMITED)
        val observerJob = scope.launch {
            try { observer.events(room).collect { received.send(it) } }
            catch (error: CancellationException) { throw error }
            catch (error: Throwable) {
                observerFailure.set(error.javaClass.simpleName)
                Log.e("M8Rca", "observer=ack event=failure type=${error.javaClass.simpleName}")
            }
        }
        val status = {
            "sender=${transport.connectionState.value},observer=${observer.connectionState.value}," +
                "observerJob=${observerJob.isActive},observerFailure=${observerFailure.get() ?: "none"}," +
                "proxyStage=${proxyStage.get()},proxyFailure=${proxyFailure.get() ?: "none"}"
        }
        val store = androidx.lifecycle.ViewModelStore()
        lateinit var chat: ChatViewModel
        composeRule.runOnUiThread { chat = ViewModelProvider(store, ChatViewModel.Factory(room, transport))[ChatViewModel::class.java] }
        try {
            phase("ack.scenario", status) { runBlocking {
                wait20("ack.both_connected", status) {
                    while (transport.connectionState.value != ChatConnectionStatus.CONNECTED || observer.connectionState.value != ChatConnectionStatus.CONNECTED) {
                        check(proxyFailure.get() == null && observerFailure.get() == null) { "async failure; see phase state" }
                        check(observerJob.isActive) { "observer collector ended; see phase state" }
                        delay(50)
                    }
                }
                composeRule.runOnUiThread { assertTrue(chat.send("synthetic ack loss")) }
                assertTrue("proxy did not discard ACK", droppedAck.await(10, TimeUnit.SECONDS))
                assertTrue("proxy did not discard sender echo", droppedEcho.await(10, TimeUnit.SECONDS))
                val initial = withTimeout(10_000) { received.receive() } as ChatEvent.Message
                withTimeout(15_000) { while (chat.uiState.value.messages.single().isUncertain.not()) delay(50) }
                val pending = chat.uiState.value.messages.single()
                assertEquals(dev.fanstage.android.feature.live.chat.Delivery.PENDING, pending.delivery)
                val id = requireNotNull(pending.clientMessageId)
                assertEquals(1, observer.fetchAfter(room, 0, Long.MAX_VALUE, 1000).size)
                composeRule.runOnUiThread { chat.retry(id) }
                withTimeout(15_000) { while (chat.uiState.value.messages.single().delivery != dev.fanstage.android.feature.live.chat.Delivery.SENT) delay(50) }
                assertEquals(id, chat.uiState.value.messages.single().clientMessageId)
                assertEquals(1, observer.fetchAfter(room, 0, Long.MAX_VALUE, 1000).size)
                assertEquals(initial.message.messageId, observer.fetchAfter(room, 0, Long.MAX_VALUE, 1000).single().messageId)
                assertNull("duplicate retry must not rebroadcast", kotlinx.coroutines.withTimeoutOrNull(1_000) { received.receive() })
                check(proxyFailure.get() == null && observerFailure.get() == null) { "async failure; see phase state" }
            } }
        } finally {
            closing.set(true)
            composeRule.runOnUiThread { store.clear() }
            observerJob.cancel(); observer.close(); scope.cancel(); received.close()
            listener.close(); sockets.forEach { it.close() }; proxy.join(2_000)
        }
    }

    /** Hold real HTTP responses while live WebSocket delivery advances the same ViewModel. */
    private fun verifyM5DelayedSnapshot(alice: String, bob: String) {
        val listener = ServerSocket(0, 20, InetAddress.getByName("127.0.0.1"))
        val sockets = java.util.concurrent.CopyOnWriteArrayList<java.net.Socket>()
        val workers = java.util.concurrent.CopyOnWriteArrayList<Thread>()
        val held = CountDownLatch(1); val release = CountDownLatch(1)
        val closing = AtomicBoolean(false)
        val proxyFailure = AtomicReference<String?>(null)
        val producerFailure = AtomicReference<String?>(null)
        fun proxyError(error: Throwable) {
            if (!closing.get()) {
                proxyFailure.compareAndSet(null, error.javaClass.simpleName)
                Log.e("M8Rca", "proxy=snapshot event=failure type=${error.javaClass.simpleName}")
            }
        }
        val proxy = thread(name = "m5-snapshot-proxy", isDaemon = true) {
            try {
                while (!listener.isClosed) {
                    val downstream = listener.accept(); sockets.add(downstream)
                    workers.add(thread(isDaemon = true) {
                        try {
                            val upstream = java.net.Socket(serverHost, serverPort); sockets.add(upstream)
                            val source = downstream.getInputStream()
                            val request = StringBuilder()
                            while (!request.endsWith("\r\n\r\n") && request.length < 16_384) {
                                val byte = source.read(); if (byte < 0) return@thread; request.append(byte.toChar())
                            }
                            val websocket = request.startsWith("GET /ws/")
                            val headers = if (websocket) request.toString() else request.toString().replace(Regex("(?im)^Connection:.*\r\n"), "Connection: close\r\n")
                            upstream.getOutputStream().write(headers.toByteArray(Charsets.US_ASCII)); upstream.getOutputStream().flush()
                            if (websocket) {
                                workers.add(thread(isDaemon = true) { try { source.copyTo(upstream.getOutputStream()) } catch (error: Throwable) { proxyError(error) } })
                                upstream.getInputStream().copyTo(downstream.getOutputStream())
                            } else {
                                val response = upstream.getInputStream().readBytes()
                                held.countDown()
                                phase("snapshot.release_wait") {
                                    check(release.await(20, TimeUnit.SECONDS)) { "snapshot release timed out" }
                                    downstream.getOutputStream().write(response); downstream.getOutputStream().flush()
                                }
                            }
                        } catch (error: Throwable) { proxyError(error) }
                    })
                }
            } catch (error: Throwable) { proxyError(error) }
        }
        val room = "m5-snapshot-" + UUID.randomUUID().toString().take(18)
        val producer = RealChatTransport(base, bob, allowInsecure = true)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val producerJob = scope.launch {
            try { producer.events(room).collect() }
            catch (error: CancellationException) { throw error }
            catch (error: Throwable) { producerFailure.set(error.javaClass.simpleName) }
        }
        val status = { "producer=${producer.connectionState.value},jobActive=${producerJob.isActive},producerFailure=${producerFailure.get() ?: "none"},proxyFailure=${proxyFailure.get() ?: "none"}" }
        val store = androidx.lifecycle.ViewModelStore()
        lateinit var chat: ChatViewModel
        try {
            phase("snapshot.scenario", status) { runBlocking {
                wait20("snapshot.producer_connected", status) {
                    while (producer.connectionState.value != ChatConnectionStatus.CONNECTED) {
                        check(producerJob.isActive && producerFailure.get() == null && proxyFailure.get() == null)
                        delay(50)
                    }
                }
                producer.send(room, UUID.randomUUID().toString(), "snapshot old")
                val real = RealChatTransport("http://127.0.0.1:${listener.localPort}", alice, allowInsecure = true)
                composeRule.runOnUiThread { chat = ViewModelProvider(store, ChatViewModel.Factory(room, real))[ChatViewModel::class.java] }
                assertTrue("history response was not held", held.await(10, TimeUnit.SECONDS))
                producer.send(room, UUID.randomUUID().toString(), "live new")
                withTimeout(10_000) { while (chat.uiState.value.messages.none { it.sequence == 2L }) delay(50) }
                assertTrue("gap must be visible while snapshot is delayed", chat.uiState.value.gapUnresolved)
                release.countDown()
                withTimeout(10_000) { while (chat.uiState.value.messages.map { it.sequence } != listOf(1L, 2L)) delay(50) }
                assertFalse(chat.uiState.value.gapUnresolved)
                check(producerFailure.get() == null && proxyFailure.get() == null) { "snapshot async failure" }
            } }
        } finally {
            closing.set(true)
            release.countDown(); composeRule.runOnUiThread { store.clear() }
            producerJob.cancel(); producer.close(); scope.cancel()
            listener.close(); sockets.forEach { it.close() }; proxy.join(2_000); workers.forEach { it.join(2_000) }
        }
    }

    private fun verifyM5RepeatedRealSubscriptions(token: String) = runBlocking {
        repeat(5) { index ->
            val transport = RealChatTransport(base, token, allowInsecure = true)
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            val job = scope.launch { transport.events("m5-cycle-$index").collect() }
            try {
                wait20("subscriptions.connected.$index", { "transport=${transport.connectionState.value},jobActive=${job.isActive}" }) {
                    while (transport.connectionState.value != ChatConnectionStatus.CONNECTED) delay(50)
                }
            } finally {
                job.cancelAndJoin(); transport.close(); scope.cancel()
            }
            assertTrue("collector must finish after screen exit", job.isCompleted)
            for (name in listOf("active", "connecting")) {
                val field = RealChatTransport::class.java.getDeclaredField(name).apply { isAccessible = true }
                assertNull("$name socket retained after exit", field.get(transport))
            }
        }
    }

    private fun verifyM5BoundedReconnect() {
        val server = ServerSocket(0, 10, InetAddress.getByName("127.0.0.1"))
        val attempts = java.util.concurrent.atomic.AtomicInteger()
        val worker = thread(name = "m5-reconnect-server", isDaemon = true) {
            try {
                while (!server.isClosed) {
                    server.accept().use { socket ->
                        socket.soTimeout = 2_000
                        val request = StringBuilder()
                        while (!request.endsWith("\r\n\r\n") && request.length < 16_384) {
                            val byte = socket.getInputStream().read(); if (byte < 0) break; request.append(byte.toChar())
                        }
                        attempts.incrementAndGet()
                        socket.getOutputStream().write("HTTP/1.1 503 Service Unavailable\r\nContent-Length: 0\r\nConnection: close\r\n\r\n".toByteArray()); socket.getOutputStream().flush()
                    }
                }
            } catch (_: IOException) { }
        }
        val transport = RealChatTransport("http://127.0.0.1:${server.localPort}", "synthetic", allowInsecure = true, initialReconnectDelayMillis = 5)
        val store = androidx.lifecycle.ViewModelStore()
        composeRule.runOnUiThread {
            val chat = ViewModelProvider(store, ChatViewModel.Factory("retry-test", transport))[ChatViewModel::class.java]
            composeRule.activity.setContent { MaterialTheme { Column(Modifier.verticalScroll(rememberScrollState())) { ChatPanel(chat, developmentControls = true) } } }
        }
        try {
            composeRule.waitUntil(5_000) { transport.connectionState.value == ChatConnectionStatus.DISCONNECTED }
            val stoppedAt = attempts.get()
            assertEquals("one initial attempt and six bounded retries", 7, stoppedAt)
            Thread.sleep(300)
            assertEquals("retries must stop without user action", stoppedAt, attempts.get())
            composeRule.onNodeWithTag("chat_retry_connection").performScrollTo().performClick()
            runBlocking { withTimeout(3_000) { while (attempts.get() == stoppedAt) delay(10) } }
            assertTrue("explicit retry must open another connection", attempts.get() > stoppedAt)
        } finally {
            composeRule.runOnUiThread { store.clear() }
            server.close(); worker.join(2_000)
        }
    }

    @Test fun androidRealWebSocketHistoryReconnectAndPlayerIsolation() {
        verifyServerInitiatedGracefulClose()
        verifyM5BoundedReconnect()
        val arguments = InstrumentationRegistry.getArguments()
        val alice = requireNotNull(arguments.getString("fanTokenA")) { "Missing temporary test token A" }
        val bob = requireNotNull(arguments.getString("fanTokenB")) { "Missing temporary test token B" }
        verifyM5AndroidAckLoss(alice, bob)
        verifyM5DelayedSnapshot(alice, bob)
        verifyM5RepeatedRealSubscriptions(alice)
        val room = "android-" + UUID.randomUUID().toString().take(18)
        val invalid = RealChatTransport(base, "invalid-temporary-token", allowInsecure = true)
        try {
            runBlocking {
                try { withTimeout(10_000) { invalid.events(room).collect() }; fail("invalid credential must fail") }
                catch (_: ChatConnectionFailure) { }
            }
        } finally { invalid.close() }
        val a = RealChatTransport(base, alice, allowInsecure = true, initialReconnectDelayMillis = 10_000)
        val b = RealChatTransport(base, bob, allowInsecure = true)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val incoming = Channel<ChatEvent>(Channel.UNLIMITED)
        val aJob = scope.launch { a.events(room).collect { incoming.send(it) } }
        val bJob = scope.launch { b.events(room).collect() }
        try {
            runBlocking {
                suspend fun sendWhenReady(transport: RealChatTransport, id: String, text: String) = withTimeout(30_000) {
                    while (true) {
                        try { transport.send(room, id, text); break }
                        catch (_: IOException) { delay(100) }
                    }
                }
                val first = UUID.randomUUID().toString()
                sendWhenReady(b, first, "first synthetic message")
                wait20("main.first_message", { "alice=${a.connectionState.value},bob=${b.connectionState.value},aliceJob=${aJob.isActive},bobJob=${bJob.isActive}" }) {
                    while (true) {
                        val event = incoming.receive()
                        if (event is ChatEvent.Message && event.message.text == "first synthetic message") {
                            assertFalse(event.message.isOwn)
                            assertNull(event.message.clientMessageId)
                            assertTrue(event.message.sentAt?.endsWith("Z") == true)
                            break
                        }
                    }
                }
                // Same identity and idempotency key must not create a second row; changed content must reject.
                b.send(room, first, "first synthetic message")
                assertEquals(1, b.fetchAfter(room, 0, Long.MAX_VALUE, 1000).size)
                try {
                    b.send(room, first, "different content")
                    fail("conflicting client id must be rejected")
                } catch (_: SendRejected) { }
                assertEquals(1, b.fetchAfter(room, 0, Long.MAX_VALUE, 1000).size)
                // Cut Alice's socket while preserving the collecting coroutine; Bob commits during the reconnect delay.
                val field = RealChatTransport::class.java.getDeclaredField("active").apply { isAccessible = true }
                val old = field.get(a) as WebSocket
                val disconnectedAt = android.os.SystemClock.elapsedRealtime()
                old.cancel()
                withTimeout(10_000) { while (field.get(a) != null) delay(50) }
                assertEquals(ChatConnectionStatus.RECONNECTING, a.connectionState.value)
                val second = UUID.randomUUID().toString()
                sendWhenReady(b, second, "missed while offline")
                assertNull("Alice must remain offline until Bob commits", field.get(a))
                withTimeout(35_000) {
                    while (true) {
                        val event = incoming.receive()
                        if (event is ChatEvent.Message && event.message.text == "missed while offline") break
                    }
                }
                assertTrue("reconnect delay must last at least 10 seconds", android.os.SystemClock.elapsedRealtime() - disconnectedAt >= 9_900)
                assertEquals(ChatConnectionStatus.CONNECTED, a.connectionState.value)
                val history = a.fetchAfter(room, 0, Long.MAX_VALUE, 1000)
                assertTrue(history.any { it.text == "first synthetic message" })
                assertTrue(history.any { it.text == "missed while offline" })
                // Change room while the old subscription is in reconnect backoff.
                (field.get(a) as WebSocket).cancel()
                withTimeout(10_000) { while (field.get(a) != null) delay(50) }
                assertEquals(ChatConnectionStatus.RECONNECTING, a.connectionState.value)
                aJob.cancelAndJoin()
                while (incoming.tryReceive().isSuccess) { }
                val nextRoom = "android-" + UUID.randomUUID().toString().take(18)
                val nextJob = scope.launch { a.events(nextRoom).collect { incoming.send(it) } }
                val nextBob = RealChatTransport(base, bob, allowInsecure = true)
                val nextBobJob = scope.launch { nextBob.events(nextRoom).collect() }
                try {
                    sendWhenReady(b, UUID.randomUUID().toString(), "late old room")
                    withTimeout(30_000) {
                        while (true) {
                            try { nextBob.send(nextRoom, UUID.randomUUID().toString(), "new room"); break }
                            catch (_: IOException) { delay(100) }
                        }
                    }
                    wait20("main.new_room_message", { "alice=${a.connectionState.value},bob=${nextBob.connectionState.value},aliceJob=${nextJob.isActive},bobJob=${nextBobJob.isActive}" }) {
                        while (true) {
                            val event = incoming.receive()
                            assertTrue("old room event reached new subscription", event.roomId == nextRoom)
                            if (event is ChatEvent.Message && event.message.text == "new room") break
                        }
                    }
                } finally { nextJob.cancelAndJoin(); nextBobJob.cancelAndJoin(); nextBob.close() }
            }
            // Real transport is injected into the same ChatPanel/ViewModel contract; playback remains independent.
            val chatHolder = arrayOfNulls<ChatViewModel>(1)
            composeRule.activityRule.scenario.onActivity { activity ->
                val fake = FakeChatTransport(room, simulateIncoming = false)
                val chat = ViewModelProvider(activity, ChatViewModel.Factory(room, fake))["real-chat", ChatViewModel::class.java]
                chatHolder[0] = chat
                activity.setContent {
                    MaterialTheme {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            PlayerPanel("real-chat")
                            ChatPanel(chat, developmentControls = true)
                        }
                    }
                }
            }
            composeRule.waitUntil(30_000L) { composeRule.onAllNodesWithTag("player_state_playing").fetchSemanticsNodes().isNotEmpty() }
            composeRule.onNodeWithTag("chat_settings_toggle").performScrollTo().performClick()
            composeRule.onNodeWithTag("chat_endpoint").performScrollTo().performTextClearance()
            composeRule.onNodeWithTag("chat_endpoint").performTextInput(base)
            // An actual server authentication error must not stop the independent player.
            composeRule.onNodeWithTag("chat_token").performScrollTo().performTextInput("invalid-temporary-token")
            composeRule.onNodeWithTag("chat_use_real").performScrollTo().performClick()
            composeRule.waitUntil(15_000L) { composeRule.onAllNodesWithTag("chat_connection_error").fetchSemanticsNodes().isNotEmpty() }
            composeRule.onNodeWithTag("chat_use_fake").assertIsSelected()
            composeRule.onNodeWithTag("player_state_playing").assertExists()
            // Debug-only UI selects the local endpoint and transient synthetic credential.
            composeRule.onNodeWithTag("chat_token").performScrollTo().performTextClearance()
            composeRule.onNodeWithTag("chat_token").performTextInput(alice)
            composeRule.onNodeWithTag("chat_use_real").performScrollTo().performClick()
            composeRule.waitUntil(10_000L) { chatHolder[0]?.uiState?.value?.localServer == true }
            composeRule.onNodeWithTag("chat_select_real").assertIsSelected()
            // Allow subscription before sending; existing snapshot messages prove the handshake completed.
            composeRule.waitUntil(30_000L) { composeRule.onAllNodesWithText("missed while offline").fetchSemanticsNodes().isNotEmpty() }
            composeRule.onNodeWithTag("chat_input").performScrollTo().performTextInput("android real send")
            composeRule.onNodeWithTag("chat_send").performScrollTo().performClick()
            composeRule.waitUntil(30_000L) { composeRule.onAllNodesWithText(composeRule.activity.getString(dev.fanstage.android.feature.live.R.string.chat_sent)).fetchSemanticsNodes().isNotEmpty() }
            composeRule.onNodeWithText("android real send").assertExists()
            composeRule.waitUntil(10_000L) { composeRule.onAllNodesWithTag("chat_timestamp").fetchSemanticsNodes().isNotEmpty() }
            composeRule.onNodeWithTag("player_state_playing").assertExists()
            // Background/foreground keeps the real chat owner while player pause remains independent.
            composeRule.activityRule.scenario.moveToState(androidx.lifecycle.Lifecycle.State.CREATED)
            composeRule.activityRule.scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
            composeRule.waitUntil(10_000L) { composeRule.onAllNodesWithTag("player_state_paused").fetchSemanticsNodes().isNotEmpty() }
            composeRule.onNodeWithTag("player_play").performScrollTo().performClick()
            phase("ui.player_resumed", { "alice=${a.connectionState.value},bob=${b.connectionState.value}" }) {
                composeRule.waitUntil(20_000L) { composeRule.onAllNodesWithTag("player_state_playing").fetchSemanticsNodes().isNotEmpty() }
            }
            val beforeRecreate = chatHolder[0]!!
            composeRule.activityRule.scenario.recreate()
            composeRule.activityRule.scenario.onActivity { activity ->
                val retained = ViewModelProvider(activity)["real-chat", ChatViewModel::class.java]
                assertTrue("real chat ViewModel should survive recreation", retained === beforeRecreate)
                activity.setContent { MaterialTheme { Column(Modifier.verticalScroll(rememberScrollState())) { PlayerPanel("real-chat"); ChatPanel(retained, developmentControls = true) } } }
            }
            composeRule.waitUntil(30_000L) { composeRule.onAllNodesWithText("android real send").fetchSemanticsNodes().isNotEmpty() }
            composeRule.waitUntil(30_000L) { composeRule.onAllNodesWithTag("player_state_playing").fetchSemanticsNodes().isNotEmpty() }
            val transportField = ChatViewModel::class.java.getDeclaredField("transport").apply { isAccessible = true }
            val uiTransport = transportField.get(chatHolder[0]) as RealChatTransport
            val activeField = RealChatTransport::class.java.getDeclaredField("active").apply { isAccessible = true }
            val uiSocket = activeField.get(uiTransport) as WebSocket
            uiSocket.cancel()
            composeRule.waitUntil(30_000L) { activeField.get(uiTransport)?.let { it !== uiSocket } == true }
            composeRule.onNodeWithTag("player_state_playing").assertExists()
            composeRule.activityRule.scenario.onActivity { it.viewModelStore.clear() }
            assertNull("screen exit must close its socket", activeField.get(uiTransport))
            val connectingField = RealChatTransport::class.java.getDeclaredField("connecting").apply { isAccessible = true }
            assertNull("screen exit must cancel pending handshake", connectingField.get(uiTransport))
            composeRule.onNodeWithTag("player_state_playing").assertExists()
        } finally {
            runBlocking { aJob.cancelAndJoin(); bJob.cancelAndJoin() }
            a.close(); b.close(); scope.cancel(); incoming.close()
        }
    }
}
