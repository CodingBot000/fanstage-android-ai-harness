package dev.fanstage.android

import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.media3.common.C
import androidx.media3.ui.PlayerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocalMediaTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private fun playerView(v: View): PlayerView? {
        if (v is PlayerView) return v
        if (v is ViewGroup) for (i in 0 until v.childCount) playerView(v.getChildAt(i))?.let { return it }
        return null
    }
    @Test fun sevenVideosRenderAndRecognizeAudio() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("fanstageLocalMedia") == "true")
        rule.onNodeWithTag("tab_media").performClick()
        rule.onNodeWithTag("media_filter_2").performClick()
        for (i in 1..7) {
            val id = "demo-%02d".format(i)
            rule.onNodeWithTag("media_grid").performScrollToNode(hasTestTag("media_$id"))
            rule.onNodeWithTag("media_$id").performClick()
            rule.waitUntil(45_000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
            if (i == 1) {
                rule.onNodeWithTag("player_pause").performClick()
                rule.waitUntil(30_000) { rule.onAllNodesWithTag("player_seek").fetchSemanticsNodes().isNotEmpty() }
                rule.onNodeWithTag("player_seek").performSemanticsAction(androidx.compose.ui.semantics.SemanticsActions.SetProgress) { it(0.5f) }
                rule.waitUntil(30_000) { rule.onAllNodesWithTag("player_play").fetchSemanticsNodes().isNotEmpty() }
                rule.onNodeWithTag("player_play").performClick()
            }
            rule.runOnUiThread {
                val player = checkNotNull(playerView(rule.activity.window.decorView)?.player)
                check(player.currentTracks.groups.any { it.type == C.TRACK_TYPE_AUDIO && it.isSupported })
                if (i == 1) check(player.currentPosition > 10_000)
            }
            rule.onNodeWithTag(if (i <= 3) "room_back" else "replay_back").performClick()
            rule.onNodeWithTag("media_screen").assertIsDisplayed()
        }
    }
    @Test fun recordedVideoAndRealChatWorkTogether() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("fanstageLocalMedia") == "true")
        val token = requireNotNull(InstrumentationRegistry.getArguments().getString("fanTokenA"))
        rule.onNodeWithTag("tab_media").performClick()
        rule.onNodeWithTag("media_filter_2").performClick()
        rule.onNodeWithTag("media_grid").performScrollToNode(hasTestTag("media_demo-01"))
        rule.onNodeWithTag("media_demo-01").performClick()
        rule.waitUntil(45_000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("chat_token").performScrollTo().performTextInput(token)
        rule.onNodeWithTag("chat_use_real").performScrollTo().performClick()
        rule.waitUntil(30_000) { rule.onAllNodesWithText(rule.activity.getString(dev.fanstage.android.feature.live.R.string.chat_connected)).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("chat_input").performScrollTo().performTextInput("Local VOD test")
        rule.onNodeWithTag("chat_send").performScrollTo().performClick()
        rule.waitUntil(30_000) { rule.onAllNodesWithText("Local VOD test").fetchSemanticsNodes().isNotEmpty() }
        rule.waitUntil(30_000) { rule.onAllNodesWithText(rule.activity.getString(dev.fanstage.android.feature.live.R.string.chat_sent)).fetchSemanticsNodes().isNotEmpty() }
    }

}
