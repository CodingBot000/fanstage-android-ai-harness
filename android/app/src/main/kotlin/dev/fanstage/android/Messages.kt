package dev.fanstage.android

import androidx.compose.foundation.Image
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.distinctUntilChanged

data class LocalMessage(val id: Long, val text: String, val fromMe: Boolean)
data class LocalConversation(val artistId: String, val messages: List<LocalMessage>, val unread: Boolean = true)
interface MessageRepository { fun initial(): List<LocalConversation> }
class FakeMessageRepository : MessageRepository {
    override fun initial() = fictionalArtists.map { LocalConversation(it.id, listOf(LocalMessage(0, "Welcome to our space!", false))) }
}
class MessagesViewModel(repository: MessageRepository = FakeMessageRepository()) : ViewModel() {
    private val mutableState = MutableStateFlow(repository.initial())
    val state = mutableState.asStateFlow()
    fun markRead(artistId: String) { mutableState.value = mutableState.value.map { if (it.artistId == artistId) it.copy(unread = false) else it } }
    fun send(artistId: String, text: String) {
        val value = text.trim(); if (value.isEmpty()) return
        mutableState.value = mutableState.value.map { conversation ->
            if (conversation.artistId == artistId) conversation.copy(messages = conversation.messages + LocalMessage((conversation.messages.maxOfOrNull { it.id } ?: 0) + 1, value, true)) else conversation
        }
    }
}
@Composable fun MessagesList(viewModel: MessagesViewModel, onOpen: (String) -> Unit) {
    val conversations by viewModel.state.collectAsStateWithLifecycle()
    LazyColumn(Modifier.fillMaxSize().testTag("messages_list"), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Text(stringResource(R.string.messages), style = MaterialTheme.typography.headlineMedium); Text(stringResource(R.string.local_demo)) }
        items(conversations, key = { it.artistId }) { conversation ->
            val artist = fictionalArtists.firstOrNull { it.id == conversation.artistId }
            if (artist != null) Card(onClick = { onOpen(artist.id) }, modifier = Modifier.fillMaxWidth().testTag("conversation_${artist.id}"), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Image(painterResource(artist.cover), null, Modifier.size(64.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                    Spacer(Modifier.width(14.dp)); Column { Text(artist.name, style = MaterialTheme.typography.titleMedium); Text(conversation.messages.lastOrNull()?.let { if (it.id == 0L && !it.fromMe) stringResource(R.string.demo_welcome) else it.text }.orEmpty(), maxLines = 1); if (conversation.unread) Text(stringResource(R.string.unread), modifier = Modifier.testTag("unread_${artist.id}"), style = MaterialTheme.typography.labelSmall) }
                }
            }
        }
    }
}
@Composable fun ConversationScreen(viewModel: MessagesViewModel, artistId: String, onBack: () -> Unit) {
    val conversations by viewModel.state.collectAsStateWithLifecycle()
    val conversation = conversations.firstOrNull { it.artistId == artistId }
    val artist = fictionalArtists.firstOrNull { it.id == artistId }
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
        TextButton(onClick = onBack) { Text(stringResource(R.string.messages_back)) }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (artist != null) Image(painterResource(artist.cover), null, Modifier.size(52.dp).clip(CircleShape), contentScale = ContentScale.Crop)
            Column {
                Text(artist?.name.orEmpty(), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.local_demo), style = MaterialTheme.typography.bodySmall)
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(value = draft, onValueChange = { draft = it }, modifier = Modifier.weight(1f).testTag("dm_input"), label = { Text(stringResource(R.string.message_input)) })
            Button(onClick = { viewModel.send(artistId, draft); draft = "" }, modifier = Modifier.testTag("dm_send")) { Text(stringResource(R.string.send_comment)) }
        }
    }
}
