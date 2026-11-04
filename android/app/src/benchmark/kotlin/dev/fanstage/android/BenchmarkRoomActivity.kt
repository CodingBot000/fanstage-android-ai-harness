package dev.fanstage.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.fanstage.android.feature.live.PlayerPanel
import dev.fanstage.android.feature.live.chat.ChatEvent
import dev.fanstage.android.feature.live.chat.ChatPanel
import dev.fanstage.android.feature.live.chat.ChatTransport
import dev.fanstage.android.feature.live.chat.ChatViewModel
import dev.fanstage.android.feature.live.chat.ServerMessage
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/** Benchmark-only synthetic screen; the production launcher and release APK are unchanged. */
class BenchmarkRoomActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val factory = remember { ChatViewModel.Factory("fixture", FixtureTransport()) }
            val chat: ChatViewModel = viewModel(factory = factory)
            val state by chat.uiState.collectAsStateWithLifecycle()
            MaterialTheme {
                Column(Modifier.fillMaxSize().semantics { testTagsAsResourceId = true }) {
                    Box(Modifier.weight(1f)) { PlayerPanel("fixture") }
                    if (state.messages.size == 500) Text("M6 fixture ready")
                    ChatPanel(chat)
                }
            }
        }
    }
}

/** Fixed, non-network transcript; matches the normal transport's message contract. */
private class FixtureTransport : ChatTransport {
    override fun events(roomId: String): Flow<ChatEvent> = flow {
        for (sequence in 1L..500L) {
            emit(ChatEvent.Message(ServerMessage(roomId, "fixture-$sequence", null, sequence, "Synthetic message $sequence", false)))
        }
        awaitCancellation()
    }
    override suspend fun send(roomId: String, clientMessageId: String, text: String) = Unit
    override suspend fun fetchAfter(roomId: String, afterSequence: Long, upperSequence: Long, limit: Int): List<ServerMessage> = emptyList()
}
