package dev.fanstage.android

import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import dev.fanstage.android.feature.live.chat.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test

class ChatRecoveryUiTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun conflictHasNoGapButtonAndRealGapRecoveryKeepsWarning() {
        val events = MutableSharedFlow<ChatEvent>()
        val one = ServerMessage("ui", "one", null, 1, "first", false)
        val two = ServerMessage("ui", "two", null, 2, "second", false)
        var history = emptyList<ServerMessage>()
        val transport = object : ChatTransport {
            override fun events(roomId: String) = events
            override suspend fun send(roomId: String, clientMessageId: String, text: String) = Unit
            override suspend fun fetchAfter(roomId: String, afterSequence: Long, upperSequence: Long, limit: Int) = history.filter { it.serverSequence > afterSequence && it.serverSequence <= upperSequence }
        }
        val store = ViewModelStore()
        lateinit var vm: ChatViewModel
        rule.runOnUiThread {
            vm = ViewModelProvider(store, ChatViewModel.Factory("ui", transport))[ChatViewModel::class.java]
            rule.activity.setContent { MaterialTheme { ChatPanel(vm) } }
        }
        try {
            runBlocking { events.emit(ChatEvent.Message(one)); events.emit(ChatEvent.Message(one.copy(messageId = "conflict"))) }
            rule.onNodeWithTag("chat_protocol_warning").assertExists()
            rule.onNodeWithTag("chat_retry_gap").assertDoesNotExist()
            rule.onNodeWithTag("chat_input").performTextInput("retained draft")
            rule.runOnUiThread { vm.openRoom("ui") }
            runBlocking { events.emit(ChatEvent.Message(two)); events.emit(ChatEvent.Message(two.copy(messageId = "conflict"))) }
            rule.onNodeWithTag("chat_protocol_warning").assertExists()
            rule.onNodeWithTag("chat_retry_gap").assertExists()
            rule.runOnUiThread { history = listOf(one, two) }
            rule.onNodeWithTag("chat_retry_gap").performClick()
            rule.waitUntil(10_000) { rule.onAllNodesWithTag("chat_retry_gap").fetchSemanticsNodes().isEmpty() }
            rule.onNodeWithTag("chat_protocol_warning").assertExists()
            rule.onNodeWithTag("chat_input").assertIsEnabled()
            rule.runOnUiThread { vm.openRoom("other") }
            rule.onNodeWithTag("chat_protocol_warning").assertDoesNotExist()
        } finally { rule.runOnUiThread { store.clear() } }
    }
}
