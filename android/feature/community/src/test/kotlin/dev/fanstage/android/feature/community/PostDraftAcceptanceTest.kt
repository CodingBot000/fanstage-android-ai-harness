package dev.fanstage.android.feature.community

import dev.fanstage.android.core.data.FakeCommunityRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PostDraftAcceptanceTest {
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
