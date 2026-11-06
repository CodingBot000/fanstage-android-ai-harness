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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.fanstage.android.core.designsystem.*
import dev.fanstage.android.core.model.*

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
