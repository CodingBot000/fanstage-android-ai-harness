package dev.fanstage.android

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import dev.fanstage.android.feature.live.PlayerRotationTrace
import org.junit.Rule
import org.junit.Test

/** Intentionally copied into androidTest only by the isolated negative-check script. */
class RotationAuditFailureProbe {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun controlledFailureCarriesRotationEvents() = RotationAudit(rule).run {
        rule.waitUntil(20_000) { rule.onAllNodesWithTag("rooms_content").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("rooms_content").performScrollToNode(hasTestTag("live_carousel"))
        rule.onNodeWithTag("live_carousel").performScrollToNode(hasTestTag("room_benchmark"))
        rule.onNodeWithTag("room_benchmark").performClick()
        rule.waitUntil(30_000) { rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().isNotEmpty() }
        rule.onNodeWithTag("player_fullscreen").performClick()
        try {
            rule.activityRule.scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
            rule.waitUntil(20_000) { rule.activity.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE }
            rule.waitUntil(10_000) { rule.onAllNodesWithTag("player_state_paused").fetchSemanticsNodes().isNotEmpty() }
            failureCheckpoint("probe_before_cleanup")
            PlayerRotationTrace.emit("event=controlled_failure_probe")
            throw AssertionError("intentional rotation diagnostic probe")
        } finally {
            rule.activityRule.scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
            rule.waitUntil(20_000) { rule.activity.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT }
        }
    }
}
