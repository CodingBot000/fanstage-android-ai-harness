package dev.fanstage.android

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.text.AnnotatedString
import androidx.lifecycle.Lifecycle
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.fanstage.android.feature.messages.ConversationDraftViewModel
import org.junit.Rule
import org.junit.Test

class DraftLifecycleTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()
    private fun empty(tag: String) = rule.onNodeWithTag(tag).assert(SemanticsMatcher.expectValue(SemanticsProperties.EditableText,AnnotatedString("")))
    private fun openDm() {
        rule.onNodeWithTag("tab_messages").performClick()
        rule.onNodeWithTag("conversation_paper-lantern").performClick()
    }
    private fun openPost() {
        rule.onNodeWithTag("tab_community").performClick()
        rule.onNodeWithTag("artist_paper-lantern").performClick()
        rule.onNodeWithTag("post_post-paper-lantern").performClick()
    }
    @Test fun dmDraftSurvivesRecreationAndBackgroundClearsAfterSendAndPop() {
        openDm()
        rule.onNodeWithTag("dm_input").performTextInput("draft DM")
        rule.activityRule.scenario.recreate()
        rule.onNodeWithTag("dm_input").assertTextContains("draft DM")
        rule.activityRule.scenario.moveToState(Lifecycle.State.CREATED)
        rule.activityRule.scenario.moveToState(Lifecycle.State.RESUMED)
        rule.onNodeWithTag("dm_input").assertTextContains("draft DM")
        rule.onNodeWithTag("dm_send").performClick(); empty("dm_input")
        rule.onNodeWithTag("dm_input").performTextInput("discard on pop")
        rule.onNodeWithTag("messages_back").performClick()
        rule.onNodeWithTag("conversation_paper-lantern").performClick(); empty("dm_input")
    }
    @Test fun commentDraftSurvivesRecreationAndClearsAfterAcceptanceAndPop() {
        openPost()
        rule.onNodeWithTag("comment_input").performTextInput("draft comment")
        rule.activityRule.scenario.recreate()
        rule.onNodeWithTag("comment_input").assertTextContains("draft comment")
        rule.onNodeWithTag("comment_send").performClick(); empty("comment_input")
        rule.onNodeWithTag("comment_input").performTextInput("discard on pop")
        rule.onNodeWithTag("post_back").performClick()
        rule.onNodeWithTag("post_post-paper-lantern").performClick(); empty("comment_input")
    }
    @Test fun savedDestinationRetainsMemoryDraftUntilFinalRemoval() {
        lateinit var nav: NavHostController
        lateinit var holder: ConversationDraftViewModel
        rule.activity.setContent {
            nav = rememberNavController()
            NavHost(nav, startDestination = "draft") {
                composable("draft") { entry ->
                    val vm: ConversationDraftViewModel = viewModel(viewModelStoreOwner = entry)
                    holder = vm
                    val text by vm.text.collectAsStateWithLifecycle()
                    Text(text.ifEmpty { "empty" }, Modifier.testTag("saved_draft"))
                }
                composable("other") { Text("other", Modifier.testTag("other_destination")) }
            }
        }
        rule.runOnIdle { holder.update("memory survives") }
        rule.onNodeWithTag("saved_draft").assertTextEquals("memory survives")
        val original = holder
        rule.runOnIdle { nav.navigate("other") { popUpTo("draft") { inclusive = true; saveState = true } } }
        rule.onNodeWithTag("other_destination").assertExists()
        rule.runOnIdle { nav.navigate("draft") { restoreState = true } }
        rule.onNodeWithTag("saved_draft").assertTextEquals("memory survives")
        rule.runOnIdle { org.junit.Assert.assertSame(original, holder); nav.popBackStack() }
        rule.onNodeWithTag("other_destination").assertExists()
        rule.runOnIdle { nav.navigate("draft") }
        rule.onNodeWithTag("saved_draft").assertTextEquals("empty")
        rule.runOnIdle { org.junit.Assert.assertNotSame(original, holder); org.junit.Assert.assertEquals("", original.text.value) }
    }
}
