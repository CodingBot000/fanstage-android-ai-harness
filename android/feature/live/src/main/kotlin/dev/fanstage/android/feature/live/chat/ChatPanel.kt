package dev.fanstage.android.feature.live.chat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.IconButton
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import dev.fanstage.android.core.designsystem.SendIcon
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.fanstage.android.feature.live.R
import java.util.TimeZone

/** Only used for messages produced by the in-process fictional transport. */
internal fun fictionalMessageResource(key: String): Int? = when {
    key == "server:fixture-1" -> R.string.fake_welcome
    key.startsWith("server:fixture-") -> R.string.fake_arrival
    else -> null
}

@Composable
fun ChatPanel(viewModel: ChatViewModel, developmentControls: Boolean = false) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val locale = LocalConfiguration.current.locales[0]
    var endpoint by remember { mutableStateOf("http://10.0.2.2:18765") }
    var token by remember { mutableStateOf("") }
    var configError by remember { mutableStateOf(false) }
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
        if (developmentControls) {
            Text(stringResource(R.string.chat_dev_warning))
            Text(stringResource(if (state.localServer) R.string.chat_mode_real else R.string.chat_mode_fake), Modifier.testTag("chat_mode"))
            OutlinedTextField(value = endpoint, onValueChange = { endpoint = it }, label = { Text(stringResource(R.string.chat_endpoint)) }, modifier = Modifier.fillMaxWidth().testTag("chat_endpoint"))
            OutlinedTextField(value = token, onValueChange = { token = it }, label = { Text(stringResource(R.string.chat_token)) }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth().testTag("chat_token"))
            Column {
                TextButton(onClick = {
                    val transport = runCatching { RealChatTransport(endpoint, token, allowInsecure = true) }.getOrNull()
                    configError = transport == null
                    if (transport != null && viewModel.replaceTransport(transport)) token = ""
                }, modifier = Modifier.testTag("chat_use_real")) { Text(stringResource(R.string.chat_use_real)) }
                TextButton(onClick = { if (viewModel.useOfflineFake()) { token = ""; configError = false } }, modifier = Modifier.testTag("chat_use_fake")) { Text(stringResource(R.string.chat_use_fake)) }
            }
            if (configError) Text(stringResource(R.string.chat_config_error))
            if (state.switchBlocked) Text(stringResource(R.string.chat_switch_blocked))
        }
        LazyColumn(Modifier.fillMaxWidth().height(200.dp).testTag("chat_messages"), state = listState) {
            items(state.messages, key = { it.key }) { message ->
                Column(Modifier.fillMaxWidth().padding(vertical = 4.dp).testTag("chat_message")) {
                    Text(stringResource(if (message.isOwn) R.string.chat_you else R.string.chat_fixture))
                    val fixtureResource = if (state.fictionalFixtures && !message.isOwn) fictionalMessageResource(message.key) else null
                    val displayText = if (fixtureResource != null) stringResource(fixtureResource) else message.text
                    Text(displayText)
                    formatChatTimestamp(message.sentAt, locale, TimeZone.getDefault())?.let {
                        Text(stringResource(R.string.chat_timestamp, it), Modifier.testTag("chat_timestamp"))
                    }
                    if (message.isOwn) {
                        Text(stringResource(when (message.delivery) {
                            Delivery.PENDING -> R.string.chat_pending
                            Delivery.SENT -> R.string.chat_sent
                            Delivery.FAILED -> R.string.chat_failed
                        }))
                        if ((message.delivery == Delivery.FAILED || message.isUncertain) && message.clientMessageId != null) {
                            Column {
                                TextButton(onClick = { viewModel.retry(message.clientMessageId) }) { Text(stringResource(R.string.chat_retry_message)) }
                                if (message.delivery == Delivery.FAILED) {
                                    TextButton(onClick = { viewModel.dismissFailed(message.clientMessageId) }) { Text(stringResource(R.string.chat_remove_failed)) }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (state.gapUnresolved) {
            Text(stringResource(R.string.chat_gap))
            TextButton(onClick = viewModel::retryGap) { Text(stringResource(R.string.chat_retry_gap)) }
        }
        if (state.localServer && state.connectionStatus != null) {
            Text(stringResource(when (state.connectionStatus) {
                ChatConnectionStatus.CONNECTING -> R.string.chat_connecting
                ChatConnectionStatus.CONNECTED -> R.string.chat_connected
                ChatConnectionStatus.RECONNECTING -> R.string.chat_reconnecting
                ChatConnectionStatus.DISCONNECTED -> R.string.chat_disconnected
                null -> R.string.chat_disconnected
            }), Modifier.testTag("chat_connection_status"))
            if (state.connectionStatus == ChatConnectionStatus.DISCONNECTED && !state.connectionError) {
                TextButton(onClick = viewModel::retryConnection, modifier = Modifier.testTag("chat_retry_connection")) {
                    Text(stringResource(R.string.chat_retry_connection))
                }
            }
        }
        if (state.connectionError) Text(stringResource(R.string.chat_connection_error), Modifier.testTag("chat_connection_error"))
        if (state.sendBlocked) Text(stringResource(R.string.chat_capacity))
        if (state.inputTooLong) Text(stringResource(R.string.chat_too_long))
        if (state.transportUncertain) Text(stringResource(R.string.chat_uncertain))
        val submit: () -> Unit = { if (viewModel.send(state.draft)) { viewModel.updateDraft(""); forceLatest = true } }
        val sendDescription = stringResource(R.string.chat_send)
        OutlinedTextField(
            value = state.draft,
            onValueChange = viewModel::updateDraft,
            modifier = Modifier.fillMaxWidth().testTag("chat_input"),
            label = { Text(stringResource(R.string.chat_message_label)) },
            trailingIcon = { IconButton(onClick = submit, modifier = Modifier.testTag("chat_send")) { SendIcon(sendDescription) } },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { submit() }),
            maxLines = 4,
        )
    }
}
