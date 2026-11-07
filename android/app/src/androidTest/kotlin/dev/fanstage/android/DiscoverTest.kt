package dev.fanstage.android

import dev.fanstage.android.feature.community.*

import dev.fanstage.android.feature.discover.*
import dev.fanstage.android.core.model.DiscoverData
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
        rule.waitUntil(20_000) { rule.onAllNodesWithTag("rooms_content").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("rooms_content").performScrollToNode(hasTestTag("room_studio-session"))
        rule.onNodeWithTag("room_studio-session").performClick()
        rule.onNodeWithTag("room_detail").assertIsDisplayed()
        rule.onNodeWithTag("video_playback_dark_screen").assertExists()
        rule.onNodeWithTag("chat_panel").assertExists()
        rule.onNodeWithTag("vod_chat_unavailable").assertDoesNotExist()
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
        rule.onNodeWithTag("messages_list").assertIsDisplayed()
        rule.onNodeWithTag("tab_rooms").performClick()
        rule.onNodeWithTag("replay_card").assertIsDisplayed()
    }
    @Test fun koreanLargeTextShowsDiscoverAndCommunity() {
        val showCommunity = androidx.compose.runtime.mutableStateOf(false)
        rule.activityRule.scenario.onActivity { activity ->
            val config = android.content.res.Configuration(activity.resources.configuration).apply { setLocale(java.util.Locale.KOREA); fontScale = 2f }
            val context = activity.createConfigurationContext(config)
            activity.setContent {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.ui.platform.LocalContext provides context,
                    androidx.compose.ui.platform.LocalConfiguration provides config,
                    androidx.compose.ui.platform.LocalDensity provides androidx.compose.ui.unit.Density(context.resources.displayMetrics.density, config.fontScale),
                ) {
                    FanStageTheme {
                        if (showCommunity.value) CommunityScreen(CommunityState.Ready(emptyList()), null, 0, {}, {}, {}, {})
                        else DiscoverScreen(DiscoverState.Ready(DiscoverData(emptyList(), emptyList())), {}, {})
                    }
                }
            }
        }
        rule.onNodeWithText("새로운 최애를 만나보세요.").assertIsDisplayed()
        rule.runOnUiThread { showCommunity.value = true }
        rule.onNodeWithText("커뮤니티").assertIsDisplayed()
    }
    @Test fun communityEmptyAndErrorStates() {
        var retry = false
        rule.activityRule.scenario.onActivity { activity -> activity.setContent { FanStageTheme { CommunityScreen(CommunityState.Error, null, 0, {}, { retry = true }, {}, {}) } } }
        rule.onNodeWithTag("community_error").assertExists()
        rule.onNodeWithText(rule.activity.getString(R.string.discover_retry)).performClick()
        rule.runOnIdle { check(retry) }
        rule.activityRule.scenario.onActivity { activity -> activity.setContent { FanStageTheme { CommunityScreen(CommunityState.Ready(emptyList()), null, 0, {}, {}, {}, {}) } } }
        rule.onNodeWithTag("community_feed").performScrollToNode(hasTestTag("community_empty"))
        rule.onNodeWithTag("community_empty").assertIsDisplayed()
    }
    @Test fun localMessageCanBeSent() {
        rule.onNodeWithTag("tab_messages").performClick()
        rule.onNodeWithTag("unread_paper-lantern", useUnmergedTree = true).assertIsDisplayed()
        rule.onNodeWithTag("conversation_paper-lantern").performClick()
        rule.onNodeWithTag("dm_input").performTextInput("Hello artist")
        rule.onNodeWithTag("dm_send").assertIsDisplayed().performClick()
        rule.onNodeWithText("Hello artist").assertIsDisplayed()
        rule.onNodeWithText(rule.activity.getString(R.string.local_sent)).assertIsDisplayed()
        rule.activityRule.scenario.recreate()
        rule.onNodeWithText("Hello artist").assertIsDisplayed()
        rule.onNodeWithTag("messages_back").performClick()
        rule.onNodeWithTag("unread_paper-lantern", useUnmergedTree = true).assertDoesNotExist()
    }
    @Test fun mediaFilterAndReplay() {
        rule.onNodeWithTag("tab_media").performClick()
        rule.onNodeWithTag("media_filter_2").performClick()
        rule.onNodeWithTag("tab_messages").performClick()
        rule.onNodeWithTag("tab_media").performClick()
        rule.onNodeWithTag("media_studio-session").assertDoesNotExist()
        val replay = rule.onNodeWithTag("media_demo-01").getUnclippedBoundsInRoot()
        val video = rule.onNodeWithTag("media_demo-02").getUnclippedBoundsInRoot()
        check(kotlin.math.abs((replay.bottom - replay.top - (video.bottom - video.top)).value) < 1f)
        rule.onNodeWithTag("media_grid").performScrollToNode(hasTestTag("media_replay"))
        rule.onNodeWithTag("media_replay").assertIsDisplayed().performClick()
        rule.onNodeWithTag("replay_detail").assertIsDisplayed()
        rule.onNodeWithTag("video_playback_dark_screen").assertExists()
        rule.onNodeWithTag("vod_chat_unavailable").assertExists()
        rule.onNodeWithTag("chat_panel").assertDoesNotExist()
        rule.waitUntil(30_000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("replay_back").performClick()
        rule.onNodeWithTag("media_screen").assertIsDisplayed()
    }
    @Test fun communityPostLikeAndComment() {
        rule.onNodeWithTag("tab_community").performClick()
        rule.waitUntil(20_000) { rule.onAllNodesWithTag("artist_paper-lantern").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("artist_paper-lantern").performClick()
        rule.waitUntil(20_000) { rule.onAllNodesWithTag("post_post-paper-lantern").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithText(rule.activity.getString(R.string.notices)).performClick()
        rule.onNodeWithTag("community_notice").assertExists()
        rule.onNodeWithText(rule.activity.getString(R.string.posts)).performClick()
        rule.onNodeWithTag("post_post-paper-lantern").performClick()
        rule.onNodeWithTag("post_like").performScrollTo().assertIsOff().performClick()
        rule.onNodeWithTag("post_like").assertIsOn()
        val commentBefore = rule.onNodeWithTag("comment_input", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val pageBefore = rule.onNodeWithTag("post_detail").getUnclippedBoundsInRoot()
        check((pageBefore.bottom - commentBefore.bottom).value in 0f..25f)
        rule.onNodeWithTag("comment_input").performTextInput("Hello community")
        val input = rule.onNodeWithTag("comment_input", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val send = rule.onNodeWithTag("comment_send", useUnmergedTree = true).getUnclippedBoundsInRoot()
        val detail = rule.onNodeWithTag("post_detail").getUnclippedBoundsInRoot()
        check((input.right - input.left) > (detail.right - detail.left) * 0.8f && send.left >= input.left && send.right <= input.right)
        rule.onNodeWithTag("comment_send").performClick()
        rule.onNodeWithTag("post_comments").performScrollToNode(hasText("Hello community"))
        rule.onNodeWithText("Hello community").assertExists()
        rule.onNodeWithTag("comment_input").performTextInput("Sent with keyboard")
        rule.onNodeWithTag("comment_input").performImeAction()
        rule.onNodeWithTag("post_comments").performScrollToNode(hasText("Sent with keyboard"))
        rule.onNodeWithText("Sent with keyboard").assertExists()
        rule.activityRule.scenario.recreate()
        rule.onNodeWithTag("post_comments").performScrollToNode(hasTestTag("post_like"))
        rule.onNodeWithTag("post_like").assertIsOn()
        rule.onNodeWithTag("post_comments").performScrollToNode(hasText("Hello community"))
        rule.onNodeWithText("Hello community").assertExists()
    }
    @Test fun replayPlaysAndReturns() {
        rule.waitUntil(20_000) { rule.onAllNodesWithTag("rooms_content").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("rooms_content").performScrollToNode(hasTestTag("benchmark_replay"))
        rule.onNodeWithTag("benchmark_replay").performClick()
        rule.onNodeWithTag("replay_detail").assertIsDisplayed()
        rule.onNodeWithTag("video_playback_dark_screen").assertExists()
        rule.onNodeWithTag("vod_chat_unavailable").assertExists()
        rule.onNodeWithTag("chat_panel").assertDoesNotExist()
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
