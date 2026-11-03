package dev.fanstage.android.core.model

interface RoomRepository {
    /** Returns a snapshot; an empty list is a successful result. Failures may throw. */
    suspend fun getRooms(): List<Room>
}
