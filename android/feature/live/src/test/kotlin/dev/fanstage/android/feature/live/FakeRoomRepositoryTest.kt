package dev.fanstage.android.feature.live

import dev.fanstage.android.core.model.Room
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FakeRoomRepositoryTest {
    @Test
    fun `fetch returns stable fictional rooms in deterministic order`() = runTest {
        val repository = FakeRoomRepository()
        val expected = listOf(
            Room("studio-session", "Studio session", "Paper Lantern", 128),
            Room("sketch-break", "Afternoon sketch break", "Blue Pencil", 42),
            Room("tabletop-night", "Tabletop night", "Dice Garden", 76),
        )

        assertEquals(expected, repository.getRooms())
        assertEquals(expected, repository.getRooms())
        assertEquals(expected.size, expected.map { it.id }.distinct().size)
        assertTrue(expected.all { it.viewerCount >= 0 })
    }

    @Test
    fun `in-memory fixtures do not require an extra scheduler turn`() = runTest {
        val result = async(start = CoroutineStart.UNDISPATCHED) { FakeRoomRepository().getRooms() }
        assertTrue("in-memory snapshot should be available in the caller turn", result.isCompleted)
        assertEquals(3, result.await().size)
    }

    @Test
    fun `caller modification of one snapshot cannot change subsequent fetches`() = runTest {
        val repository = FakeRoomRepository()
        val first = repository.getRooms()
        val expected = first.toList()
        (first as MutableList<Room>).clear()

        assertEquals(expected, repository.getRooms())
    }
}
