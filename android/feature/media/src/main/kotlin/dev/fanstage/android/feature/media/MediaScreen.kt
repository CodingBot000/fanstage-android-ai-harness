package dev.fanstage.android.feature.media

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.fanstage.android.core.artwork.artworkResource
import dev.fanstage.android.core.designsystem.CardTwoLineText
import dev.fanstage.android.core.model.ArtworkKey
import dev.fanstage.android.core.model.DemoVideo
import dev.fanstage.android.core.model.DiscoverContent
import dev.fanstage.android.core.model.DiscoverData

@Composable
fun MediaScreen(
    state: MediaState,
    filter: Int,
    onFilterSelected: (Int) -> Unit,
    onRetry: () -> Unit,
    onLive: (String) -> Unit,
    onReplay: () -> Unit,
    videos: List<DemoVideo> = emptyList(),
    onVideo: (String) -> Unit = {},
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp).testTag("media_screen")) {
        Spacer(Modifier.height(5.dp))
        Text(stringResource(R.string.media), style = MaterialTheme.typography.headlineMedium)
        MediaFilterRow(filter, onFilterSelected)
        when (val current = state) {
            MediaState.Loading -> CircularProgressIndicator()
            MediaState.Error ->
                Button(onClick = onRetry) { Text(stringResource(R.string.discover_retry)) }
            is MediaState.Ready ->
                LazyVerticalGrid(
                    modifier = Modifier.weight(1f).testTag("media_grid"),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (filter != 2)
                        items(current.data.content, key = { it.id }) { content ->
                            LiveMediaCard(content, current.data, videos, onLive)
                        }
                    if (filter != 1)
                        items(videos, key = { it.id }) { video ->
                            LocalVideoCard(video, current.data, onVideo)
                        }
                    item { ReplayCard(onReplay) }
                }
        }
    }
}

@Composable
private fun mediaContentTitle(content: DiscoverContent): String =
    when (content.id) {
        "studio-session" -> stringResource(R.string.room_studio_title)
        "sketch-break" -> stringResource(R.string.room_sketch_title)
        "tabletop-night" -> stringResource(R.string.room_tabletop_title)
        else -> content.title
    }

@Composable
private fun MediaFilterRow(filter: Int, onFilterSelected: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(stringResource(R.string.all_media), "LIVE", stringResource(R.string.replay))
            .forEachIndexed { index, label ->
                FilterChip(
                    selected = filter == index,
                    onClick = { onFilterSelected(index) },
                    label = { Text(label) },
                    modifier = Modifier.testTag("media_filter_$index"),
                )
            }
    }
}

@Composable
private fun LiveMediaCard(
    content: DiscoverContent,
    data: DiscoverData,
    videos: List<DemoVideo>,
    onLive: (String) -> Unit,
) {
    Card(onClick = { onLive(content.id) }, modifier = Modifier.testTag("media_${content.id}")) {
        Column {
            Image(
                painterResource(
                    artworkResource(
                        data.artists.firstOrNull { it.id == content.artistId }?.feed
                            ?: content.artwork
                    )
                ),
                null,
                Modifier.fillMaxWidth().aspectRatio(16f / 10f),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
            )
            Column(Modifier.padding(12.dp)) {
                CardTwoLineText(
                    if (videos.isNotEmpty()) stringResource(R.string.local_vod)
                    else stringResource(R.string.demo_live),
                    style =
                        MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.primary
                        ),
                )
                CardTwoLineText(
                    mediaContentTitle(content),
                    style = MaterialTheme.typography.titleMedium,
                )
                CardTwoLineText(content.artist, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun LocalVideoCard(video: DemoVideo, data: DiscoverData, onVideo: (String) -> Unit) {
    Card(onClick = { onVideo(video.id) }, modifier = Modifier.testTag("media_${video.id}")) {
        Column {
            Image(
                painterResource(
                    artworkResource(data.artists.first { it.id == video.artistId }.feed)
                ),
                null,
                Modifier.fillMaxWidth().aspectRatio(16f / 10f),
                contentScale = ContentScale.Crop,
            )
            Column(Modifier.padding(12.dp)) {
                CardTwoLineText(
                    "Local VOD",
                    style =
                        MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.primary
                        ),
                )
                CardTwoLineText(video.title, style = MaterialTheme.typography.titleMedium)
                CardTwoLineText(
                    data.artists.firstOrNull { it.id == video.artistId }?.name.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun ReplayCard(onReplay: () -> Unit) {
    Card(onClick = onReplay, modifier = Modifier.testTag("media_replay")) {
        Column {
            Image(
                painterResource(artworkResource(ArtworkKey.PAPER_FEED)),
                null,
                Modifier.fillMaxWidth().aspectRatio(16f / 10f),
                contentScale = ContentScale.Crop,
                alignment = Alignment.TopCenter,
            )
            Column(Modifier.padding(12.dp)) {
                CardTwoLineText(
                    stringResource(R.string.benchmark_card),
                    style =
                        MaterialTheme.typography.labelLarge.copy(
                            color = MaterialTheme.colorScheme.primary
                        ),
                )
                CardTwoLineText("Synthetic HLS", style = MaterialTheme.typography.titleMedium)
                CardTwoLineText("Paper Lantern", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
