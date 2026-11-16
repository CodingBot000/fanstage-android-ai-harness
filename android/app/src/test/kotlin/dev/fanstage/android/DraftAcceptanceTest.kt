package dev.fanstage.android

import dev.fanstage.android.core.data.FakeMessageRepository
import dev.fanstage.android.core.data.FakeCommunityRepository
import dev.fanstage.android.core.model.MessageRepository
import dev.fanstage.android.feature.messages.*
import dev.fanstage.android.feature.community.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DraftAcceptanceTest {
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
    @Test fun commentRetainsLoadingAndMissingTargetsAndAcceptsRepeatedText() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val repo = FakeCommunityRepository(); val vm = CommunityViewModel(repo)
            val draft = PostDraftViewModel(); draft.update("hello")
            assertFalse(draft.submit { vm.submitComment("post-paper-lantern",it) })
            assertEquals("hello",draft.text.value)
            runCurrent()
            assertFalse(draft.submit { vm.submitComment("absent",it) })
            assertTrue(draft.submit { vm.submitComment("post-paper-lantern",it) })
            draft.update("hello")
            assertTrue(draft.submit { vm.submitComment("post-paper-lantern",it) })
            assertEquals("",draft.text.value)
            assertEquals(listOf("hello","hello"),repo.posts().first().comments)
        } finally { Dispatchers.resetMain() }
    }
}
