package dev.fanstage.android

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performScrollTo
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import androidx.compose.ui.test.onAllNodesWithText
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HiltRoomLifecycleTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private fun findPlayerView(view: View): PlayerView? {
        if (view is PlayerView) return view
        if (view is ViewGroup) for (i in 0 until view.childCount) findPlayerView(view.getChildAt(i))?.let { return it }
        return null
    }

    @Test fun playbackRemainsActiveAfterChatSendWithKeyboardVisible() {
        rule.waitUntil(20_000) { rule.onAllNodesWithTag("rooms_content").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("rooms_content").performScrollToNode(hasTestTag("live_carousel"))
        rule.onNodeWithTag("live_carousel").performScrollToNode(hasTestTag("room_benchmark"))
        rule.onNodeWithTag("room_benchmark").performClick()
        rule.waitUntil(30_000) { rule.onAllNodesWithTag("player_state_playing").fetchSemanticsNodes().isNotEmpty() }
        rule.waitUntil(30_000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
        lateinit var original: Player
        rule.runOnUiThread { original = requireNotNull(findPlayerView(rule.activity.window.decorView)?.player) }
        rule.onNodeWithTag("chat_input").performTextInput("Hello from test")
        rule.waitUntil(10_000) {
            ViewCompat.getRootWindowInsets(rule.activity.window.decorView)?.isVisible(WindowInsetsCompat.Type.ime()) == true
        }
        rule.onNodeWithTag("chat_send").performClick()
        rule.waitUntil(10_000) { rule.onAllNodesWithText(rule.activity.getString(dev.fanstage.android.feature.live.R.string.chat_sent)).fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("chat_messages").performScrollTo()
        rule.onAllNodesWithText("Hello from test").fetchSemanticsNodes().also { assertTrue(it.isNotEmpty()) }
        rule.runOnUiThread {
            val current = requireNotNull(findPlayerView(rule.activity.window.decorView)?.player)
            assertSame(original, current)
            assertTrue("player stopped after chat send", current.isPlaying)
        }
    }

    @Test fun roomDraftSurvivesRecreationAndIsClearedAfterReentry() {
        rule.waitUntil(20_000) { rule.onAllNodesWithTag("rooms_content").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("rooms_content").performScrollToNode(hasTestTag("live_carousel"))
        rule.onNodeWithTag("live_carousel").performScrollToNode(hasTestTag("room_benchmark"))
        rule.onNodeWithTag("room_benchmark").performClick()
        rule.onNodeWithTag("room_detail").assertIsDisplayed()
        rule.onNodeWithTag("chat_input").performTextInput("lifecycle draft")
        rule.activityRule.scenario.recreate()
        rule.onNodeWithTag("room_detail").assertIsDisplayed()
        rule.onNodeWithTag("chat_input").assertTextContains("lifecycle draft")
        rule.onNodeWithTag("room_back").performClick()
        rule.onNodeWithTag("room_benchmark").performClick()
        rule.onNodeWithTag("room_detail").assertIsDisplayed()
        rule.onNodeWithTag("chat_input").assert(!hasText("lifecycle draft", substring = true))
    }
}
