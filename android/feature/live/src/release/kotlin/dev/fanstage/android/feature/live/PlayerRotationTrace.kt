package dev.fanstage.android.feature.live

/** Release variants never enable or retain rotation diagnostics. */
object PlayerRotationTrace {
    val supported: Boolean get() = false
    val enabled: Boolean get() = false
    fun start(runId: String) = Unit
    fun emit(message: String) = Unit
    fun snapshot(): List<String> = emptyList()
    fun stop() = Unit
}
