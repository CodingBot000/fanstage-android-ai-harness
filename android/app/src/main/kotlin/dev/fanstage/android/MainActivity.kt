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
import androidx.compose.ui.res.painterResource
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FanStageApp() {
    val navController = rememberNavController()
    val communityViewModel: CommunityViewModel = viewModel()
    val discoverViewModel: DiscoverViewModel = viewModel()
    val messagesViewModel: MessagesViewModel = viewModel()

    val entry by navController.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route
    val tabs = listOf("rooms", "community", "media", "messages")
    Scaffold(topBar = {
        if (currentRoute in tabs) TopAppBar(title = { Text("fanstage", color = dev.fanstage.android.core.designsystem.Plum, style = MaterialTheme.typography.titleLarge) })
    }, bottomBar = {
        if (currentRoute in tabs) NavigationBar {
            tabs.forEachIndexed { index, route ->
                val label = stringResource(listOf(R.string.discover, R.string.community, R.string.media, R.string.messages)[index])
                NavigationBarItem(selected = currentRoute == route, onClick = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }, icon = { Icon(painterResource(listOf(R.drawable.nav_discover, R.drawable.nav_community, R.drawable.nav_media, R.drawable.nav_messages)[index]), contentDescription = null) }, label = { Text(label) }, modifier = Modifier.testTag("tab_$route"))
            }
        }
    }) { shellPadding ->
    NavHost(navController = navController, startDestination = "rooms", modifier = Modifier.padding(shellPadding)) {
        composable("rooms") {
            DiscoverRoute(viewModel = discoverViewModel, onRoomClick = { roomId ->
                navController.navigate("rooms/${Uri.encode(roomId)}") {
                    launchSingleTop = true
                }
            }, onReplayClick = { navController.navigate("replay") { launchSingleTop = true } }, onArtistClick = { navController.navigate("community/artist/${Uri.encode(it)}") })
        }
        composable("messages") { MessagesList(messagesViewModel, onOpen = { navController.navigate("messages/${Uri.encode(it)}") }) }
        composable("messages/{artistId}") { messageEntry ->
            ConversationScreen(messagesViewModel, requireNotNull(messageEntry.arguments?.getString("artistId")), onBack = { navController.popBackStack() })
        }
        composable("media") {
            MediaRoute(discoverViewModel, onLive = { navController.navigate("rooms/${Uri.encode(it)}") }, onReplay = { navController.navigate("replay") })
        }
        composable("community") {
            CommunityRoute(communityViewModel, null, onArtist = { navController.navigate("community/artist/${Uri.encode(it)}") }, onPost = { navController.navigate("community/post/${Uri.encode(it)}") })
        }
        composable("community/artist/{artistId}") { artistEntry ->
            val artistId = requireNotNull(artistEntry.arguments?.getString("artistId"))
            CommunityRoute(communityViewModel, artistId, onArtist = {}, onPost = { navController.navigate("community/post/${Uri.encode(it)}") }, onBack = { navController.popBackStack() })
        }
        composable("community/post/{postId}") { postEntry ->
            PostDetail(communityViewModel, requireNotNull(postEntry.arguments?.getString("postId")), onBack = { navController.popBackStack() })
        }
        composable("replay") {
            Scaffold { padding ->
                Column(Modifier.fillMaxSize().padding(padding).padding(16.dp).testTag("replay_detail")) {
                    TextButton(onClick = { navController.popBackStack() }, modifier = Modifier.testTag("replay_back")) { Text(stringResource(R.string.community_back)) }
                    Text(stringResource(R.string.replay), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
                    Text("Paper Lantern · Studio diary", style = MaterialTheme.typography.headlineMedium)
                    Spacer(Modifier.height(16.dp))
                    Card(Modifier.fillMaxWidth()) { Column(Modifier.padding(12.dp)) { PlayerPanel("replay-studio-diary") } }
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
