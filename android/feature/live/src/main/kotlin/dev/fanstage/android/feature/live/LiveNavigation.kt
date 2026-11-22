package dev.fanstage.android.feature.live

import android.net.Uri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import dev.fanstage.android.feature.live.chat.ChatViewModel

private const val ROOM_ID = "roomId"
private const val VIDEO_ID = "videoId"
const val LIVE_ROOM_ROUTE = "rooms/{$ROOM_ID}"
const val LOCAL_VIDEO_ROUTE = "local-video/{$VIDEO_ID}"
const val REPLAY_ROUTE = "replay"

fun liveRoomRoute(roomId: String): String = "rooms/${Uri.encode(roomId)}"

fun localVideoRoute(videoId: String): String = "local-video/$videoId"

data class LocalVideoPlayback(val sourceUri: String, val title: String)

fun NavGraphBuilder.liveDestinations(
    developmentControls: Boolean,
    liveSource: (String) -> String?,
    localVideo: (String) -> LocalVideoPlayback?,
    onBack: () -> Unit,
) {
    composable(LOCAL_VIDEO_ROUTE) { entry ->
        entry.arguments?.getString(VIDEO_ID)?.let(localVideo)?.let { video ->
            ReplayScreen(onBack = onBack, sourceUri = video.sourceUri, title = video.title)
        }
    }
    composable(REPLAY_ROUTE) { ReplayScreen(onBack = onBack) }
    composable(
        route = LIVE_ROOM_ROUTE,
        arguments = listOf(navArgument(ROOM_ID) { type = NavType.StringType }),
    ) { entry ->
        val roomId = requireNotNull(entry.arguments?.getString(ROOM_ID))
        val chatViewModel: ChatViewModel = hiltViewModel(viewModelStoreOwner = entry)
        RoomDetailScreen(
            roomId = roomId,
            chatViewModel = chatViewModel,
            developmentControls = developmentControls,
            sourceUri = liveSource(roomId) ?: DEFAULT_LIVE_SOURCE_URI,
            onBack = onBack,
        )
    }
}
