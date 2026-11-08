package dev.fanstage.android.feature.live

import android.os.SystemClock
import android.util.Log

/** Opt-in, bounded, non-content diagnostic events for rotation instrumentation. */
object PlayerRotationTrace {
    val supported: Boolean get() = true
    private val events = ArrayDeque<String>()
    private var dropped = 0
    @Volatile private var currentRun: String? = null
    val enabled: Boolean get() = currentRun != null

    @Synchronized fun start(runId: String) {
        require(runId.matches(Regex("[A-Za-z0-9_-]{1,40}")))
        events.clear()
        dropped = 0
        currentRun = runId
    }

    @Synchronized fun emit(message: String) {
        val run = currentRun ?: return
        val event = "run=$run ns=${SystemClock.elapsedRealtimeNanos()} $message"
        if (events.size == 1024) { events.removeFirst(); dropped++ }
        events.addLast(event)
        Log.i("FSRotationAudit", event)
    }

    @Synchronized fun snapshot(): List<String> = listOf("event=trace_summary dropped=$dropped") + events.toList()
    @Synchronized fun stop() { currentRun = null; events.clear() }
}
