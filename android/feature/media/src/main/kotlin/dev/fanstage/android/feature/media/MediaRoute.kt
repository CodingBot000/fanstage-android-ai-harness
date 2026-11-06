package dev.fanstage.android.feature.media

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.fanstage.android.core.model.*

@Composable
fun MediaRoute(
    viewModel: MediaViewModel,
    onLive: (String) -> Unit,
    onReplay: () -> Unit,
    videos: List<DemoVideo> = emptyList(),
    onVideo: (String) -> Unit = {},
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableIntStateOf(0) }
    MediaScreen(
        state = state,
        filter = filter,
        onFilterSelected = { filter = it },
        onRetry = { viewModel.onAction(MediaUiAction.RetryClicked) },
        onLive = onLive,
        onReplay = onReplay,
        videos = videos,
        onVideo = onVideo,
    )
}
