package dev.fanstage.android.feature.messages

import android.net.Uri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.fanstage.android.core.model.FictionalArtist

const val MESSAGES_ROUTE = "messages"
private const val ARTIST_ID = "artistId"

fun conversationRoute(artistId: String): String = "$MESSAGES_ROUTE/${Uri.encode(artistId)}"

fun NavGraphBuilder.messagesDestinations(
    messagesViewModel: MessagesViewModel,
    artists: List<FictionalArtist>,
    onOpenConversation: (String) -> Unit,
    onBack: () -> Unit,
) {
    composable(MESSAGES_ROUTE) {
        MessagesListRoute(messagesViewModel, artists, onOpen = onOpenConversation)
    }
    composable("$MESSAGES_ROUTE/{$ARTIST_ID}") { entry ->
        val artistId = requireNotNull(entry.arguments?.getString(ARTIST_ID))
        val draftViewModel: ConversationDraftViewModel = viewModel(viewModelStoreOwner = entry)
        ConversationRoute(messagesViewModel, artistId, artists, onBack, draftViewModel)
    }
}
