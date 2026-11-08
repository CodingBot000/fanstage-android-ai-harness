package dev.fanstage.android.feature.media

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fanstage.android.core.model.DiscoverRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class MediaViewModel @Inject constructor(private val repository: DiscoverRepository) : ViewModel() {
    private val mutableState = MutableStateFlow<MediaState>(MediaState.Loading)
    val state = mutableState.asStateFlow()
    private var loadJob: Job? = null

    init {
        retry()
    }

    fun onAction(action: MediaUiAction) {
        when (action) {
            MediaUiAction.RetryClicked -> retry()
        }
    }

    fun retry() {
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                mutableState.value = MediaState.Loading
                try {
                    val data = repository.load()
                    currentCoroutineContext().ensureActive()
                    mutableState.value = MediaState.Ready(data)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    currentCoroutineContext().ensureActive()
                    mutableState.value = MediaState.Error
                }
            }
    }
}
