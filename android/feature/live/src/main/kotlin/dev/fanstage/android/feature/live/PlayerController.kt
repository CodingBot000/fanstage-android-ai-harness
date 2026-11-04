package dev.fanstage.android.feature.live

import kotlinx.coroutines.flow.StateFlow

sealed interface PlaybackState {
    data object Loading : PlaybackState
    data object Buffering : PlaybackState
    data object Playing : PlaybackState
    data object Paused : PlaybackState
    data object Error : PlaybackState
}

/** Screen-owned playback contract; never place a player in a ViewModel. */
interface PlayerController {
    val state: StateFlow<PlaybackState>
    fun play()
    fun pause()
    fun retry()
    fun release()
}

/** Lifecycle decisions kept independent of Android and the playback implementation. */
class PlaybackSession(private val player: PlayerController, initiallyAutoPlay: Boolean = true) {
    private var autoPlay = initiallyAutoPlay
    private var released = false

    fun onResume() {
        if (!released && autoPlay) {
            autoPlay = false
            player.play()
        }
    }

    fun onPause() {
        if (!released) {
            autoPlay = false
            player.pause()
        }
    }

    fun release() {
        if (!released) {
            released = true
            player.release()
        }
    }
}
