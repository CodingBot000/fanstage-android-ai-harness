package dev.fanstage.android

import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.media3.common.C
import androidx.media3.ui.PlayerView
import androidx.test.ext.junit.runners.AndroidJUnit4
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
        rule.onNodeWithTag("tab_media").performClick()
        rule.onNodeWithTag("media_filter_2").performClick()
        for (i in 1..7) {
            val id = "demo-%02d".format(i)
            rule.onNodeWithTag("media_grid").performScrollToNode(hasTestTag("media_$id"))
            rule.onNodeWithTag("media_$id").performClick()
            rule.waitUntil(45_000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
            rule.runOnUiThread {
                val player = checkNotNull(playerView(rule.activity.window.decorView)?.player)
                check(player.currentTracks.groups.any { it.type == C.TRACK_TYPE_AUDIO && it.isSupported })
                if (i == 1) { player.pause(); check(!player.playWhenReady); player.seekTo(10_000); player.play(); check(player.playWhenReady) }
            }
            rule.onNodeWithTag(if (i <= 3) "room_back" else "replay_back").performClick()
            rule.onNodeWithTag("media_screen").assertIsDisplayed()
        }
    }
}
