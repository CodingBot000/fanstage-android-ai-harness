package dev.fanstage.android

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.lifecycle.LifecycleOwner
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.platform.app.InstrumentationRegistry
import dev.fanstage.android.feature.live.PlayerRotationTrace
import java.util.UUID

/** Captures only synthetic test state; no URLs, messages, credentials, or view contents. */
internal class RotationAudit(
    private val rule: AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>,
) {
    private val callbacks = object : Application.ActivityLifecycleCallbacks {
        private fun event(name: String, activity: Activity) {
            if (activity is MainActivity && PlayerRotationTrace.enabled) {
                val state = (activity as LifecycleOwner).lifecycle.currentState
                PlayerRotationTrace.emit("event=activity_$name activity=${System.identityHashCode(activity)} orientation=${activity.resources.configuration.orientation} lifecycle=$state")
            }
        }
        override fun onActivityCreated(activity: Activity, state: Bundle?) = event("created", activity)
        override fun onActivityStarted(activity: Activity) = event("started", activity)
        override fun onActivityResumed(activity: Activity) = event("resumed", activity)
        override fun onActivityPaused(activity: Activity) = event("paused", activity)
        override fun onActivityStopped(activity: Activity) = event("stopped", activity)
        override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) = event("save_state", activity)
        override fun onActivityDestroyed(activity: Activity) = event("destroyed", activity)
    }

    fun checkpoint(name: String) {
        val states = listOf("playing", "paused", "loading", "buffering", "error").joinToString(",") { state ->
            "$state:${rule.onAllNodesWithTag("player_state_$state").fetchSemanticsNodes().size}"
        }
        val frame = rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().size
        val fullscreen = rule.onAllNodesWithTag("player_fullscreen").fetchSemanticsNodes().size
        PlayerRotationTrace.emit("event=ui_checkpoint name=$name states=$states frame=$frame fullscreenControl=$fullscreen")
    }

    fun run(block: RotationAudit.() -> Unit) {
        val supplied = InstrumentationRegistry.getArguments().getString("fanRotationRunId")
        val runId = supplied ?: "rot-${UUID.randomUUID().toString().take(12)}"
        PlayerRotationTrace.start(runId)
        val app = rule.activity.application
        rule.runOnUiThread { app.registerActivityLifecycleCallbacks(callbacks) }
        PlayerRotationTrace.emit("event=test_start activity=${System.identityHashCode(rule.activity)} orientation=${rule.activity.resources.configuration.orientation}")
        try {
            checkpoint("start")
            block()
            checkpoint("complete")
        } catch (error: Throwable) {
            runCatching { checkpoint("failure") }
            error.addSuppressed(AssertionError("rotation diagnostic events:\n" + PlayerRotationTrace.snapshot().joinToString("\n")))
            throw error
        } finally {
            rule.runOnUiThread { app.unregisterActivityLifecycleCallbacks(callbacks) }
            PlayerRotationTrace.stop()
        }
    }
}
