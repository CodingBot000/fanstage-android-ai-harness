package dev.fanstage.android.navigation

import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import dev.fanstage.android.BuildConfig
import dev.fanstage.android.core.data.DemoVideos
import dev.fanstage.android.core.model.FictionalArtist
import dev.fanstage.android.feature.community.CommunityViewModel
import dev.fanstage.android.feature.community.communityArtistRoute
import dev.fanstage.android.feature.community.communityDestinations
import dev.fanstage.android.feature.community.communityPostRoute
import dev.fanstage.android.feature.discover.DISCOVER_ROUTE
import dev.fanstage.android.feature.discover.DiscoverViewModel
import dev.fanstage.android.feature.discover.discoverDestination
import dev.fanstage.android.feature.live.LocalVideoPlayback
import dev.fanstage.android.feature.live.REPLAY_ROUTE
import dev.fanstage.android.feature.live.liveDestinations
import dev.fanstage.android.feature.live.liveRoomRoute
import dev.fanstage.android.feature.live.localVideoRoute
import dev.fanstage.android.feature.media.MediaViewModel
import dev.fanstage.android.feature.media.mediaDestination
import dev.fanstage.android.feature.messages.MessagesViewModel
import dev.fanstage.android.feature.messages.conversationRoute
import dev.fanstage.android.feature.messages.messagesDestinations

@Composable
internal fun FanStageNavHost(
    navController: NavHostController,
    hazeState: HazeState,
    communityViewModel: CommunityViewModel,
    discoverViewModel: DiscoverViewModel,
    messagesViewModel: MessagesViewModel,
    mediaViewModel: MediaViewModel,
    artists: List<FictionalArtist>,
) {
    val navigateBack: () -> Unit = { navController.popBackStack() }
    val openRoom: (String) -> Unit = { roomId ->
        navController.navigate(liveRoomRoute(roomId)) { launchSingleTop = true }
    }
    val openArtist: (String) -> Unit = { artistId ->
        navController.navigate(communityArtistRoute(artistId))
    }
    NavHost(
        navController = navController,
        startDestination = DISCOVER_ROUTE,
        modifier = Modifier.fillMaxSize().hazeSource(hazeState),
    ) {
        discoverDestination(
            viewModel = discoverViewModel,
            localVideos = BuildConfig.DEBUG,
            onOpenRoom = openRoom,
            onOpenReplay = {
                val route = if (BuildConfig.DEBUG) {
                    localVideoRoute(DemoVideos.all[3 % DemoVideos.all.size].id)
                } else {
                    REPLAY_ROUTE
                }
                navController.navigate(route) { launchSingleTop = true }
            },
            onOpenBenchmark = { navController.navigate(REPLAY_ROUTE) },
            onOpenArtist = openArtist,
        )
        messagesDestinations(
            messagesViewModel,
            artists,
            onOpenConversation = { navController.navigate(conversationRoute(it)) },
            onBack = navigateBack,
        )
        mediaDestination(
            viewModel = mediaViewModel,
            videos = if (BuildConfig.DEBUG) DemoVideos.all else emptyList(),
            onOpenRoom = openRoom,
            onOpenReplay = { navController.navigate(REPLAY_ROUTE) },
            onOpenVideo = { navController.navigate(localVideoRoute(it)) },
        )
        communityDestinations(
            communityViewModel,
            artists,
            onOpenArtist = openArtist,
            onOpenPost = { navController.navigate(communityPostRoute(it)) },
            onBack = navigateBack,
        )
        liveDestinations(
            developmentControls = BuildConfig.DEBUG,
            liveSource = { if (BuildConfig.DEBUG) liveDemoSource(it) else null },
            localVideo = { if (BuildConfig.DEBUG) localPlayback(it) else null },
            onBack = navigateBack,
        )
    }
}

// A prerecorded source can play inside a LIVE room without changing its chat destination.
private fun liveDemoSource(roomId: String): String? {
    val videoId = when (roomId) {
        "studio-session" -> "demo-01"
        "sketch-break" -> "demo-02"
        "tabletop-night" -> "demo-03"
        else -> return null
    }
    return DemoVideos.all.firstOrNull { it.id == videoId }?.let { localDemoVideoUrl(it.fileName) }
}

private fun localPlayback(videoId: String): LocalVideoPlayback? {
    val video = DemoVideos.all.firstOrNull { it.id == videoId } ?: return null
    return LocalVideoPlayback(sourceUri = localDemoVideoUrl(video.fileName), title = video.title)
}

/** Devices can reach the same host via an explicit ADB reverse port. */
private fun localDemoVideoUrl(fileName: String): String {
    val emulator = Build.HARDWARE.contains("ranchu", ignoreCase = true) ||
        Build.HARDWARE.contains("goldfish", ignoreCase = true)
    val host = if (emulator) "10.0.2.2" else "127.0.0.1"
    return "http://$host:18765/demo-media/$fileName"
}
