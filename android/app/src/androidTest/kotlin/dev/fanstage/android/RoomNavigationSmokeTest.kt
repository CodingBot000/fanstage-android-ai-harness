package dev.fanstage.android

import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasTestTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.lifecycle.Lifecycle
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.net.Uri
import android.os.SystemClock
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertTrue
import java.io.File
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import java.lang.ref.WeakReference
import dev.fanstage.android.feature.live.Media3PlayerController
import dev.fanstage.android.feature.live.PlaybackState
import dev.fanstage.android.feature.live.PlayerPanel
import dev.fanstage.android.feature.live.chat.ChatPanel
import dev.fanstage.android.feature.live.chat.ChatViewModel
import dev.fanstage.android.feature.live.chat.FakeChatTransport
import org.junit.Assert.assertNotSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises the real MainActivity and its production FakeRoomRepository on a connected Android device. */
@RunWith(AndroidJUnit4::class)
class RoomNavigationSmokeTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Test fun rcFaultUiAndReleasedListener() {
        verifyErrorRetryUi()
        verifyChatFailureDoesNotInterruptPlayer()
        verifyReleasedListenerCanBeCollected()
    }

    @Test fun sanitizedRcaRetainsLocationsAndCauseTypes() {
        val secret = "synthetic-sensitive-marker"
        val cause = IllegalStateException("header $secret")
        cause.stackTrace = arrayOf(StackTraceElement("dev.fanstage.android.Caller", "nested", "$secret.kt", 23))
        val original = AssertionError("body $secret").apply {
            initCause(cause)
            stackTrace = arrayOf(StackTraceElement("dev.fanstage.android.Caller", "verify", "$secret.kt", 17))
        }
        val failure = RcaDiagnostics.RcaFailure("phase=synthetic", original)
        val safe = requireNotNull(failure.cause)
        val output = java.io.StringWriter().also { failure.printStackTrace(java.io.PrintWriter(it)) }.toString()
        assertTrue(output.contains("phase=synthetic"))
        assertTrue(output.contains("type=java.lang.AssertionError"))
        assertTrue(output.contains("type=java.lang.IllegalStateException"))
        assertTrue(output.contains("source:17"))
        assertTrue(output.contains("source:23"))
        org.junit.Assert.assertEquals("verify", safe.stackTrace.single().methodName)
        org.junit.Assert.assertEquals(17, safe.stackTrace.single().lineNumber)
        org.junit.Assert.assertEquals(23, safe.cause!!.stackTrace.single().lineNumber)
        org.junit.Assert.assertFalse(output.contains(secret))
    }

    @Test fun rcUncertainMessageAndChatCleanup() {
        val cancelled = AtomicBoolean(false)
        val closed = AtomicBoolean(false)
        val transport = object : dev.fanstage.android.feature.live.chat.ChatTransport {
            override fun events(roomId: String) = kotlinx.coroutines.flow.flow<dev.fanstage.android.feature.live.chat.ChatEvent> {
                try { kotlinx.coroutines.awaitCancellation() } finally { cancelled.set(true) }
            }
            override suspend fun send(roomId: String, clientMessageId: String, text: String) { throw java.io.IOException("unknown outcome") }
            override suspend fun fetchAfter(roomId: String, afterSequence: Long, upperSequence: Long, limit: Int) = emptyList<dev.fanstage.android.feature.live.chat.ServerMessage>()
            override fun close() { closed.set(true) }
        }
        val store = androidx.lifecycle.ViewModelStore()
        lateinit var chat: ChatViewModel
        composeRule.runOnUiThread {
            chat = ViewModelProvider(store, ChatViewModel.Factory("uncertain", transport))[ChatViewModel::class.java]
            assertTrue(chat.send("uncertain"))
        }
        try {
            composeRule.waitUntil(10_000) { chat.uiState.value.messages.singleOrNull()?.isUncertain == true }
            val message = chat.uiState.value.messages.single()
            assertTrue(message.delivery == dev.fanstage.android.feature.live.chat.Delivery.PENDING)
            assertTrue(chat.uiState.value.transportUncertain)
        } finally { composeRule.runOnUiThread { store.clear() } }
        composeRule.waitUntil(10_000) { cancelled.get() && closed.get() }
    }

    @Test fun roomListOpensDetailAndReturns() {
        composeRule.waitUntil(20_000L) {
            composeRule.onAllNodesWithTag("rooms_content").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("rooms_content").performScrollToNode(hasTestTag("live_carousel"))
        composeRule.onNodeWithTag("live_carousel").performScrollToNode(hasTestTag("room_benchmark"))
        composeRule.onNodeWithTag("room_benchmark").performClick()
        composeRule.onNodeWithTag("room_detail").assertIsDisplayed()
        composeRule.waitUntil(30_000L) { composeRule.onAllNodesWithTag("player_state_playing").fetchSemanticsNodes().isNotEmpty() }
        composeRule.waitUntil(30_000L) { composeRule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
        // Chat is independent of playback: send via Fake, then verify the player is still playing.
        composeRule.onNodeWithTag("chat_input").performTextInput("Hello from test")
        composeRule.onNodeWithTag("chat_send").performClick()
        composeRule.waitUntil(10_000L) { composeRule.onAllNodesWithText(composeRule.activity.getString(dev.fanstage.android.feature.live.R.string.chat_sent)).fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithTag("chat_messages").performScrollTo()
        composeRule.onNodeWithText("Hello from test").assertExists()
        composeRule.onNodeWithTag("player_state_playing").assertExists()
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitUntil(10_000L) { composeRule.onAllNodesWithTag("player_state_paused").fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithTag("player_play").performScrollTo().performClick()
        composeRule.waitUntil(10_000L) { composeRule.onAllNodesWithTag("player_state_playing").fetchSemanticsNodes().isNotEmpty() }
        // Draft stays in the retained ViewModel, not saved-state storage.
        composeRule.onNodeWithTag("chat_input").performTextInput("draft survives")
        // ActivityScenario.recreate() verifies Activity/nav state restoration, not rotation or process death.
        val originalActivity = composeRule.activity
        composeRule.activityRule.scenario.recreate()
        assertNotSame(originalActivity, composeRule.activity)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("room_detail").assertIsDisplayed()
        composeRule.onNodeWithTag("chat_input").assertTextContains("draft survives").performTextClearance()
        composeRule.waitUntil(10_000L) { composeRule.onAllNodesWithTag("player_state_paused").fetchSemanticsNodes().isNotEmpty() }
        // The controls must remain reachable after a real device orientation change.
        composeRule.activityRule.scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        composeRule.waitUntil(20_000L) { composeRule.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
        composeRule.waitUntil(10_000L) { composeRule.onAllNodesWithTag("player_play").fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithTag("player_play").performScrollTo().assertIsDisplayed()
        composeRule.activityRule.scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
        composeRule.waitUntil(20_000L) { composeRule.activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT }
        composeRule.onNodeWithTag("room_back").performClick()
        composeRule.onNodeWithTag("room_benchmark").assertIsDisplayed()
        repeat(5) {
            composeRule.onNodeWithTag("room_benchmark").performClick()
            composeRule.waitUntil(30_000L) { composeRule.onAllNodesWithTag("player_state_playing").fetchSemanticsNodes().isNotEmpty() }
            composeRule.onNodeWithTag("room_back").performClick()
            composeRule.onNodeWithTag("room_benchmark").assertIsDisplayed()
        }
        verifyLivePlaylistRefresh()
        verifyPlaybackErrorAndRetry()
        verifyM5SegmentFaultsAndRecovery()
        verifyM5BoundedTraffic()
        verifyReleasedListenerCanBeCollected()
        verifyChatFailureDoesNotInterruptPlayer()
        verifyErrorRetryUi()
        verifyM7LocaleAndLargeText()
    }

    private fun verifyM7LocaleAndLargeText() {
        val language = androidx.compose.runtime.mutableStateOf(java.util.Locale.US)
        val large = androidx.compose.runtime.mutableStateOf(false)
        val roomState = androidx.compose.runtime.mutableStateOf<dev.fanstage.android.feature.live.RoomUiState>(
            dev.fanstage.android.feature.live.RoomUiState.Content(listOf(dev.fanstage.android.core.model.Room("studio-session", "Studio session", "Paper Lantern", 128)))
        )
        val store = androidx.lifecycle.ViewModelStore()
        lateinit var chat: ChatViewModel
        composeRule.activityRule.scenario.onActivity { activity ->
            chat = ViewModelProvider(store, ChatViewModel.Factory("locale", FakeChatTransport("locale", simulateIncoming = false)))[ChatViewModel::class.java]
            activity.setContent {
                val config = android.content.res.Configuration(activity.resources.configuration).apply {
                    setLocale(language.value); fontScale = if (large.value) 2f else 1f
                }
                val context = androidx.compose.runtime.remember(language.value, large.value) { activity.createConfigurationContext(config) }
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.ui.platform.LocalContext provides context,
                    androidx.compose.ui.platform.LocalConfiguration provides config,
                    androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(context.resources.displayMetrics.density, config.fontScale),
                ) {
                    MaterialTheme {
                        if (roomState.value is dev.fanstage.android.feature.live.RoomUiState.Content || roomState.value is dev.fanstage.android.feature.live.RoomUiState.Error) {
                            dev.fanstage.android.feature.live.RoomListScreen(roomState.value, onRetry = {}, onRoomClick = {})
                        } else {
                            Column(Modifier.verticalScroll(rememberScrollState())) { PlayerPanel("locale"); ChatPanel(chat) }
                        }
                    }
                }
            }
        }
        try {
            composeRule.onNodeWithText("Studio session").assertIsDisplayed()
            composeRule.runOnUiThread { language.value = java.util.Locale.KOREA; large.value = true }
            composeRule.waitUntil(10_000) { composeRule.onAllNodesWithText("스튜디오 라이브").fetchSemanticsNodes().isNotEmpty() }
            composeRule.onNodeWithText("스튜디오 라이브").assertIsDisplayed()
            composeRule.runOnUiThread { roomState.value = dev.fanstage.android.feature.live.RoomUiState.Error("internal") }
            composeRule.waitUntil(10_000) { composeRule.onAllNodesWithText("방 목록을 불러오지 못했습니다. 다시 시도해 주세요.").fetchSemanticsNodes().isNotEmpty() }
            composeRule.onNodeWithText("다시 시도").assertIsDisplayed()
            composeRule.runOnUiThread { roomState.value = dev.fanstage.android.feature.live.RoomUiState.Empty }
            composeRule.waitUntil(10_000) { composeRule.onAllNodesWithText("방에 오신 것을 환영합니다!").fetchSemanticsNodes().isNotEmpty() }
            composeRule.onNodeWithTag("chat_input").performScrollTo().performTextInput("테스트")
            composeRule.onNodeWithTag("chat_send").performScrollTo().assertIsDisplayed().performClick()
            composeRule.waitUntil(10_000) { composeRule.onAllNodesWithText("전송됨").fetchSemanticsNodes().isNotEmpty() }
            composeRule.onNodeWithContentDescription("동영상 플레이어").performScrollTo().assertExists()
            composeRule.runOnUiThread { language.value = java.util.Locale.US; large.value = false }
            composeRule.waitUntil(10_000) { composeRule.onAllNodesWithText("Welcome to the room!").fetchSemanticsNodes().isNotEmpty() }
            composeRule.onNodeWithText("Sent").assertExists()
        } finally { composeRule.runOnUiThread { store.clear() } }
    }

    private fun verifyChatFailureDoesNotInterruptPlayer() {
        composeRule.activityRule.scenario.onActivity { activity ->
            val factory = ChatViewModel.Factory("chat-error", FakeChatTransport("chat-error", rejectSends = true, simulateIncoming = false))
            val chat = ViewModelProvider(activity, factory)["chat-error", ChatViewModel::class.java]
            activity.setContent {
                MaterialTheme {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        PlayerPanel("chat-error")
                        ChatPanel(chat)
                    }
                }
            }
        }
        composeRule.waitUntil(30_000L) { composeRule.onAllNodesWithTag("player_state_playing").fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithTag("chat_input").performScrollTo().performTextInput("rejected test")
        composeRule.onNodeWithTag("chat_send").performScrollTo().performClick()
        composeRule.waitUntil(10_000L) { composeRule.onAllNodesWithText(composeRule.activity.getString(dev.fanstage.android.feature.live.R.string.chat_failed)).fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithText(composeRule.activity.getString(dev.fanstage.android.feature.live.R.string.chat_retry_message)).performScrollTo().performClick()
        composeRule.waitUntil(10_000L) { composeRule.onAllNodesWithText(composeRule.activity.getString(dev.fanstage.android.feature.live.R.string.chat_failed)).fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithTag("player_state_playing").assertExists()
    }

    private fun verifyErrorRetryUi() {
        composeRule.activityRule.scenario.onActivity {
            it.setContent {
                MaterialTheme { PlayerPanel("missing-fixture", "asset:///hls/missing.m3u8") }
            }
        }
        composeRule.waitUntil(20_000L) { composeRule.onAllNodesWithTag("player_retry").fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithTag("player_retry").assertIsDisplayed().performClick()
        composeRule.waitUntil(20_000L) { composeRule.onAllNodesWithTag("player_retry").fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithTag("player_retry").assertIsDisplayed()
    }

    /** Checks reachability after release, not a whole-process heap leak audit. */
    private fun verifyReleasedListenerCanBeCollected() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val references = AtomicReference<Pair<WeakReference<Any>, WeakReference<Any>>>()
        instrumentation.runOnMainSync {
            val controller = Media3PlayerController(instrumentation.targetContext, "asset:///hls/missing.m3u8")
            val field = Media3PlayerController::class.java.getDeclaredField("listener").apply { isAccessible = true }
            references.set(WeakReference<Any>(controller) to WeakReference(field.get(controller)))
            controller.release()
        }
        // Drain queued player callbacks and do not keep WeakReference.get() temporaries
        // live on the test thread's stack while requesting collection.
        instrumentation.waitForIdleSync()
        fun cleared(ref: WeakReference<Any>) = ref.get() == null
        val end = SystemClock.elapsedRealtime() + 15_000
        while (SystemClock.elapsedRealtime() < end) {
            instrumentation.runOnMainSync { }
            System.gc()
            System.runFinalization()
            SystemClock.sleep(200)
            if (cleared(references.get().first) && cleared(references.get().second)) break
        }
        assertTrue("released controller retained", cleared(references.get().first))
        assertTrue("released player listener retained", cleared(references.get().second))
    }

    private fun verifyPlaybackErrorAndRetry() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        var controller: Media3PlayerController? = null
        fun waitForError(): Boolean {
            val end = SystemClock.elapsedRealtime() + 20_000
            while (SystemClock.elapsedRealtime() < end && controller?.state?.value != PlaybackState.Error) {
                SystemClock.sleep(100)
            }
            return controller?.state?.value == PlaybackState.Error
        }
        try {
            instrumentation.runOnMainSync {
                controller = Media3PlayerController(instrumentation.targetContext, "asset:///hls/missing.m3u8")
                controller!!.play()
            }
            assertTrue("missing HLS did not report error", waitForError())
            instrumentation.runOnMainSync { controller!!.retry() }
            assertTrue("manual retry did not return to error for missing fixture", waitForError())
        } finally {
            instrumentation.runOnMainSync { controller?.release(); controller?.release() }
        }
    }

    /** Loopback-only HLS fixture with one-shot network faults; never changes the server. */
    private fun verifyM5SegmentFaultsAndRecovery() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val server = java.net.ServerSocket(0, 10, java.net.InetAddress.getByName("127.0.0.1"))
        val mode = AtomicReference("404")
        val requested = java.util.concurrent.atomic.AtomicInteger()
        val workers = java.util.concurrent.CopyOnWriteArrayList<Thread>()
        val listener = kotlin.concurrent.thread(name = "m5-media-01-fixture", isDaemon = true) {
            try {
                while (!server.isClosed) {
                    val socket = server.accept()
                    workers.add(kotlin.concurrent.thread(isDaemon = true) {
                        try {
                            socket.use {
                                socket.soTimeout = 5_000
                                val input = socket.getInputStream()
                                val request = StringBuilder()
                                while (!request.endsWith("\r\n\r\n") && request.length < 16_384) {
                                    val byte = input.read(); if (byte < 0) break; request.append(byte.toChar())
                                }
                                val path = request.lineSequence().firstOrNull()?.split(" ")?.getOrNull(1).orEmpty().substringBefore('?')
                                val isPlaylist = path.endsWith(".m3u8")
                                val isSegment = path.endsWith(".ts")
                                if (isSegment) requested.incrementAndGet()
                                val fault = if (isSegment) mode.get() else "good"
                                if (fault == "disconnect" && mode.compareAndSet("disconnect", "good")) return@thread
                                if (fault == "timeout") {
                                    SystemClock.sleep(12_000); return@thread
                                }
                                val ok = isPlaylist || (isSegment && fault != "404")
                                val body = if (ok) context.assets.open(if (isPlaylist) "hls/vod.m3u8" else "hls/" + path.substringAfterLast('/')).use { it.readBytes() } else ByteArray(0)
                                val output = socket.getOutputStream()
                                output.write(("HTTP/1.1 " + (if (ok) "200 OK" else "404 Not Found") + "\r\nContent-Length: " + body.size + "\r\nConnection: close\r\n\r\n").toByteArray())
                                output.write(body); output.flush()
                            }
                        } catch (_: java.io.IOException) { /* expected when client cancels a faulted request */ }
                    })
                }
            } catch (_: java.io.IOException) { /* listener closed after scenario */ }
        }
        var controller: Media3PlayerController? = null
        val chatStore = androidx.lifecycle.ViewModelStore()
        lateinit var independentChat: ChatViewModel
        instrumentation.runOnMainSync {
            independentChat = ViewModelProvider(chatStore, ChatViewModel.Factory("media-fault", FakeChatTransport("media-fault", simulateIncoming = false)))[ChatViewModel::class.java]
        }
        fun start() = instrumentation.runOnMainSync {
            controller = Media3PlayerController(context, "http://127.0.0.1:${server.localPort}/vod.m3u8")
            controller!!.play()
        }
        fun release() = instrumentation.runOnMainSync { controller?.release(); controller = null }
        fun waitState(state: PlaybackState, timeout: Long): Boolean {
            val until = SystemClock.elapsedRealtime() + timeout
            while (SystemClock.elapsedRealtime() < until && controller?.state?.value != state) SystemClock.sleep(100)
            return controller?.state?.value == state
        }
        try {
            start()
            assertTrue("segment 404 must produce a player error", waitState(PlaybackState.Error, 30_000))
            assertTrue("segment request was not made", requested.get() > 0)
            instrumentation.runOnMainSync { assertTrue(independentChat.send("chat survives player error")) }
            val chatDeadline = SystemClock.elapsedRealtime() + 10_000
            while (SystemClock.elapsedRealtime() < chatDeadline && independentChat.uiState.value.messages.none { it.text == "chat survives player error" && it.delivery == dev.fanstage.android.feature.live.chat.Delivery.SENT }) SystemClock.sleep(50)
            assertTrue("player error must not prevent chat delivery", independentChat.uiState.value.messages.any { it.text == "chat survives player error" && it.delivery == dev.fanstage.android.feature.live.chat.Delivery.SENT })
            mode.set("good")
            instrumentation.runOnMainSync { controller!!.retry() }
            assertTrue("manual retry should resume after segment recovery", waitState(PlaybackState.Playing, 30_000))
            release()
            for (fault in listOf("disconnect", "timeout")) {
                val before = requested.get(); mode.set(fault)
                if (fault == "timeout") workers.add(kotlin.concurrent.thread(isDaemon = true) {
                    SystemClock.sleep(10_000); mode.compareAndSet("timeout", "good")
                })
                val startedAt = SystemClock.elapsedRealtime(); start()
                assertTrue("$fault must recover to playing", waitState(PlaybackState.Playing, 45_000))
                if (fault == "timeout") assertTrue("timeout must wait for read deadline", SystemClock.elapsedRealtime() - startedAt >= 7_500)
                assertTrue("$fault must cause another segment request", requested.get() >= before + 2)
                release()
            }
        } finally {
            release(); instrumentation.runOnMainSync { chatStore.clear() }
            server.close(); listener.join(2_000)
            workers.forEach { it.join(15_000) }
        }
    }

    /** Bounded synthetic traffic with the actual Compose screen and main dispatcher. */
    private fun verifyM5BoundedTraffic() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val stream = kotlinx.coroutines.flow.MutableSharedFlow<dev.fanstage.android.feature.live.chat.ChatEvent>()
        val transport = object : dev.fanstage.android.feature.live.chat.ChatTransport {
            override fun events(roomId: String) = stream
            override suspend fun send(roomId: String, clientMessageId: String, text: String) = Unit
            override suspend fun fetchAfter(roomId: String, afterSequence: Long, upperSequence: Long, limit: Int) = emptyList<dev.fanstage.android.feature.live.chat.ServerMessage>()
        }
        val store = androidx.lifecycle.ViewModelStore()
        lateinit var chat: ChatViewModel
        instrumentation.runOnMainSync {
            chat = ViewModelProvider(store, ChatViewModel.Factory("traffic", transport))[ChatViewModel::class.java]
            composeRule.activity.setContent { MaterialTheme { ChatPanel(chat) } }
        }
        try {
            kotlinx.coroutines.runBlocking {
                kotlinx.coroutines.withTimeout(10_000) {
                    while (stream.subscriptionCount.value == 0) kotlinx.coroutines.delay(10)
                }
                var seq = 0L
                suspend fun emitOne() {
                    seq++
                    stream.emit(dev.fanstage.android.feature.live.chat.ChatEvent.Message(
                        dev.fanstage.android.feature.live.chat.ServerMessage("traffic", "synthetic-$seq", null, seq, "m$seq", false)
                    ))
                }
                repeat(750) { emitOne() }
                for (rate in listOf(10, 50, 100)) {
                    val begin = SystemClock.elapsedRealtime()
                    var maxHeartbeat = 0L
                    repeat(rate * 3) { index ->
                        val wait = begin + index * 1_000L / rate - SystemClock.elapsedRealtime()
                        if (wait > 0) kotlinx.coroutines.delay(wait)
                        emitOne()
                        if (index % 10 == 0) {
                            val heartbeat = SystemClock.elapsedRealtime()
                            instrumentation.runOnMainSync { }
                            val latency = SystemClock.elapsedRealtime() - heartbeat
                            maxHeartbeat = maxOf(maxHeartbeat, latency)
                            assertTrue("main dispatcher heartbeat blocked for two seconds", latency < 2_000)
                        }
                    }
                    val elapsed = SystemClock.elapsedRealtime() - begin
                    android.util.Log.i("M5Traffic", "scenario=m5-net06-seed-73 target_per_s=$rate count=${rate * 3} elapsed_ms=$elapsed max_main_heartbeat_ms=$maxHeartbeat")
                    assertTrue("bounded traffic fell more than five seconds behind schedule", elapsed < 8_000)
                }
                instrumentation.runOnMainSync { }
                assertTrue("display limit exceeded", chat.uiState.value.messages.size <= 500)
                val confirmed = ChatViewModel::class.java.getDeclaredField("confirmed").apply { isAccessible = true }.get(chat) as Map<*, *>
                assertTrue("retention limit exceeded", confirmed.size <= 1_000)
                assertTrue("last message was not consumed", chat.uiState.value.messages.last().sequence == seq)
            }
        } finally { instrumentation.runOnMainSync { store.clear() } }
    }

    private fun verifyLivePlaylistRefresh() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val directory = File(context.cacheDir, "m2-live-fixture").apply { mkdirs() }
        for (index in 0..2) {
            context.assets.open("hls/segment%02d.ts".format(index)).use { input ->
                File(directory, "segment%02d.ts".format(index)).outputStream().use(input::copyTo)
            }
        }
        val manifest = File(directory, "live.m3u8")
        fun writePlaylist(segments: Int) {
            val text = buildString {
                append("#EXTM3U\n#EXT-X-VERSION:3\n#EXT-X-TARGETDURATION:2\n#EXT-X-MEDIA-SEQUENCE:0\n")
                repeat(segments) { append("#EXTINF:2.000000,\nsegment%02d.ts\n".format(it)) }
            }
            val next = File(directory, "next.m3u8")
            next.writeText(text)
            check(next.renameTo(manifest))
        }
        writePlaylist(2)
        val liveDuration = AtomicLong(-1)
        val played = AtomicBoolean(false)
        val player = arrayOfNulls<ExoPlayer>(1)
        try {
            instrumentation.runOnMainSync {
                player[0] = ExoPlayer.Builder(context).build().apply {
                    addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) { if (isPlaying) played.set(true) }
                        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                            if (!timeline.isEmpty && isCurrentMediaItemLive) {
                                liveDuration.set(timeline.getWindow(0, Timeline.Window()).durationMs)
                            }
                        }
                    })
                    setMediaItem(MediaItem.fromUri(Uri.fromFile(manifest)))
                    prepare()
                    play()
                }
            }
            fun awaitDuration(minimum: Long): Boolean {
                val end = SystemClock.elapsedRealtime() + 20_000
                while (SystemClock.elapsedRealtime() < end && liveDuration.get() < minimum) SystemClock.sleep(100)
                return liveDuration.get() >= minimum
            }
            assertTrue("initial LIVE timeline did not load", awaitDuration(4_000))
            writePlaylist(3)
            assertTrue("LIVE playlist did not refresh after a new segment", awaitDuration(6_000))
            val untilPlaying = SystemClock.elapsedRealtime() + 10_000
            while (!played.get() && SystemClock.elapsedRealtime() < untilPlaying) SystemClock.sleep(100)
            assertTrue("LIVE stream never played", played.get())
        } finally {
            instrumentation.runOnMainSync { player[0]?.release() }
            directory.deleteRecursively()
        }
    }
}
