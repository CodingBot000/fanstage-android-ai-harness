package dev.fanstage.android

import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.lifecycle.ViewModelProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.fanstage.android.feature.live.PlayerPanel
import dev.fanstage.android.feature.live.chat.ChatConnectionFailure
import dev.fanstage.android.feature.live.chat.ChatConnectionStatus
import dev.fanstage.android.feature.live.chat.ChatEvent
import dev.fanstage.android.feature.live.chat.ChatPanel
import dev.fanstage.android.feature.live.chat.ChatViewModel
import dev.fanstage.android.feature.live.chat.FakeChatTransport
import dev.fanstage.android.feature.live.chat.RealChatTransport
import dev.fanstage.android.feature.live.chat.SendRejected
import android.util.Base64
import java.io.IOException
import java.net.InetAddress
import java.net.ServerSocket
import java.security.MessageDigest
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import okhttp3.WebSocket
import org.junit.Assert.fail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RealChatIntegrationTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()
    private val base = "http://10.0.2.2:18765"

    private fun verifyServerInitiatedGracefulClose() {
        val server = ServerSocket(0, 1, InetAddress.getByName("127.0.0.1"))
        val replied = AtomicBoolean(false)
        val done = CountDownLatch(1)
        val worker = thread(name = "synthetic-close-peer") {
            try {
                server.accept().use { socket ->
                    socket.soTimeout = 7_000
                    val input = socket.getInputStream()
                    val request = StringBuilder()
                    while (!request.endsWith("\r\n\r\n") && request.length < 16_384) {
                        val c = input.read(); if (c < 0) break; request.append(c.toChar())
                    }
                    val key = request.lines().first { it.startsWith("Sec-WebSocket-Key:", ignoreCase = true) }.substringAfter(':').trim()
                    val digest = MessageDigest.getInstance("SHA-1").digest((key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11").toByteArray())
                    val accept = Base64.encodeToString(digest, Base64.NO_WRAP)
                    val output = socket.getOutputStream()
                    output.write(("HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: $accept\r\n\r\n").toByteArray())
                    val subscribed = "{\"type\":\"subscribed\",\"headSequence\":0}".toByteArray()
                    output.write(byteArrayOf(0x81.toByte(), subscribed.size.toByte())); output.write(subscribed)
                    output.write(byteArrayOf(0x88.toByte(), 2, 0x03, 0xe8.toByte())); output.flush()
                    val first = input.read()
                    replied.set(first >= 0 && first and 0x0f == 8)
                }
            } catch (_: Exception) { /* Reported by the assertion, without exposing request headers. */ }
            finally { done.countDown() }
        }
        val transport = RealChatTransport("http://127.0.0.1:${server.localPort}", "synthetic-test-only", allowInsecure = true)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val job = scope.launch { transport.events("close-test").collect() }
        try {
            assertTrue("server close reply timed out", done.await(12, TimeUnit.SECONDS))
            assertTrue("client did not acknowledge server close", replied.get())
        } finally {
            job.cancel(); transport.close(); scope.cancel(); server.close(); worker.join(1_000)
        }
    }

    @Test fun androidRealWebSocketHistoryReconnectAndPlayerIsolation() {
        verifyServerInitiatedGracefulClose()
        val arguments = InstrumentationRegistry.getArguments()
        val alice = requireNotNull(arguments.getString("fanTokenA")) { "Missing temporary test token A" }
        val bob = requireNotNull(arguments.getString("fanTokenB")) { "Missing temporary test token B" }
        val room = "android-" + UUID.randomUUID().toString().take(18)
        val invalid = RealChatTransport(base, "invalid-temporary-token", allowInsecure = true)
        try {
            runBlocking {
                try { withTimeout(10_000) { invalid.events(room).collect() }; fail("invalid credential must fail") }
                catch (_: ChatConnectionFailure) { }
            }
        } finally { invalid.close() }
        val a = RealChatTransport(base, alice, allowInsecure = true, initialReconnectDelayMillis = 10_000)
        val b = RealChatTransport(base, bob, allowInsecure = true)
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        val incoming = Channel<ChatEvent>(Channel.UNLIMITED)
        val aJob = scope.launch { a.events(room).collect { incoming.send(it) } }
        val bJob = scope.launch { b.events(room).collect() }
        try {
            runBlocking {
                suspend fun sendWhenReady(transport: RealChatTransport, id: String, text: String) = withTimeout(30_000) {
                    while (true) {
                        try { transport.send(room, id, text); break }
                        catch (_: IOException) { delay(100) }
                    }
                }
                val first = UUID.randomUUID().toString()
                sendWhenReady(b, first, "first synthetic message")
                withTimeout(20_000) {
                    while (true) {
                        val event = incoming.receive()
                        if (event is ChatEvent.Message && event.message.text == "first synthetic message") {
                            assertFalse(event.message.isOwn)
                            assertNull(event.message.clientMessageId)
                            assertTrue(event.message.sentAt?.endsWith("Z") == true)
                            break
                        }
                    }
                }
                // Same identity and idempotency key must not create a second row; changed content must reject.
                b.send(room, first, "first synthetic message")
                assertEquals(1, b.fetchAfter(room, 0, Long.MAX_VALUE, 1000).size)
                try {
                    b.send(room, first, "different content")
                    fail("conflicting client id must be rejected")
                } catch (_: SendRejected) { }
                assertEquals(1, b.fetchAfter(room, 0, Long.MAX_VALUE, 1000).size)
                // Cut Alice's socket while preserving the collecting coroutine; Bob commits during the reconnect delay.
                val field = RealChatTransport::class.java.getDeclaredField("active").apply { isAccessible = true }
                val old = field.get(a) as WebSocket
                old.cancel()
                withTimeout(10_000) { while (field.get(a) != null) delay(50) }
                assertEquals(ChatConnectionStatus.RECONNECTING, a.connectionState.value)
                val second = UUID.randomUUID().toString()
                sendWhenReady(b, second, "missed while offline")
                assertNull("Alice must remain offline until Bob commits", field.get(a))
                withTimeout(35_000) {
                    while (true) {
                        val event = incoming.receive()
                        if (event is ChatEvent.Message && event.message.text == "missed while offline") break
                    }
                }
                assertEquals(ChatConnectionStatus.CONNECTED, a.connectionState.value)
                val history = a.fetchAfter(room, 0, Long.MAX_VALUE, 1000)
                assertTrue(history.any { it.text == "first synthetic message" })
                assertTrue(history.any { it.text == "missed while offline" })
                // A changes room while Bob continues sending in the old room.
                aJob.cancelAndJoin()
                while (incoming.tryReceive().isSuccess) { }
                val nextRoom = "android-" + UUID.randomUUID().toString().take(18)
                val nextJob = scope.launch { a.events(nextRoom).collect { incoming.send(it) } }
                val nextBob = RealChatTransport(base, bob, allowInsecure = true)
                val nextBobJob = scope.launch { nextBob.events(nextRoom).collect() }
                try {
                    sendWhenReady(b, UUID.randomUUID().toString(), "late old room")
                    withTimeout(30_000) {
                        while (true) {
                            try { nextBob.send(nextRoom, UUID.randomUUID().toString(), "new room"); break }
                            catch (_: IOException) { delay(100) }
                        }
                    }
                    withTimeout(20_000) {
                        while (true) {
                            val event = incoming.receive()
                            assertTrue("old room event reached new subscription", event.roomId == nextRoom)
                            if (event is ChatEvent.Message && event.message.text == "new room") break
                        }
                    }
                } finally { nextJob.cancelAndJoin(); nextBobJob.cancelAndJoin(); nextBob.close() }
            }
            // Real transport is injected into the same ChatPanel/ViewModel contract; playback remains independent.
            val chatHolder = arrayOfNulls<ChatViewModel>(1)
            composeRule.activityRule.scenario.onActivity { activity ->
                val fake = FakeChatTransport(room, simulateIncoming = false)
                val chat = ViewModelProvider(activity, ChatViewModel.Factory(room, fake))["real-chat", ChatViewModel::class.java]
                chatHolder[0] = chat
                activity.setContent {
                    MaterialTheme {
                        Column(Modifier.verticalScroll(rememberScrollState())) {
                            PlayerPanel("real-chat")
                            ChatPanel(chat, developmentControls = true)
                        }
                    }
                }
            }
            composeRule.waitUntil(30_000L) { composeRule.onAllNodesWithTag("player_state_playing").fetchSemanticsNodes().isNotEmpty() }
            // An actual server authentication error must not stop the independent player.
            composeRule.onNodeWithTag("chat_token").performScrollTo().performTextInput("invalid-temporary-token")
            composeRule.onNodeWithTag("chat_use_real").performScrollTo().performClick()
            composeRule.waitUntil(15_000L) { composeRule.onAllNodesWithTag("chat_connection_error").fetchSemanticsNodes().isNotEmpty() }
            composeRule.onNodeWithTag("player_state_playing").assertExists()
            // Debug-only UI selects the local endpoint and transient synthetic credential.
            composeRule.onNodeWithTag("chat_token").performScrollTo().performTextInput(alice)
            composeRule.onNodeWithTag("chat_use_real").performScrollTo().performClick()
            composeRule.waitUntil(10_000L) { composeRule.onAllNodesWithText("Local server").fetchSemanticsNodes().isNotEmpty() }
            // Allow subscription before sending; existing snapshot messages prove the handshake completed.
            composeRule.waitUntil(30_000L) { composeRule.onAllNodesWithText("missed while offline").fetchSemanticsNodes().isNotEmpty() }
            composeRule.onNodeWithTag("chat_input").performScrollTo().performTextInput("android real send")
            composeRule.onNodeWithTag("chat_send").performScrollTo().performClick()
            composeRule.waitUntil(30_000L) { composeRule.onAllNodesWithText("Sent").fetchSemanticsNodes().isNotEmpty() }
            composeRule.onNodeWithText("android real send").assertExists()
            composeRule.onNodeWithTag("player_state_playing").assertExists()
            val transportField = ChatViewModel::class.java.getDeclaredField("transport").apply { isAccessible = true }
            val uiTransport = transportField.get(chatHolder[0]) as RealChatTransport
            val activeField = RealChatTransport::class.java.getDeclaredField("active").apply { isAccessible = true }
            val uiSocket = activeField.get(uiTransport) as WebSocket
            uiSocket.cancel()
            composeRule.waitUntil(30_000L) { activeField.get(uiTransport)?.let { it !== uiSocket } == true }
            composeRule.onNodeWithTag("player_state_playing").assertExists()
            composeRule.activityRule.scenario.onActivity { it.viewModelStore.clear() }
            assertNull("screen exit must close its socket", activeField.get(uiTransport))
            val connectingField = RealChatTransport::class.java.getDeclaredField("connecting").apply { isAccessible = true }
            assertNull("screen exit must cancel pending handshake", connectingField.get(uiTransport))
            composeRule.onNodeWithTag("player_state_playing").assertExists()
        } finally {
            runBlocking { aJob.cancelAndJoin(); bJob.cancelAndJoin() }
            a.close(); b.close(); scope.cancel(); incoming.close()
        }
    }
}
