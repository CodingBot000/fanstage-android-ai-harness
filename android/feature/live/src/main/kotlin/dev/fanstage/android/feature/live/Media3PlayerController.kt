package dev.fanstage.android.feature.live

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class Media3PlayerController(context: Context, uri: String) : PlayerController {
    private val mutableState = MutableStateFlow<PlaybackState>(PlaybackState.Loading)
    override val state = mutableState.asStateFlow()
    internal val hasRenderedFrame = MutableStateFlow(false)
    internal val player: ExoPlayer = ExoPlayer.Builder(context.applicationContext).build()
    private var released = false
    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            mutableState.value = when {
                player.playerError != null -> PlaybackState.Error
                player.playbackState == Player.STATE_BUFFERING -> PlaybackState.Buffering
                player.playbackState == Player.STATE_IDLE -> PlaybackState.Loading
                player.isPlaying -> PlaybackState.Playing
                else -> PlaybackState.Paused
            }
        }
        override fun onRenderedFirstFrame() { hasRenderedFrame.value = true }
        override fun onPlayerError(error: PlaybackException) { mutableState.value = PlaybackState.Error }
    }

    init {
        val source = HlsMediaSource.Factory(DefaultDataSource.Factory(context.applicationContext))
            .setLoadErrorHandlingPolicy(object : DefaultLoadErrorHandlingPolicy(2) {
                override fun getRetryDelayMsFor(loadErrorInfo: androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy.LoadErrorInfo): Long =
                    if (loadErrorInfo.errorCount > 2) C.TIME_UNSET else super.getRetryDelayMsFor(loadErrorInfo)
            })
            .createMediaSource(MediaItem.fromUri(uri))
        player.repeatMode = Player.REPEAT_MODE_ONE
        player.addListener(listener)
        player.setMediaSource(source)
        player.prepare()
    }

    override fun play() {
        if (released) return
        if (player.playbackState == Player.STATE_ENDED) player.seekTo(0)
        player.play()
    }
    override fun pause() { if (!released) player.pause() }
    override fun retry() {
        if (released) return
        mutableState.value = PlaybackState.Loading
        hasRenderedFrame.value = false
        player.prepare()
        player.play()
    }
    override fun release() {
        if (released) return
        released = true
        player.removeListener(listener)
        player.clearVideoSurface()
        player.release()
    }
}
