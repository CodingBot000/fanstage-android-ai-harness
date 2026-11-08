package dev.fanstage.android.feature.live

import android.content.Context
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.TrackGroup
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.hls.HlsManifest
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import android.net.Uri
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class Media3PlayerController(context: Context, uri: String) : PlayerController {
    private val appContext = context.applicationContext
    private val mutableState = MutableStateFlow<PlaybackState>(PlaybackState.Loading)
    override val state = mutableState.asStateFlow()
    private val mutableUiState = MutableStateFlow(PlayerUiState())
    override val uiState = mutableUiState.asStateFlow()
    internal data class SurfaceFrame(val generation: Long = 0, val rendered: Boolean = false)
    private val mutableSurfaceFrame = MutableStateFlow(SurfaceFrame())
    internal val surfaceFrame = mutableSurfaceFrame.asStateFlow()
    private var attachedView: androidx.media3.ui.PlayerView? = null
    private val player: ExoPlayer = ExoPlayer.Builder(context.applicationContext).build()
    private var released = false
    private val groupIds = TrackGroupIds<TrackGroup>()
    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            updateTracks(player)
            mutableState.value = when {
                player.playerError != null -> PlaybackState.Error
                player.playbackState == Player.STATE_BUFFERING -> PlaybackState.Buffering
                player.playbackState == Player.STATE_IDLE -> PlaybackState.Loading
                player.isPlaying -> PlaybackState.Playing
                else -> PlaybackState.Paused
            }
            mutableUiState.value = mutableUiState.value.copy(playback = mutableState.value)
            if (PlayerRotationTrace.enabled) PlayerRotationTrace.emit("event=player_events controller=${System.identityHashCode(this@Media3PlayerController)} player=${System.identityHashCode(player)} playback=${player.playbackState} ready=${player.playWhenReady} playing=${player.isPlaying} error=${player.playerError?.errorCodeName ?: "none"} flow=${mutableState.value.javaClass.simpleName}")
        }
        override fun onRenderedFirstFrame() {
            if (PlayerRotationTrace.enabled) PlayerRotationTrace.emit("event=first_frame controller=${System.identityHashCode(this@Media3PlayerController)} player=${System.identityHashCode(player)} generation=${mutableSurfaceFrame.value.generation} ${attachedView?.let(::surfaceInfo) ?: "surfaceType=none surface=0 available=false"}")
            if (attachedView != null) mutableSurfaceFrame.value = mutableSurfaceFrame.value.copy(rendered = true)
        }
        override fun onPlayerError(error: PlaybackException) {
            if (PlayerRotationTrace.enabled) PlayerRotationTrace.emit("event=player_error controller=${System.identityHashCode(this@Media3PlayerController)} player=${System.identityHashCode(player)} error=${error.errorCodeName}")
            mutableState.value = PlaybackState.Error
        }
    }

    init {
        val source = if (Uri.parse(uri).path?.endsWith(".mp4", ignoreCase = true) == true) {
            ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context.applicationContext))
                .createMediaSource(MediaItem.fromUri(uri))
        } else HlsMediaSource.Factory(DefaultDataSource.Factory(context.applicationContext))
            .setLoadErrorHandlingPolicy(object : DefaultLoadErrorHandlingPolicy(2) {
                override fun getRetryDelayMsFor(loadErrorInfo: androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy.LoadErrorInfo): Long =
                    if (loadErrorInfo.errorCount > 2) C.TIME_UNSET else super.getRetryDelayMsFor(loadErrorInfo)
            })
            .createMediaSource(MediaItem.fromUri(uri))
        player.repeatMode = Player.REPEAT_MODE_ONE
        player.addListener(listener)
        if (PlayerRotationTrace.enabled) PlayerRotationTrace.emit("event=listener_registered controller=${System.identityHashCode(this)} player=${System.identityHashCode(player)} listener=${System.identityHashCode(listener)}")
        player.setMediaSource(source)
        player.prepare()
    }

    override fun setSpeed(value: Float) {
        if (speedChangeAllowed(value, !released && player.isCommandAvailable(Player.COMMAND_SET_SPEED_AND_PITCH), released)) {
            player.setPlaybackSpeed(value)
        }
    }

    private fun updateTracks(player: Player) {
        val groups = player.currentTracks.groups
        val params = player.trackSelectionParameters
        val validGroups = groups.map { it.mediaTrackGroup }.toSet()
        groupIds.reconcile(validGroups)
        val staleTypes = listOf(C.TRACK_TYPE_VIDEO, C.TRACK_TYPE_TEXT).filter { type ->
            params.overrides.values.any { override ->
                if (override.type != type) false else {
                    val group = groups.firstOrNull { it.mediaTrackGroup == override.mediaTrackGroup }
                    !overrideIsValid(group?.length, override.trackIndices) { index -> group?.isTrackSupported(index) == true }
                }
            }
        }
        if (staleTypes.isNotEmpty() && player.isCommandAvailable(Player.COMMAND_SET_TRACK_SELECTION_PARAMETERS)) {
            val builder = params.buildUpon()
            staleTypes.forEach { builder.clearOverridesOfType(it) }
            player.trackSelectionParameters = builder.build()
        }
        val hlsManifest = player.currentManifest as? HlsManifest
        val undeclaredCaptions = hlsManifest != null && hlsManifest.multivariantPlaylist.muxedCaptionFormats == null
        fun options(type: Int): List<PlayerTrackOption> = groups.flatMap { group ->
            if (group.type != type) emptyList() else (0 until group.length).filter { group.isTrackSupported(it) }.map { index ->
                val format = group.getTrackFormat(index)
                val label = if (type == C.TRACK_TYPE_VIDEO) {
                    if (format.height > 0) "${format.height}p" + (if (format.bitrate > 0) " · ${format.bitrate / 1000} kbps" else "") else format.label ?: appContext.getString(R.string.player_video_track, index + 1)
                } else if (undeclaredCaptions && format.sampleMimeType == MimeTypes.APPLICATION_CEA608) {
                    appContext.getString(R.string.player_subtitle_cea_unknown)
                } else format.label ?: format.language ?: appContext.getString(R.string.player_subtitle_track, index + 1)
                PlayerTrackOption("${groupIds.id(group.mediaTrackGroup)}:$index", label, group.isTrackSelected(index))
            }
        }
        val video = options(C.TRACK_TYPE_VIDEO)
        val text = options(C.TRACK_TYPE_TEXT)
        val videoOverride = params.overrides.values.firstOrNull { it.type == C.TRACK_TYPE_VIDEO && it.mediaTrackGroup in validGroups }
        val selectedVideo = videoOverride?.let { override ->
            override.trackIndices.singleOrNull()?.let { "${groupIds.id(override.mediaTrackGroup)}:$it" }
        }?.takeIf { id -> video.any { it.id == id } }
        mutableUiState.value = PlayerUiState(
            playback = mutableState.value,
            speed = player.playbackParameters.speed,
            canSetSpeed = player.isCommandAvailable(Player.COMMAND_SET_SPEED_AND_PITCH),
            canSelectTracks = player.isCommandAvailable(Player.COMMAND_SET_TRACK_SELECTION_PARAMETERS),
            videoTracks = video, selectedVideoId = selectedVideo, textTracks = text,
            subtitlesOff = subtitlesAreOff(text),
        )
    }

    private fun selectTrack(type: Int, id: String?) {
        if (released || !player.isCommandAvailable(Player.COMMAND_SET_TRACK_SELECTION_PARAMETERS)) return
        val builder = player.trackSelectionParameters.buildUpon().clearOverridesOfType(type)
        if (type == C.TRACK_TYPE_TEXT) builder.setTrackTypeDisabled(type, id == null)
        if (id != null) {
            val parts = id.split(':')
            val key = parts.getOrNull(0)?.toLongOrNull() ?: return
            val group = player.currentTracks.groups.firstOrNull { groupIds.id(it.mediaTrackGroup) == key && it.type == type } ?: return
            val index = parts.getOrNull(1)?.toIntOrNull() ?: return
            if (group.type != type || index !in 0 until group.length || !group.isTrackSupported(index)) return
            builder.setOverrideForType(TrackSelectionOverride(group.mediaTrackGroup, index))
        }
        player.trackSelectionParameters = builder.build()
    }
    override fun selectVideo(id: String?) = selectTrack(C.TRACK_TYPE_VIDEO, id)
    override fun selectSubtitle(id: String?) = selectTrack(C.TRACK_TYPE_TEXT, id)

    private fun surfaceInfo(view: androidx.media3.ui.PlayerView): String {
        val surface = view.videoSurfaceView
        val available = when (surface) {
            is android.view.TextureView -> surface.isAvailable
            is android.view.SurfaceView -> surface.holder.surface.isValid
            else -> false
        }
        return "surfaceType=${surface?.javaClass?.simpleName ?: "none"} surface=${surface?.let(System::identityHashCode) ?: 0} available=$available"
    }

    internal fun attach(view: androidx.media3.ui.PlayerView): Long {
        if (attachedView !== view) {
            if (PlayerRotationTrace.enabled) PlayerRotationTrace.emit("event=surface_replace controller=${System.identityHashCode(this)} player=${System.identityHashCode(player)} oldView=${attachedView?.let(System::identityHashCode) ?: 0} newView=${System.identityHashCode(view)}")
            attachedView?.player = null
            attachedView = view
            mutableSurfaceFrame.value = SurfaceFrame(mutableSurfaceFrame.value.generation + 1)
            view.player = player
            if (PlayerRotationTrace.enabled) PlayerRotationTrace.emit("event=surface_attached controller=${System.identityHashCode(this)} player=${System.identityHashCode(player)} view=${System.identityHashCode(view)} generation=${mutableSurfaceFrame.value.generation} attached=${view.isAttachedToWindow} ${surfaceInfo(view)}")
        }
        return mutableSurfaceFrame.value.generation
    }
    internal fun detach(view: androidx.media3.ui.PlayerView) {
        if (PlayerRotationTrace.enabled) PlayerRotationTrace.emit("event=surface_detached controller=${System.identityHashCode(this)} player=${System.identityHashCode(player)} view=${System.identityHashCode(view)} current=${attachedView === view} ${surfaceInfo(view)}")
        if (attachedView === view) {
            attachedView = null
            mutableSurfaceFrame.value = mutableSurfaceFrame.value.copy(rendered = false)
        }
        view.player = null
    }
    internal fun positionSnapshot(): Triple<Boolean, Long, Long> = Triple(
        player.isCurrentMediaItemSeekable && player.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM),
        player.currentPosition.coerceAtLeast(0), player.duration.coerceAtLeast(0),
    )
    internal fun seek(position: Long) { if (!released && player.isCommandAvailable(Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)) player.seekTo(position) }

    override fun play() {
        if (released) return
        if (player.playbackState == Player.STATE_ENDED) player.seekTo(0)
        player.play()
    }
    override fun pause() { if (!released) player.pause() }
    override fun retry() {
        if (released) return
        mutableState.value = PlaybackState.Loading
        mutableSurfaceFrame.value = mutableSurfaceFrame.value.copy(rendered = false)
        player.prepare()
        player.play()
    }
    override fun release() {
        if (released) return
        released = true
        attachedView = null
        mutableSurfaceFrame.value = mutableSurfaceFrame.value.copy(rendered = false)
        player.removeListener(listener)
        if (PlayerRotationTrace.enabled) PlayerRotationTrace.emit("event=listener_removed controller=${System.identityHashCode(this)} player=${System.identityHashCode(player)} listener=${System.identityHashCode(listener)}")
        player.clearVideoSurface()
        player.release()
        if (PlayerRotationTrace.enabled) PlayerRotationTrace.emit("event=player_released controller=${System.identityHashCode(this)} player=${System.identityHashCode(player)}")
    }
}

internal val PLAYBACK_SPEEDS = listOf(0.5f, 0.75f, 1f, 1.25f, 1.5f, 2f)
