package dev.fanstage.android

import android.net.Uri
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.compose.setContent
import androidx.media3.common.Player
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.text.CueGroup
import java.util.concurrent.atomic.AtomicBoolean
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Density
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.onNodeWithContentDescription
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import java.util.concurrent.ConcurrentLinkedQueue
import androidx.media3.ui.PlayerView
import androidx.media3.common.text.Cue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextContains
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import dev.fanstage.android.feature.live.RoomDetailScreen
import dev.fanstage.android.feature.live.ReplayScreen
import dev.fanstage.android.feature.live.chat.ChatViewModel
import dev.fanstage.android.feature.live.chat.FakeChatTransport
import androidx.test.platform.app.InstrumentationRegistry
import dev.fanstage.android.feature.live.Media3PlayerController
import dev.fanstage.android.feature.live.PlayerPanel
import dev.fanstage.android.feature.live.PlayerSurfaceGeneration
import dev.fanstage.android.feature.live.PlaybackState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.After
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class PlayerSettingsTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val fixtureDirs = mutableListOf<File>()

    private fun fixtureDir(): File = java.nio.file.Files.createTempDirectory(
        InstrumentationRegistry.getInstrumentation().targetContext.cacheDir.toPath(), "player001-test-",
    ).toFile().also { fixtureDirs.add(it) }

    @After fun removeTestFixtures() {
        if (fixtureDirs.isNotEmpty()) {
            rule.activity.setContent { }
            rule.waitForIdle()
            fixtureDirs.forEach { check(it.deleteRecursively()) }
        }
    }

    @Test fun actualTracksAndSpeed() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val assets = instrumentation.context.assets
        val dir = fixtureDir()
        assets.list("player001")!!.forEach { name -> assets.open("player001/$name").use { input -> File(dir, name).outputStream().use(input::copyTo) } }
        lateinit var controller: Media3PlayerController
        rule.runOnUiThread { controller = Media3PlayerController(instrumentation.targetContext, Uri.fromFile(File(dir, "master.m3u8")).toString()); controller.play() }
        val firstFrame = AtomicBoolean(false)
        val captionSeen = AtomicBoolean(false)
        lateinit var playerView: PlayerView
        val captionCleared = AtomicBoolean(false)
        lateinit var player: Player
        val listener = object : Player.Listener {
            override fun onRenderedFirstFrame() { firstFrame.set(true) }
            override fun onCues(cueGroup: CueGroup) {
                if (cueGroup.cues.any { it.text?.contains("Synthetic test caption") == true }) captionSeen.set(true)
                if (captionSeen.get() && cueGroup.cues.isEmpty()) captionCleared.set(true)
            }
        }
        rule.runOnUiThread {
            val field = Media3PlayerController::class.java.getDeclaredField("player").apply { isAccessible = true }
            player = field.get(controller) as Player
            player.addListener(listener)

        }
        rule.activity.setContent {
            AndroidView(modifier = Modifier.fillMaxWidth().height(200.dp), factory = { context ->
                (LayoutInflater.from(context).inflate(dev.fanstage.android.feature.live.R.layout.fanstage_player_view, null, false) as PlayerView).also {
                    playerView = it
                    it.player = player
                }
            })
        }
        try {
            rule.waitUntil(30000) { firstFrame.get() }
            rule.waitUntil(30000) { controller.uiState.value.videoTracks.size >= 2 && controller.uiState.value.textTracks.isNotEmpty() }
            val first = controller.uiState.value.videoTracks.first().id
            rule.runOnUiThread { controller.selectVideo(first); controller.setSpeed(1.5f) }
            rule.waitUntil(10000) { controller.uiState.value.selectedVideoId == first && controller.uiState.value.videoTracks.any { it.id == first && it.selected } && controller.uiState.value.speed == 1.5f }
            val subtitle = controller.uiState.value.textTracks.first { it.label == "English" }.id
            rule.runOnUiThread { controller.selectSubtitle(subtitle); player.seekTo(0) }
            rule.waitUntil(10000) { controller.uiState.value.textTracks.any { it.id == subtitle && it.selected } }
            rule.waitUntil(10000) { captionSeen.get() }
            rule.runOnUiThread {
                val subtitleView = requireNotNull(playerView.subtitleView)
                val cuesField = subtitleView.javaClass.getDeclaredField("cues").apply { isAccessible = true }
                @Suppress("UNCHECKED_CAST")
                val displayedCues = cuesField.get(subtitleView) as List<Cue>
                assertTrue(subtitleView.isShown && subtitleView.width > 0 && displayedCues.any { it.text?.contains("Synthetic test caption") == true })
            }
            rule.runOnUiThread { player.pause() }
            instrumentation.uiAutomation.waitForIdle(500, 5000)
            rule.waitForIdle()
            rule.runOnUiThread { controller.selectVideo(null); controller.selectSubtitle(null) }
            rule.waitUntil(10000) { controller.uiState.value.selectedVideoId == null && controller.uiState.value.subtitlesOff && captionCleared.get() }
            assertNull(controller.uiState.value.selectedVideoId)
            assertEquals(1.5f, controller.uiState.value.speed)
        } finally { rule.runOnUiThread { playerView.player = null; player.removeListener(listener); controller.release() } }
        lateinit var newSession: Media3PlayerController
        rule.runOnUiThread { newSession = Media3PlayerController(instrumentation.targetContext, Uri.fromFile(File(dir, "master.m3u8")).toString()) }
        try {
            rule.waitUntil(30000) { newSession.uiState.value.videoTracks.isNotEmpty() }
            assertEquals(1f, newSession.uiState.value.speed)
        } finally { rule.runOnUiThread { newSession.release() } }
    }

    @Test fun fullscreenBackAndSpeed() {
        rule.activity.setContent { MaterialTheme { PlayerPanel("player-settings-test") } }
        fun renderedGeneration(): Long? = runCatching {
            rule.onNodeWithTag("player_first_frame").fetchSemanticsNode().config[PlayerSurfaceGeneration]
        }.getOrNull()
        rule.waitUntil(30000) { renderedGeneration() != null }
        fun findPlayerView(view: View): PlayerView? {
            if (view is PlayerView) return view
            if (view is ViewGroup) for (i in 0 until view.childCount) findPlayerView(view.getChildAt(i))?.let { return it }
            return null
        }
        lateinit var actualPlayer: ExoPlayer
        lateinit var outputField: java.lang.reflect.Field
        val outputEvents = ConcurrentLinkedQueue<Any>()
        val analytics = object : AnalyticsListener {
            override fun onRenderedFirstFrame(eventTime: AnalyticsListener.EventTime, output: Any, renderTimeMs: Long) {
                if (output === outputField.get(actualPlayer)) outputEvents.add(output)
            }
        }
        rule.runOnUiThread {
            actualPlayer = requireNotNull(findPlayerView(rule.activity.window.decorView)?.player) as ExoPlayer
            outputField = actualPlayer.javaClass.getDeclaredField("videoOutput").apply { isAccessible = true }
            actualPlayer.addAnalyticsListener(analytics)
        }
        fun currentOutput(): Any? {
            var value: Any? = null
            rule.runOnUiThread { value = outputField.get(actualPlayer) }
            return value
        }
        fun waitForNewOutput(previous: Any?) {
            rule.waitUntil(10000) { outputEvents.any { it !== previous } }
            assertTrue(outputEvents.any { it === currentOutput() && it !== previous })
        }
        rule.onNodeWithTag("player_pause").performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithTag("player_state_paused").fetchSemanticsNodes().isNotEmpty() && rule.onAllNodesWithTag("player_seek").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("player_seek").performSemanticsAction(SemanticsActions.SetProgress) { it(0.5f) }
        val before = rule.onNodeWithTag("player_seek").fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo].current
        rule.onNodeWithTag("player_speed").performClick()
        rule.onNodeWithTag("player_speed_1.5").performClick()
        var previous = requireNotNull(renderedGeneration())
        var oldOutput = currentOutput()
        outputEvents.clear()
        rule.onNodeWithTag("player_fullscreen").performClick()
        rule.waitUntil(10000) { (renderedGeneration() ?: -1) > previous }
        waitForNewOutput(oldOutput)
        assertTrue(rule.onAllNodesWithTag("player_state_paused").fetchSemanticsNodes().isNotEmpty())
        val after = rule.onNodeWithTag("player_seek").fetchSemanticsNode().config[SemanticsProperties.ProgressBarRangeInfo].current
        assertEquals(before, after, 0.02f)
        rule.onNodeWithTag("player_speed").performClick()
        rule.onNodeWithTag("player_speed_1.5").assertIsSelected()
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("input keyevent KEYCODE_BACK").close()
        rule.waitUntil(10000) { rule.onAllNodesWithTag("player_speed_1.5").fetchSemanticsNodes().isEmpty() }
        previous = requireNotNull(renderedGeneration())
        oldOutput = currentOutput()
        outputEvents.clear()
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("input keyevent KEYCODE_BACK").close()
        rule.waitUntil(10000) { (renderedGeneration() ?: -1) > previous }
        waitForNewOutput(oldOutput)
        repeat(2) {
            previous = requireNotNull(renderedGeneration())
            oldOutput = currentOutput()
            outputEvents.clear()
            rule.onNodeWithTag("player_fullscreen").performClick()
            rule.waitUntil(10000) { (renderedGeneration() ?: -1) > previous }
            waitForNewOutput(oldOutput)
            previous = requireNotNull(renderedGeneration())
            oldOutput = currentOutput()
            outputEvents.clear()
            rule.onNodeWithTag("player_fullscreen").performClick()
            rule.waitUntil(10000) { (renderedGeneration() ?: -1) > previous }
            waitForNewOutput(oldOutput)
        }
        rule.runOnUiThread { actualPlayer.removeAnalyticsListener(analytics) }
        assertTrue(rule.onAllNodesWithTag("player_panel").fetchSemanticsNodes().size == 1)
    }

    @Test fun liveAndReplayFullscreen() {
        val store = ViewModelStore()
        lateinit var chat: ChatViewModel
        rule.runOnUiThread { chat = ViewModelProvider(store, ChatViewModel.Factory("player-test", FakeChatTransport("player-test", simulateIncoming = false)))[ChatViewModel::class.java] }
        try {
            rule.activity.setContent { MaterialTheme { RoomDetailScreen("player-test", chat, onBack = {}) } }
            rule.waitUntil(30000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
            rule.onNodeWithTag("player_fullscreen").performClick()
            rule.waitUntil(10000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
            rule.onNodeWithTag("player_fullscreen").performClick()
            rule.waitUntil(10000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
            rule.activity.setContent { MaterialTheme { ReplayScreen(onBack = {}) } }
            rule.waitUntil(30000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
            rule.onNodeWithTag("player_fullscreen").performClick()
            rule.waitUntil(10000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
            rule.onNodeWithTag("player_fullscreen").performClick()
            rule.waitUntil(10000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
        } finally { rule.runOnUiThread { store.clear() } }
    }

    @Test fun homePausesAndRequiresManualResume() {
        rule.activity.setContent { MaterialTheme { PlayerPanel("home-policy-test") } }
        rule.waitUntil(30000) { rule.onAllNodesWithTag("player_state_playing").fetchSemanticsNodes().isNotEmpty() }
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("input keyevent KEYCODE_HOME").close()
        rule.waitUntil(10000) { !rule.activity.hasWindowFocus() }
        val target = InstrumentationRegistry.getInstrumentation().targetContext
        val intent = requireNotNull(target.packageManager.getLaunchIntentForPackage(target.packageName))
            .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
        target.startActivity(intent)
        rule.waitUntil(10000) { rule.activity.hasWindowFocus() }
        rule.waitUntil(10000) { rule.onAllNodesWithTag("player_state_paused").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("player_play").performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithTag("player_state_playing").fetchSemanticsNodes().isNotEmpty() }
    }

    @Test fun fullscreenRestoresAfterActualRotation() {
        rule.waitUntil(20000) { rule.onAllNodesWithTag("rooms_content").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("rooms_content").performScrollToNode(hasTestTag("live_carousel"))
        rule.onNodeWithTag("live_carousel").performScrollToNode(hasTestTag("room_benchmark"))
        rule.onNodeWithTag("room_benchmark").performClick()
        rule.waitUntil(30000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("player_fullscreen").performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
        try {
            rule.activityRule.scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
            rule.waitUntil(20000) { rule.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
            rule.waitUntil(20000) { rule.onAllNodesWithTag("player_fullscreen").fetchSemanticsNodes().isNotEmpty() }
            rule.onNodeWithText(rule.activity.getString(dev.fanstage.android.feature.live.R.string.player_exit_fullscreen)).assertExists()
            rule.waitUntil(10000) { rule.onAllNodesWithTag("player_state_paused").fetchSemanticsNodes().isNotEmpty() }
            rule.onNodeWithTag("player_fullscreen").performClick()
        } finally {
            rule.activityRule.scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
            rule.waitUntil(20000) { rule.activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT }
        }
        rule.waitUntil(20000) { rule.onAllNodesWithTag("room_back").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("room_back").performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithTag("player_panel").fetchSemanticsNodes().isEmpty() }
        val ui = InstrumentationRegistry.getInstrumentation().uiAutomation
        ui.waitForIdle(500, 5000)
    }

    @Test fun portraitVideoRendersInFitSurface() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val file = File(fixtureDir(), "portrait.mp4")
        instrumentation.context.assets.open("player001/portrait.mp4").use { input -> file.outputStream().use(input::copyTo) }
        rule.activity.setContent { MaterialTheme { PlayerPanel("portrait-test", Uri.fromFile(file).toString()) } }
        rule.waitUntil(30000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
        rule.waitForIdle()
        rule.onNodeWithTag("player_speed").performClick()
        rule.onNodeWithText(rule.activity.getString(dev.fanstage.android.feature.live.R.string.player_quality_single)).assertExists()
        rule.onNodeWithText(rule.activity.getString(dev.fanstage.android.feature.live.R.string.player_unsupported)).assertExists()
    }

    @Test fun sourceChangeInvalidatesTrackOverrides() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val dir = fixtureDir()
        instrumentation.context.assets.list("player001")!!.forEach { name ->
            instrumentation.context.assets.open("player001/$name").use { input -> File(dir, name).outputStream().use(input::copyTo) }
        }
        lateinit var controller: Media3PlayerController
        lateinit var player: Player
        rule.runOnUiThread {
            controller = Media3PlayerController(instrumentation.targetContext, Uri.fromFile(File(dir, "master.m3u8")).toString())
            player = Media3PlayerController::class.java.getDeclaredField("player").apply { isAccessible = true }.get(controller) as Player
            controller.play()
        }
        try {
            rule.waitUntil(30000) { controller.uiState.value.videoTracks.size >= 2 && controller.uiState.value.textTracks.any { it.label == "English" } }
            val oldVideo = controller.uiState.value.videoTracks.first().id
            val oldSubtitle = controller.uiState.value.textTracks.first { it.label == "English" }.id
            rule.runOnUiThread { controller.selectVideo(oldVideo); controller.selectSubtitle(oldSubtitle) }
            rule.waitUntil(10000) { controller.uiState.value.selectedVideoId == oldVideo && controller.uiState.value.textTracks.any { it.id == oldSubtitle && it.selected } }
            rule.runOnUiThread { player.setMediaItem(MediaItem.fromUri(Uri.fromFile(File(dir, "portrait.mp4")))); player.prepare() }
            rule.waitUntil(30000) { controller.uiState.value.videoTracks.size == 1 && controller.uiState.value.videoTracks.none { it.id == oldVideo } }
            rule.runOnUiThread {
                assertNull(controller.uiState.value.selectedVideoId)
                assertTrue(player.trackSelectionParameters.overrides.values.none { it.type == C.TRACK_TYPE_VIDEO || it.type == C.TRACK_TYPE_TEXT })
                controller.selectVideo(oldVideo)
                controller.selectSubtitle(oldSubtitle)
                assertTrue(player.trackSelectionParameters.overrides.values.none { it.type == C.TRACK_TYPE_VIDEO || it.type == C.TRACK_TYPE_TEXT })
            }
            assertTrue(controller.uiState.value.textTracks.isEmpty())
        } finally { rule.runOnUiThread { controller.release() } }
    }

    @Test fun doubleFontScaleControlsHaveAccessibleLabels() {
        rule.activity.setContent {
            val current = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(current.density, 2f)) {
                MaterialTheme { PlayerPanel("large-font-test") }
            }
        }
        rule.waitUntil(30000) { rule.onAllNodesWithTag("player_pause").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithContentDescription(rule.activity.getString(dev.fanstage.android.feature.live.R.string.player_pause)).assertExists()
        rule.onNodeWithTag("player_fullscreen").assertExists()
        rule.onNodeWithTag("player_speed").performClick()
        rule.onNodeWithText(rule.activity.getString(dev.fanstage.android.feature.live.R.string.player_quality)).assertExists()
        rule.onNodeWithText(rule.activity.getString(dev.fanstage.android.feature.live.R.string.player_subtitles)).assertExists()
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("input keyevent KEYCODE_BACK").close()
        rule.waitUntil(10000) { rule.onAllNodesWithTag("player_speed_1.5").fetchSemanticsNodes().isEmpty() }
    }

    @Test fun removingPlayerAfterFullscreenLeavesNoVideoOverlay() {
        val show = mutableStateOf(true)
        rule.activity.setContent {
            MaterialTheme {
                Box(Modifier.fillMaxSize().background(Color(0xFF28634A))) {
                    if (show.value) PlayerPanel("surface-removal-test")
                }
            }
        }
        rule.waitUntil(30000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("player_fullscreen").performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("player_fullscreen").performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
        rule.runOnUiThread { show.value = false }
        rule.waitUntil(10000) { rule.onAllNodesWithTag("player_panel").fetchSemanticsNodes().isEmpty() }
        val ui = InstrumentationRegistry.getInstrumentation().uiAutomation
        ui.waitForIdle(500, 5000)
        val screenshot = ui.takeScreenshot()
        try {
            assertEquals(android.graphics.Color.rgb(0x28, 0x63, 0x4A), screenshot.getPixel(screenshot.width / 2, screenshot.height / 8))
        } finally { screenshot.recycle() }
    }

    @Test fun manualSettingsSurviveFullscreenRoundTrip() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val dir = fixtureDir()
        instrumentation.context.assets.list("player001")!!.forEach { name ->
            instrumentation.context.assets.open("player001/$name").use { input -> File(dir, name).outputStream().use(input::copyTo) }
        }
        rule.activity.setContent { MaterialTheme { PlayerPanel("settings-persist", Uri.fromFile(File(dir, "master.m3u8")).toString()) } }
        rule.waitUntil(30000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("player_speed").performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithText("720p", substring = true).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText("720p", substring = true).performScrollTo().performClick()
        rule.onNodeWithText("English", substring = true).performScrollTo().performClick()
        rule.onNodeWithTag("player_speed_1.5").performScrollTo().performClick()
        rule.onNodeWithTag("player_fullscreen").performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("player_speed").performClick()
        rule.onNodeWithText("720p", substring = true).assertIsSelected().assertTextContains("✓", substring = true)
        rule.onNodeWithText("English", substring = true).assertIsSelected().assertTextContains("✓", substring = true)
        rule.onNodeWithTag("player_speed_1.5").performScrollTo().assertIsSelected()
        instrumentation.uiAutomation.executeShellCommand("input keyevent KEYCODE_BACK").close()
        rule.waitUntil(10000) { rule.onAllNodesWithTag("player_speed_1.5").fetchSemanticsNodes().isEmpty() }
        rule.onNodeWithTag("player_fullscreen").performClick()
        rule.waitUntil(10000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("player_speed").performClick()
        rule.onNodeWithText("720p", substring = true).assertIsSelected().assertTextContains("✓", substring = true)
        rule.onNodeWithText("English", substring = true).assertIsSelected().assertTextContains("✓", substring = true)
        rule.onNodeWithTag("player_speed_1.5").performScrollTo().assertIsSelected()
    }

    @Test fun captionlessDefaultHlsHasNoSubtitleChoices() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        lateinit var controller: Media3PlayerController
        rule.runOnUiThread { controller = Media3PlayerController(context, "asset:///hls/captionless.m3u8"); controller.play() }
        try {
            rule.waitUntil(30000) { controller.state.value == PlaybackState.Playing && controller.uiState.value.videoTracks.isNotEmpty() }
            assertTrue("Captionless fixture must not advertise subtitles", controller.uiState.value.textTracks.isEmpty())
        } finally { rule.runOnUiThread { controller.release() } }
    }

    @Test fun undeclaredCeaCandidateIsExplicitlyUncertainAndSelectable() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        lateinit var controller: Media3PlayerController
        rule.runOnUiThread { controller = Media3PlayerController(context, "asset:///hls/vod.m3u8"); controller.play() }
        try {
            rule.waitUntil(30000) { controller.state.value == PlaybackState.Playing && controller.uiState.value.textTracks.isNotEmpty() }
            val candidate = controller.uiState.value.textTracks.single()
            assertEquals(context.getString(dev.fanstage.android.feature.live.R.string.player_subtitle_cea_unknown), candidate.label)
            rule.runOnUiThread { controller.selectSubtitle(candidate.id) }
            rule.waitUntil(10000) { controller.uiState.value.textTracks.any { it.id == candidate.id && it.selected } }
        } finally { rule.runOnUiThread { controller.release() } }
    }

}
