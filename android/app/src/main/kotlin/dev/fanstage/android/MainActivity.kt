package dev.fanstage.android

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.fanstage.android.feature.live.FakeRoomRepository
import dev.fanstage.android.feature.live.RoomDetailScreen
import dev.fanstage.android.feature.live.RoomListRoute
import dev.fanstage.android.feature.live.RoomListViewModel
import dev.fanstage.android.feature.live.chat.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme { FanStageApp() }
        }
    }
}

@Composable
private fun FanStageApp() {
    val navController = rememberNavController()
    val repository = remember { FakeRoomRepository() }
    val factory = remember(repository) { RoomListViewModel.Factory(repository) }

    NavHost(navController = navController, startDestination = "rooms") {
        composable("rooms") {
            val roomViewModel: RoomListViewModel = viewModel(factory = factory)
            RoomListRoute(viewModel = roomViewModel, onRoomClick = { roomId ->
                navController.navigate("rooms/${Uri.encode(roomId)}") {
                    launchSingleTop = true
                }
            })
        }
        composable(
            route = "rooms/{roomId}",
            arguments = listOf(navArgument("roomId") { type = NavType.StringType }),
        ) { entry ->
            val roomId = requireNotNull(entry.arguments?.getString("roomId"))
            val chatFactory = remember(roomId) { ChatViewModel.Factory(roomId) }
            val chatViewModel: ChatViewModel = viewModel(viewModelStoreOwner = entry, factory = chatFactory)
            RoomDetailScreen(
                roomId = roomId,
                chatViewModel = chatViewModel,
                developmentControls = BuildConfig.DEBUG,
                onBack = { navController.popBackStack() },
            )
        }
    }
}
