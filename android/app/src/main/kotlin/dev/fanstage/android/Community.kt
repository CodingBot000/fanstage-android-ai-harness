package dev.fanstage.android

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CommunityPost(val id: String, val artistId: String, val title: String, val body: String, val image: Int, val liked: Boolean = false, val comments: List<String> = emptyList())
interface CommunityRepository { suspend fun posts(): List<CommunityPost> }
class FakeCommunityRepository : CommunityRepository {
    override suspend fun posts() = fictionalArtists.map { CommunityPost("post-${it.id}", it.id, it.name, "A little moment from our world.", it.feed) }
}
sealed interface CommunityState {
    data object Loading : CommunityState
    data object Error : CommunityState
    data class Ready(val posts: List<CommunityPost>) : CommunityState
}
class CommunityViewModel(private val repository: CommunityRepository = FakeCommunityRepository()) : ViewModel() {
    private val mutableState = MutableStateFlow<CommunityState>(CommunityState.Loading)
    val state = mutableState.asStateFlow()
    private var loadJob: Job? = null
    init { retry() }
    fun retry() { loadJob?.cancel(); loadJob = viewModelScope.launch { mutableState.value = CommunityState.Loading; try { val posts = repository.posts(); currentCoroutineContext().ensureActive(); mutableState.value = CommunityState.Ready(posts) } catch (e: CancellationException) { throw e } catch (_: Exception) { currentCoroutineContext().ensureActive(); mutableState.value = CommunityState.Error } } }
    fun like(id: String) = update(id) { it.copy(liked = !it.liked) }
    fun comment(id: String, text: String) { val value = text.trim(); if (value.isNotEmpty()) update(id) { it.copy(comments = it.comments + value) } }
    private fun update(id: String, transform: (CommunityPost) -> CommunityPost) { val current = mutableState.value as? CommunityState.Ready ?: return; mutableState.value = current.copy(posts = current.posts.map { if (it.id == id) transform(it) else it }) }
}
@Composable fun CommunityRoute(viewModel: CommunityViewModel, artistId: String?, onArtist: (String) -> Unit, onPost: (String) -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    CommunityScreen(state, artistId, viewModel::retry, onArtist, onPost)
}
@Composable fun CommunityScreen(state: CommunityState, artistId: String?, onRetry: () -> Unit, onArtist: (String) -> Unit, onPost: (String) -> Unit) {
    var selectedTab by rememberSaveable(artistId) { mutableIntStateOf(0) }
    LazyColumn(Modifier.fillMaxSize().testTag("community_feed"), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Text(stringResource(R.string.community), style = MaterialTheme.typography.headlineMedium) }
        if (artistId != null) {
            val artist = fictionalArtists.firstOrNull { it.id == artistId }
            if (artist != null) item {
                Column {
                    Image(painterResource(artist.cover), null, Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(20.dp)), contentScale = ContentScale.Crop)
                    Text(artist.name, style = MaterialTheme.typography.headlineMedium)
                    TabRow(selectedTabIndex = selectedTab) {
                        Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text(stringResource(R.string.posts)) })
                        Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text(stringResource(R.string.notices)) })
                    }
                }
            }
        }
        if (artistId == null) items(fictionalArtists, key = { it.id }) { artist ->
            Card(onClick = { onArtist(artist.id) }, modifier = Modifier.fillMaxWidth().testTag("artist_${artist.id}")) {
                Row(Modifier.padding(12.dp)) { Image(painterResource(artist.artwork), null, Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)), contentScale = ContentScale.Crop); Spacer(Modifier.width(16.dp)); Text(artist.name, style = MaterialTheme.typography.titleLarge) }
            }
        }
        when (state) {
            CommunityState.Loading -> item { CircularProgressIndicator(Modifier.testTag("community_loading")) }
            CommunityState.Error -> item { Text(stringResource(R.string.community_error), Modifier.testTag("community_error")); Button(onClick = onRetry) { Text(stringResource(R.string.discover_retry)) } }
            is CommunityState.Ready -> {
                val posts = state.posts.filter { (artistId == null || it.artistId == artistId) && selectedTab == 0 }
                if (artistId != null && selectedTab == 1) item {
                    Card(Modifier.fillMaxWidth().testTag("community_notice")) {
                        Column(Modifier.padding(16.dp)) { Text(stringResource(R.string.notice_title), style = MaterialTheme.typography.titleMedium); Text(stringResource(R.string.notice_body)) }
                    }
                } else if (posts.isEmpty()) item { Text(stringResource(R.string.community_empty), Modifier.testTag("community_empty")) }
                items(posts, key = { it.id }) { post ->
                    Card(onClick = { onPost(post.id) }, modifier = Modifier.fillMaxWidth().testTag("post_${post.id}")) {
                        Column { Image(painterResource(post.image), null, Modifier.fillMaxWidth().height(180.dp), contentScale = ContentScale.Crop); Column(Modifier.padding(16.dp)) { Text(post.title, style = MaterialTheme.typography.titleLarge); Text(if (post.body == "A little moment from our world.") stringResource(R.string.demo_post_body) else post.body) } }
                    }
                }
            }
        }
    }
}
@Composable fun PostDetail(viewModel: CommunityViewModel, postId: String, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var comment by remember { mutableStateOf("") }
    val post = (state as? CommunityState.Ready)?.posts?.firstOrNull { it.id == postId }
    LazyColumn(Modifier.fillMaxSize().testTag("post_detail"), contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { TextButton(onClick = onBack) { Text(stringResource(R.string.replay_back)) } }
        if (post != null) {
            item { Image(painterResource(post.image), null, Modifier.fillMaxWidth().height(200.dp), contentScale = ContentScale.Crop); Text(post.title, style = MaterialTheme.typography.headlineMedium); Text(if (post.body == "A little moment from our world.") stringResource(R.string.demo_post_body) else post.body) }
            item { Button(onClick = { viewModel.like(post.id) }, modifier = Modifier.testTag("post_like")) { Text(if (post.liked) "♥" else "♡") } }
            items(post.comments) { Text(it) }
            item { OutlinedTextField(value = comment, onValueChange = { comment = it }, label = { Text(stringResource(R.string.comment)) }, modifier = Modifier.testTag("comment_input")); Button(onClick = { viewModel.comment(post.id, comment); comment = "" }, modifier = Modifier.testTag("comment_send")) { Text(stringResource(R.string.send_comment)) } }
        } else item { Text(stringResource(R.string.community_empty)) }
    }
}
