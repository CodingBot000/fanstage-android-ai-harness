package dev.fanstage.android.feature.live

/** Release variants never enable or retain rotation diagnostics. */
object PlayerRotationTrace {
    val enabled: Boolean get() = false
    fun emit(message: String) = Unit
}
