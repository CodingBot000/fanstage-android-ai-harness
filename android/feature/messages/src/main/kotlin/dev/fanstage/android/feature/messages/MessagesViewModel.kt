package dev.fanstage.android.feature.messages

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import dev.fanstage.android.core.model.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class MessagesViewModel @Inject constructor(private val repository: MessageRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(MessagesUiState(repository.snapshot()))
    val state = mutableState.asStateFlow()

    fun submitMessage(artistId: String, text: String): Boolean {
        val clean = text.trim()
        val before = mutableState.value.conversations.firstOrNull { it.artistId == artistId } ?: return false
        if (clean.isEmpty()) return false
        val result = repository.send(artistId, clean)
        val oldIds = before.messages.map { it.id }.toSet()
        val accepted = result.firstOrNull { it.artistId == artistId }?.messages?.any {
            it.fromMe && it.text == clean && it.id !in oldIds
        } == true
        mutableState.value = MessagesUiState(result)
        return accepted
    }

    fun onAction(action: MessagesUiAction) {
        mutableState.value =
            MessagesUiState(
                when (action) {
                    is MessagesUiAction.ConversationOpened -> repository.markRead(action.artistId)
                    is MessagesUiAction.SendClicked -> repository.send(action.artistId, action.text)
                }
            )
    }
}
