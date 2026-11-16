package dev.fanstage.android.feature.community

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PostDetailRoute(viewModel: CommunityViewModel, postId: String, onBack: () -> Unit, draftViewModel: PostDraftViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val comment by draftViewModel.text.collectAsStateWithLifecycle()
    val post = (state as? CommunityState.Ready)?.posts?.firstOrNull { it.id == postId }
    PostDetailScreen(post, comment, draftViewModel::update, viewModel::onAction, onBack, onSubmit = { draftViewModel.submit { viewModel.submitComment(postId, it) } })
}
