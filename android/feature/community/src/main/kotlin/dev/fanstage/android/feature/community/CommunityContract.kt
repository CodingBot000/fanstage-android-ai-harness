package dev.fanstage.android.feature.community

import dev.fanstage.android.core.model.CommunityPost

sealed interface CommunityState {
    data object Loading : CommunityState

    data object Error : CommunityState

    data class Ready(val posts: List<CommunityPost>) : CommunityState
}

sealed interface CommunityUiAction {
    data object RetryClicked : CommunityUiAction

    data class LikeClicked(val postId: String) : CommunityUiAction

    data class CommentSubmitted(val postId: String, val text: String) : CommunityUiAction
}
