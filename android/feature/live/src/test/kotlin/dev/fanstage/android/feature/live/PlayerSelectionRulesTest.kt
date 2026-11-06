package dev.fanstage.android.feature.live

import org.junit.Assert.*
import org.junit.Test

class PlayerSelectionRulesTest {
    @Test fun overrideRequiresCurrentGroupAndSupportedIndices() {
        assertTrue(overrideIsValid(2, listOf(1)) { true })
        assertFalse(overrideIsValid(null, listOf(1)) { true })
        assertFalse(overrideIsValid(1, listOf(1)) { true })
        assertFalse(overrideIsValid(2, listOf(-1)) { true })
        assertFalse(overrideIsValid(2, listOf(0, 1)) { it == 0 })
    }

    @Test fun subtitleStatusFollowsSelectedTracks() {
        val off = listOf(PlayerTrackOption("a", "English"), PlayerTrackOption("b", "한국어"))
        assertTrue(subtitlesAreOff(emptyList()))
        assertTrue(subtitlesAreOff(off))
        assertFalse(subtitlesAreOff(off.map { it.copy(selected = it.id == "b") }))
    }

    @Test fun speedChangesRequireSupportedValueAvailableCommandAndLivePlayer() {
        listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f).forEach {
            assertTrue(speedChangeAllowed(it, commandAvailable = true, released = false))
        }
        assertFalse(speedChangeAllowed(1.1f, commandAvailable = true, released = false))
        assertFalse(speedChangeAllowed(1.5f, commandAvailable = false, released = false))
        assertFalse(speedChangeAllowed(1.5f, commandAvailable = true, released = true))
        assertFalse(speedChangeAllowed(Float.NaN, commandAvailable = true, released = false))
    }
}
