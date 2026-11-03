package dev.fanstage.android.feature.live

import dev.fanstage.android.core.model.Room

sealed interface RoomUiState {
    data object Loading : RoomUiState
    data class Content(val rooms: List<Room>) : RoomUiState
    data object Empty : RoomUiState
    data class Error(val message: String) : RoomUiState
}
