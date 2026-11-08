package dev.fanstage.android.feature.live

import android.os.SystemClock
import android.util.Log

/** Opt-in, bounded, non-content diagnostic events for rotation instrumentation. */
object PlayerRotationTrace {
    private val events = ArrayDeque<String>()
    @Volatile private var currentRun: String? = null
    val enabled: Boolean get() = currentRun != null

    @Synchronized fun start(runId: String) {
        require(runId.matches(Regex("[A-Za-z0-9_-]{1,40}")))
        events.clear()
        currentRun = runId
    }

    @Synchronized fun emit(message: String) {
        val run = currentRun ?: return
        val event = "run=$run ns=${SystemClock.elapsedRealtimeNanos()} $message"
        if (events.size == 1024) events.removeFirst()
        events.addLast(event)
        Log.i("FSRotationAudit", event)
    }

    @Synchronized fun snapshot(): List<String> = events.toList()
    @Synchronized fun stop() { currentRun = null; events.clear() }
}
