package dev.fanstage.android.feature.messages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.fanstage.android.core.model.*

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
