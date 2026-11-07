package dev.fanstage.android.feature.live

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.fanstage.android.core.designsystem.BackTitleBar
import dev.fanstage.android.core.designsystem.CardSingleLineText
import dev.fanstage.android.feature.live.chat.ChatPanel
import dev.fanstage.android.feature.live.chat.ChatComposer
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.fanstage.android.feature.live.chat.ChatViewModel

@Composable
fun RoomListRoute(viewModel: RoomListViewModel, onRoomClick: (String) -> Unit) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RoomListScreen(state = state, onRetry = viewModel::retry, onRoomClick = onRoomClick)
}

@Composable
fun RoomListScreen(
    state: RoomUiState,
    onRetry: () -> Unit,
    onRoomClick: (String) -> Unit,
) {
    Scaffold { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(stringResource(R.string.live_rooms), style = MaterialTheme.typography.headlineMedium)
            when (state) {
                RoomUiState.Loading -> {
                    val loadingDescription = stringResource(R.string.loading_rooms)
                    Box(Modifier.fillMaxSize().testTag("rooms_loading"), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.semantics { contentDescription = loadingDescription })
                    }
                }
                is RoomUiState.Content -> LazyColumn(
                    modifier = Modifier.fillMaxSize().testTag("rooms_content"),
                    contentPadding = PaddingValues(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.rooms, key = { it.id }) { room ->
                        Card(
                            onClick = { onRoomClick(room.id) },
                            modifier = Modifier.fillMaxWidth().testTag("room_${room.id}"),
                        ) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                val title = when (room.id to room.title) {
                                    "studio-session" to "Studio session" -> stringResource(R.string.room_studio_title)
                                    "sketch-break" to "Afternoon sketch break" -> stringResource(R.string.room_sketch_title)
                                    "tabletop-night" to "Tabletop night" -> stringResource(R.string.room_tabletop_title)
                                    else -> room.title
                                }
                                CardSingleLineText(title, style = MaterialTheme.typography.titleMedium)
                                CardSingleLineText(stringResource(R.string.room_host, room.hostName), style = MaterialTheme.typography.bodyMedium)
                                CardSingleLineText(pluralStringResource(R.plurals.room_viewers, room.viewerCount, room.viewerCount), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                RoomUiState.Empty -> Column(Modifier.testTag("rooms_empty")) {
                    Text(stringResource(R.string.no_rooms), Modifier.padding(vertical = 16.dp))
                    Button(onClick = onRetry) { Text(stringResource(R.string.refresh)) }
                }
                is RoomUiState.Error -> Column(Modifier.testTag("rooms_error")) {
                    Text(stringResource(R.string.room_error), Modifier.padding(vertical = 16.dp))
                    Button(onClick = onRetry, modifier = Modifier.testTag("rooms_retry")) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        }
    }
}

@Composable
fun RoomDetailScreen(roomId: String, chatViewModel: ChatViewModel, developmentControls: Boolean = false, sourceUri: String = "asset:///hls/captionless.m3u8", onBack: () -> Unit) {
    val title = when (roomId) {
        "studio-session" -> stringResource(R.string.room_studio_title)
        "sketch-break" -> stringResource(R.string.room_sketch_title)
        "tabletop-night" -> stringResource(R.string.room_tabletop_title)
        else -> stringResource(R.string.room_detail)
    }
    var sendSignal by remember(roomId) { mutableIntStateOf(0) }
    val keyboardVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    var developmentFieldFocused by remember(roomId) { mutableStateOf(false) }
    Scaffold(topBar = { BackTitleBar(title, onBack, backTag = "room_back") }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).imePadding().testTag("room_detail")) {
        Column(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 16.dp)) {
            Text(if (roomId == "benchmark") "Benchmark · test video" else if (sourceUri.endsWith(".mp4")) stringResource(R.string.local_server_vod) else stringResource(R.string.demo_live_room), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) { PlayerPanel(roomId, sourceUri, compact = keyboardVisible) }
            }
        }
        Column(
            modifier = Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(12.dp)) { ChatPanel(chatViewModel, developmentControls, showComposer = false, sendSignal = sendSignal, onDevelopmentFocus = { developmentFieldFocused = it }) }
            }
            Text(stringResource(R.string.placeholder_description), style = MaterialTheme.typography.bodySmall)
        }
        if (!developmentFieldFocused || !keyboardVisible) {
            Box(Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) { ChatComposer(chatViewModel) { sendSignal++ } }
        }
        }
    }
}
