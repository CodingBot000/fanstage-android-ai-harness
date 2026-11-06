package dev.fanstage.android.feature.community

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PostDetailRoute(viewModel: CommunityViewModel, postId: String, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var comment by remember { mutableStateOf("") }
    val post = (state as? CommunityState.Ready)?.posts?.firstOrNull { it.id == postId }
    PostDetailScreen(post, comment, { comment = it }, viewModel::onAction, onBack)
}
