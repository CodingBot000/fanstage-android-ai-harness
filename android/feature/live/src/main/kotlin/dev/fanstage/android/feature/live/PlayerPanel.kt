package dev.fanstage.android.feature.live

import android.view.LayoutInflater
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView

val PlayerSurfaceGeneration = SemanticsPropertyKey<Long>("PlayerSurfaceGeneration")
var SemanticsPropertyReceiver.playerSurfaceGeneration by PlayerSurfaceGeneration

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun PlayerPanel(roomId: String, sourceUri: String = "asset:///hls/captionless.m3u8", compact: Boolean = false, settingsAtEnd: Boolean = false) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var autoPlay by rememberSaveable(roomId, sourceUri) { mutableStateOf(true) }
    val controller = remember(roomId, sourceUri) { Media3PlayerController(context, sourceUri) }
    val session = remember(controller) { PlaybackSession(controller, autoPlay) }
    val playerUi by controller.uiState.collectAsStateWithLifecycle()
    var showSpeed by remember(controller) { mutableStateOf(false) }
    var showSpeedOptions by remember(controller) { mutableStateOf(false) }
    var fullscreen by rememberSaveable(roomId, sourceUri) { mutableStateOf(false) }
    val ui = playerUi.copy(fullscreen = fullscreen)
    val state = ui.playback
    var playbackControlsVisible by remember(controller) { mutableStateOf(false) }
    var controlInteraction by remember(controller) { mutableLongStateOf(0L) }
    SideEffect {
        if (PlayerRotationTrace.enabled) PlayerRotationTrace.emit("event=compose_recompose controller=${System.identityHashCode(controller)} state=${state.javaClass.simpleName} fullscreen=$fullscreen compact=$compact pausedTag=${!compact && state == PlaybackState.Paused}")
    }
    LaunchedEffect(state) {
        if (PlayerRotationTrace.enabled) PlayerRotationTrace.emit("event=compose_state_observed controller=${System.identityHashCode(controller)} state=${state.javaClass.simpleName}")
        when (state) {
            PlaybackState.Paused -> playbackControlsVisible = true
            else -> Unit
        }
    }
    LaunchedEffect(state, playbackControlsVisible, controlInteraction) {
        if (state == PlaybackState.Playing && playbackControlsVisible) {
            delay(2_000)
            playbackControlsVisible = false
        }
    }
    val speed = ui.speed
    val canSetSpeed = ui.canSetSpeed
    val frame by controller.surfaceFrame.collectAsStateWithLifecycle()
    var position by remember(controller) { mutableStateOf(0L) }
    var duration by remember(controller) { mutableStateOf(0L) }
    var seekable by remember(controller) { mutableStateOf(false) }
    LaunchedEffect(controller, owner) {
        owner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                val snapshot = controller.positionSnapshot()
                seekable = snapshot.first
                position = snapshot.second
                duration = snapshot.third
                delay(500)
            }
        }
    }
    val playerDescription = stringResource(R.string.player_description)
    val seekDescription = stringResource(R.string.player_seek)

    DisposableEffect(owner, session) {
        val observer = LifecycleEventObserver { _, event ->
            if (PlayerRotationTrace.enabled) PlayerRotationTrace.emit("event=owner_lifecycle controller=${System.identityHashCode(controller)} lifecycle=$event")
            when (event) {
                Lifecycle.Event.ON_RESUME -> { session.onResume(); autoPlay = false }
                Lifecycle.Event.ON_PAUSE -> { session.onPause(); autoPlay = false }
                else -> Unit
            }
        }
        owner.lifecycle.addObserver(observer)
        if (owner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            session.onResume()
            autoPlay = false
        }
        onDispose {
            if (PlayerRotationTrace.enabled) PlayerRotationTrace.emit("event=compose_dispose controller=${System.identityHashCode(controller)}")
            owner.lifecycle.removeObserver(observer)
            session.release()
        }
    }

    val playerContent: @Composable (Boolean) -> Unit = { full ->
    var surfaceId by remember(full) { mutableLongStateOf(-1L) }
    Column((if (full) Modifier.fillMaxSize().background(Color.Black).windowInsetsPadding(WindowInsets.safeDrawing) else Modifier.fillMaxWidth().background(Color.Black)).testTag("player_panel")) {
        Box(if (full) Modifier.fillMaxWidth().weight(1f) else Modifier.fillMaxWidth().height(200.dp)) {
            AndroidView(
                factory = { context -> (LayoutInflater.from(context).inflate(R.layout.fanstage_player_view, null, false) as PlayerView).apply { surfaceId = controller.attach(this) } },
                update = { surfaceId = controller.attach(it) },
                onRelease = { controller.detach(it) },
                modifier = Modifier.fillMaxSize().semantics { contentDescription = playerDescription; playerSurfaceGeneration = surfaceId }.testTag(if (surfaceId >= 0 && frame.generation == surfaceId && frame.rendered) "player_first_frame" else "player_surface"),
            )
            Box(Modifier.fillMaxSize().clickable { playbackControlsVisible = true; controlInteraction++ }.testTag("player_video_touch"))
            TextButton(onClick = { fullscreen = !fullscreen }, modifier = Modifier.align(Alignment.BottomEnd).clip(CircleShape).background(Color(0xB31C2327)).testTag("player_fullscreen")) {
                Text(stringResource(if (full) R.string.player_exit_fullscreen else R.string.player_enter_fullscreen), color = Color.White)
            }
            when (state) {
                PlaybackState.Playing, PlaybackState.Paused -> if (playbackControlsVisible) {
                    val playing = state == PlaybackState.Playing
                    IconButton(
                        onClick = { controlInteraction++; if (playing) controller.pause() else controller.play() },
                        modifier = Modifier.align(Alignment.Center).size(56.dp).clip(CircleShape)
                            .background(Color(0xB31C2327)).testTag(if (playing) "player_pause" else "player_play"),
                    ) {
                        Icon(
                            painterResource(if (playing) R.drawable.ic_player_pause else R.drawable.ic_player_play),
                            contentDescription = stringResource(if (playing) R.string.player_pause else R.string.player_play),
                            tint = Color.White,
                            modifier = Modifier.size(28.dp),
                        )
                    }
                }
                else -> Unit
            }
        }
        if (!compact) {
        if (seekable && duration > 0) {
            val seekColors = SliderDefaults.colors(
                thumbColor = Color(0xFFE76559),
                activeTrackColor = Color(0xFFE76559),
                inactiveTrackColor = Color(0xFFB8C0C1),
            )
            Slider(
                value = (position.toFloat() / duration).coerceIn(0f, 1f),
                onValueChange = { controller.seek((it * duration).toLong()); position = (it * duration).toLong() },
                modifier = Modifier.fillMaxWidth().height(28.dp).semantics { contentDescription = seekDescription }.testTag("player_seek"),
                colors = seekColors,
                thumb = { Box(Modifier.size(12.dp).clip(CircleShape).background(Color(0xFFE76559))) },
                track = { sliderState -> SliderDefaults.Track(sliderState = sliderState, modifier = Modifier.height(3.dp), colors = seekColors, thumbTrackGapSize = 0.dp, drawStopIndicator = null) },
            )
        }
        TextButton(onClick = { showSpeedOptions = false; showSpeed = true }, enabled = canSetSpeed, colors = if (full) ButtonDefaults.textButtonColors(contentColor = Color.White, disabledContentColor = Color.LightGray) else ButtonDefaults.textButtonColors(), contentPadding = if (settingsAtEnd) PaddingValues(horizontal = 12.dp, vertical = 4.dp) else PaddingValues(horizontal = 12.dp), modifier = (if (settingsAtEnd) Modifier.align(Alignment.End) else Modifier).testTag("player_speed")) {
            Text(stringResource(R.string.player_speed, speed))
        }
        if (state == PlaybackState.Playing || state == PlaybackState.Paused) {
            Box(Modifier.size(0.dp).testTag("player_state_${state.javaClass.simpleName.lowercase()}"))
        } else {
            Text(
                stringResource(when (state) {
                    PlaybackState.Loading -> R.string.player_loading
                    PlaybackState.Buffering -> R.string.player_buffering
                    PlaybackState.Error -> R.string.player_error
                    else -> R.string.player_loading
                }),
                Modifier.testTag("player_state_${state.javaClass.simpleName.lowercase()}"),
                color = if (full) Color.White else Color.Unspecified,
            )
        }
        when (state) {
            PlaybackState.Error -> Button(onClick = controller::retry, modifier = Modifier.testTag("player_retry")) {
                Text(stringResource(R.string.player_retry))
            }
            else -> Unit
        }
        }
    }
    }
    if (fullscreen) {
        DisposableEffect(controller) {
            if (PlayerRotationTrace.enabled) PlayerRotationTrace.emit("event=dialog_created controller=${System.identityHashCode(controller)}")
            onDispose { if (PlayerRotationTrace.enabled) PlayerRotationTrace.emit("event=dialog_disposed controller=${System.identityHashCode(controller)}") }
        }
        Dialog(onDismissRequest = { fullscreen = false }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) { playerContent(true) }
    } else {
        playerContent(false)
    }
    if (showSpeed) {
        ModalBottomSheet(onDismissRequest = { showSpeed = false; showSpeedOptions = false }, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (showSpeedOptions) {
                    Text(stringResource(R.string.player_speed_title), style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    PLAYBACK_SPEEDS.forEach { option ->
                        TextButton(onClick = { controller.setSpeed(option); showSpeed = false; showSpeedOptions = false }, modifier = Modifier.fillMaxWidth().semantics { selected = speed == option }.testTag("player_speed_$option")) {
                            Text(stringResource(R.string.player_speed_option, option, if (speed == option) "✓" else ""))
                        }
                    }
                } else {
                    val quality = when {
                        ui.videoTracks.isEmpty() -> stringResource(R.string.player_unsupported)
                        ui.videoTracks.size == 1 -> stringResource(R.string.player_quality_single)
                        ui.selectedVideoId == null -> stringResource(R.string.player_auto)
                        else -> ui.videoTracks.firstOrNull { it.id == ui.selectedVideoId }?.label ?: stringResource(R.string.player_auto)
                    }
                    val subtitle = when {
                        ui.textTracks.isEmpty() -> stringResource(R.string.player_unsupported)
                        ui.subtitlesOff -> stringResource(R.string.player_off)
                        else -> ui.textTracks.firstOrNull { it.selected }?.label ?: stringResource(R.string.player_off)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.player_quality)); Text(quality)
                    }
                    if (ui.videoTracks.size > 1) {
                        TextButton(onClick = { controller.selectVideo(null) }, enabled = ui.canSelectTracks, modifier = Modifier.fillMaxWidth().semantics { selected = ui.selectedVideoId == null }.testTag("player_quality_auto")) { Text(stringResource(R.string.player_auto) + if (ui.selectedVideoId == null) " ✓" else "") }
                        ui.videoTracks.forEach { option ->
                            TextButton(onClick = { controller.selectVideo(option.id) }, enabled = ui.canSelectTracks, modifier = Modifier.fillMaxWidth().semantics { selected = ui.selectedVideoId == option.id }.testTag("player_quality_${option.id}")) { Text(option.label + if (ui.selectedVideoId == option.id) " ✓" else "") }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.player_subtitles)); Text(subtitle)
                    }
                    if (ui.textTracks.isNotEmpty()) {
                        TextButton(onClick = { controller.selectSubtitle(null) }, enabled = ui.canSelectTracks, modifier = Modifier.fillMaxWidth().semantics { selected = ui.subtitlesOff }.testTag("player_subtitle_off")) { Text(stringResource(R.string.player_off) + if (ui.subtitlesOff) " ✓" else "") }
                        ui.textTracks.forEach { option ->
                            TextButton(onClick = { controller.selectSubtitle(option.id) }, enabled = ui.canSelectTracks, modifier = Modifier.fillMaxWidth().semantics { selected = option.selected }.testTag("player_subtitle_${option.id}")) { Text(option.label + if (option.selected) " ✓" else "") }
                        }
                    }
                    Row(Modifier.fillMaxWidth().clickable { showSpeedOptions = true }.padding(vertical = 14.dp).testTag("player_speed_row"), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(stringResource(R.string.player_speed_title)); Text(stringResource(R.string.player_speed_option, speed, ""))
                    }
                }
            }
        }
    }

}
