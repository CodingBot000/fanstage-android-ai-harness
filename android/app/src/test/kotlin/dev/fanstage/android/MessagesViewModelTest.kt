package dev.fanstage.android

import dev.fanstage.android.feature.messages.*

import dev.fanstage.android.core.data.FakeMessageRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MessagesViewModelTest {
    @Test fun blankMessageIsIgnoredAndReadIsScopedToConversation() {
        val model = MessagesViewModel(FakeMessageRepository())
        val initialSize = model.state.value.conversations.first().messages.size
        model.onAction(MessagesUiAction.SendClicked("paper-lantern", "  "))
        assertEquals(initialSize, model.state.value.conversations.first().messages.size)
        model.onAction(MessagesUiAction.ConversationOpened("paper-lantern"))
        assertFalse(model.state.value.conversations.first().unread)
        assertEquals(true, model.state.value.conversations.last().unread)
        val longMessage = "message ".repeat(200)
        model.onAction(MessagesUiAction.SendClicked("paper-lantern", longMessage))
        assertEquals(longMessage.trim(), model.state.value.conversations.first().messages.last().text)
        assertEquals(initialSize, model.state.value.conversations.last().messages.size)
    }
}
