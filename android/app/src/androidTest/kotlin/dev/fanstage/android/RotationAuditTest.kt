package dev.fanstage.android

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.fanstage.android.feature.live.PlayerRotationTrace
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class RotationAuditTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun syntheticFailureRetainsDiagnosticEvents() {
        val error = try {
            RotationAudit(rule).run { throw IllegalStateException("synthetic diagnostic probe") }
            throw AssertionError("expected synthetic failure")
        } catch (expected: IllegalStateException) {
            expected
        }
        assertEquals("synthetic diagnostic probe", error.message)
        val stack = java.io.StringWriter().also { error.printStackTrace(java.io.PrintWriter(it)) }.toString()
        if (PlayerRotationTrace.supported) {
            assertTrue(error.suppressed.any { it.message?.contains("event=test_start") == true })
            assertTrue(stack.contains("event=ui_checkpoint name=failure_after_cleanup"))
        } else {
            assertTrue(stack.contains("rotation diagnostics unavailable"))
        }
    }
}
