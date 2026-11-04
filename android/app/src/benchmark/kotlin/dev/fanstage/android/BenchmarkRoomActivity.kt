package dev.fanstage.android

import android.os.Bundle
import android.os.SystemClock
import android.os.Trace
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/** Benchmark-only synthetic screen; the production launcher and release APK are unchanged. */
class BenchmarkRoomActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val traffic = intent.getBooleanExtra("m6_traffic", false)
        setContent {
            val fixture = remember { FixtureTransport(traffic) }
            val factory = remember { ChatViewModel.Factory("fixture", fixture) }
            val chat: ChatViewModel = viewModel(factory = factory)
            val state by chat.uiState.collectAsStateWithLifecycle()
            MaterialTheme {
                Column(Modifier.fillMaxSize().semantics { testTagsAsResourceId = true }) {
                    Box(Modifier.weight(1f)) { PlayerPanel("fixture") }
                    if (state.messages.size == 500) Text("M6 fixture ready")
                    if (traffic && state.messages.lastOrNull()?.sequence == 1000L) Text("M6 traffic complete")
                    if (traffic) Button(onClick = { fixture.start.value = true }) { Text("M6 start traffic") }
                    ChatPanel(chat)
                }
            }
        }
    }
}

/** Fixed, non-network transcript; dynamic case is isolated to this benchmark-only APK. */
private class FixtureTransport(private val traffic: Boolean) : ChatTransport {
    val start = MutableStateFlow(false)
    override fun events(roomId: String): Flow<ChatEvent> = flow {
        for (sequence in 1L..500L) {
            emit(ChatEvent.Message(ServerMessage(roomId, "fixture-$sequence", null, sequence, "Synthetic message $sequence", false)))
        }
        if (traffic) {
            start.first { it }
            val started = SystemClock.elapsedRealtime()
            for (sequence in 501L..1000L) {
                val target = started + (sequence - 501L) * 10L
                val remaining = target - SystemClock.elapsedRealtime()
                if (remaining > 0) delay(remaining)
                Trace.beginSection("M6ChatAccept")
                try {
                    emit(ChatEvent.Message(ServerMessage(roomId, "fixture-$sequence", null, sequence, "Synthetic message $sequence", false)))
                } finally { Trace.endSection() }
            }
        }
        awaitCancellation()
    }
    override suspend fun send(roomId: String, clientMessageId: String, text: String) = Unit
    override suspend fun fetchAfter(roomId: String, afterSequence: Long, upperSequence: Long, limit: Int): List<ServerMessage> = emptyList()
}
