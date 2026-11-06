package dev.fanstage.android.feature.discover

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.fanstage.android.core.artwork.artworkResource
import dev.fanstage.android.core.designsystem.*
import dev.fanstage.android.core.model.*

@Composable
fun DiscoverScreen(
    state: DiscoverState,
    onRetry: () -> Unit,
    onRoomClick: (String) -> Unit,
    onReplayClick: () -> Unit = {},
    onArtistClick: (String) -> Unit = {},
    localVideos: Boolean = false,
    onBenchmarkClick: () -> Unit = onReplayClick,
) {
    Scaffold(containerColor = Color(0xFFFAF8FC)) { padding ->
        when (state) {
            DiscoverState.Loading ->
                Box(
                    Modifier.fillMaxSize().padding(padding).testTag("discover_loading"),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            DiscoverState.Error ->
                Column(
                    Modifier.fillMaxSize().padding(padding).testTag("discover_error"),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(stringResource(R.string.discover_error))
                    Button(onClick = onRetry) { Text(stringResource(R.string.discover_retry)) }
                }
            is DiscoverState.Ready ->
                LazyColumn(
                    Modifier.fillMaxSize().padding(padding).testTag("rooms_content"),
                    verticalArrangement = Arrangement.spacedBy(22.dp),
                ) {
                    item { DiscoverHeading() }
                    item { FeaturedHeroCarousel(state.data, onRoomClick) }
                    item { CommunityAvatarRow(state.data, onArtistClick) }
                    item { SectionTitle(stringResource(R.string.live_now)) }
                    item { LiveCarousel(state.data, localVideos, onRoomClick) }
                    item { ReplaySection(localVideos, onReplayClick, onBenchmarkClick) }
                    item { RecommendedContentRow(state.data, onRoomClick) }
                }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        Modifier.padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
        style = MaterialTheme.typography.titleLarge,
    )
}

@Preview(showBackground = true)
@Composable
private fun DiscoverReadyPreview() {
    FanStageTheme {
        DiscoverScreen(
            DiscoverState.Ready(
                DiscoverData(
                    emptyList(),
                    listOf(DiscoverContent("studio-session", "Studio session", "Paper Lantern")),
                )
            ),
            {},
            {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DiscoverLoadingPreview() {
    FanStageTheme { DiscoverScreen(DiscoverState.Loading, {}, {}) }
}

@Preview(showBackground = true)
@Composable
private fun DiscoverErrorPreview() {
    FanStageTheme { DiscoverScreen(DiscoverState.Error, {}, {}) }
}

@Composable
fun localizedContentTitle(content: DiscoverContent): String =
    when (content.id) {
        "studio-session" -> stringResource(R.string.room_studio_title)
        "sketch-break" -> stringResource(R.string.room_sketch_title)
        "tabletop-night" -> stringResource(R.string.room_tabletop_title)
        else -> content.title
    }

@Composable
private fun DiscoverHeading() {

    Column(Modifier.padding(horizontal = 20.dp).padding(top = 5.dp)) {
        Text(
            stringResource(R.string.discover_eyebrow),
            color = Plum,
            style = MaterialTheme.typography.labelLarge,
        )
        Spacer(Modifier.height(5.dp))
        Text(
            stringResource(R.string.discover_heading),
            style = MaterialTheme.typography.headlineMedium,
        )
    }
}

@Composable
private fun FeaturedHeroCarousel(data: DiscoverData, onRoomClick: (String) -> Unit) {

    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(data.content, key = { it.id }) { content ->
            Box(
                Modifier.fillParentMaxWidth(0.9f)
                    .aspectRatio(16f / 9f)
                    .clip(RoundedCornerShape(24.dp))
                    .clickable { onRoomClick(content.id) }
            ) {
                Image(
                    painterResource(artworkResource(content.artwork)),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(
                    Modifier.fillMaxSize()
                        .background(
                            Brush.verticalGradient(listOf(Color.Transparent, Color(0xE91B1029)))
                        )
                )
                Column(Modifier.align(Alignment.BottomStart).padding(20.dp)) {
                    Text(
                        stringResource(R.string.featured),
                        color = Color(0xFFFFD5E7),
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        content.artist,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        localizedContentTitle(content),
                        color = Color.White,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun CommunityAvatarRow(data: DiscoverData, onArtistClick: (String) -> Unit) {

    Column {
        SectionTitle(stringResource(R.string.communities))
        LazyRow(
            contentPadding = PaddingValues(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(data.artists, key = { it.id }) { artist ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier =
                        Modifier.width(80.dp)
                            .clickable { onArtistClick(artist.id) }
                            .testTag("discover_artist_${artist.id}"),
                ) {
                    Image(
                        painterResource(artworkResource(artist.artwork)),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(64.dp).clip(CircleShape),
                    )
                    Spacer(Modifier.height(7.dp))
                    Text(
                        artist.name,
                        style = MaterialTheme.typography.labelSmall,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveCarousel(data: DiscoverData, localVideos: Boolean, onRoomClick: (String) -> Unit) {

    LazyRow(
        modifier = Modifier.testTag("live_carousel"),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(data.content, key = { it.id }) { content ->
            Card(
                onClick = { onRoomClick(content.id) },
                modifier = Modifier.width(190.dp).testTag("room_${content.id}"),
                colors = CardDefaults.cardColors(containerColor = Color.White),
            ) {
                Column {
                    Image(
                        painterResource(artworkResource(content.artwork)),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().aspectRatio(16f / 10f),
                    )
                    Column(
                        Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        CardTwoLineText(
                            if (localVideos) stringResource(R.string.local_vod)
                            else stringResource(R.string.demo_live),
                            style =
                                MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFFD64B78),
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                        CardTwoLineText(
                            localizedContentTitle(content),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        CardTwoLineText(content.artist, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        item {
            Card(
                onClick = { onRoomClick("benchmark") },
                modifier = Modifier.width(190.dp).testTag("room_benchmark"),
            ) {
                Column {
                    Image(
                        painterResource(artworkResource(ArtworkKey.PAPER_HERO)),
                        null,
                        Modifier.fillMaxWidth().aspectRatio(16f / 10f),
                        contentScale = ContentScale.Crop,
                    )
                    Column(
                        Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        CardTwoLineText(
                            stringResource(R.string.benchmark_card),
                            style =
                                MaterialTheme.typography.labelMedium.copy(
                                    color = Color(0xFFD64B78),
                                    fontWeight = FontWeight.Bold,
                                ),
                        )
                        CardTwoLineText(
                            "Synthetic HLS",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        CardTwoLineText("FanStage", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun ReplaySection(
    localVideos: Boolean,
    onReplayClick: () -> Unit,
    onBenchmarkClick: () -> Unit,
) {

    Column(Modifier.padding(horizontal = 20.dp)) {
        Text(stringResource(R.string.replay), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Card(
            onClick = onReplayClick,
            modifier = Modifier.testTag("replay_card"),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFECE5F6)),
        ) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Text(
                    if (localVideos) stringResource(R.string.local_vod) else "REPLAY",
                    color = Plum,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text("Paper Lantern · Studio diary", style = MaterialTheme.typography.titleMedium)
                Text(
                    stringResource(R.string.replay_play),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        if (localVideos) {
            Spacer(Modifier.height(12.dp))
            Card(
                onClick = onBenchmarkClick,
                modifier = Modifier.fillMaxWidth().testTag("benchmark_replay"),
            ) {
                Text(stringResource(R.string.benchmark_card), Modifier.padding(18.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
        Text(stringResource(R.string.recommended), style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        Text(stringResource(R.string.recommendation), style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun RecommendedContentRow(data: DiscoverData, onRoomClick: (String) -> Unit) {

    LazyRow(
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(data.content, key = { it.id }) { content ->
            Card(onClick = { onRoomClick(content.id) }, modifier = Modifier.width(180.dp)) {
                Column {
                    Image(
                        painterResource(artworkResource(content.artwork)),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(110.dp),
                    )
                    Column(Modifier.padding(12.dp)) {
                        CardTwoLineText(
                            localizedContentTitle(content),
                            style = MaterialTheme.typography.titleMedium,
                        )
                        CardTwoLineText(content.artist, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
