package dev.fanstage.android.feature.live

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.fanstage.android.core.designsystem.BackTitleBar
import dev.fanstage.android.feature.live.chat.ChatComposer
import dev.fanstage.android.feature.live.chat.ChatPanel
import dev.fanstage.android.feature.live.chat.ChatViewModel

@Composable
fun RoomDetailScreen(roomId: String, chatViewModel: ChatViewModel, developmentControls: Boolean = false, sourceUri: String = "asset:///hls/captionless.m3u8", onBack: () -> Unit) {
    VideoPlaybackTheme {
    val title = when (roomId) {
        "studio-session" -> stringResource(R.string.room_studio_title)
        "sketch-break" -> stringResource(R.string.room_sketch_title)
        "tabletop-night" -> stringResource(R.string.room_tabletop_title)
        else -> stringResource(R.string.room_detail)
    }
    var sendSignal by remember(roomId) { mutableIntStateOf(0) }
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    var developmentFieldFocused by remember(roomId) { mutableStateOf(false) }
    Scaffold(
        modifier = Modifier.testTag("video_playback_dark_screen"),
        containerColor = Color.Black,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = { BackTitleBar(title, onBack, backTag = "room_back") },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().testTag("room_detail")) {
        Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            Text(if (roomId == "benchmark") stringResource(R.string.benchmark_stream) else if (sourceUri.endsWith(".mp4")) stringResource(R.string.live_demo_recording) else stringResource(R.string.demo_live_room), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.Black, contentColor = MaterialTheme.colorScheme.onBackground),
            ) {
                Column(Modifier.padding(12.dp)) { PlayerPanel(roomId, sourceUri, compact = keyboardVisible, settingsAtEnd = true) }
            }
        }
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF111315), contentColor = MaterialTheme.colorScheme.onSurface),
            ) {
                Column(Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) { ChatPanel(chatViewModel, developmentControls, showComposer = false, sendSignal = sendSignal, onDevelopmentFocus = { developmentFieldFocused = it }, compactDevelopmentControls = developmentFieldFocused && keyboardVisible) }
            }
        }
        if (!developmentFieldFocused || !keyboardVisible) {
            Box(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) { ChatComposer(chatViewModel) { sendSignal++ } }
        }
        }
    }
    }
}
