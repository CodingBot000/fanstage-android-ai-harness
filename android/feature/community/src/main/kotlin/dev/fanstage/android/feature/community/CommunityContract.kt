package dev.fanstage.android.feature.community

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import dev.fanstage.android.core.model.*

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
