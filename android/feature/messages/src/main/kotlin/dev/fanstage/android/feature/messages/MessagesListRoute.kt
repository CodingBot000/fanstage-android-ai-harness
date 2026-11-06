package dev.fanstage.android.feature.messages

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.fanstage.android.core.model.*

@Composable
fun MessagesListRoute(
    viewModel: MessagesViewModel,
    artists: List<FictionalArtist>,
    onOpen: (String) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    MessagesListScreen(state, artists, onOpen)
}
