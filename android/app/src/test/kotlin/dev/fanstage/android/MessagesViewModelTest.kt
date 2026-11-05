package dev.fanstage.android

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MessagesViewModelTest {
    @Test fun blankMessageIsIgnoredAndReadIsScopedToConversation() {
        val model = MessagesViewModel(object : MessageRepository {
            override fun initial() = listOf(LocalConversation("a", emptyList()), LocalConversation("b", emptyList()))
        })
        model.send("a", "  ")
        assertEquals(0, model.state.value.first().messages.size)
        model.markRead("a")
        assertFalse(model.state.value.first().unread)
        assertEquals(true, model.state.value.last().unread)
        val longMessage = "message ".repeat(200)
        model.send("a", longMessage)
        assertEquals(longMessage.trim(), model.state.value.first().messages.single().text)
        assertEquals(0, model.state.value.last().messages.size)
    }
}
