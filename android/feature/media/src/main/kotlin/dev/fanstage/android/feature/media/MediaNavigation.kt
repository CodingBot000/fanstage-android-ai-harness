package dev.fanstage.android.feature.media

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.fanstage.android.core.model.DemoVideo

const val MEDIA_ROUTE = "media"

fun NavGraphBuilder.mediaDestination(
    viewModel: MediaViewModel,
    videos: List<DemoVideo>,
    onOpenRoom: (String) -> Unit,
    onOpenReplay: () -> Unit,
    onOpenVideo: (String) -> Unit,
) {
    composable(MEDIA_ROUTE) {
        MediaRoute(
            viewModel = viewModel,
            videos = videos,
            onLive = onOpenRoom,
            onReplay = onOpenReplay,
            onVideo = onOpenVideo,
        )
    }
}
