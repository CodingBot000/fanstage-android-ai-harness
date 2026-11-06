package dev.fanstage.android.feature.community

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.fanstage.android.core.model.*

@Composable
fun PostDetailRoute(viewModel: CommunityViewModel, postId: String, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var comment by remember { mutableStateOf("") }
    val post = (state as? CommunityState.Ready)?.posts?.firstOrNull { it.id == postId }
    PostDetailScreen(post, comment, { comment = it }, viewModel::onAction, onBack)
}
