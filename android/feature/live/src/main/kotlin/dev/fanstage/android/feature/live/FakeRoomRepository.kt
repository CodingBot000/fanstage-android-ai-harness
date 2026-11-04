package dev.fanstage.android.feature.live

import dev.fanstage.android.core.model.Room
import dev.fanstage.android.core.model.RoomRepository

/** Local, fictional fixtures. No media URLs, network access, or persistent storage. */
class FakeRoomRepository : RoomRepository {
    override suspend fun getRooms(): List<Room> {
        return fixtures.toList()
    }

    private companion object {
        val fixtures = listOf(
            Room("studio-session", "Studio session", "Paper Lantern", 128),
            Room("sketch-break", "Afternoon sketch break", "Blue Pencil", 42),
            Room("tabletop-night", "Tabletop night", "Dice Garden", 76),
        )
    }
}
