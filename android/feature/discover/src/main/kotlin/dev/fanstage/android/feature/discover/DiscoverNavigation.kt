package dev.fanstage.android.feature.discover

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val DISCOVER_ROUTE = "rooms"

fun NavGraphBuilder.discoverDestination(
    viewModel: DiscoverViewModel,
    localVideos: Boolean,
    onOpenRoom: (String) -> Unit,
    onOpenReplay: () -> Unit,
    onOpenBenchmark: () -> Unit,
    onOpenArtist: (String) -> Unit,
) {
    composable(DISCOVER_ROUTE) {
        DiscoverRoute(
            viewModel = viewModel,
            localVideos = localVideos,
            onRoomClick = onOpenRoom,
            onReplayClick = onOpenReplay,
            onBenchmarkClick = onOpenBenchmark,
            onArtistClick = onOpenArtist,
        )
    }
}
