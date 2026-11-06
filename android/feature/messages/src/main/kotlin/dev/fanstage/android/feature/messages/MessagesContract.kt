package dev.fanstage.android.feature.messages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import dev.fanstage.android.core.model.*

data class MessagesUiState(val conversations: List<LocalConversation>)

sealed interface MessagesUiAction {
    data class ConversationOpened(val artistId: String) : MessagesUiAction

    data class SendClicked(val artistId: String, val text: String) : MessagesUiAction
}
