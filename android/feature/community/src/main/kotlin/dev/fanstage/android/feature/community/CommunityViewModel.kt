package dev.fanstage.android.feature.community

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.fanstage.android.core.model.CommunityRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CommunityViewModel @Inject constructor(private val repository: CommunityRepository) : ViewModel() {
    private val mutableState = MutableStateFlow<CommunityState>(CommunityState.Loading)
    val state = mutableState.asStateFlow()
    private var loadJob: Job? = null

    init {
        retry()
    }

    fun onAction(action: CommunityUiAction) {
        when (action) {
            CommunityUiAction.RetryClicked -> retry()
            is CommunityUiAction.LikeClicked -> like(action.postId)
            is CommunityUiAction.CommentSubmitted -> comment(action.postId, action.text)
        }
    }

    fun retry() {
        loadJob?.cancel()
        loadJob =
            viewModelScope.launch {
                mutableState.value = CommunityState.Loading
                try {
                    val posts = repository.posts()
                    currentCoroutineContext().ensureActive()
                    mutableState.value = CommunityState.Ready(posts)
                } catch (e: CancellationException) {
                    throw e
                } catch (_: Exception) {
                    currentCoroutineContext().ensureActive()
                    mutableState.value = CommunityState.Error
                }
            }
    }

    fun submitComment(postId: String, text: String): Boolean {
        val ready = mutableState.value as? CommunityState.Ready ?: return false
        val before = ready.posts.firstOrNull { it.id == postId } ?: return false
        val clean = text.trim()
        if (clean.isEmpty()) return false
        val result = repository.addComment(postId, clean)
        val after = result.firstOrNull { it.id == postId }
        val accepted = after != null && after.comments.count { it == clean } > before.comments.count { it == clean }
        mutableState.value = CommunityState.Ready(result)
        return accepted
    }

    fun like(id: String) {
        if (mutableState.value is CommunityState.Ready)
            mutableState.value = CommunityState.Ready(repository.toggleLike(id))
    }

    fun comment(id: String, text: String) {
        if (mutableState.value is CommunityState.Ready)
            mutableState.value = CommunityState.Ready(repository.addComment(id, text))
    }
}
