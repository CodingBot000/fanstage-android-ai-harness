package dev.fanstage.android.feature.messages

import androidx.lifecycle.ViewModel
import dev.fanstage.android.core.model.MessageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MessagesViewModel(private val repository: MessageRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(MessagesUiState(repository.snapshot()))
    val state = mutableState.asStateFlow()

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
