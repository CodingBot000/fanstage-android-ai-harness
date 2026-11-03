package dev.fanstage.android.feature.live

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
                                Text(room.title, style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(R.string.room_host, room.hostName))
                                Text(pluralStringResource(R.plurals.room_viewers, room.viewerCount, room.viewerCount))
                            }
                        }
                    }
                }
                RoomUiState.Empty -> Column(Modifier.testTag("rooms_empty")) {
                    Text(stringResource(R.string.no_rooms), Modifier.padding(vertical = 16.dp))
                    Button(onClick = onRetry) { Text(stringResource(R.string.refresh)) }
                }
                is RoomUiState.Error -> Column(Modifier.testTag("rooms_error")) {
                    Text(state.message, Modifier.padding(vertical = 16.dp))
                    Button(onClick = onRetry, modifier = Modifier.testTag("rooms_retry")) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }
        }
    }
}

@Composable
fun RoomDetailScreen(roomId: String, onBack: () -> Unit) {
    Scaffold { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp).testTag("room_detail"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            TextButton(onClick = onBack, modifier = Modifier.testTag("room_back")) {
                Text(stringResource(R.string.back_to_rooms))
            }
            Text(stringResource(R.string.room_detail), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.room_identifier, roomId))
            Card(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.player_placeholder), Modifier.padding(24.dp))
            }
            Card(Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.chat_placeholder), Modifier.padding(24.dp))
            }
            Text(stringResource(R.string.placeholder_description))
        }
    }
}
