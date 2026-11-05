package dev.fanstage.android

import java.util.Collections
import java.util.IdentityHashMap

/** Copy only trusted code locations and exception types; never retain exception messages. */
internal object RcaDiagnostics {
    class RcaFailure(message: String, cause: Throwable) : AssertionError(message) {
        init { initCause(sanitizedCause(cause)) }
    }
    private val trusted = listOf("dev.fanstage.", "android.", "androidx.", "java.", "kotlin.", "kotlinx.", "okhttp3.", "org.junit.")
    private val identifier = Regex("[A-Za-z0-9_.$<>-]{1,200}")

    fun sanitizedCause(error: Throwable): Throwable {
        val seen = Collections.newSetFromMap(IdentityHashMap<Throwable, Boolean>())
        fun copy(source: Throwable, depth: Int): Throwable {
            seen.add(source)
            val type = source.javaClass.name.let { if (trusted.any(it::startsWith) && identifier.matches(it)) it else "unlisted" }
            val safe = Throwable("type=$type")
            safe.stackTrace = source.stackTrace.filter {
                trusted.any(it.className::startsWith) && identifier.matches(it.className) && identifier.matches(it.methodName)
            }.map {
                // Drop source names: a malicious or unexpected frame can include sensitive text there.
                StackTraceElement(it.className, it.methodName, "source", it.lineNumber)
            }.toTypedArray()
            val cause = source.cause
            if (cause != null && depth < 7 && cause !in seen) safe.initCause(copy(cause, depth + 1))
            return safe
        }
        return copy(error, 0)
    }
}
