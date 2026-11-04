package dev.fanstage.android.feature.live.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.fanstage.android.feature.live.R

@Composable
fun ChatPanel(viewModel: ChatViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var draft by rememberSaveable(state.roomId) { mutableStateOf("") }
    var tooLong by rememberSaveable(state.roomId) { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var followLatest by remember(state.roomId) { mutableStateOf(true) }
    var forceLatest by remember(state.roomId) { mutableStateOf(false) }
    LaunchedEffect(listState) {
        snapshotFlow {
            val layout = listState.layoutInfo
            val lastVisible = layout.visibleItemsInfo.lastOrNull()?.index ?: -1
            layout.totalItemsCount == 0 || lastVisible >= layout.totalItemsCount - 2
        }.collect { followLatest = it }
    }
    LaunchedEffect(state.roomId, state.messages.size, state.messages.lastOrNull()?.key) {
        if ((followLatest || forceLatest) && state.messages.isNotEmpty()) {
            listState.scrollToItem(state.messages.lastIndex)
            forceLatest = false
        }
    }
    Column(Modifier.fillMaxWidth().testTag("chat_panel")) {
        Text(stringResource(R.string.chat_title))
        LazyColumn(Modifier.fillMaxWidth().height(200.dp).testTag("chat_messages"), state = listState) {
            items(state.messages, key = { it.key }) { message ->
                Column(Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("chat_message")) {
                    Text(stringResource(if (message.isOwn) R.string.chat_you else R.string.chat_fixture))
                    Text(message.text)
                    if (message.isOwn) {
                        Text(stringResource(when (message.delivery) {
                            Delivery.PENDING -> R.string.chat_pending
                            Delivery.SENT -> R.string.chat_sent
                            Delivery.FAILED -> R.string.chat_failed
                        }))
                        if (message.delivery == Delivery.FAILED && message.clientMessageId != null) {
                            Row {
                                TextButton(onClick = { viewModel.retry(message.clientMessageId) }) { Text(stringResource(R.string.retry)) }
                                TextButton(onClick = { viewModel.dismissFailed(message.clientMessageId) }) { Text(stringResource(R.string.chat_remove_failed)) }
                            }
                        }
                    }
                }
            }
        }
        if (state.gapUnresolved) {
            Text(stringResource(R.string.chat_gap))
            TextButton(onClick = viewModel::retryGap) { Text(stringResource(R.string.retry)) }
        }
        if (state.sendBlocked) Text(stringResource(R.string.chat_capacity))
        if (tooLong || state.inputTooLong) Text(stringResource(R.string.chat_too_long))
        if (state.transportUncertain) Text(stringResource(R.string.chat_uncertain))
        Row(Modifier.fillMaxWidth()) {
            OutlinedTextField(value = draft, onValueChange = {
                if (it.chatCodePoints() <= ChatLimits.MAX_CODE_POINTS) { draft = it; tooLong = false }
                else tooLong = true
            }, modifier = Modifier.weight(1f).testTag("chat_input"), label = { Text(stringResource(R.string.chat_message_label)) })
            Button(onClick = { if (viewModel.send(draft)) { draft = ""; forceLatest = true } }, modifier = Modifier.testTag("chat_send")) { Text(stringResource(R.string.chat_send)) }
        }
    }
}
