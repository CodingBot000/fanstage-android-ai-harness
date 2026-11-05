package dev.fanstage.android

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.fanstage.android.feature.live.PlayerPanel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.navArgument
import dev.fanstage.android.feature.live.RoomDetailScreen
import dev.fanstage.android.feature.live.chat.ChatViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            dev.fanstage.android.core.designsystem.FanStageTheme { FanStageApp() }
        }
    }
}

@Composable
private fun FanStageApp() {
    val navController = rememberNavController()

    val entry by navController.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route
    val tabs = listOf("rooms", "community", "media", "messages")
    Scaffold(bottomBar = {
        if (currentRoute in tabs) NavigationBar {
            tabs.forEachIndexed { index, route ->
                val label = stringResource(listOf(R.string.discover, R.string.community, R.string.media, R.string.messages)[index])
                NavigationBarItem(selected = currentRoute == route, onClick = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }, icon = { Text(listOf("✦", "◉", "▶", "✉")[index]) }, label = { Text(label) }, modifier = Modifier.testTag("tab_$route"))
            }
        }
    }) { shellPadding ->
    NavHost(navController = navController, startDestination = "rooms", modifier = Modifier.padding(shellPadding)) {
        composable("rooms") {
            val homeViewModel: DiscoverViewModel = viewModel()
            DiscoverRoute(viewModel = homeViewModel, onRoomClick = { roomId ->
                navController.navigate("rooms/${Uri.encode(roomId)}") {
                    launchSingleTop = true
                }
            }, onReplayClick = { navController.navigate("replay") { launchSingleTop = true } })
        }
        listOf("community", "media", "messages").forEach { route ->
            composable(route) {
                Column(Modifier.fillMaxSize().padding(24.dp).testTag("screen_$route")) {
                    Text(stringResource(when (route) { "community" -> R.string.community; "media" -> R.string.media; else -> R.string.messages }), style = MaterialTheme.typography.headlineMedium)
                    Text(stringResource(R.string.coming_soon))
                }
            }
        }
        composable("replay") {
            Scaffold { padding ->
                Column(Modifier.fillMaxSize().padding(padding).padding(16.dp).testTag("replay_detail")) {
                    TextButton(onClick = { navController.popBackStack() }, modifier = Modifier.testTag("replay_back")) { Text(stringResource(R.string.replay_back)) }
                    Text("Paper Lantern · Studio diary", style = MaterialTheme.typography.headlineMedium)
                    PlayerPanel("replay-studio-diary")
                }
            }
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
}
