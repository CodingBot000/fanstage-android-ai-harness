package dev.fanstage.android

import android.content.Intent
import android.os.Debug
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import androidx.test.platform.app.InstrumentationRegistry
import java.lang.ref.WeakReference
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** Repeated lifecycle observations; memory and weak references are not a leak proof. */
class ResourceLifecycleAuditTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private val players = mutableListOf<WeakReference<Player>>()
    private val runId = InstrumentationRegistry.getArguments().getString("fanAuditRunId") ?: "unspecified"

    private fun findPlayer(view: View): PlayerView? {
        if (view is PlayerView) return view
        if (view is ViewGroup) for (i in 0 until view.childCount) findPlayer(view.getChildAt(i))?.let { return it }
        return null
    }

    private fun observe(stage: String) {
        val info = Debug.MemoryInfo().also { Debug.getMemoryInfo(it) }
        val runtime = Runtime.getRuntime()
        Log.i("FSResourceAudit", "run=$runId stage=$stage pss_kb=${info.totalPss} java_used_kb=${(runtime.totalMemory() - runtime.freeMemory()) / 1024} threads=${Thread.getAllStackTraces().size} gc_count=${Debug.getRuntimeStat("art.gc.gc-count") ?: "unavailable"} reachable_players=${players.count { it.get() != null }}")
    }

    @Test fun repeatedNavigationAndLifecycleObservation() {
        rule.waitUntil(20_000) { rule.onAllNodesWithTag("rooms_content").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("rooms_content").performScrollToNode(hasTestTag("live_carousel"))
        rule.onNodeWithTag("live_carousel").performScrollToNode(hasTestTag("room_benchmark"))
        observe("before")
        repeat(30) { index ->
            rule.onNodeWithTag("room_benchmark").performClick()
            rule.waitUntil(30_000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
            rule.waitUntil(30_000) { rule.onAllNodesWithTag("player_state_playing").fetchSemanticsNodes().isNotEmpty() }
            rule.runOnUiThread {
                val player = requireNotNull(findPlayer(rule.activity.window.decorView)?.player)
                assertTrue("LIVE must be playing", player.isPlaying)
                players.add(WeakReference(player))
            }
            if (index < 10) {
                rule.onNodeWithTag("player_fullscreen").performClick()
                rule.onNodeWithText(rule.activity.getString(dev.fanstage.android.feature.live.R.string.player_exit_fullscreen)).assertIsDisplayed()
                rule.onNodeWithTag("player_fullscreen").performClick()
                rule.onNodeWithText(rule.activity.getString(dev.fanstage.android.feature.live.R.string.player_enter_fullscreen)).assertIsDisplayed()
                InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("input keyevent KEYCODE_HOME").close()
                rule.waitUntil(10_000) { !rule.activity.hasWindowFocus() }
                val target = InstrumentationRegistry.getInstrumentation().targetContext
                val intent = requireNotNull(target.packageManager.getLaunchIntentForPackage(target.packageName))
                    .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
                target.startActivity(intent)
                rule.waitUntil(10_000) { rule.activity.hasWindowFocus() }
                rule.waitUntil(10_000) { rule.onAllNodesWithTag("player_state_paused").fetchSemanticsNodes().isNotEmpty() }
                rule.onNodeWithTag("player_play").performClick()
                rule.waitUntil(10_000) { rule.onAllNodesWithTag("player_state_playing").fetchSemanticsNodes().isNotEmpty() }
            }
            rule.onNodeWithTag("room_back").performClick()
            rule.waitUntil(10_000) { rule.onAllNodesWithTag("player_panel").fetchSemanticsNodes().isEmpty() }
            if ((index + 1) % 10 == 0) observe("live_${index + 1}")
        }
        rule.onNodeWithTag("tab_media").performClick()
        rule.onNodeWithTag("media_grid").performScrollToNode(hasTestTag("media_replay"))
        repeat(10) {
            rule.onNodeWithTag("media_replay").performClick()
            rule.waitUntil(30_000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
            rule.onNodeWithTag("replay_back").performClick()
            rule.waitUntil(10_000) { rule.onAllNodesWithTag("player_panel").fetchSemanticsNodes().isEmpty() }
        }
        observe("after_replay_10")
    }
}
