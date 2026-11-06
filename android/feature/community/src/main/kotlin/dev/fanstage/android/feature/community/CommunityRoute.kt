package dev.fanstage.android.feature.community

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.fanstage.android.core.model.*

@Composable
fun CommunityRoute(
    viewModel: CommunityViewModel,
    artistId: String?,
    onArtist: (String) -> Unit,
    onPost: (String) -> Unit,
    onBack: () -> Unit = {},
    artists: List<FictionalArtist> = emptyList(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable(artistId) { mutableIntStateOf(0) }
    CommunityScreen(
        state,
        artistId,
        selectedTab,
        { selectedTab = it },
        viewModel::onAction,
        onArtist,
        onPost,
        onBack,
        artists,
    )
}
