package dev.fanstage.android

import dev.fanstage.android.feature.messages.*
import dev.fanstage.android.core.data.DemoVideos

import dev.fanstage.android.feature.media.*

import dev.fanstage.android.feature.community.*

import dev.fanstage.android.feature.discover.*

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
import dev.fanstage.android.feature.live.ReplayScreen
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
    val container = remember { AppContainer() }
    val communityViewModel: CommunityViewModel = viewModel(factory = container.viewModelFactory)
    val discoverViewModel: DiscoverViewModel = viewModel(factory = container.viewModelFactory)
    val messagesViewModel: MessagesViewModel = viewModel(factory = container.viewModelFactory)
    val mediaViewModel: MediaViewModel = viewModel(factory = container.viewModelFactory)
    val artists = container.artists

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
        composable("messages") { MessagesList(messagesViewModel, artists, onOpen = { navController.navigate("messages/${Uri.encode(it)}") }) }
        composable("messages/{artistId}") { messageEntry ->
            ConversationScreen(messagesViewModel, requireNotNull(messageEntry.arguments?.getString("artistId")), artists, onBack = { navController.popBackStack() })
        }
        composable("media") {
            MediaRoute(mediaViewModel, onLive = { navController.navigate("rooms/${Uri.encode(it)}") }, onReplay = { navController.navigate("replay") }, videos = if (BuildConfig.DEBUG) DemoVideos.all else emptyList(), onVideo = { navController.navigate("local-video/$it") })
        }
        composable("community") {
            CommunityRoute(communityViewModel, null, onArtist = { navController.navigate("community/artist/${Uri.encode(it)}") }, onPost = { navController.navigate("community/post/${Uri.encode(it)}") }, artists = artists)
        }
        composable("community/artist/{artistId}") { artistEntry ->
            val artistId = requireNotNull(artistEntry.arguments?.getString("artistId"))
            CommunityRoute(communityViewModel, artistId, onArtist = {}, onPost = { navController.navigate("community/post/${Uri.encode(it)}") }, onBack = { navController.popBackStack() }, artists = artists)
        }
        composable("community/post/{postId}") { postEntry ->
            PostDetail(communityViewModel, requireNotNull(postEntry.arguments?.getString("postId")), onBack = { navController.popBackStack() })
        }
        composable("local-video/{videoId}") { videoEntry ->
            val video = DemoVideos.all.firstOrNull { it.id == videoEntry.arguments?.getString("videoId") }
            if (BuildConfig.DEBUG && video != null) {
                val source = "http://10.0.2.2:18765/demo-media/${video.fileName}"
                val index = DemoVideos.all.indexOf(video)
                val roomId = listOf("studio-session", "sketch-break", "tabletop-night").getOrNull(index)
                if (roomId != null) {
                    val chatFactory = remember(roomId) { ChatViewModel.Factory(roomId) }
                    val chatViewModel: ChatViewModel = viewModel(viewModelStoreOwner = videoEntry, factory = chatFactory)
                    RoomDetailScreen(roomId, chatViewModel, developmentControls = true, sourceUri = source, onBack = { navController.popBackStack() })
                } else ReplayScreen(onBack = { navController.popBackStack() }, sourceUri = source, title = video.title)
            }
        }
        composable("replay") { ReplayScreen(onBack = { navController.popBackStack() }) }
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
