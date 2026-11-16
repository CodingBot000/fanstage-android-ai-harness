package dev.fanstage.android.feature.live.chat

import org.junit.Assert.*
import org.junit.Test

class ChatTimelineStoreTest {
    private fun message(sequence: Long, room: String = "a") =
        ServerMessage(room, "server-$sequence", null, sequence, "message-$sequence", false)

    @Test fun `out of order events keep gaps until every preceding sequence arrives`() {
        val budget = ChatMemoryBudget()
        val store = ChatTimelineStore("a", budget) {}
        store.accept(message(3))
        store.accept(message(1))
        assertEquals(1L, store.cursor)
        assertTrue(store.hasGap)
        store.accept(message(2))
        store.accept(message(3))
        assertEquals(3L, store.cursor)
        assertFalse(store.hasGap)
        assertEquals(listOf(1L, 2L, 3L), store.displayedMessages().map { it.sequence })
        assertEquals(3, budget.size)
        store.reset("b")
        assertEquals(0, budget.size)
    }

    @Test fun `unknown send retries the same identity and ack keeps its display key`() {
        val store = ChatTimelineStore("a", ChatMemoryBudget()) {}
        assertTrue(store.enqueue("client", "hello"))
        store.markUncertain("client")
        assertTrue(store.transportUncertain)
        assertEquals("hello", store.retry("client"))
        assertFalse(store.transportUncertain)
        store.accept(ServerMessage("a", "ack", "client", 1, "hello", true))
        store.accept(ServerMessage("a", "ack", "client", 1, "hello", true))
        assertEquals(0, store.pendingCount)
        val confirmed = store.displayedMessages().single()
        assertEquals("client:client", confirmed.key)
        assertEquals(Delivery.SENT, confirmed.delivery)
    }

    @Test fun `conflicting ack cannot remove a pending item or its protected lease`() {
        val budget = ChatMemoryBudget()
        val store = ChatTimelineStore("a", budget) {}
        store.enqueue("client", "intended")
        store.accept(ServerMessage("a", "conflict", "client", 1, "different", true))
        assertTrue(store.invalidEvent)
        assertEquals(1, store.pendingCount)
        assertEquals("intended", store.displayedMessages().single().text)
        assertEquals(1, budget.size)
        store.dismissFailed("client")
        assertEquals(1, budget.size)
        store.reject("client")
        store.dismissFailed("client")
        assertEquals(0, budget.size)
    }

    @Test fun `shared budget eviction updates both room snapshots and closing releases all leases`() {
        val budget = ChatMemoryBudget()
        var changesA = 0
        var changesB = 0
        val a = ChatTimelineStore("a", budget) { changesA++ }
        val b = ChatTimelineStore("b", budget) { changesB++ }
        repeat(ChatLimits.RETAINED + 20) { i ->
            val target = if (i % 2 == 0) a else b
            target.accept(message(i / 2 + 1L, if (i % 2 == 0) "a" else "b"))
        }
        assertEquals(ChatLimits.RETAINED, budget.size)
        assertEquals(budget.size, a.retainedMessageCount + b.retainedMessageCount)
        assertTrue(changesA > (ChatLimits.RETAINED + 20) / 2)
        assertTrue(changesB > (ChatLimits.RETAINED + 20) / 2)
        a.reset("a")
        assertEquals(b.retainedMessageCount, budget.size)
        b.reset("b")
        assertEquals(0, budget.size)
    }

    @Test fun `all protected slots reject a new send without discarding pending messages`() {
        val budget = ChatMemoryBudget()
        val store = ChatTimelineStore("a", budget) {}
        repeat(ChatLimits.RETAINED) { assertTrue(store.enqueue("id-$it", "pending")) }
        assertFalse(store.enqueue("overflow", "pending"))
        assertEquals(ChatLimits.RETAINED, store.retainedMessageCount)
        assertEquals(ChatLimits.DISPLAYED, store.displayedMessages().size)
        store.reset("b")
        assertEquals(0, budget.size)
        assertEquals(0L, store.cursor)
        assertFalse(store.invalidEvent)
    }
}
