package dev.fanstage.android.feature.live.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import java.util.Locale
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.coroutineScope
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.focus.onFocusChanged
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
import androidx.compose.ui.platform.LocalFocusManager
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatPanel(viewModel: ChatViewModel, developmentControls: Boolean = false, showComposer: Boolean = true, sendSignal: Int = 0, onDevelopmentFocus: (Boolean) -> Unit = {}) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()
    val locale = LocalConfiguration.current.locales[0]
    val focusManager = LocalFocusManager.current
    var endpoint by rememberSaveable { mutableStateOf("http://10.0.2.2:18765") }
    var settingsExpanded by rememberSaveable { mutableStateOf(false) }
    var checking by remember { mutableStateOf(false) }
    var checkJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
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
    LaunchedEffect(sendSignal) { if (sendSignal > 0) forceLatest = true }
    LaunchedEffect(state.roomId, state.messages.size, state.messages.lastOrNull()?.key) {
        if ((followLatest || forceLatest) && state.messages.isNotEmpty()) {
            listState.scrollToItem(state.messages.lastIndex)
            forceLatest = false
        }
    }
    Column(
        Modifier.fillMaxWidth().testTag("chat_panel"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                stringResource(R.string.chat_title),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
            )
            if (developmentControls) {
                val connectionLabel = if (!state.localServer) R.string.chat_mode_fake else when (state.connectionStatus) {
                    ChatConnectionStatus.CONNECTING -> R.string.chat_connecting
                    ChatConnectionStatus.CONNECTED -> R.string.chat_connected
                    ChatConnectionStatus.RECONNECTING -> R.string.chat_reconnecting
                    ChatConnectionStatus.DISCONNECTED -> R.string.chat_disconnected
                    null -> R.string.chat_mode_real
                }
                val indicator = when {
                    !state.localServer || state.connectionStatus == ChatConnectionStatus.CONNECTED -> Color(0xFF26846B)
                    state.connectionStatus == ChatConnectionStatus.DISCONNECTED -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.primary
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.size(7.dp).background(indicator, CircleShape))
                    Text(
                        stringResource(connectionLabel),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
        if (developmentControls) {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().testTag("chat_mode")) {
                SegmentedButton(
                    selected = !state.localServer,
                    onClick = {
                        checkJob?.cancel()
                        checking = false
                        focusManager.clearFocus()
                        if (state.localServer && viewModel.useOfflineFake()) {
                            token = ""
                            configError = false
                        }
                    },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                    icon = {},
                    label = {
                        Text(
                            stringResource(R.string.chat_mode_fake),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    modifier = Modifier.testTag("chat_use_fake"),
                )
                SegmentedButton(
                    selected = state.localServer,
                    onClick = { if (!state.localServer) settingsExpanded = true },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                    icon = {},
                    label = {
                        Text(
                            stringResource(R.string.chat_mode_real),
                            maxLines = 1,
                            softWrap = false,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    modifier = Modifier.testTag("chat_select_real"),
                )
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(
                    onClick = {
                        if (settingsExpanded) focusManager.clearFocus()
                        settingsExpanded = !settingsExpanded
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("chat_settings_toggle"),
                ) {
                    Icon(painterResource(R.drawable.ic_chat_settings), contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(stringResource(R.string.chat_settings), style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.width(4.dp))
                    Text(if (settingsExpanded) "⌃" else "⌄", style = MaterialTheme.typography.labelMedium)
                }
            }
            if (settingsExpanded) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        OutlinedTextField(
                            value = endpoint,
                            onValueChange = { endpoint = it },
                            label = { Text(stringResource(R.string.chat_endpoint)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                                .onFocusChanged { onDevelopmentFocus(it.isFocused) }
                                .testTag("chat_endpoint"),
                        )
                        OutlinedTextField(
                            value = token,
                            onValueChange = { token = it },
                            label = { Text(stringResource(R.string.chat_token)) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                                .onFocusChanged { onDevelopmentFocus(it.isFocused) }
                                .testTag("chat_token"),
                        )
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            Button(enabled = !checking, onClick = {
                                focusManager.clearFocus()
                                val target = endpoint
                                val credential = token
                                val room = state.roomId
                                val probe = runCatching { RealChatTransport(target, credential, allowInsecure = true) }.getOrNull()
                                configError = probe == null
                                if (probe != null) {
                                    checking = true
                                    checkJob = scope.launch {
                                        try {
                                            val valid = try {
                                                withTimeoutOrNull(10_000) {
                                                    coroutineScope {
                                                        val reader = async { probe.events(room).collect {} }
                                                        try { probe.connectionState.first { it == ChatConnectionStatus.CONNECTED }; true }
                                                        finally { reader.cancelAndJoin() }
                                                    }
                                                } == true
                                            } catch (cancelled: CancellationException) { throw cancelled }
                                            catch (_: Exception) { false }
                                            if (valid) {
                                                val next = RealChatTransport(target, credential, allowInsecure = true)
                                                if (viewModel.replaceTransport(next)) {
                                                    token = ""
                                                    settingsExpanded = false
                                                    configError = false
                                                }
                                            } else configError = true
                                        } finally {
                                            probe.close()
                                            checking = false
                                        }
                                    }
                                }
                            }, modifier = Modifier.testTag("chat_use_real")) {
                                Text(stringResource(if (checking) R.string.chat_connecting else R.string.chat_use_real))
                            }
                        }
                    }
                }
            }
            if (configError) Text(
                stringResource(R.string.chat_config_error),
                modifier = Modifier.testTag("chat_connection_error"),
                color = MaterialTheme.colorScheme.error,
            )
            if (state.switchBlocked) Text(
                stringResource(R.string.chat_switch_blocked),
                color = MaterialTheme.colorScheme.error,
            )
        }
        LazyColumn(
            Modifier.fillMaxWidth().height(200.dp).testTag("chat_messages"),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.messages, key = { it.key }) { message ->
                ChatBubbleRow(
                    message = message,
                    fictionalFixtures = state.fictionalFixtures,
                    locale = locale,
                    onRetry = viewModel::retry,
                    onDismissFailed = viewModel::dismissFailed,
                )
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
        if (showComposer) ChatComposer(viewModel) { forceLatest = true }
    }
}

@Composable
private fun ChatBubbleRow(
    message: ChatMessage,
    fictionalFixtures: Boolean,
    locale: Locale,
    onRetry: (String) -> Unit,
    onDismissFailed: (String) -> Unit,
) {
    val incomingName = stringResource(if (fictionalFixtures) R.string.chat_fixture else R.string.chat_participant)
    val fixtureResource = if (fictionalFixtures && !message.isOwn) fictionalMessageResource(message.key) else null
    val displayText = if (fixtureResource != null) stringResource(fixtureResource) else message.text
    val timestamp = formatChatTimestamp(message.sentAt, locale, TimeZone.getDefault())
    BoxWithConstraints(Modifier.fillMaxWidth().testTag("chat_message")) {
        val bubbleMaxWidth = maxWidth * 0.78f
        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = if (message.isOwn) Arrangement.End else Arrangement.Start,
        ) {
            if (!message.isOwn) {
                Box(
                    Modifier.padding(top = 18.dp).size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .testTag("chat_avatar_other"),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        incomingName.take(1),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.width(8.dp))
            }
            Column(
                Modifier.widthIn(max = bubbleMaxWidth),
                horizontalAlignment = if (message.isOwn) Alignment.End else Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (!message.isOwn) {
                    Text(
                        incomingName,
                        modifier = Modifier.padding(start = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Surface(
                    shape = if (message.isOwn) {
                        RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp, bottomEnd = 4.dp, bottomStart = 18.dp)
                    } else {
                        RoundedCornerShape(topStart = 4.dp, topEnd = 18.dp, bottomEnd = 18.dp, bottomStart = 18.dp)
                    },
                    color = if (message.isOwn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.widthIn(max = bubbleMaxWidth)
                        .testTag(if (message.isOwn) "chat_bubble_own" else "chat_bubble_other"),
                ) {
                    Text(
                        displayText,
                        Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    timestamp?.let {
                        Text(
                            stringResource(R.string.chat_timestamp, it),
                            Modifier.testTag("chat_timestamp"),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    if (message.isOwn) {
                        Text(
                            stringResource(when (message.delivery) {
                                Delivery.PENDING -> R.string.chat_pending
                                Delivery.SENT -> R.string.chat_sent
                                Delivery.FAILED -> R.string.chat_failed
                            }),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (message.delivery == Delivery.FAILED) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                if (message.isOwn && (message.delivery == Delivery.FAILED || message.isUncertain) && message.clientMessageId != null) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(onClick = { onRetry(message.clientMessageId) }) {
                            Text(stringResource(R.string.chat_retry_message))
                        }
                        if (message.delivery == Delivery.FAILED) {
                            TextButton(onClick = { onDismissFailed(message.clientMessageId) }) {
                                Text(stringResource(R.string.chat_remove_failed))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ChatComposer(viewModel: ChatViewModel, onSent: () -> Unit = {}) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val submit: () -> Unit = { if (viewModel.send(state.draft)) { viewModel.updateDraft(""); onSent() } }
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
