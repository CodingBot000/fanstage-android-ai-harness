package dev.fanstage.android.feature.messages

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.fanstage.android.core.artwork.artworkResource
import dev.fanstage.android.core.designsystem.BackTitleBar
import dev.fanstage.android.core.designsystem.SendIcon
import dev.fanstage.android.core.model.FictionalArtist
import dev.fanstage.android.core.model.LocalConversation
import dev.fanstage.android.core.model.LocalMessage
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop

@Composable
fun ConversationScreen(
    conversation: LocalConversation?,
    artistId: String,
    artists: List<FictionalArtist>,
    draft: String,
    onDraftChange: (String) -> Unit,
    onAction: (MessagesUiAction) -> Unit,
    onBack: () -> Unit,
) {
    val artist = artists.firstOrNull { it.id == artistId }
    val transcript = conversation?.messages.orEmpty()
    val transcriptState = rememberLazyListState()
    LaunchedEffect(artistId, transcript.size) {
        if (transcript.isNotEmpty()) transcriptState.requestScrollToItem(transcript.lastIndex)
    }
    // Keep the tail reachable when the viewport shrinks, without pulling a user
    // who is reading older messages back to the bottom.
    LaunchedEffect(artistId, transcriptState) {
        snapshotFlow { transcriptState.layoutInfo.viewportSize.height }
            .distinctUntilChanged()
            .drop(1)
            .collect {
                val layout = transcriptState.layoutInfo
                val last = layout.visibleItemsInfo.lastOrNull()?.index ?: return@collect
                if (layout.totalItemsCount > 0 && last >= layout.totalItemsCount - 2) {
                    transcriptState.requestScrollToItem(layout.totalItemsCount - 1)
                }
            }
    }
    // With edge-to-edge enabled, keep the composer above the software keyboard.
    Column(Modifier.fillMaxSize().imePadding().padding(16.dp).testTag("conversation_detail")) {
        BackTitleBar(artist?.name.orEmpty(), onBack, backTag = "messages_back")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (artist != null)
                Image(
                    painterResource(artworkResource(artist.cover)),
                    null,
                    Modifier.size(52.dp).clip(CircleShape),
                    contentScale = ContentScale.Crop,
                )
            Column {
                Text(
                    stringResource(R.string.local_demo),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                )
            }
        }
        LazyColumn(
            Modifier.weight(1f).fillMaxWidth().testTag("conversation_transcript"),
            state = transcriptState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
        ) {
            items(transcript, key = { it.id }) { message -> ConversationBubble(message) }
        }
        ConversationComposer(artistId, draft, onDraftChange, onAction)
    }
}

@Composable
private fun ConversationBubble(message: LocalMessage) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.fromMe) Arrangement.End else Arrangement.Start,
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color =
                if (message.fromMe) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface,
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    if (message.id == 0L && !message.fromMe) stringResource(R.string.demo_welcome)
                    else message.text
                )
                if (message.fromMe)
                    Text(
                        stringResource(R.string.local_sent),
                        style = MaterialTheme.typography.labelSmall,
                    )
            }
        }
    }
}

@Composable
private fun ConversationComposer(
    artistId: String,
    draft: String,
    onDraftChange: (String) -> Unit,
    onAction: (MessagesUiAction) -> Unit,
) {
    val submit: () -> Unit = {
        onAction(MessagesUiAction.SendClicked(artistId, draft))
        onDraftChange("")
    }
    val sendDescription = stringResource(R.string.send_comment)
    OutlinedTextField(
        value = draft,
        onValueChange = onDraftChange,
        modifier = Modifier.fillMaxWidth().testTag("dm_input"),
        label = { Text(stringResource(R.string.message_input)) },
        trailingIcon = {
            IconButton(onClick = submit, modifier = Modifier.testTag("dm_send")) {
                SendIcon(sendDescription)
            }
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = { submit() }),
        maxLines = 4,
    )
}
