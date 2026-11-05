package dev.fanstage.android.feature.live

import android.view.LayoutInflater
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.ui.PlayerView

@Composable
fun PlayerPanel(roomId: String, sourceUri: String = "asset:///hls/vod.m3u8") {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    var autoPlay by rememberSaveable(roomId, sourceUri) { mutableStateOf(true) }
    val controller = remember(roomId, sourceUri) { Media3PlayerController(context, sourceUri) }
    val session = remember(controller) { PlaybackSession(controller, autoPlay) }
    val state by controller.state.collectAsStateWithLifecycle()
    val rendered by controller.hasRenderedFrame.collectAsStateWithLifecycle()
    var position by remember(controller) { mutableStateOf(0L) }
    var duration by remember(controller) { mutableStateOf(0L) }
    val isMp4 = remember(sourceUri) { Uri.parse(sourceUri).path?.endsWith(".mp4", ignoreCase = true) == true }
    LaunchedEffect(controller, isMp4) {
        while (isMp4) {
            position = controller.player.currentPosition.coerceAtLeast(0)
            duration = controller.player.duration.coerceAtLeast(0)
            delay(500)
        }
    }
    val playerDescription = stringResource(R.string.player_description)
    val seekDescription = stringResource(R.string.player_seek)

    DisposableEffect(owner, session) {
        val observer = LifecycleEventObserver { _, event ->
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
            owner.lifecycle.removeObserver(observer)
            session.release()
        }
    }

    Column(Modifier.fillMaxWidth().testTag("player_panel")) {
        AndroidView(
            factory = { context -> (LayoutInflater.from(context).inflate(R.layout.fanstage_player_view, null, false) as PlayerView).apply { player = controller.player } },
            update = { it.player = controller.player },
            onRelease = { it.player = null },
            modifier = Modifier.fillMaxWidth().height(200.dp).semantics { contentDescription = playerDescription }.testTag(if (rendered) "player_first_frame" else "player_surface"),
        )
        if (isMp4 && duration > 0) {
            Slider(value = (position.toFloat() / duration).coerceIn(0f, 1f), onValueChange = { controller.player.seekTo((it * duration).toLong()); position = (it * duration).toLong() }, modifier = Modifier.semantics { contentDescription = seekDescription }.testTag("player_seek"))
        }
        Text(
            stringResource(when (state) {
                PlaybackState.Loading -> R.string.player_loading
                PlaybackState.Buffering -> R.string.player_buffering
                PlaybackState.Playing -> R.string.player_playing
                PlaybackState.Paused -> R.string.player_paused
                PlaybackState.Error -> R.string.player_error
            }),
            Modifier.testTag("player_state_${state.javaClass.simpleName.lowercase()}"),
        )
        when (state) {
            PlaybackState.Error -> Button(onClick = controller::retry, modifier = Modifier.testTag("player_retry")) {
                Text(stringResource(R.string.player_retry))
            }
            PlaybackState.Playing -> Button(onClick = controller::pause, modifier = Modifier.testTag("player_pause")) {
                Text(stringResource(R.string.player_pause))
            }
            PlaybackState.Paused -> Button(onClick = controller::play, modifier = Modifier.testTag("player_play")) {
                Text(stringResource(R.string.player_play))
            }
            else -> Unit
        }
    }
}
