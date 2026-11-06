package dev.fanstage.android.feature.live

/** Applies equally to video and subtitle overrides from the current track snapshot. */
internal fun overrideIsValid(trackCount: Int?, indices: List<Int>, isSupported: (Int) -> Boolean): Boolean =
    trackCount != null && indices.all { it in 0 until trackCount && isSupported(it) }

internal fun speedChangeAllowed(value: Float, commandAvailable: Boolean, released: Boolean): Boolean =
    !released && commandAvailable && value in PLAYBACK_SPEEDS

internal fun subtitlesAreOff(tracks: List<PlayerTrackOption>): Boolean = tracks.none { it.selected }
