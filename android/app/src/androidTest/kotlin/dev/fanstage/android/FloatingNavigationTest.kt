package dev.fanstage.android

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FloatingNavigationTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun tabsFloatAndLastContentRemainsReachable() {
        rule.waitUntil(20_000) { rule.onAllNodesWithTag("rooms_content").fetchSemanticsNodes().isNotEmpty() }
        fun checkLast(list: String, last: String) {
            rule.onNodeWithTag(list).performTouchInput { repeat(4) { swipeUp() } }
            rule.onNodeWithTag(list).performScrollToNode(hasTestTag(last))
            val item = rule.onNodeWithTag(last).getUnclippedBoundsInRoot()
            val bar = rule.onNodeWithTag("floating_navigation").getUnclippedBoundsInRoot()
            check(item.bottom <= bar.top) { "$last is hidden behind navigation" }
            rule.onNodeWithTag(last).assertIsDisplayed()
        }
        val bar = rule.onNodeWithTag("floating_navigation").getUnclippedBoundsInRoot()
        val screen = rule.onNodeWithTag("rooms_content").getUnclippedBoundsInRoot()
        check(bar.bottom < screen.bottom) { "navigation must float above the system area" }
        checkLast("rooms_content", "recommended_carousel")
        rule.onNodeWithTag("tab_community").performClick()
        checkLast("community_feed", "post_post-dice-garden")
        rule.onNodeWithTag("tab_media").performClick()
        checkLast("media_grid", "media_replay")
        rule.onNodeWithTag("tab_messages").performClick()
        checkLast("messages_list", "conversation_dice-garden")
        rule.onNodeWithTag("conversation_dice-garden").performClick()
        rule.onNodeWithTag("floating_navigation").assertDoesNotExist()
        rule.onNodeWithTag("messages_back").performClick()
        rule.onNodeWithTag("floating_navigation").assertIsDisplayed()
    }
}
