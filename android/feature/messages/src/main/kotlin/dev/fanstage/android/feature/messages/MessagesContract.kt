package dev.fanstage.android.feature.messages

import dev.fanstage.android.core.model.LocalConversation

data class MessagesUiState(val conversations: List<LocalConversation>)

sealed interface MessagesUiAction {
    data class ConversationOpened(val artistId: String) : MessagesUiAction

    data class SendClicked(val artistId: String, val text: String) : MessagesUiAction
}
