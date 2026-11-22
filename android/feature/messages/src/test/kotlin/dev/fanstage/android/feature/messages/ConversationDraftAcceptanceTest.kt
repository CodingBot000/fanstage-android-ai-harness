package dev.fanstage.android.feature.messages

import dev.fanstage.android.core.data.FakeMessageRepository
import dev.fanstage.android.core.model.MessageRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConversationDraftAcceptanceTest {
    @Test fun messageAcceptsOnlyActualNewRepositoryMessage() {
        val repo = FakeMessageRepository(); val vm = MessagesViewModel(repo)
        val draft = ConversationDraftViewModel(); draft.update(" hello ")
        assertFalse(draft.submit { vm.submitMessage("absent",it) }); assertEquals(" hello ",draft.text.value)
        assertTrue(draft.submit { vm.submitMessage("paper-lantern",it) }); assertEquals("",draft.text.value)
        assertEquals("hello",repo.snapshot().first().messages.last().text)
        val refusing = MessagesViewModel(object : MessageRepository by repo {
            override fun send(artistId: String,text: String) = repo.snapshot()
        })
        draft.update("retain")
        assertFalse(draft.submit { refusing.submitMessage("paper-lantern",it) })
        assertEquals("retain",draft.text.value)
    }
}
