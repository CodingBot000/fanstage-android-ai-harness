package dev.fanstage.android.feature.community

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.fanstage.android.core.artwork.artworkResource
import dev.fanstage.android.core.designsystem.BackTitleBar
import dev.fanstage.android.core.designsystem.CardTwoLineText
import dev.fanstage.android.core.model.CommunityPost
import dev.fanstage.android.core.model.FictionalArtist

@Composable
fun CommunityScreen(
    state: CommunityState,
    artistId: String?,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    onAction: (CommunityUiAction) -> Unit,
    onArtist: (String) -> Unit,
    onPost: (String) -> Unit,
    onBack: () -> Unit = {},
    artists: List<FictionalArtist> = emptyList(),
) {
    val artist = artists.firstOrNull { it.id == artistId }
    Scaffold(
        topBar = {
            if (artistId != null)
                BackTitleBar(
                    artist?.name ?: stringResource(R.string.community),
                    onBack,
                    backTag = "community_back",
                )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).testTag("community_feed"),
            contentPadding =
                PaddingValues(
                    start = 20.dp,
                    top = if (artistId == null) 5.dp else 20.dp,
                    end = 20.dp,
                    bottom = if (artistId == null) 100.dp else 20.dp,
                ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (artistId == null)
                item {
                    Text(
                        stringResource(R.string.community),
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
            if (artistId != null) {
                if (artist != null)
                    item { CommunityArtistHeader(artist, selectedTab, onTabSelected) }
            }
            if (artistId == null)
                items(artists, key = { it.id }) { artist ->
                    Card(
                        onClick = { onArtist(artist.id) },
                        modifier = Modifier.fillMaxWidth().testTag("artist_${artist.id}"),
                    ) {
                        Row(Modifier.padding(12.dp)) {
                            Image(
                                painterResource(artworkResource(artist.artwork)),
                                null,
                                Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop,
                            )
                            Spacer(Modifier.width(16.dp))
                            CardTwoLineText(
                                artist.name,
                                style = MaterialTheme.typography.titleLarge,
                            )
                        }
                    }
                }
            when (state) {
                CommunityState.Loading ->
                    item { CircularProgressIndicator(Modifier.testTag("community_loading")) }
                CommunityState.Error ->
                    item {
                        Text(
                            stringResource(R.string.community_error),
                            Modifier.testTag("community_error"),
                        )
                        Button(onClick = { onAction(CommunityUiAction.RetryClicked) }) {
                            Text(stringResource(R.string.discover_retry))
                        }
                    }
                is CommunityState.Ready -> {
                    val posts =
                        state.posts.filter {
                            (artistId == null || it.artistId == artistId) && selectedTab == 0
                        }
                    if (artistId != null && selectedTab == 1)
                        item {
                            Card(Modifier.fillMaxWidth().testTag("community_notice")) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(
                                        stringResource(R.string.notice_title),
                                        style = MaterialTheme.typography.titleMedium,
                                    )
                                    Text(stringResource(R.string.notice_body))
                                }
                            }
                        }
                    else if (posts.isEmpty())
                        item {
                            Text(
                                stringResource(R.string.community_empty),
                                Modifier.testTag("community_empty"),
                            )
                        }
                    items(posts, key = { it.id }) { post -> CommunityPostCard(post, onPost) }
                }
            }
        }
    }
}

@Composable
private fun CommunityArtistHeader(
    artist: FictionalArtist,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(24.dp))) {
            Image(
                painterResource(artworkResource(artist.cover)),
                null,
                Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            Box(
                Modifier.fillMaxSize()
                    .background(
                        Brush.verticalGradient(listOf(Color.Transparent, Color(0xD921152C)))
                    )
            )
            Row(
                Modifier.align(Alignment.BottomStart).padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painterResource(artworkResource(artist.artwork)),
                    null,
                    Modifier.size(60.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        artist.name,
                        color = Color.White,
                        style = MaterialTheme.typography.headlineMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        stringResource(R.string.community),
                        color = Color.White,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                text = { Text(stringResource(R.string.posts)) },
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                text = { Text(stringResource(R.string.notices)) },
            )
        }
    }
}

@Composable
private fun CommunityPostCard(post: CommunityPost, onPost: (String) -> Unit) {
    Card(
        onClick = { onPost(post.id) },
        modifier = Modifier.fillMaxWidth().testTag("post_${post.id}"),
    ) {
        Column {
            Image(
                painterResource(artworkResource(post.image)),
                null,
                Modifier.fillMaxWidth().aspectRatio(1.25f),
                contentScale = ContentScale.Crop,
            )
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                CardTwoLineText(post.title, style = MaterialTheme.typography.titleLarge)
                Text(
                    stringResource(R.string.community),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelMedium,
                )
                CardTwoLineText(
                    if (post.body == "A little moment from our world.")
                        stringResource(R.string.demo_post_body)
                    else post.body,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}
