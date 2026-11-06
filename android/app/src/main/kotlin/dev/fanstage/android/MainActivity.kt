package dev.fanstage.android

import dev.fanstage.android.feature.messages.*
import dev.fanstage.android.core.data.DemoVideos

import dev.fanstage.android.feature.media.*

import dev.fanstage.android.feature.community.*

import dev.fanstage.android.feature.discover.*

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeInput
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import dev.chrisbanes.haze.blur.hazeBlur
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
    val hazeState = rememberHazeState()
    val isTab = currentRoute in tabs
    Scaffold(topBar = {
        if (isTab) TopAppBar(title = { Text("fanstage", color = dev.fanstage.android.core.designsystem.Plum, style = MaterialTheme.typography.titleLarge) })
    }) { shellPadding ->
    Box(Modifier.fillMaxSize().padding(shellPadding).consumeWindowInsets(shellPadding)) {
    NavHost(navController = navController, startDestination = "rooms", modifier = Modifier.fillMaxSize().hazeSource(hazeState)) {
        composable("rooms") {
            DiscoverRoute(viewModel = discoverViewModel, localVideos = BuildConfig.DEBUG, onRoomClick = { roomId ->
                val index = listOf("studio-session", "sketch-break", "tabletop-night").indexOf(roomId)
                if (BuildConfig.DEBUG && index >= 0) navController.navigate("local-video/${DemoVideos.all[index % DemoVideos.all.size].id}")
                else navController.navigate("rooms/${Uri.encode(roomId)}") { launchSingleTop = true }
            }, onReplayClick = { navController.navigate(if (BuildConfig.DEBUG) "local-video/${DemoVideos.all[3 % DemoVideos.all.size].id}" else "replay") { launchSingleTop = true } }, onBenchmarkClick = { navController.navigate("replay") }, onArtistClick = { navController.navigate("community/artist/${Uri.encode(it)}") })
        }
        composable("messages") { MessagesListRoute(messagesViewModel, artists, onOpen = { navController.navigate("messages/${Uri.encode(it)}") }) }
        composable("messages/{artistId}") { messageEntry ->
            ConversationRoute(messagesViewModel, requireNotNull(messageEntry.arguments?.getString("artistId")), artists, onBack = { navController.popBackStack() })
        }
        composable("media") {
            MediaRoute(mediaViewModel, onLive = { roomId -> val index = listOf("studio-session", "sketch-break", "tabletop-night").indexOf(roomId); if (BuildConfig.DEBUG && index >= 0) navController.navigate("local-video/${DemoVideos.all[index % DemoVideos.all.size].id}") else navController.navigate("rooms/${Uri.encode(roomId)}") }, onReplay = { navController.navigate("replay") }, videos = if (BuildConfig.DEBUG) DemoVideos.all else emptyList(), onVideo = { navController.navigate("local-video/$it") })
        }
        composable("community") {
            CommunityRoute(communityViewModel, null, onArtist = { navController.navigate("community/artist/${Uri.encode(it)}") }, onPost = { navController.navigate("community/post/${Uri.encode(it)}") }, artists = artists)
        }
        composable("community/artist/{artistId}") { artistEntry ->
            val artistId = requireNotNull(artistEntry.arguments?.getString("artistId"))
            CommunityRoute(communityViewModel, artistId, onArtist = {}, onPost = { navController.navigate("community/post/${Uri.encode(it)}") }, onBack = { navController.popBackStack() }, artists = artists)
        }
        composable("community/post/{postId}") { postEntry ->
            PostDetailRoute(communityViewModel, requireNotNull(postEntry.arguments?.getString("postId")), onBack = { navController.popBackStack() })
        }
        composable("local-video/{videoId}") { videoEntry ->
            val video = DemoVideos.all.firstOrNull { it.id == videoEntry.arguments?.getString("videoId") }
            if (BuildConfig.DEBUG && video != null) {
                val emulator = android.os.Build.HARDWARE.contains("ranchu", ignoreCase = true) || android.os.Build.HARDWARE.contains("goldfish", ignoreCase = true)
                val host = if (emulator) "10.0.2.2" else "127.0.0.1"
                val source = "http://$host:18765/demo-media/${video.fileName}"
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
    if (isTab) {
        val shape = RoundedCornerShape(50)
        NavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter).padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                .fillMaxWidth().height(72.dp).clip(shape)
                .hazeBlur(input = HazeInput.Backdrop(hazeState))
                .background(Color(0xFFDDE6E7).copy(alpha = 0.62f))
                .border(1.dp, Color.White.copy(alpha = 0.55f), shape)
                .testTag("floating_navigation"),
            containerColor = Color.Transparent,
            tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0),
        ) {
            tabs.forEachIndexed { index, route ->
                val label = stringResource(listOf(R.string.discover, R.string.community, R.string.media, R.string.messages)[index])
                NavigationBarItem(selected = currentRoute == route, onClick = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }, icon = { Icon(painterResource(listOf(R.drawable.nav_discover, R.drawable.nav_community, R.drawable.nav_media, R.drawable.nav_messages)[index]), contentDescription = null) }, label = { Text(label) }, modifier = Modifier.testTag("tab_$route"), colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Black.copy(alpha = 0.08f), selectedIconColor = Color(0xFF172022), selectedTextColor = Color(0xFF172022), unselectedIconColor = Color(0xFF172022), unselectedTextColor = Color(0xFF172022)))
            }
        }
    }
    }
    }
}
