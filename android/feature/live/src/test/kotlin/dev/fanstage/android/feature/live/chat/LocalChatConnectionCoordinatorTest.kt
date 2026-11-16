package dev.fanstage.android.feature.live.chat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LocalChatConnectionCoordinatorTest {
    private class Probe(
        private val connects: Boolean = true,
        private val failure: Exception? = null,
        private val late: CompletableDeferred<Unit>? = null,
    ) : ChatTransport {
        override val connectionState = MutableStateFlow(ChatConnectionStatus.CONNECTING)
        var closes = 0
        var cancellations = 0
        override fun events(roomId: String) = flow<ChatEvent> {
            try {
                late?.let { withContext(NonCancellable) { it.await() } }
                failure?.let { throw it }
                if (connects) connectionState.value = ChatConnectionStatus.CONNECTED
                awaitCancellation()
            } finally { cancellations++ }
        }
        override suspend fun send(roomId: String, clientMessageId: String, text: String) = Unit
        override suspend fun fetchAfter(roomId: String, afterSequence: Long, upperSequence: Long, limit: Int) = emptyList<ServerMessage>()
        override fun close() { closes++ }
    }

    @Test fun `successful verification closes only probe and hands active connection to owner`() = runTest {
        val probe = Probe()
        val next = Probe()
        val inputs = mutableListOf<Pair<String, String>>()
        val coordinator = LocalChatConnectionCoordinator(backgroundScope) { endpoint, token ->
            inputs += endpoint to token
            if (inputs.size == 1) probe else next
        }
        var accepted: ChatTransport? = null
        var connected = 0
        coordinator.connect("https://fixture.invalid", "synthetic-token", "a", { accepted = it; true }, { connected++ })
        assertTrue(coordinator.state.value.checking)
        runCurrent()
        assertSame(next, accepted)
        assertEquals(1, connected)
        assertEquals(1, probe.closes)
        assertEquals(1, probe.cancellations)
        assertEquals(0, next.closes)
        assertEquals(listOf("https://fixture.invalid" to "synthetic-token", "https://fixture.invalid" to "synthetic-token"), inputs)
        coordinator.close()
        assertEquals(0, next.closes)
        assertFalse(coordinator.state.value.checking)
        next.close()
    }

    @Test fun `authentication failure preserves current connection and closes probe`() = runTest {
        val probe = Probe(failure = ChatConnectionFailure())
        val coordinator = LocalChatConnectionCoordinator(backgroundScope) { _, _ -> probe }
        coordinator.connect("fixture", "token", "a", { fail("must not replace"); false }, { fail("must not connect") })
        runCurrent()
        assertTrue(coordinator.state.value.configError)
        assertFalse(coordinator.state.value.checking)
        assertEquals(1, probe.closes)
    }

    @Test fun `verification timeout cancels subscription and releases probe`() = runTest {
        val probe = Probe(connects = false)
        val coordinator = LocalChatConnectionCoordinator(backgroundScope) { _, _ -> probe }
        coordinator.connect("fixture", "token", "a", { fail("must not replace"); false }, {})
        runCurrent()
        advanceTimeBy(10_000)
        runCurrent()
        assertTrue(coordinator.state.value.configError)
        assertFalse(coordinator.state.value.checking)
        assertEquals(1, probe.closes)
        assertEquals(1, probe.cancellations)
    }

    @Test fun `cancelled late verification cannot overwrite a later successful connection`() = runTest {
        val late = CompletableDeferred<Unit>()
        val old = Probe(late = late)
        val fresh = Probe()
        val active = Probe()
        val transports = ArrayDeque(listOf(old, fresh, active))
        val coordinator = LocalChatConnectionCoordinator(backgroundScope) { _, _ -> transports.removeFirst() }
        val accepted = mutableListOf<ChatTransport>()
        coordinator.connect("old", "token", "a", { accepted += it; true }, { fail("old result") })
        runCurrent()
        coordinator.cancel()
        coordinator.connect("fresh", "token", "b", { accepted += it; true }, {})
        runCurrent()
        late.complete(Unit)
        runCurrent()
        assertEquals(listOf(active), accepted)
        assertEquals(1, old.closes)
        assertEquals(1, fresh.closes)
        assertFalse(coordinator.state.value.configError)
        assertFalse(coordinator.state.value.checking)
        coordinator.close()
        active.close()
    }

    @Test fun `rejected handoff is closed by replacement owner exactly once`() = runTest {
        val probe = Probe()
        val next = Probe()
        var creates = 0
        val coordinator = LocalChatConnectionCoordinator(backgroundScope) { _, _ -> if (creates++ == 0) probe else next }
        coordinator.connect("fixture", "token", "a", { it.close(); false }, { fail("must remain blocked") })
        runCurrent()
        coordinator.close()
        assertEquals(1, probe.closes)
        assertEquals(1, next.closes)
        assertFalse(coordinator.state.value.configError)
    }

    @Test fun `disposal closes a checking probe and prevents future allocation`() = runTest {
        val probe = Probe(connects = false)
        var creates = 0
        val coordinator = LocalChatConnectionCoordinator(backgroundScope) { _, _ -> creates++; probe }
        coordinator.connect("fixture", "token", "a", { fail("disposed"); false }, {})
        runCurrent()
        coordinator.close()
        coordinator.close()
        runCurrent()
        coordinator.connect("fixture", "token", "a", { false }, {})
        assertEquals(1, creates)
        assertEquals(1, probe.closes)
    }

    @Test fun `already cancelled scope does not allocate transport`() = runTest {
        val job = Job().apply { cancel() }
        val coordinator = LocalChatConnectionCoordinator(CoroutineScope(coroutineContext + job)) { _, _ ->
            fail("cancelled scope allocated a transport")
            Probe()
        }
        coordinator.connect("fixture", "token", "a", { false }, {})
        assertFalse(coordinator.state.value.checking)
    }

    @Test fun `cancellation while creating handoff candidate releases both unowned transports`() = runTest {
        val owner = Job()
        val probe = Probe()
        val next = Probe()
        var creates = 0
        val coordinator = LocalChatConnectionCoordinator(CoroutineScope(coroutineContext + owner)) { _, _ ->
            if (creates++ == 0) probe else next.also { owner.cancel() }
        }
        coordinator.connect("fixture", "token", "a", { fail("cancelled handoff"); false }, {})
        runCurrent()
        assertEquals(1, probe.closes)
        assertEquals(1, next.closes)
        assertFalse(coordinator.state.value.checking)
    }

    @Test fun `invalid configuration does not start checking and can be cleared`() = runTest {
        val coordinator = LocalChatConnectionCoordinator(backgroundScope) { _, _ -> throw IllegalArgumentException() }
        coordinator.connect("invalid", "", "a", { false }, {})
        assertTrue(coordinator.state.value.configError)
        assertFalse(coordinator.state.value.checking)
        coordinator.clearError()
        assertFalse(coordinator.state.value.configError)
    }
}
