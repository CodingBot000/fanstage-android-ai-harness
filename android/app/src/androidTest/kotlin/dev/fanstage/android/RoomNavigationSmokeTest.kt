package dev.fanstage.android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Exercises the real MainActivity and its production FakeRoomRepository on an emulator. */
@RunWith(AndroidJUnit4::class)
class RoomNavigationSmokeTest {
    @get:Rule val composeRule = createAndroidComposeRule<MainActivity>()

    @Test fun roomListOpensDetailAndReturns() {
        composeRule.waitUntil(20_000L) {
            composeRule.onAllNodesWithTag("room_studio-session")
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithTag("rooms_content").assertExists()
        composeRule.onNodeWithTag("room_studio-session").performClick()
        composeRule.onNodeWithTag("room_detail").assertIsDisplayed()
        composeRule.onNodeWithTag("room_back").performClick()
        composeRule.onNodeWithTag("room_studio-session").assertIsDisplayed()
    }
}
