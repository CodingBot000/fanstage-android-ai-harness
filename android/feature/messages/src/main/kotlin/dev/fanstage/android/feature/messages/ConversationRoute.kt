package dev.fanstage.android.feature.messages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.fanstage.android.core.model.FictionalArtist

@Composable
fun ConversationRoute(
    viewModel: MessagesViewModel,
    artistId: String,
    artists: List<FictionalArtist>,
    onBack: () -> Unit,
    draftViewModel: ConversationDraftViewModel,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val draft by draftViewModel.text.collectAsStateWithLifecycle()
    LaunchedEffect(artistId) { viewModel.onAction(MessagesUiAction.ConversationOpened(artistId)) }
    ConversationScreen(
        state.conversations.firstOrNull { it.artistId == artistId },
        artistId,
        artists,
        draft,
        draftViewModel::update,
        { draftViewModel.submit { viewModel.submitMessage(artistId, it) } },
        onBack,
    )
}
