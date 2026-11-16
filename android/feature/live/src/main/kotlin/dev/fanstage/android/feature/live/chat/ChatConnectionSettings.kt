package dev.fanstage.android.feature.live.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.fanstage.android.feature.live.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ChatConnectionSettings(
    viewModel: ChatViewModel,
    state: ChatUiState,
    visible: Boolean,
    compactDevelopmentControls: Boolean,
    onDevelopmentFocus: (Boolean) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var endpoint by rememberSaveable { mutableStateOf("http://10.0.2.2:18765") }
    var settingsExpanded by rememberSaveable { mutableStateOf(false) }
    var token by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val coordinator = remember(scope, viewModel) { LocalChatConnectionCoordinator(scope) }
    val connection by coordinator.state.collectAsStateWithLifecycle()
    val checking = connection.checking
    val configError = connection.configError
    DisposableEffect(coordinator) { onDispose { coordinator.close() } }
    if (visible) {
        if (!compactDevelopmentControls) {
            // The row subtracts space for a shared border; negative space leaves a 4dp gap.
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().testTag("chat_mode"), space = (-4).dp) {
                SegmentedButton(
                    selected = !state.localServer,
                    onClick = {
                        coordinator.cancel()
                        focusManager.clearFocus()
                        if (state.localServer && viewModel.useOfflineFake()) {
                            token = ""
                            coordinator.clearError()
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
                            coordinator.connect(endpoint, token, state.roomId, viewModel::replaceTransport) {
                                token = ""
                                settingsExpanded = false
                                coordinator.clearError()
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
}
