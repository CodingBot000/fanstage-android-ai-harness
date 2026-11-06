package dev.fanstage.android.feature.messages

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.fanstage.android.core.model.FictionalArtist

@Composable
fun MessagesListRoute(
    viewModel: MessagesViewModel,
    artists: List<FictionalArtist>,
    onOpen: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MessagesListScreen(state, artists, onOpen)
}
