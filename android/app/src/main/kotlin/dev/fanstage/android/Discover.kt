package dev.fanstage.android

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.fanstage.android.core.designsystem.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FictionalArtist(val id: String, val name: String, val artwork: Int)
data class DiscoverContent(val id: String, val title: String, val artist: String, val artistId: String = id, val artwork: Int = R.drawable.discover_hero)
val fictionalArtists = listOf(
    FictionalArtist("paper-lantern", "Paper Lantern", R.drawable.discover_hero),
    FictionalArtist("blue-pencil", "Blue Pencil", R.drawable.blue_pencil),
    FictionalArtist("dice-garden", "Dice Garden", R.drawable.dice_garden),
)
data class DiscoverData(val artists: List<FictionalArtist>, val content: List<DiscoverContent>)
interface DiscoverRepository { suspend fun load(): DiscoverData }
class FakeDiscoverRepository : DiscoverRepository {
    override suspend fun load() = DiscoverData(fictionalArtists, listOf(DiscoverContent("studio-session", "Studio session", "Paper Lantern", "paper-lantern"), DiscoverContent("sketch-break", "Afternoon sketch break", "Blue Pencil", "blue-pencil", R.drawable.blue_pencil), DiscoverContent("tabletop-night", "Tabletop night", "Dice Garden", "dice-garden", R.drawable.dice_garden)))
}
sealed interface DiscoverState {
    data object Loading : DiscoverState
    data object Error : DiscoverState
    data class Ready(val data: DiscoverData) : DiscoverState
}
class DiscoverViewModel(private val repository: DiscoverRepository = FakeDiscoverRepository()) : ViewModel() {
    private val mutableState = MutableStateFlow<DiscoverState>(DiscoverState.Loading)
    val state = mutableState.asStateFlow()
    private var loadJob: Job? = null
    init { retry() }
    fun retry() { loadJob?.cancel(); loadJob = viewModelScope.launch { mutableState.value = DiscoverState.Loading; try { val content = repository.load(); currentCoroutineContext().ensureActive(); mutableState.value = DiscoverState.Ready(content) } catch (e: CancellationException) { throw e } catch (_: Exception) { mutableState.value = DiscoverState.Error } } }
}
@Composable fun DiscoverRoute(viewModel: DiscoverViewModel, onRoomClick: (String) -> Unit, onReplayClick: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    DiscoverScreen(state, viewModel::retry, onRoomClick, onReplayClick)
}
@Composable fun DiscoverScreen(state: DiscoverState, onRetry: () -> Unit, onRoomClick: (String) -> Unit, onReplayClick: () -> Unit = {}) {
    Scaffold(containerColor = Color(0xFFFAF8FC)) { padding ->
        when (state) {
            DiscoverState.Loading -> Box(Modifier.fillMaxSize().padding(padding).testTag("discover_loading"), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            DiscoverState.Error -> Column(Modifier.fillMaxSize().padding(padding).testTag("discover_error"), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) { Text(stringResource(R.string.discover_error)); Button(onClick = onRetry) { Text(stringResource(R.string.discover_retry)) } }
            is DiscoverState.Ready -> LazyColumn(Modifier.fillMaxSize().padding(padding).testTag("rooms_content"), verticalArrangement = Arrangement.spacedBy(22.dp)) {
                item { Row(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 18.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text("fanstage", style = MaterialTheme.typography.headlineMedium, color = Plum); Text("✦", color = Plum) } }
                item { Column(Modifier.padding(horizontal = 20.dp)) { Text(stringResource(R.string.discover_eyebrow), color = Plum, style = MaterialTheme.typography.labelLarge); Spacer(Modifier.height(5.dp)); Text(stringResource(R.string.discover_heading), style = MaterialTheme.typography.headlineMedium) } }
                item {
                    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.data.content, key = { it.id }) { content ->
                            Box(Modifier.width(320.dp).height(220.dp).clip(RoundedCornerShape(24.dp)).clickable { onRoomClick(content.id) }) {
                                Image(painterResource(content.artwork), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xE91B1029)))))
                                Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                                    Text(stringResource(R.string.featured), color = Color(0xFFFFD5E7), style = MaterialTheme.typography.labelMedium)
                                    Text(content.artist, color = Color.White, style = MaterialTheme.typography.headlineMedium)
                                    Text(content.title, color = Color.White)
                                }
                            }
                        }
                    }
                }
                item { Column { SectionTitle(stringResource(R.string.communities)); LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) { items(state.data.artists, key = { it.id }) { artist -> Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(80.dp)) { Image(painterResource(artist.artwork), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(64.dp).clip(CircleShape)); Spacer(Modifier.height(7.dp)); Text(artist.name, style = MaterialTheme.typography.labelSmall, maxLines = 2) } } } } }
                item { SectionTitle(stringResource(R.string.live_now)) }
                items(state.data.content, key = { it.id }) { content -> Card(onClick = { onRoomClick(content.id) }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).testTag("room_${content.id}"), colors = CardDefaults.cardColors(containerColor = Color.White)) { Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) { Image(painterResource(content.artwork), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.size(76.dp).clip(RoundedCornerShape(14.dp))); Spacer(Modifier.width(14.dp)); Column { Text("LIVE", color = Color(0xFFD64B78), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold); Text(content.title, style = MaterialTheme.typography.titleMedium); Text(content.artist, style = MaterialTheme.typography.bodySmall) } } } }
                item { Column(Modifier.padding(horizontal = 20.dp)) {
                    Text(stringResource(R.string.replay), style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    Card(onClick = onReplayClick, modifier = Modifier.testTag("replay_card"), colors = CardDefaults.cardColors(containerColor = Color(0xFFECE5F6))) {
                        Column(Modifier.fillMaxWidth().padding(18.dp)) { Text("REPLAY", color = Plum, style = MaterialTheme.typography.labelMedium); Text("Paper Lantern · Studio diary", style = MaterialTheme.typography.titleMedium); Text(stringResource(R.string.replay_play), style = MaterialTheme.typography.bodySmall) }
                    }
                    Spacer(Modifier.height(24.dp))
                    Text(stringResource(R.string.recommended), style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(12.dp))
                    Text(stringResource(R.string.recommendation), style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(12.dp))
                } }
                item {
                    LazyRow(contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.data.content, key = { it.id }) { content ->
                            Card(onClick = { onRoomClick(content.id) }, modifier = Modifier.width(180.dp)) {
                                Column {
                                    Image(painterResource(content.artwork), contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxWidth().height(110.dp))
                                    Column(Modifier.padding(12.dp)) { Text(content.title, style = MaterialTheme.typography.titleMedium); Text(content.artist, style = MaterialTheme.typography.bodySmall) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
@Composable private fun SectionTitle(text: String) { Text(text, Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp), style = MaterialTheme.typography.titleLarge) }
@Preview(showBackground = true) @Composable private fun DiscoverReadyPreview() { FanStageTheme { DiscoverScreen(DiscoverState.Ready(DiscoverData(fictionalArtists, listOf(DiscoverContent("studio-session", "Studio session", "Paper Lantern")))), {}, {}) } }
@Preview(showBackground = true) @Composable private fun DiscoverLoadingPreview() { FanStageTheme { DiscoverScreen(DiscoverState.Loading, {}, {}) } }
@Preview(showBackground = true) @Composable private fun DiscoverErrorPreview() { FanStageTheme { DiscoverScreen(DiscoverState.Error, {}, {}) } }
