package dev.fanstage.android.feature.messages

import androidx.compose.foundation.Image
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.fanstage.android.core.designsystem.BackTitleBar
import dev.fanstage.android.core.designsystem.CardTwoLineText
import dev.fanstage.android.core.designsystem.SendIcon
import dev.fanstage.android.core.model.*
import dev.fanstage.android.core.artwork.artworkResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.distinctUntilChanged

class MessagesViewModel(private val repository: MessageRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(repository.snapshot())
    val state = mutableState.asStateFlow()
    fun markRead(artistId: String) { mutableState.value = repository.markRead(artistId) }
    fun send(artistId: String, text: String) { mutableState.value = repository.send(artistId, text) }
}
@Composable fun MessagesList(viewModel: MessagesViewModel, artists: List<FictionalArtist>, onOpen: (String) -> Unit) {
    val conversations by viewModel.state.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize().testTag("messages_list"), contentPadding = PaddingValues(start = 20.dp, top = 5.dp, end = 20.dp, bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text(stringResource(R.string.messages), style = MaterialTheme.typography.headlineMedium); Text(stringResource(R.string.local_demo)) }
        items(conversations, key = { it.artistId }) { conversation ->
            val artist = artists.firstOrNull { it.id == conversation.artistId }
            if (artist != null) Card(onClick = { onOpen(artist.id) }, modifier = Modifier.fillMaxWidth().testTag("conversation_${artist.id}"), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(artworkResource(artist.cover)), null, Modifier.size(64.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(14.dp)); Column(Modifier.weight(1f)) { CardTwoLineText(artist.name, style = MaterialTheme.typography.titleMedium); CardTwoLineText(conversation.messages.lastOrNull()?.let { if (it.id == 0L && !it.fromMe) stringResource(R.string.demo_welcome) else it.text }.orEmpty(), style = MaterialTheme.typography.bodyMedium); Box(Modifier.height(with(androidx.compose.ui.platform.LocalDensity.current) { MaterialTheme.typography.labelSmall.lineHeight.toDp() })) { if (conversation.unread) Text(stringResource(R.string.unread), modifier = Modifier.testTag("unread_${artist.id}"), style = MaterialTheme.typography.labelSmall) } }
                }
            }
        }
    }
}
@Composable fun ConversationScreen(viewModel: MessagesViewModel, artistId: String, artists: List<FictionalArtist>, onBack: () -> Unit) {
    val conversations by viewModel.state.collectAsStateWithLifecycle()
    val conversation = conversations.firstOrNull { it.artistId == artistId }
    val artist = artists.firstOrNull { it.id == artistId }
    var draft by remember(artistId) { mutableStateOf("") }
    val transcript = conversation?.messages.orEmpty()
    val transcriptState = rememberLazyListState()
    LaunchedEffect(artistId) { viewModel.markRead(artistId) }
    LaunchedEffect(artistId, transcript.size) {
        if (transcript.isNotEmpty()) transcriptState.scrollToItem(transcript.lastIndex)
    }
    // Keep the tail reachable when the viewport shrinks, without pulling a user
    // who is reading older messages back to the bottom.
    LaunchedEffect(artistId, transcriptState) {
        snapshotFlow { transcriptState.layoutInfo.viewportSize.height }.distinctUntilChanged().drop(1).collect {
            val layout = transcriptState.layoutInfo
            val last = layout.visibleItemsInfo.lastOrNull()?.index ?: return@collect
            if (layout.totalItemsCount > 0 && last >= layout.totalItemsCount - 2) {
                transcriptState.scrollToItem(layout.totalItemsCount - 1)
            }
        }
    }
    // With edge-to-edge enabled, keep the composer above the software keyboard.
    Column(Modifier.fillMaxSize().imePadding().padding(16.dp).testTag("conversation_detail")) {
        BackTitleBar(artist?.name.orEmpty(), onBack, backTag = "messages_back")
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (artist != null) Image(painterResource(artworkResource(artist.cover)), null, Modifier.size(52.dp).clip(CircleShape), contentScale = ContentScale.Crop)
            Column {
                Text(stringResource(R.string.local_demo), style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
            }
        }
        LazyColumn(Modifier.weight(1f).fillMaxWidth(), state = transcriptState, verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 16.dp)) {
            items(transcript, key = { it.id }) { message ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (message.fromMe) Arrangement.End else Arrangement.Start) {
                    Surface(shape = RoundedCornerShape(20.dp), color = if (message.fromMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface) {
                        Column(Modifier.padding(12.dp)) { Text(if (message.id == 0L && !message.fromMe) stringResource(R.string.demo_welcome) else message.text); if (message.fromMe) Text(stringResource(R.string.local_sent), style = MaterialTheme.typography.labelSmall) }
                    }
                }
            }
        }
        val submit: () -> Unit = { viewModel.send(artistId, draft); draft = "" }
        val sendDescription = stringResource(R.string.send_comment)
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            modifier = Modifier.fillMaxWidth().testTag("dm_input"),
            label = { Text(stringResource(R.string.message_input)) },
            trailingIcon = { IconButton(onClick = submit, modifier = Modifier.testTag("dm_send")) { SendIcon(sendDescription) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { submit() }),
            maxLines = 4,
        )
    }
}
