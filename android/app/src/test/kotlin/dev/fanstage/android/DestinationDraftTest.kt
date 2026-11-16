package dev.fanstage.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import dev.fanstage.android.feature.messages.ConversationDraftViewModel
import dev.fanstage.android.feature.community.PostDraftViewModel
import kotlinx.coroutines.CancellationException
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class DestinationDraftTest(private val kind: String) {
    companion object { @JvmStatic @Parameterized.Parameters(name = "{0}") fun cases() = listOf(arrayOf("DM"), arrayOf("comment")) }
    private class Draft(val read: () -> String, val update: (String) -> Unit, val submit: ((String) -> Boolean) -> Boolean)
    private fun create(store: ViewModelStore, key: String = "entry"): Draft {
        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T = modelClass.cast(
                if (kind == "DM") ConversationDraftViewModel() else PostDraftViewModel()
            )!!
        }
        return if (kind == "DM") {
            val vm = ViewModelProvider(store, factory)[key, ConversationDraftViewModel::class.java]
            Draft({ vm.text.value }, vm::update, vm::submit)
        } else {
            val vm = ViewModelProvider(store, factory)[key, PostDraftViewModel::class.java]
            Draft({ vm.text.value }, vm::update, vm::submit)
        }
    }
    @Test fun ownerReuseRetainsTextButOtherEntriesAndNewProcessesStartEmpty() {
        val store = ViewModelStore(); val fresh = ViewModelStore()
        try {
            val first = create(store); first.update("private draft")
            assertEquals("private draft", create(store).read())
            assertEquals("", create(store,"other").read())
            assertEquals("", create(fresh).read())
            store.clear()
            assertEquals("",first.read())
            assertEquals("",create(store).read())
        } finally { store.clear(); fresh.clear() }
    }
    @Test fun onlyAcceptedSubmissionClearsTheSubmittedDraft() {
        val store = ViewModelStore()
        try {
            val draft = create(store)
            draft.update("   ")
            assertFalse(draft.submit { fail("blank must not submit"); true })
            assertEquals("   ",draft.read())
            draft.update("keep")
            assertFalse(draft.submit { false }); assertEquals("keep",draft.read())
            assertFalse(draft.submit { throw IllegalStateException() }); assertEquals("keep",draft.read())
            assertThrows(CancellationException::class.java) { draft.submit { throw CancellationException() } }
            assertEquals("keep",draft.read())
            assertTrue(draft.submit { assertEquals("keep",it); true })
            assertEquals("",draft.read())
            draft.update("old")
            assertTrue(draft.submit { draft.update("new edit"); true })
            assertEquals("new edit",draft.read())
        } finally { store.clear() }
    }
}
