package dev.fanstage.android

import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
import org.junit.Assert.assertNotSame
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises the real MainActivity and its production FakeRoomRepository on a connected Android device. */
@RunWith(AndroidJUnit4::class)
class RoomNavigationSmokeTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Test fun roomListOpensDetailAndReturns() {
        composeRule.waitUntil(20_000L) {
            composeRule.onAllNodesWithTag("room_studio-session")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("rooms_content").assertExists()
        composeRule.onNodeWithTag("room_studio-session").performClick()
        composeRule.onNodeWithTag("room_detail").assertIsDisplayed()
        composeRule.waitUntil(30_000L) { composeRule.onAllNodesWithTag("player_state_playing").fetchSemanticsNodes().isNotEmpty() }
        composeRule.waitUntil(30_000L) { composeRule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        composeRule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        composeRule.waitUntil(10_000L) { composeRule.onAllNodesWithTag("player_state_paused").fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithTag("player_play").performClick()
        composeRule.waitUntil(10_000L) { composeRule.onAllNodesWithTag("player_state_playing").fetchSemanticsNodes().isNotEmpty() }
        // ActivityScenario.recreate() verifies Activity/nav state restoration, not rotation or process death.
        val originalActivity = composeRule.activity
        composeRule.activityRule.scenario.recreate()
        assertNotSame(originalActivity, composeRule.activity)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("room_detail").assertIsDisplayed()
        composeRule.waitUntil(10_000L) { composeRule.onAllNodesWithTag("player_state_paused").fetchSemanticsNodes().isNotEmpty() }
        // The controls must remain reachable after a real device orientation change.
        composeRule.activityRule.scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
        composeRule.waitUntil(20_000L) { composeRule.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
        composeRule.waitUntil(10_000L) { composeRule.onAllNodesWithTag("player_play").fetchSemanticsNodes().isNotEmpty() }
        composeRule.onNodeWithTag("player_play").performScrollTo().assertIsDisplayed()
        composeRule.activityRule.scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
        composeRule.waitUntil(20_000L) { composeRule.activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT }
        composeRule.onNodeWithTag("room_back").performScrollTo().performClick()
        composeRule.onNodeWithTag("room_studio-session").assertIsDisplayed()
        repeat(5) {
            composeRule.onNodeWithTag("room_studio-session").performClick()
            composeRule.waitUntil(30_000L) { composeRule.onAllNodesWithTag("player_state_playing").fetchSemanticsNodes().isNotEmpty() }
            composeRule.onNodeWithTag("room_back").performClick()
            composeRule.onNodeWithTag("room_studio-session").assertIsDisplayed()
        }
        verifyLivePlaylistRefresh()
        verifyPlaybackErrorAndRetry()
        verifyReleasedListenerCanBeCollected()
        verifyErrorRetryUi()
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
