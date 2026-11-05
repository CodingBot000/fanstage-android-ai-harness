package dev.fanstage.android

import dev.fanstage.android.feature.messages.*

import dev.fanstage.android.core.data.FakeMessageRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MessagesViewModelTest {
    @Test fun blankMessageIsIgnoredAndReadIsScopedToConversation() {
        val model = MessagesViewModel(FakeMessageRepository())
        val initialSize = model.state.value.first().messages.size
        model.send("paper-lantern", "  ")
        assertEquals(initialSize, model.state.value.first().messages.size)
        model.markRead("paper-lantern")
        assertFalse(model.state.value.first().unread)
        assertEquals(true, model.state.value.last().unread)
        val longMessage = "message ".repeat(200)
        model.send("paper-lantern", longMessage)
        assertEquals(longMessage.trim(), model.state.value.first().messages.last().text)
        assertEquals(initialSize, model.state.value.last().messages.size)
    }
}
