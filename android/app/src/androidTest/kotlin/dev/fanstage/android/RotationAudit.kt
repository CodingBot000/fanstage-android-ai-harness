package dev.fanstage.android

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.TextureView
import android.view.SurfaceView
import android.view.inspector.WindowInspector
import androidx.media3.ui.PlayerView
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

    private fun playerSnapshot(name: String) {
        if (!PlayerRotationTrace.enabled) return
        try {
            rule.runOnUiThread {
                val roots = if (Build.VERSION.SDK_INT >= 29) WindowInspector.getGlobalWindowViews()
                    else listOf(rule.activity.window.decorView)
                val found = mutableListOf<PlayerView>()
                fun scan(view: View) {
                    if (view is PlayerView) found.add(view)
                    if (view is ViewGroup) for (index in 0 until view.childCount) scan(view.getChildAt(index))
                }
                roots.forEach(::scan)
                if (found.isEmpty()) PlayerRotationTrace.emit("event=player_snapshot name=$name views=0")
                found.distinctBy(System::identityHashCode).forEach { view ->
                    val player = view.player
                    val surface = view.videoSurfaceView
                    val available = when (surface) {
                        is TextureView -> surface.isAvailable
                        is SurfaceView -> surface.holder.surface.isValid
                        else -> false
                    }
                    PlayerRotationTrace.emit("event=player_snapshot name=$name view=${System.identityHashCode(view)} attached=${view.isAttachedToWindow} player=${player?.let(System::identityHashCode) ?: 0} playback=${player?.playbackState ?: -1} ready=${player?.playWhenReady ?: false} playing=${player?.isPlaying ?: false} error=${player?.playerError?.errorCodeName ?: "none"} surfaceType=${surface?.javaClass?.simpleName ?: "none"} surface=${surface?.let(System::identityHashCode) ?: 0} available=$available")
                }
            }
        } catch (error: Exception) {
            PlayerRotationTrace.emit("event=player_snapshot_failed name=$name type=${error.javaClass.simpleName}")
        }
    }

    fun checkpoint(name: String) {
        if (!PlayerRotationTrace.enabled) return
        playerSnapshot(name)
        val states = listOf("playing", "paused", "loading", "buffering", "error").joinToString(",") { state ->
            "$state:${rule.onAllNodesWithTag("player_state_$state").fetchSemanticsNodes().size}"
        }
        val frame = rule.onAllNodesWithTag("player_first_frame").fetchSemanticsNodes().size
        val fullscreen = rule.onAllNodesWithTag("player_fullscreen").fetchSemanticsNodes().size
        PlayerRotationTrace.emit("event=ui_checkpoint name=$name states=$states frame=$frame fullscreenControl=$fullscreen")
    }

    fun failureCheckpoint(name: String) {
        runCatching { checkpoint(name) }.onFailure { issue ->
            PlayerRotationTrace.emit("event=checkpoint_failed name=$name type=${issue.javaClass.simpleName}")
        }
    }

    fun run(block: RotationAudit.() -> Unit) {
        val supplied = InstrumentationRegistry.getArguments().getString("fanRotationRunId")
        val runId = supplied ?: "rot-${UUID.randomUUID().toString().take(12)}"
        val app = rule.activity.application
        PlayerRotationTrace.start(runId)
        var registered = false
        var primaryFailure: Throwable? = null
        try {
            rule.runOnUiThread { app.registerActivityLifecycleCallbacks(callbacks); registered = true }
            PlayerRotationTrace.emit("event=test_start activity=${System.identityHashCode(rule.activity)} orientation=${rule.activity.resources.configuration.orientation}")
            checkpoint("start")
            block()
            checkpoint("complete")
        } catch (error: Throwable) {
            primaryFailure = error
            failureCheckpoint("failure_after_cleanup")
            val events = PlayerRotationTrace.snapshot()
            val details = if (events.isEmpty()) "rotation diagnostics unavailable for this build variant" else events.joinToString("\n")
            error.addSuppressed(AssertionError("rotation diagnostic events:\n$details"))
            throw error
        } finally {
            var cleanupFailure: Throwable? = null
            try {
                if (registered) rule.runOnUiThread { app.unregisterActivityLifecycleCallbacks(callbacks) }
            } catch (error: Throwable) {
                cleanupFailure = error
            }
            try {
                PlayerRotationTrace.stop()
            } catch (error: Throwable) {
                if (cleanupFailure == null) cleanupFailure = error else cleanupFailure.addSuppressed(error)
            }
            if (cleanupFailure != null) {
                if (primaryFailure != null) primaryFailure.addSuppressed(cleanupFailure) else throw cleanupFailure
            }
        }
    }
}
