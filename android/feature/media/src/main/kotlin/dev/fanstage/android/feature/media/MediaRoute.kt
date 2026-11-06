package dev.fanstage.android.feature.media

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.fanstage.android.core.model.DemoVideo

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
