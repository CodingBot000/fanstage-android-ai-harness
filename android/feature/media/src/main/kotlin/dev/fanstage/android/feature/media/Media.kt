package dev.fanstage.android.feature.media


import dev.fanstage.android.core.model.*
import dev.fanstage.android.core.designsystem.CardTwoLineText
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dev.fanstage.android.core.artwork.artworkResource
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

sealed interface MediaState {
    data object Loading : MediaState
    data object Error : MediaState
    data class Ready(val data: DiscoverData) : MediaState
}
class MediaViewModel(private val repository: DiscoverRepository) : ViewModel() {
    private val mutableState = MutableStateFlow<MediaState>(MediaState.Loading)
    val state = mutableState.asStateFlow()
    private var loadJob: Job? = null
    init { retry() }
    fun retry() { loadJob?.cancel(); loadJob = viewModelScope.launch { mutableState.value = MediaState.Loading; try { val data = repository.load(); currentCoroutineContext().ensureActive(); mutableState.value = MediaState.Ready(data) } catch (e: CancellationException) { throw e } catch (_: Exception) { currentCoroutineContext().ensureActive(); mutableState.value = MediaState.Error } } }
}
@Composable fun MediaRoute(viewModel: MediaViewModel, onLive: (String) -> Unit, onReplay: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MediaScreen(state = state, onRetry = viewModel::retry, onLive = onLive, onReplay = onReplay)
}
@Composable fun MediaScreen(state: MediaState, onRetry: () -> Unit, onLive: (String) -> Unit, onReplay: () -> Unit) {
    var filter by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).testTag("media_screen")) {
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.media), style = MaterialTheme.typography.headlineMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(stringResource(R.string.all_media), "LIVE", stringResource(R.string.replay)).forEachIndexed { index, label -> FilterChip(selected = filter == index, onClick = { filter = index }, label = { Text(label) }, modifier = Modifier.testTag("media_filter_$index")) }
        }
        when (val current = state) {
            MediaState.Loading -> CircularProgressIndicator()
            MediaState.Error -> Button(onClick = onRetry) { Text(stringResource(R.string.discover_retry)) }
            is MediaState.Ready -> LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (filter != 2) items(current.data.content, key = { it.id }) { content ->
                    Card(onClick = { onLive(content.id) }, modifier = Modifier.testTag("media_${content.id}")) { Column { Image(painterResource(artworkResource(current.data.artists.firstOrNull { it.id == content.artistId }?.feed ?: content.artwork)), null, Modifier.fillMaxWidth().aspectRatio(16f / 10f), contentScale = ContentScale.Crop, alignment = Alignment.TopCenter); Column(Modifier.padding(12.dp)) { CardTwoLineText(stringResource(R.string.demo_live), style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)); CardTwoLineText(mediaContentTitle(content), style = MaterialTheme.typography.titleMedium); CardTwoLineText(content.artist, style = MaterialTheme.typography.bodyMedium) } } }
                }
                if (filter != 1) item {
                    Card(onClick = onReplay, modifier = Modifier.testTag("media_replay")) { Column { Image(painterResource(artworkResource(ArtworkKey.PAPER_FEED)), null, Modifier.fillMaxWidth().aspectRatio(16f / 10f), contentScale = ContentScale.Crop, alignment = Alignment.TopCenter); Column(Modifier.padding(12.dp)) { CardTwoLineText(stringResource(R.string.replay), style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)); CardTwoLineText("Studio diary", style = MaterialTheme.typography.titleMedium); CardTwoLineText("Paper Lantern", style = MaterialTheme.typography.bodyMedium) } } }
                }
            }
        }
    }
}

@Composable private fun mediaContentTitle(content: DiscoverContent): String = when (content.id) {
    "studio-session" -> stringResource(R.string.room_studio_title)
    "sketch-break" -> stringResource(R.string.room_sketch_title)
    "tabletop-night" -> stringResource(R.string.room_tabletop_title)
    else -> content.title
}
