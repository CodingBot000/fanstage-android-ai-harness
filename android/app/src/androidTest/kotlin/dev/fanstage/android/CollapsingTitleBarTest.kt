package dev.fanstage.android

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CollapsingTitleBarTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun tabBarHidesOnUpAndReturnsOnDown() {
        rule.waitUntil(20_000) { rule.onAllNodesWithTag("rooms_content").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("rooms_content").performTouchInput { swipeUp() }
        rule.onNodeWithTag("collapsing_top_bar").assertHeightIsEqualTo(0.dp)
        rule.onNodeWithTag("floating_navigation").assertIsDisplayed()
        rule.onNodeWithTag("rooms_content").performTouchInput { swipeDown() }
        rule.onNodeWithTag("collapsing_top_bar").assertHeightIsEqualTo(64.dp)
        rule.onNodeWithTag("tab_community").performClick()
        rule.onNodeWithTag("collapsing_top_bar").assertHeightIsEqualTo(64.dp)
        rule.onNodeWithTag("community_feed").performTouchInput { swipeUp() }
        rule.onNodeWithTag("collapsing_top_bar").assertHeightIsEqualTo(0.dp)
        rule.onNodeWithTag("community_feed").performTouchInput { swipeDown() }
        rule.onNodeWithTag("collapsing_top_bar").assertHeightIsEqualTo(64.dp)
    }

    @Test fun detailBackBarHidesAndReturns() {
        rule.onNodeWithTag("tab_community").performClick()
        rule.onNodeWithTag("artist_paper-lantern").performClick()
        rule.onNodeWithTag("community_back").assertIsDisplayed()
        rule.onNodeWithTag("community_feed").performTouchInput { swipeUp() }
        rule.onNodeWithTag("community_back").assertIsNotDisplayed()
        rule.onNodeWithTag("community_feed").performTouchInput { swipeDown() }
        rule.onNodeWithTag("community_back").assertIsDisplayed().performClick()
    }

    @Test fun replayBarRespondsToSwipe() {
        rule.onNodeWithTag("tab_media").performClick()
        rule.onNodeWithTag("media_grid").performScrollToNode(hasTestTag("media_replay"))
        rule.onNodeWithTag("media_replay").performClick()
        rule.onNodeWithTag("replay_back").assertIsDisplayed()
        rule.onNodeWithTag("replay_detail").performTouchInput { swipeUp() }
        rule.onNodeWithTag("replay_back").assertIsNotDisplayed()
        rule.onNodeWithTag("replay_detail").performTouchInput { swipeDown() }
        rule.onNodeWithTag("replay_back").assertIsDisplayed().performClick()
    }
    @Test fun mediaAndMessagesBarsRespondToSwipe() {
        rule.onNodeWithTag("tab_media").performClick()
        rule.onNodeWithTag("media_grid").performTouchInput { swipeUp() }
        rule.onNodeWithTag("collapsing_top_bar").assertHeightIsEqualTo(0.dp)
        rule.onNodeWithTag("media_grid").performTouchInput { swipeDown() }
        rule.onNodeWithTag("collapsing_top_bar").assertHeightIsEqualTo(64.dp)
        rule.onNodeWithTag("tab_messages").performClick()
        rule.onNodeWithTag("messages_list").performTouchInput { swipeUp() }
        rule.onNodeWithTag("collapsing_top_bar").assertHeightIsEqualTo(0.dp)
        rule.onNodeWithTag("messages_list").performTouchInput { swipeDown() }
        rule.onNodeWithTag("collapsing_top_bar").assertHeightIsEqualTo(64.dp)
        rule.onNodeWithTag("conversation_paper-lantern").performClick()
        rule.onNodeWithTag("conversation_transcript").performTouchInput { swipeUp() }
        rule.onNodeWithTag("messages_back").assertIsNotDisplayed()
        rule.onNodeWithTag("conversation_transcript").performTouchInput { swipeDown() }
        rule.onNodeWithTag("messages_back").assertIsDisplayed()
        rule.onNodeWithTag("dm_input").assertIsDisplayed()
    }

}
