package dev.fanstage.android.feature.messages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.fanstage.android.core.artwork.artworkResource
import dev.fanstage.android.core.designsystem.CardTwoLineText
import dev.fanstage.android.core.model.*

@Composable
fun MessagesListScreen(
    state: MessagesUiState,
    artists: List<FictionalArtist>,
    onOpen: (String) -> Unit,
) {
    val conversations = state.conversations
    LazyColumn(
        Modifier.fillMaxSize().testTag("messages_list"),
        contentPadding = PaddingValues(start = 20.dp, top = 5.dp, end = 20.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(stringResource(R.string.messages), style = MaterialTheme.typography.headlineMedium)
            Text(stringResource(R.string.local_demo))
        }
        items(conversations, key = { it.artistId }) { conversation ->
            val artist = artists.firstOrNull { it.id == conversation.artistId }
            if (artist != null)
                Card(
                    onClick = { onOpen(artist.id) },
                    modifier = Modifier.fillMaxWidth().testTag("conversation_${artist.id}"),
                    colors =
                        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painterResource(artworkResource(artist.cover)),
                            null,
                            Modifier.size(64.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop,
                        )
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            CardTwoLineText(
                                artist.name,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            CardTwoLineText(
                                conversation.messages
                                    .lastOrNull()
                                    ?.let {
                                        if (it.id == 0L && !it.fromMe)
                                            stringResource(R.string.demo_welcome)
                                        else it.text
                                    }
                                    .orEmpty(),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Box(
                                Modifier.height(
                                    with(androidx.compose.ui.platform.LocalDensity.current) {
                                        MaterialTheme.typography.labelSmall.lineHeight.toDp()
                                    }
                                )
                            ) {
                                if (conversation.unread)
                                    Text(
                                        stringResource(R.string.unread),
                                        modifier = Modifier.testTag("unread_${artist.id}"),
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                            }
                        }
                    }
                }
        }
    }
}
