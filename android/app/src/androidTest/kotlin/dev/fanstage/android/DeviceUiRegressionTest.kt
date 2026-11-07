package dev.fanstage.android

import android.view.TextureView
import android.view.View
import androidx.test.platform.app.InstrumentationRegistry
import android.view.ViewGroup
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.ui.PlayerView
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.fanstage.android.core.designsystem.CardSingleLineText
import dev.fanstage.android.core.designsystem.FanStageTheme
import dev.fanstage.android.core.data.FakeCatalog
import dev.fanstage.android.core.data.FakeMessageRepository
import dev.fanstage.android.feature.messages.MessagesListRoute
import dev.fanstage.android.feature.messages.MessagesUiAction
import dev.fanstage.android.feature.messages.MessagesViewModel
import dev.fanstage.android.core.model.ArtworkKey
import dev.fanstage.android.core.model.DiscoverContent
import dev.fanstage.android.core.model.DiscoverData
import dev.fanstage.android.feature.media.MediaScreen
import dev.fanstage.android.feature.media.MediaState
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class DeviceUiRegressionTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun gridCardsHaveEqualHeightsAndLongTextEllipsizes() {
        val first = DiscoverContent("short", "Short", "Artist", "artist", ArtworkKey.PAPER_HERO)
        val second = DiscoverContent("long", "A very long title that must occupy two lines and never change card height", "An exceptionally long artist name that also wraps", "artist", ArtworkKey.BLUE_HERO)
        for (scale in listOf(1f, 2f)) {
            val layout = AtomicReference<TextLayoutResult>()
            rule.activityRule.scenario.onActivity { activity -> activity.setContent {
                val current = LocalDensity.current
                CompositionLocalProvider(LocalDensity provides Density(current.density, scale)) {
                    FanStageTheme { Column {
                        CardSingleLineText("A deliberately long line of words that cannot fit into the two available rows of this narrow card", TextStyle(fontSize = 20.sp, lineHeight = 24.sp), Modifier.width(120.dp), onTextLayout = { layout.set(it) })
                        MediaScreen(MediaState.Ready(DiscoverData(emptyList(), listOf(first, second))), 0, {}, {}, {}, {})
                    } }
                }
            } }
            rule.waitForIdle()
            val result = requireNotNull(layout.get())
            assertEquals("scale=$scale size=${result.size} overflow=${result.hasVisualOverflow}", 1, result.lineCount)
            assertTrue(result.isLineEllipsized(0))
            val one = rule.onNodeWithTag("media_short").getUnclippedBoundsInRoot()
            val two = rule.onNodeWithTag("media_long").getUnclippedBoundsInRoot()
            assertTrue("peer card heights differ at fontScale=$scale: ${one.bottom - one.top} vs ${two.bottom - two.top}", abs((one.bottom - one.top).value - (two.bottom - two.top).value) < 1f)
        }
    }

    @Test fun messageCardsRemainAlignedWithUnreadAndLongPreview() {
        val model = MessagesViewModel(FakeMessageRepository())
        model.onAction(MessagesUiAction.ConversationOpened("paper-lantern"))
        model.onAction(MessagesUiAction.SendClicked("paper-lantern", "A long preview ".repeat(30)))
        rule.activityRule.scenario.onActivity { activity -> activity.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                FanStageTheme { MessagesListRoute(model, FakeCatalog.artists(), {}) }
            }
        } }
        rule.waitForIdle()
        val first = rule.onNodeWithTag("conversation_paper-lantern").getUnclippedBoundsInRoot()
        val second = rule.onNodeWithTag("conversation_blue-pencil").getUnclippedBoundsInRoot()
        assertTrue("message card heights must match at large font", abs((first.bottom - first.top).value - (second.bottom - second.top).value) < 1f)
    }

    @Test fun detailHeadersUseBackIconAndKeyboardSendWorks() {
        val backDescription = rule.activity.getString(dev.fanstage.android.core.designsystem.R.string.navigation_back)
        rule.onNodeWithTag("tab_messages").performClick()
        rule.onNodeWithTag("conversation_paper-lantern").performClick()
        rule.onNodeWithTag("messages_back").assertIsDisplayed().assert(hasContentDescription(backDescription))
        val backBounds = rule.onNodeWithTag("messages_back").getUnclippedBoundsInRoot()
        val titleBounds = rule.onNodeWithText("Paper Lantern").getUnclippedBoundsInRoot()
        assertTrue("back icon must be left of title", backBounds.right <= titleBounds.left)
        rule.onNodeWithTag("dm_input").performClick()
        assertAboveKeyboard("dm_input")
        rule.onNodeWithTag("dm_input").performTextInput("Device IME message")
        assertComposerBounds("dm_input", "dm_send")
        rule.onNodeWithTag("dm_input").performImeAction()
        rule.onNodeWithText("Device IME message").assertIsDisplayed()
        rule.onNodeWithTag("messages_back").performClick()
        rule.onNodeWithTag("tab_community").performClick()
        rule.onNodeWithTag("artist_paper-lantern").performClick()
        rule.onNodeWithTag("community_back").assertIsDisplayed().assert(hasContentDescription(backDescription))
        rule.onNodeWithTag("community_feed").performScrollToNode(hasTestTag("post_post-paper-lantern"))
        rule.onNodeWithTag("post_post-paper-lantern").performClick()
        rule.onNodeWithTag("post_back").assertIsDisplayed().assert(hasContentDescription(backDescription))
        rule.onNodeWithTag("comment_input").performClick()
        assertAboveKeyboard("comment_input")
        InstrumentationRegistry.getInstrumentation().uiAutomation.executeShellCommand("input keyevent KEYCODE_BACK").close()
        rule.waitUntil(10_000) { androidx.core.view.ViewCompat.getRootWindowInsets(rule.activity.window.decorView)?.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime()) == false }
        rule.onNodeWithTag("post_back").performClick()
        rule.waitUntil(10_000) { rule.onAllNodesWithTag("community_back").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("community_back").performClick()
        rule.onNodeWithTag("tab_media").performClick()
        rule.onNodeWithTag("media_grid").performScrollToNode(hasTestTag("media_replay"))
        rule.onNodeWithTag("media_replay").performClick()
        rule.onNodeWithTag("replay_back").assertIsDisplayed().assert(hasContentDescription(backDescription))
    }

    @Test fun liveChatKeyboardSendAndBack() {
        rule.waitUntil(20_000) { rule.onAllNodesWithTag("rooms_content").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("rooms_content").performScrollToNode(hasTestTag("live_carousel"))
        rule.onNodeWithTag("live_carousel").performScrollToNode(hasTestTag("room_benchmark"))
        rule.onNodeWithTag("room_benchmark").performClick()
        rule.waitUntil(30_000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
        val backDescription = rule.activity.getString(dev.fanstage.android.core.designsystem.R.string.navigation_back)
        rule.onNodeWithTag("room_back").assertIsDisplayed().assert(hasContentDescription(backDescription))
        val beforeVideo = rule.onNodeWithTag("player_first_frame").getUnclippedBoundsInRoot()
        rule.onNodeWithTag("chat_input").performClick()
        assertAboveKeyboard("chat_input")
        val afterVideo = rule.onNodeWithTag("player_first_frame").getUnclippedBoundsInRoot()
        assertTrue("video must stay fixed when IME opens: $beforeVideo -> $afterVideo", kotlin.math.abs((beforeVideo.top - afterVideo.top).value) < 1f)
        rule.onNodeWithTag("chat_input").performTextInput("Device LIVE IME")
        assertComposerBounds("chat_input", "chat_send")
        rule.onNodeWithTag("chat_input").performImeAction()
        rule.waitUntil(10_000) { rule.onAllNodesWithText("Device LIVE IME").fetchSemanticsNodes().isNotEmpty() }
        rule.waitUntil(10_000) { rule.onAllNodesWithTag("chat_bubble_own").fetchSemanticsNodes().isNotEmpty() && rule.onAllNodesWithTag("chat_bubble_other").fetchSemanticsNodes().isNotEmpty() }
        val ownBubble = rule.onAllNodesWithTag("chat_bubble_own")[0].getUnclippedBoundsInRoot()
        val otherBubble = rule.onAllNodesWithTag("chat_bubble_other")[0].getUnclippedBoundsInRoot()
        assertTrue("own and other bubbles must align to opposite sides", ownBubble.left > otherBubble.left && ownBubble.right > otherBubble.right)
        rule.onNodeWithTag("chat_avatar_other").assertExists()
        val transcript = rule.onNodeWithTag("chat_messages").getUnclippedBoundsInRoot()
        assertTrue(
            "short incoming bubble must wrap content instead of reserving 80% of the transcript",
            otherBubble.width.value < transcript.width.value * 0.75f,
        )
        rule.onNodeWithTag("room_back").performClick()
        rule.onNodeWithTag("rooms_content").assertExists()
        rule.waitUntil(10_000) { findPlayer(rule.activity.findViewById(android.R.id.content)) == null }
    }

    @Test fun videoUsesTextureAndLeavesWithDetail() {
        rule.onNodeWithTag("tab_media").performClick()
        rule.onNodeWithTag("media_grid").performScrollToNode(hasTestTag("media_replay"))
        rule.onNodeWithTag("media_replay").performClick()
        rule.waitUntil(30_000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
        rule.runOnIdle {
            val playerView = findPlayer(rule.activity.findViewById(android.R.id.content))
            assertNotNull("PlayerView not attached", playerView)
            assertTrue("video must be composited with screen transition", playerView!!.videoSurfaceView is TextureView)
        }
        rule.onNodeWithTag("replay_back").performClick()
        rule.onNodeWithTag("media_screen").assertIsDisplayed()
        rule.waitUntil(10_000) { findPlayer(rule.activity.findViewById(android.R.id.content)) == null }
    }

    @Test fun developmentFieldsStayAboveImeWithoutMovingVideo() {
        rule.waitUntil(20_000) { rule.onAllNodesWithTag("rooms_content").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("rooms_content").performScrollToNode(hasTestTag("live_carousel"))
        rule.onNodeWithTag("live_carousel").performScrollToNode(hasTestTag("room_benchmark"))
        rule.onNodeWithTag("room_benchmark").performClick()
        rule.waitUntil(30_000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("chat_settings_toggle").performScrollTo().performClick()
        for (tag in listOf("chat_endpoint", "chat_token")) {
            rule.onNodeWithTag(tag).performScrollTo()
            val video = rule.onNodeWithTag("player_first_frame").getUnclippedBoundsInRoot()
            rule.onNodeWithTag(tag).performClick()
            assertAboveKeyboard(tag)
            val current = rule.onNodeWithTag("player_first_frame").getUnclippedBoundsInRoot()
            assertTrue("video moved for $tag: $video -> $current", abs((video.top - current.top).value) < 1f)
        }
    }

    @Test fun chatSettingsStartCollapsedAndInvalidConnectionKeepsDemoSelected() {
        rule.waitUntil(20_000) { rule.onAllNodesWithTag("rooms_content").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("rooms_content").performScrollToNode(hasTestTag("live_carousel"))
        rule.onNodeWithTag("live_carousel").performScrollToNode(hasTestTag("room_benchmark"))
        rule.onNodeWithTag("room_benchmark").performClick()
        rule.waitUntil(30_000) { rule.onAllNodesWithTag("chat_settings_toggle").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("chat_endpoint").assertDoesNotExist()
        rule.onNodeWithTag("chat_use_fake").assertIsSelected()
        rule.onNodeWithTag("chat_select_real").performScrollTo().performClick()
        rule.onNodeWithTag("chat_endpoint").assertExists()
        rule.onNodeWithTag("chat_use_real").performScrollTo().performClick()
        rule.onNodeWithTag("chat_connection_error").assertExists()
        rule.onNodeWithTag("chat_use_fake").assertIsSelected()
        rule.onNodeWithTag("chat_select_real").assertIsNotSelected()
        rule.onNodeWithTag("chat_settings_toggle").performScrollTo().performClick()
        rule.onNodeWithTag("chat_endpoint").assertDoesNotExist()
        rule.onNodeWithTag("chat_connection_error").assertExists()
    }

    @Test fun chatSettingsSurviveActivityRecreation() {
        rule.waitUntil(20_000) { rule.onAllNodesWithTag("rooms_content").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("rooms_content").performScrollToNode(hasTestTag("live_carousel"))
        rule.onNodeWithTag("live_carousel").performScrollToNode(hasTestTag("room_benchmark"))
        rule.onNodeWithTag("room_benchmark").performClick()
        rule.waitUntil(30_000) { rule.onAllNodesWithTag("chat_settings_toggle").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("chat_settings_toggle").performScrollTo().performClick()
        rule.onNodeWithTag("chat_endpoint").assertExists()
        rule.activityRule.scenario.recreate()
        rule.waitUntil(30_000) { rule.onAllNodesWithTag("chat_endpoint").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("chat_use_fake").assertIsSelected()
        val original = rule.activity.requestedOrientation
        try {
            rule.runOnUiThread { rule.activity.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
            rule.waitUntil(30_000) { rule.activity.resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE }
            rule.waitUntil(30_000) { rule.onAllNodesWithTag("chat_endpoint").fetchSemanticsNodes().isNotEmpty() }
            rule.onNodeWithTag("chat_use_fake").assertIsSelected()
        } finally {
            rule.runOnUiThread { rule.activity.requestedOrientation = original }
        }
    }

    @Test fun chatModeControlsRemainSeparateAtDoubleFontAndNarrowWidth() {
        val vm = dev.fanstage.android.feature.live.chat.ChatViewModel(dev.fanstage.android.feature.live.chat.FakeChatTransport("mode-layout", simulateIncoming = false), "mode-layout")
        rule.activity.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 2f)) {
                MaterialTheme { androidx.compose.foundation.layout.Box(Modifier.width(320.dp)) { dev.fanstage.android.feature.live.chat.ChatPanel(vm, developmentControls = true) } }
            }
        }
        val demo = rule.onNodeWithTag("chat_use_fake").assertIsDisplayed().getUnclippedBoundsInRoot()
        val server = rule.onNodeWithTag("chat_select_real").assertIsDisplayed().getUnclippedBoundsInRoot()
        assertTrue("mode controls must not overlap", demo.right <= server.left)
        rule.onNodeWithTag("chat_use_fake").assertIsSelected()
    }

    private fun assertAboveKeyboard(tag: String) {
        rule.waitUntil(10_000) {
            val insets = androidx.core.view.ViewCompat.getRootWindowInsets(rule.activity.window.decorView)
            insets?.isVisible(androidx.core.view.WindowInsetsCompat.Type.ime()) == true
        }
        rule.waitUntil(10_000) {
            val decor = rule.activity.window.decorView
            val insets = androidx.core.view.ViewCompat.getRootWindowInsets(decor) ?: return@waitUntil false
            val ime = insets.getInsets(androidx.core.view.WindowInsetsCompat.Type.ime()).bottom
            val location = IntArray(2)
            rule.activity.findViewById<View>(android.R.id.content).getLocationOnScreen(location)
            val bottom = rule.onNodeWithTag(tag, useUnmergedTree = true).getUnclippedBoundsInRoot().bottom.value * rule.density.density + location[1]
            ime > 0 && bottom <= decor.height - ime + 2
        }
    }

    private fun assertComposerBounds(inputTag: String, sendTag: String) {
        // Bounds must be sampled after the IME animation settles; otherwise the
        // two reads can observe different positions of the same moving composer.
        var previous: Pair<androidx.compose.ui.unit.DpRect, androidx.compose.ui.unit.DpRect>? = null
        var stableSince = 0L
        rule.waitUntil(10_000) {
            val input = rule.onNodeWithTag(inputTag, useUnmergedTree = true).getUnclippedBoundsInRoot()
            val send = rule.onNodeWithTag(sendTag, useUnmergedTree = true).getUnclippedBoundsInRoot()
            val bounds = input to send
            val now = android.os.SystemClock.uptimeMillis()
            if (bounds != previous) {
                previous = bounds
                stableSince = now
            }
            now - stableSince >= 300
        }
        val (input, send) = checkNotNull(previous)
        assertTrue(
            "$sendTag must be inside $inputTag: input=$input send=$send",
            send.left >= input.left && send.right <= input.right &&
                send.top >= input.top && send.bottom <= input.bottom,
        )
    }

    private fun findPlayer(view: View): PlayerView? {
        if (view is PlayerView) return view
        if (view is ViewGroup) for (i in 0 until view.childCount) findPlayer(view.getChildAt(i))?.let { return it }
        return null
    }
}
