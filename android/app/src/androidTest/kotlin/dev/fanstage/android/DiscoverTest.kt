package dev.fanstage.android
import androidx.activity.compose.setContent
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.fanstage.android.core.designsystem.FanStageTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class DiscoverTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    @Test fun liveOpensAndReturns() {
        rule.waitUntil(20_000) { rule.onAllNodesWithTag("room_studio-session").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("room_studio-session").performScrollTo().performClick()
        rule.onNodeWithTag("room_detail").assertIsDisplayed()
        rule.onNodeWithTag("room_back").performClick()
        rule.onNodeWithTag("rooms_content").assertIsDisplayed()
    }
    @Test fun tabsRestoreDiscoverScroll() {
        rule.waitUntil(20_000) { rule.onAllNodesWithTag("rooms_content").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("rooms_content").performScrollToNode(hasTestTag("replay_card"))
        rule.onNodeWithTag("replay_card").assertIsDisplayed()
        rule.onNodeWithTag("tab_community").performClick()
        rule.onNodeWithTag("community_feed").assertIsDisplayed()
        rule.onNodeWithTag("tab_media").performClick()
        rule.onNodeWithTag("media_screen").assertIsDisplayed()
        rule.onNodeWithTag("tab_messages").performClick()
        rule.onNodeWithTag("screen_messages").assertIsDisplayed()
        rule.onNodeWithTag("tab_rooms").performClick()
        rule.onNodeWithTag("replay_card").assertIsDisplayed()
    }
    @Test fun mediaFilterAndReplay() {
        rule.onNodeWithTag("tab_media").performClick()
        rule.onNodeWithTag("media_filter_2").performClick()
        rule.onNodeWithTag("media_replay").assertIsDisplayed().performClick()
        rule.onNodeWithTag("replay_detail").assertIsDisplayed()
        rule.waitUntil(30_000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("replay_back").performClick()
        rule.onNodeWithTag("media_screen").assertIsDisplayed()
    }
    @Test fun communityPostLikeAndComment() {
        rule.onNodeWithTag("tab_community").performClick()
        rule.waitUntil(20_000) { rule.onAllNodesWithTag("artist_paper-lantern").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("artist_paper-lantern").performClick()
        rule.waitUntil(20_000) { rule.onAllNodesWithTag("post_post-paper-lantern").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("post_post-paper-lantern").performClick()
        rule.onNodeWithTag("post_like").performScrollTo().performClick()
        rule.onNodeWithText("♥").assertIsDisplayed()
        rule.onNodeWithTag("comment_input").performScrollTo().performTextInput("Hello community")
        rule.onNodeWithTag("comment_send").performScrollTo().performClick()
        rule.onNodeWithText("Hello community").assertExists()
    }
    @Test fun replayPlaysAndReturns() {
        rule.waitUntil(20_000) { rule.onAllNodesWithTag("rooms_content").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("rooms_content").performScrollToNode(hasTestTag("replay_card"))
        rule.onNodeWithTag("replay_card").performClick()
        rule.onNodeWithTag("replay_detail").assertIsDisplayed()
        rule.waitUntil(30_000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("replay_back").performClick()
        rule.onNodeWithTag("rooms_content").assertIsDisplayed()
    }
    @Test fun loadingAndErrorRetryRender() {
        var retried = false
        rule.activityRule.scenario.onActivity { activity -> activity.setContent { FanStageTheme { DiscoverScreen(DiscoverState.Loading, {}, {}) } } }
        rule.onNodeWithTag("discover_loading").assertIsDisplayed()
        rule.activityRule.scenario.onActivity { activity -> activity.setContent { FanStageTheme { DiscoverScreen(DiscoverState.Error, { retried = true }, {}) } } }
        rule.onNodeWithTag("discover_error").assertIsDisplayed()
        rule.onNodeWithText(rule.activity.getString(R.string.discover_retry)).performClick()
        rule.runOnIdle { check(retried) }
    }
}
