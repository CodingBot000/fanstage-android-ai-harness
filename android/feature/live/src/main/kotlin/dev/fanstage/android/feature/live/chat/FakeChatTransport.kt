package dev.fanstage.android.feature.live.chat

import kotlin.random.Random
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** One screen-scoped, deterministic fake server; no network, disk or real people. */
class FakeChatTransport(
    roomId: String,
    seed: Int = 73,
    private val rejectSends: Boolean = false,
    private val simulateIncoming: Boolean = true,
    override val memoryBudget: ChatMemoryBudget = ChatMemoryBudget(),
) : ChatTransport {
    private val owner = Any()
    private var activeRoom = roomId
    private val events = MutableSharedFlow<ChatEvent>()
    private val lock = Mutex()
    private val random = Random(seed)
    private val history = ArrayDeque<ServerMessage>()
    private var nextSequence = 1L

    init { seedRoom(roomId) }

    private fun seedRoom(roomId: String) {
        val initial = ServerMessage(roomId, "fixture-1", null, nextSequence, "Welcome to the room!", false)
        if (memoryBudget.acquire(initial.memoryKey(), owner, false) { history.remove(initial) }) {
            nextSequence++
            history.add(initial)
        }
    }

    override fun events(roomId: String): Flow<ChatEvent> {
        if (roomId != activeRoom) {
            close()
            activeRoom = roomId
            nextSequence = 1L
            seedRoom(roomId)
        }
        return channelFlow {
        // Register before emitting the snapshot so a concurrent ACK cannot fall into a subscription gap.
        launch(start = CoroutineStart.UNDISPATCHED) {
            events.filter { it.roomId == roomId }.collect { send(it) }
        }
        val initial = lock.withLock { history.toList() }
        initial.forEach { send(ChatEvent.Message(it)) }
        if (simulateIncoming) launch {
            val jitter = lock.withLock { random.nextLong(1_200, 1_801) }
            delay(jitter)
            val message = lock.withLock {
                if (roomId != activeRoom) return@withLock null
                val sequence = nextSequence
                val candidate = ServerMessage(roomId, "fixture-$sequence", null, sequence, "Glad to be here!", false)
                if (memoryBudget.acquire(candidate.memoryKey(), owner, false) { history.remove(candidate) }) {
                    nextSequence++
                    history.addLast(candidate)
                    candidate
                } else null
            }
            if (message != null) send(ChatEvent.Message(message))
        }
        awaitCancellation()
        }.buffer(0)
    }

    override suspend fun send(roomId: String, clientMessageId: String, text: String) {
        if (roomId != activeRoom || rejectSends || text.chatCodePoints() > ChatLimits.MAX_CODE_POINTS) throw SendRejected()
        val jitter = lock.withLock { random.nextLong(120, 301) }
        delay(jitter)
        val message = lock.withLock {
            if (roomId != activeRoom) throw SendRejected()
            history.firstOrNull { it.clientMessageId == clientMessageId }?.also {
                if (it.text != text) throw SendRejected()
            } ?: ServerMessage(roomId, "fake-${nextSequence}", clientMessageId, nextSequence, text, true).also {
                if (!memoryBudget.acquire(it.memoryKey(), owner, false) { history.remove(it) }) throw SendRejected()
                nextSequence++
                history.addLast(it)
            }
        }
        events.emit(ChatEvent.Ack(message))
        events.emit(ChatEvent.Message(message)) // Deliberate duplicate echo: consumers must deduplicate.
    }

    override fun close() {
        history.forEach { memoryBudget.release(it.memoryKey(), owner) }
        history.clear()
    }

    override suspend fun fetchAfter(roomId: String, afterSequence: Long, upperSequence: Long, limit: Int): List<ServerMessage> =
        lock.withLock {
            if (roomId != activeRoom) emptyList()
            else history.asSequence().filter { it.serverSequence > afterSequence && it.serverSequence <= upperSequence }
                .take(limit.coerceIn(0, ChatLimits.RETAINED)).toList()
        }
}
