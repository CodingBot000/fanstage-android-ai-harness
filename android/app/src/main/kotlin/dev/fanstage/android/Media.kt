package dev.fanstage.android

import dev.fanstage.android.core.model.ArtworkKey
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

@Composable fun MediaRoute(viewModel: DiscoverViewModel, onLive: (String) -> Unit, onReplay: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var filter by rememberSaveable { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).testTag("media_screen")) {
        Spacer(Modifier.height(20.dp))
        Text(stringResource(R.string.media), style = MaterialTheme.typography.headlineMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(stringResource(R.string.all_media), "LIVE", stringResource(R.string.replay)).forEachIndexed { index, label -> FilterChip(selected = filter == index, onClick = { filter = index }, label = { Text(label) }, modifier = Modifier.testTag("media_filter_$index")) }
        }
        when (val current = state) {
            DiscoverState.Loading -> CircularProgressIndicator()
            DiscoverState.Error -> Button(onClick = viewModel::retry) { Text(stringResource(R.string.discover_retry)) }
            is DiscoverState.Ready -> LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (filter != 2) items(current.data.content, key = { it.id }) { content ->
                    Card(onClick = { onLive(content.id) }, modifier = Modifier.testTag("media_${content.id}")) { Column { Image(painterResource(artworkResource(current.data.artists.firstOrNull { it.id == content.artistId }?.feed ?: content.artwork)), null, Modifier.fillMaxWidth().aspectRatio(16f / 10f), contentScale = ContentScale.Crop, alignment = Alignment.TopCenter); Column(Modifier.padding(12.dp)) { Text(stringResource(R.string.demo_live), color = MaterialTheme.colorScheme.primary); Text(localizedContentTitle(content), style = MaterialTheme.typography.titleMedium); Text(content.artist) } } }
                }
                if (filter != 1) item {
                    Card(onClick = onReplay, modifier = Modifier.testTag("media_replay")) { Column { Image(painterResource(artworkResource(ArtworkKey.PAPER_FEED)), null, Modifier.fillMaxWidth().aspectRatio(16f / 10f), contentScale = ContentScale.Crop, alignment = Alignment.TopCenter); Column(Modifier.padding(12.dp)) { Text("REPLAY", color = MaterialTheme.colorScheme.primary); Text("Studio diary", style = MaterialTheme.typography.titleMedium); Text("Paper Lantern") } } }
                }
            }
        }
    }
}
