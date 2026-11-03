package dev.fanstage.android.feature.live

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.fanstage.android.core.model.RoomRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RoomListViewModel(private val repository: RoomRepository) : ViewModel() {
    private val mutableUiState = MutableStateFlow<RoomUiState>(RoomUiState.Loading)
    val uiState: StateFlow<RoomUiState> = mutableUiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        retry()
    }

    fun retry() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            mutableUiState.value = RoomUiState.Loading
            try {
                val rooms = repository.getRooms().toList()
                currentCoroutineContext().ensureActive()
                mutableUiState.value = if (rooms.isEmpty()) {
                    RoomUiState.Empty
                } else {
                    RoomUiState.Content(rooms)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                currentCoroutineContext().ensureActive()
                // Never expose repository exception details to the screen or logs.
                mutableUiState.value = RoomUiState.Error("Rooms could not be loaded.")
            }
        }
    }

    class Factory(private val repository: RoomRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == RoomListViewModel::class.java)
            return modelClass.cast(RoomListViewModel(repository))!!
        }
    }
}
