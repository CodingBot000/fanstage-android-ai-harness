package dev.fanstage.android.feature.messages

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.CancellationException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class ConversationDraftViewModelTest {
    private fun create(store: ViewModelStore, key: String = "entry"): ConversationDraftViewModel =
        ViewModelProvider(store, ViewModelProvider.NewInstanceFactory())[key, ConversationDraftViewModel::class.java]

    @Test fun ownerReuseRetainsTextButOtherEntriesAndNewProcessesStartEmpty() {
        val store = ViewModelStore(); val fresh = ViewModelStore()
        try {
            val first = create(store); first.update("private draft")
            assertEquals("private draft", create(store).text.value)
            assertEquals("", create(store,"other").text.value)
            assertEquals("", create(fresh).text.value)
            store.clear()
            assertEquals("",first.text.value)
            assertEquals("",create(store).text.value)
        } finally { store.clear(); fresh.clear() }
    }

    @Test fun onlyAcceptedSubmissionClearsTheSubmittedDraft() {
        val store = ViewModelStore()
        try {
            val draft = create(store)
            draft.update("   ")
            assertFalse(draft.submit { fail("blank must not submit"); true })
            assertEquals("   ",draft.text.value)
            draft.update("keep")
            assertFalse(draft.submit { false }); assertEquals("keep",draft.text.value)
            assertFalse(draft.submit { throw IllegalStateException() }); assertEquals("keep",draft.text.value)
            assertThrows(CancellationException::class.java) { draft.submit { throw CancellationException() } }
            assertEquals("keep",draft.text.value)
            assertTrue(draft.submit { assertEquals("keep",it); true })
            assertEquals("",draft.text.value)
            draft.update("old")
            assertTrue(draft.submit { draft.update("new edit"); true })
            assertEquals("new edit",draft.text.value)
        } finally { store.clear() }
    }
}
