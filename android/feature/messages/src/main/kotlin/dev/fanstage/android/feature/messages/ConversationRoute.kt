package dev.fanstage.android.feature.messages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.fanstage.android.core.model.FictionalArtist

@Composable
fun ConversationRoute(
    viewModel: MessagesViewModel,
    artistId: String,
    artists: List<FictionalArtist>,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var draft by remember(artistId) { mutableStateOf("") }
    LaunchedEffect(artistId) { viewModel.onAction(MessagesUiAction.ConversationOpened(artistId)) }
    ConversationScreen(
        state.conversations.firstOrNull { it.artistId == artistId },
        artistId,
        artists,
        draft,
        { draft = it },
        viewModel::onAction,
        onBack,
    )
}
