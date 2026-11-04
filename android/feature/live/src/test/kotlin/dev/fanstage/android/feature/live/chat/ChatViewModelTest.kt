package dev.fanstage.android.feature.live.chat

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {
    private lateinit var store: ViewModelStore
    @Before fun setup() { Dispatchers.setMain(StandardTestDispatcher()); store = ViewModelStore() }
    @After fun teardown() { store.clear(); Dispatchers.resetMain() }
    private fun create(t: ChatTransport, room: String = "a") = ViewModelProvider(store, ChatViewModel.Factory(room, t))[ChatViewModel::class.java]
    private fun msg(seq: Long, room: String = "a", client: String? = null) = ServerMessage(room, "s$seq", client, seq, "message-$seq", client != null)

    private class Controlled : ChatTransport {
        val stream = MutableSharedFlow<ChatEvent>()
        var history = emptyList<ServerMessage>()
        var sendAction: suspend (String, String, String) -> Unit = { _, _, _ -> }
        var fetchAction: suspend (String, Long, Long, Int) -> List<ServerMessage> = { room, after, upper, limit ->
            history.filter { it.roomId == room && it.serverSequence > after && it.serverSequence <= upper }.take(limit)
        }
        var subscriptions = 0
        var cancellations = 0
        override fun events(roomId: String): Flow<ChatEvent> = flow {
            subscriptions++
            try { stream.collect { emit(it) } } finally { cancellations++ }
        }
        override suspend fun send(roomId: String, clientMessageId: String, text: String) = sendAction(roomId, clientMessageId, text)
        override suspend fun fetchAfter(roomId: String, afterSequence: Long, upperSequence: Long, limit: Int) = fetchAction(roomId, afterSequence, upperSequence, limit)
    }

    @Test fun `fake seed sends pending then ack and duplicate echo converge`() = runTest {
        val fake = FakeChatTransport("a", seed = 73, simulateIncoming = false)
        val vm = create(fake)
        runCurrent()
        assertEquals("Welcome to the room!", vm.uiState.value.messages.single().text)
        assertTrue(vm.send("hello"))
        assertEquals(Delivery.PENDING, vm.uiState.value.messages.last().delivery)
        advanceUntilIdle()
        assertEquals(2, vm.uiState.value.messages.size)
        assertEquals(Delivery.SENT, vm.uiState.value.messages.last().delivery)
        assertEquals(2L, vm.confirmedCursor)
    }

    @Test fun `seeded synthetic arrival uses reproducible virtual time`() = runTest {
        val fake = FakeChatTransport("a", seed = 73)
        val vm = create(fake); runCurrent()
        assertEquals(1, vm.uiState.value.messages.size)
        advanceUntilIdle()
        assertEquals(listOf("Welcome to the room!", "Glad to be here!"), vm.uiState.value.messages.map { it.text })
        assertEquals(listOf(1L, 2L), vm.uiState.value.messages.map { it.sequence })
    }

    @Test fun `echo preceding delayed ack reconciles the same stable message`() = runTest {
        val t = Controlled(); val vm = create(t); runCurrent()
        vm.send("first echo"); runCurrent()
        val before = vm.uiState.value.messages.single()
        val event = ServerMessage("a", "server-1", before.clientMessageId, 1, "first echo", true)
        t.stream.emit(ChatEvent.Message(event)); runCurrent()
        t.stream.emit(ChatEvent.Ack(event)); advanceUntilIdle()
        assertEquals(1, vm.uiState.value.messages.size)
        assertEquals(before.key, vm.uiState.value.messages.single().key)
        assertEquals(Delivery.SENT, vm.uiState.value.messages.single().delivery)
    }

    @Test fun `fake transport supports switching rooms while cancelling old send`() = runTest {
        val fake = FakeChatTransport("a", seed = 73, simulateIncoming = false)
        val vm = create(fake); runCurrent()
        vm.send("old-room"); runCurrent()
        vm.openRoom("b"); advanceUntilIdle()
        assertEquals("b", vm.uiState.value.roomId)
        assertEquals(listOf("Welcome to the room!"), vm.uiState.value.messages.map { it.text })
        vm.send("new-room"); advanceUntilIdle()
        assertEquals(listOf("Welcome to the room!", "new-room"), vm.uiState.value.messages.map { it.text })
        assertEquals(Delivery.SENT, vm.uiState.value.messages.last().delivery)
        assertFalse(vm.uiState.value.gapUnresolved)
    }

    @Test fun `fake retries same client id idempotently and rejects changed content`() = runTest {
        val fake = FakeChatTransport("a", seed = 73, simulateIncoming = false)
        fake.send("a", "same-id", "hello")
        fake.send("a", "same-id", "hello")
        val history = fake.fetchAfter("a", 0, 100, 100)
        assertEquals(2, history.size)
        assertEquals(1, history.count { it.clientMessageId == "same-id" })
        try { fake.send("a", "same-id", "different"); fail("expected rejection") } catch (_: SendRejected) { }
    }

    @Test fun `gap flag clears when missing event arrives without explicit retry`() = runTest {
        val t = Controlled(); val vm = create(t); runCurrent()
        t.history = emptyList()
        t.stream.emit(ChatEvent.Message(msg(2))); advanceUntilIdle()
        assertTrue(vm.uiState.value.gapUnresolved)
        t.stream.emit(ChatEvent.Message(msg(1))); advanceUntilIdle()
        assertEquals(2L, vm.confirmedCursor)
        assertFalse(vm.uiState.value.gapUnresolved)
    }

    @Test fun `uncertain delivery remains pending across unrelated events until ack`() = runTest {
        val t = Controlled(); val vm = create(t); runCurrent()
        t.sendAction = { _, _, _ -> error("unknown") }
        vm.send("mine"); advanceUntilIdle()
        val id = vm.uiState.value.messages.single().clientMessageId!!
        t.stream.emit(ChatEvent.Message(msg(1))); advanceUntilIdle()
        assertTrue(vm.uiState.value.transportUncertain)
        t.stream.emit(ChatEvent.Ack(ServerMessage("a", "ack", id, 2, "mine", true))); advanceUntilIdle()
        assertFalse(vm.uiState.value.transportUncertain)
        assertEquals(Delivery.SENT, vm.uiState.value.messages.last().delivery)
    }

    @Test fun `out of order duplicates and repeated ack fill gaps without skipping`() = runTest {
        val t = Controlled(); val vm = create(t); runCurrent()
        t.history = listOf(msg(1), msg(2), msg(3))
        t.stream.emit(ChatEvent.Message(msg(3))); advanceUntilIdle()
        assertEquals(listOf(1L, 2L, 3L), vm.uiState.value.messages.map { it.sequence })
        t.stream.emit(ChatEvent.Message(msg(2))); t.stream.emit(ChatEvent.Message(msg(3))); advanceUntilIdle()
        assertEquals(3, vm.uiState.value.messages.size)
        assertTrue(vm.send("my message")); runCurrent()
        val pending = vm.uiState.value.messages.last(); val id = pending.clientMessageId!!
        val ack = ServerMessage("a", "mine", id, 4, "my message", true)
        t.stream.emit(ChatEvent.Ack(ack)); t.stream.emit(ChatEvent.Ack(ack)); t.stream.emit(ChatEvent.Message(ack)); advanceUntilIdle()
        assertEquals(4, vm.uiState.value.messages.size)
        assertEquals(pending.key, vm.uiState.value.messages.last().key)
        assertEquals(Delivery.SENT, vm.uiState.value.messages.last().delivery)
    }

    @Test fun `large gap recovers in bounded pages without advancing across missing sequence`() = runTest {
        val t = Controlled(); val vm = create(t); runCurrent()
        t.history = (1L..1_050L).map { msg(it) }
        t.stream.emit(ChatEvent.Message(msg(1_050))); advanceUntilIdle()
        assertEquals(1_050L, vm.confirmedCursor)
        assertTrue(vm.totalLogicalMessages <= 1_000)
        assertEquals(500, vm.uiState.value.messages.size)
        assertFalse(vm.uiState.value.gapUnresolved)
    }

    @Test fun `only explicit removal of a rejected message frees its slot`() = runTest {
        val t = Controlled(); val vm = create(t); runCurrent()
        t.sendAction = { _, _, _ -> throw SendRejected() }
        vm.send("failed"); advanceUntilIdle()
        val id = vm.uiState.value.messages.single().clientMessageId!!
        assertEquals(1, vm.totalLogicalMessages)
        vm.dismissFailed(id)
        assertTrue(vm.uiState.value.messages.isEmpty())
        assertEquals(0, vm.totalLogicalMessages)
        t.sendAction = { _, _, _ -> awaitCancellation() }
        vm.send("pending"); runCurrent()
        val pendingId = vm.uiState.value.messages.single().clientMessageId!!
        vm.dismissFailed(pendingId)
        assertEquals(Delivery.PENDING, vm.uiState.value.messages.single().delivery)
    }

    @Test fun `failed send retries with identical client id and converges on ack`() = runTest {
        val t = Controlled(); val vm = create(t); runCurrent()
        t.sendAction = { _, _, _ -> throw SendRejected() }
        vm.send("retry me"); advanceUntilIdle()
        val message = vm.uiState.value.messages.single()
        assertEquals(Delivery.FAILED, message.delivery)
        t.sendAction = { room, id, text -> t.stream.emit(ChatEvent.Ack(ServerMessage(room, "s1", id, 1, text, true))) }
        vm.retry(message.clientMessageId!!); advanceUntilIdle()
        assertEquals(Delivery.SENT, vm.uiState.value.messages.single().delivery)
        assertEquals(message.key, vm.uiState.value.messages.single().key)
    }

    @Test fun `missing page remains visible and retry completes contiguous gap`() = runTest {
        val t = Controlled(); val vm = create(t); runCurrent()
        t.history = listOf(msg(1), msg(3))
        t.stream.emit(ChatEvent.Message(msg(3))); advanceUntilIdle()
        assertEquals(1L, vm.confirmedCursor)
        assertTrue(vm.uiState.value.gapUnresolved)
        t.history = listOf(msg(1), msg(2), msg(3))
        vm.retryGap(); advanceUntilIdle()
        assertEquals(3L, vm.confirmedCursor)
        assertFalse(vm.uiState.value.gapUnresolved)
        assertEquals(listOf(1L, 2L, 3L), vm.uiState.value.messages.map { it.sequence })
    }

    @Test fun `definite rejection fails but unknown result remains pending`() = runTest {
        val t = Controlled(); val vm = create(t); runCurrent()
        t.sendAction = { _, _, _ -> throw SendRejected() }
        vm.send("rejected"); advanceUntilIdle()
        assertEquals(Delivery.FAILED, vm.uiState.value.messages.last().delivery)
        t.sendAction = { _, _, _ -> error("unknown") }
        vm.send("unknown"); advanceUntilIdle()
        assertEquals(Delivery.PENDING, vm.uiState.value.messages.last().delivery)
        assertTrue(vm.uiState.value.transportUncertain)
    }

    @Test fun `old room delayed recovery and noncancellable send cannot modify new room`() = runTest {
        val t = Controlled(); val vm = create(t); runCurrent()
        val late = CompletableDeferred<List<ServerMessage>>()
        val sendFinished = CompletableDeferred<Unit>()
        t.fetchAction = { _, _, _, _ -> withContext(NonCancellable) { late.await() } }
        t.sendAction = { _, _, _ -> withContext(NonCancellable) { sendFinished.await() }; throw SendRejected() }
        vm.send("old")
        t.stream.emit(ChatEvent.Message(msg(3))); runCurrent()
        vm.openRoom("b"); runCurrent()
        late.complete(listOf(msg(1), msg(2))); sendFinished.complete(Unit); advanceUntilIdle()
        t.stream.emit(ChatEvent.Message(msg(1, room = "a"))); runCurrent()
        assertTrue(vm.uiState.value.messages.isEmpty())
        t.stream.emit(ChatEvent.Message(msg(1, room = "b"))); advanceUntilIdle()
        assertEquals("b", vm.uiState.value.roomId)
        assertEquals(listOf("message-1"), vm.uiState.value.messages.map { it.text })
        assertEquals(Delivery.SENT, vm.uiState.value.messages.single().delivery)
        assertTrue(t.cancellations >= 1)
    }

    @Test fun `eviction respects both limits and does not lose cursor progress`() = runTest {
        val t = Controlled(); val vm = create(t); runCurrent()
        repeat(1_100) { t.stream.emit(ChatEvent.Message(msg(it + 1L))); runCurrent() }
        assertEquals(1_100L, vm.confirmedCursor)
        assertEquals(1_000, vm.retainedMessageCount)
        assertEquals(500, vm.uiState.value.messages.size)
        assertEquals(601L, vm.uiState.value.messages.first().sequence)
        assertEquals(1_100L, vm.uiState.value.messages.last().sequence)
    }

    @Test fun `conflicting payload does not reconcile pending id`() = runTest {
        val t = Controlled(); val vm = create(t); runCurrent()
        vm.send("intended"); runCurrent()
        val id = vm.uiState.value.messages.single().clientMessageId!!
        t.stream.emit(ChatEvent.Ack(ServerMessage("a", "bad", id, 1, "other", true))); advanceUntilIdle()
        assertEquals(Delivery.PENDING, vm.uiState.value.messages.single().delivery)
        assertTrue(vm.uiState.value.gapUnresolved)
    }

    @Test fun `duplicate sequence with different identity is rejected`() = runTest {
        val t = Controlled(); val vm = create(t); runCurrent()
        t.stream.emit(ChatEvent.Message(msg(1))); runCurrent()
        t.stream.emit(ChatEvent.Message(ServerMessage("a", "conflict", null, 1, "wrong", false))); advanceUntilIdle()
        assertEquals("message-1", vm.uiState.value.messages.single().text)
        assertTrue(vm.uiState.value.gapUnresolved)
    }

    @Test fun `combined fake and screen share at most 1000 logical messages`() = runTest {
        val fake = FakeChatTransport("a", seed = 73, simulateIncoming = false)
        val vm = create(fake); runCurrent()
        repeat(1_050) {
            fake.send("a", "id-$it", "text-$it")
            assertTrue(vm.totalLogicalMessages <= 1_000)
        }
        advanceUntilIdle()
        assertEquals(1_000, vm.totalLogicalMessages)
        assertEquals(1_000, vm.retainedMessageCount)
        assertEquals(500, vm.uiState.value.messages.size)
        assertEquals(1_051L, vm.confirmedCursor)
        assertTrue(vm.send("pending"))
        assertEquals(1_000, vm.totalLogicalMessages)
        assertEquals(Delivery.PENDING, vm.uiState.value.messages.last().delivery)
        advanceUntilIdle()
        assertEquals(1_000, vm.totalLogicalMessages)
        assertEquals(Delivery.SENT, vm.uiState.value.messages.last().delivery)
    }

    @Test fun `overlapping rooms share process budget and release slots on close`() = runTest {
        val budget = ChatMemoryBudget()
        val first = FakeChatTransport("a", simulateIncoming = false, memoryBudget = budget)
        val second = FakeChatTransport("b", simulateIncoming = false, memoryBudget = budget)
        val a = ViewModelProvider(store, ChatViewModel.Factory("a", first))["room-a", ChatViewModel::class.java]
        val b = ViewModelProvider(store, ChatViewModel.Factory("b", second))["room-b", ChatViewModel::class.java]
        runCurrent()
        repeat(550) { first.send("a", "a-$it", "message"); second.send("b", "b-$it", "message"); assertTrue(budget.size <= 1_000) }
        advanceUntilIdle()
        assertEquals(1_000, budget.size)
        assertTrue(a.retainedMessageCount + b.retainedMessageCount <= 1_000)
        store.clear(); advanceUntilIdle()
        assertEquals(0, budget.size)
    }

    @Test fun `a new fake room does not crash when all shared slots are pinned`() = runTest {
        val budget = ChatMemoryBudget(); val owner = Any()
        repeat(1_000) { assertTrue(budget.acquire("pending-$it", owner, true) {}) }
        val fake = FakeChatTransport("b", simulateIncoming = false, memoryBudget = budget)
        assertTrue(fake.fetchAfter("b", 0, 10, 10).isEmpty())
        assertEquals(1_000, budget.size)
        budget.release("pending-0", owner)
        fake.send("b", "new-id", "hello")
        assertEquals(1L, fake.fetchAfter("b", 0, 10, 10).single().serverSequence)
        assertEquals(1_000, budget.size)
    }

    @Test fun `message length counts unicode code points and rejects oversized input and events`() = runTest {
        val t = Controlled(); val vm = create(t); runCurrent()
        assertTrue(vm.send("😀".repeat(1_000)))
        assertFalse(vm.send("😀".repeat(1_001)))
        assertTrue(vm.uiState.value.inputTooLong)
        t.stream.emit(ChatEvent.Message(ServerMessage("a", "oversize", null, 1, "x".repeat(1_001), false)))
        advanceUntilIdle()
        assertTrue(vm.uiState.value.gapUnresolved)
        assertEquals(1, vm.retainedMessageCount)
    }

    @Test fun `1000 pending records are retained and a new send is explicitly rejected`() = runTest {
        val t = Controlled(); t.sendAction = { _, _, _ -> awaitCancellation() }
        val vm = create(t); runCurrent()
        repeat(1_000) { assertTrue(vm.send("pending-$it")) }
        runCurrent()
        assertFalse(vm.send("must not drop old pending"))
        assertEquals(1_000, vm.retainedMessageCount)
        assertEquals(1_000, vm.totalLogicalMessages)
        assertEquals(500, vm.uiState.value.messages.size)
        assertTrue(vm.uiState.value.sendBlocked)
    }

    @Test fun `closed screen ignores a transport that converts cancellation to rejection`() = runTest {
        val t = Controlled()
        t.sendAction = { _, _, _ -> try { awaitCancellation() } catch (_: CancellationException) { throw SendRejected() } }
        val vm = create(t); runCurrent(); vm.send("pending"); runCurrent()
        store.clear(); advanceUntilIdle()
        assertEquals(Delivery.PENDING, vm.uiState.value.messages.single().delivery)
    }

    @Test fun `room switch cancels several pending sends safely`() = runTest {
        val t = Controlled(); t.sendAction = { _, _, _ -> awaitCancellation() }
        val vm = create(t); runCurrent(); repeat(10) { vm.send("pending-$it") }; runCurrent()
        vm.openRoom("b"); advanceUntilIdle()
        assertEquals("b", vm.uiState.value.roomId)
        assertTrue(vm.uiState.value.messages.isEmpty())
    }

    @Test fun `clearing viewmodel cancels subscription and unfinished send`() = runTest {
        val t = Controlled(); var cancelled = false
        t.sendAction = { _, _, _ -> try { awaitCancellation() } catch (e: CancellationException) { cancelled = true; throw e } }
        val vm = create(t); runCurrent(); vm.send("pending"); runCurrent()
        store.clear(); advanceUntilIdle()
        assertTrue(cancelled)
        assertEquals(1, t.cancellations)
    }
}
