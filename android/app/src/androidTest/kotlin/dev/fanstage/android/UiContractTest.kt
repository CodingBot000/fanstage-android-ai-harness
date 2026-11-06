package dev.fanstage.android

import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.fanstage.android.core.data.FakeCatalog
import dev.fanstage.android.core.designsystem.FanStageTheme
import dev.fanstage.android.core.model.*
import dev.fanstage.android.feature.community.*
import dev.fanstage.android.feature.messages.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UiContractTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun postScreenEmitsActionsWithoutViewModel() {
        val actions = mutableListOf<CommunityUiAction>()
        val post = CommunityPost("post", "artist", "Title", "Body", ArtworkKey.PAPER_FEED)
        rule.activityRule.scenario.onActivity { activity -> activity.setContent { FanStageTheme {
            var draft by remember { mutableStateOf("") }
            PostDetailScreen(post, draft, { draft = it }, { actions.add(it) }, {})
        } } }
        rule.onNodeWithTag("post_like").performScrollTo().performClick()
        rule.onNodeWithTag("comment_input").performTextInput("Hello")
        rule.onNodeWithTag("comment_send").performClick()
        rule.runOnIdle { check(actions == listOf(CommunityUiAction.LikeClicked("post"), CommunityUiAction.CommentSubmitted("post", "Hello"))) }
    }

    @Test fun conversationScreenEmitsSendWithoutViewModel() {
        val actions = mutableListOf<MessagesUiAction>()
        val id = "paper-lantern"
        rule.activityRule.scenario.onActivity { activity -> activity.setContent { FanStageTheme {
            var draft by remember { mutableStateOf("") }
            ConversationScreen(LocalConversation(id, emptyList()), id, FakeCatalog.artists(), draft, { draft = it }, { actions.add(it) }, {})
        } } }
        rule.onNodeWithTag("dm_input").performTextInput("Hi")
        rule.onNodeWithTag("dm_send").performClick()
        rule.runOnIdle { check(actions == listOf(MessagesUiAction.SendClicked(id, "Hi"))) }
    }
}
