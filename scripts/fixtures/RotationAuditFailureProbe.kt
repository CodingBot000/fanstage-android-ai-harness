package dev.fanstage.android

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import dev.fanstage.android.feature.live.PlayerRotationTrace
import org.junit.Rule
import org.junit.Test

/** Intentionally copied into androidTest only by the isolated negative-check script. */
class RotationAuditFailureProbe {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun controlledFailureCarriesRotationEvents() = RotationAudit(rule).run {
        PlayerRotationTrace.emit("event=controlled_failure_probe")
        throw AssertionError("intentional rotation diagnostic probe")
    }
}
