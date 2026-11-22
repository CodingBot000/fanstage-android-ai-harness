package dev.fanstage.android

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.runtime.CompositionLocalProvider
import dev.fanstage.android.core.designsystem.CollapsingTitleBarState
import dev.fanstage.android.core.designsystem.LocalCollapsingTitleBar
import dev.fanstage.android.core.designsystem.rememberCollapsingTitleBarState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeInput
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import dev.fanstage.android.feature.community.COMMUNITY_ROUTE
import dev.fanstage.android.feature.community.CommunityViewModel
import dev.fanstage.android.feature.discover.DISCOVER_ROUTE
import dev.fanstage.android.feature.discover.DiscoverViewModel
import dev.fanstage.android.feature.live.LIVE_ROOM_ROUTE
import dev.fanstage.android.feature.live.LOCAL_VIDEO_ROUTE
import dev.fanstage.android.feature.live.REPLAY_ROUTE
import dev.fanstage.android.feature.media.MEDIA_ROUTE
import dev.fanstage.android.feature.media.MediaViewModel
import dev.fanstage.android.feature.messages.MESSAGES_ROUTE
import dev.fanstage.android.feature.messages.MessagesViewModel
import dev.fanstage.android.navigation.FanStageNavHost

@dagger.hilt.android.AndroidEntryPoint
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
    val communityViewModel: CommunityViewModel = hiltViewModel()
    val discoverViewModel: DiscoverViewModel = hiltViewModel()
    val messagesViewModel: MessagesViewModel = hiltViewModel()
    val mediaViewModel: MediaViewModel = hiltViewModel()
    val artists = remember { dev.fanstage.android.core.data.FakeCatalog.artists() }

    val entry by navController.currentBackStackEntryAsState()
    val currentRoute = entry?.destination?.route
    val tabs = listOf(DISCOVER_ROUTE, COMMUNITY_ROUTE, MEDIA_ROUTE, MESSAGES_ROUTE)
    val hazeState = rememberHazeState()
    val isTab = currentRoute in tabs
    val isPlaybackRoute = currentRoute == LIVE_ROOM_ROUTE ||
        currentRoute == LOCAL_VIDEO_ROUTE || currentRoute == REPLAY_ROUTE
    val titleBarState = rememberCollapsingTitleBarState(entry)
    FanStageAppShell(navController, currentRoute, tabs, isTab, isPlaybackRoute, titleBarState, hazeState) {
        FanStageNavHost(navController, hazeState, communityViewModel, discoverViewModel, messagesViewModel, mediaViewModel, artists)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FanStageAppShell(
    navController: NavHostController,
    currentRoute: String?,
    tabs: List<String>,
    isTab: Boolean,
    isPlaybackRoute: Boolean,
    titleBarState: CollapsingTitleBarState,
    hazeState: HazeState,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(LocalCollapsingTitleBar provides titleBarState) {
    Scaffold(modifier = Modifier.nestedScroll(titleBarState.connection), containerColor = if (isPlaybackRoute) Color.Black else MaterialTheme.colorScheme.background, topBar = {
        if (isTab) Column(Modifier.statusBarsPadding()) {
            Box(Modifier.fillMaxWidth().height(64.dp * titleBarState.visibleFraction).clipToBounds().testTag("collapsing_top_bar")) {
                TopAppBar(title = { Text(stringResource(R.string.app_name), color = dev.fanstage.android.core.designsystem.Plum, style = MaterialTheme.typography.titleLarge) }, windowInsets = WindowInsets(0, 0, 0, 0))
            }
        }
    }) { shellPadding ->
    Box(Modifier.fillMaxSize().padding(shellPadding).consumeWindowInsets(shellPadding)) {
    content()
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
}
