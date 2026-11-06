package dev.fanstage.android.feature.discover

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun DiscoverRoute(
    viewModel: DiscoverViewModel,
    onRoomClick: (String) -> Unit,
    onReplayClick: () -> Unit,
    onArtistClick: (String) -> Unit,
    localVideos: Boolean = false,
    onBenchmarkClick: () -> Unit = onReplayClick,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DiscoverScreen(
        state,
        { viewModel.onAction(DiscoverUiAction.RetryClicked) },
        onRoomClick,
        onReplayClick,
        onArtistClick,
        localVideos,
        onBenchmarkClick,
    )
}
