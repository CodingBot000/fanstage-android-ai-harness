package dev.fanstage.android.feature.discover

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fanstage.android.core.designsystem.*
import dev.fanstage.android.core.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DiscoverViewModel(private val repository: DiscoverRepository) : ViewModel() {
    private val mutableState = MutableStateFlow<DiscoverState>(DiscoverState.Loading)
    val state = mutableState.asStateFlow()
    private var loadJob: Job? = null

    init {
        retry()
    }

    fun onAction(action: DiscoverUiAction) {
        when (action) {
            DiscoverUiAction.RetryClicked -> retry()
        }
    }

    fun retry() {
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                mutableState.value = DiscoverState.Loading
                try {
                    val content = repository.load()
                    currentCoroutineContext().ensureActive()
                    mutableState.value = DiscoverState.Ready(content)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    currentCoroutineContext().ensureActive()
                    mutableState.value = DiscoverState.Error
                }
            }
    }
}
