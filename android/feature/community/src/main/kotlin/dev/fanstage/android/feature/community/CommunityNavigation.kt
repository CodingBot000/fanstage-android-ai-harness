package dev.fanstage.android.feature.community

import android.net.Uri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import dev.fanstage.android.core.model.FictionalArtist

const val COMMUNITY_ROUTE = "community"
private const val ARTIST_ID = "artistId"
private const val POST_ID = "postId"

fun communityArtistRoute(artistId: String): String = "$COMMUNITY_ROUTE/artist/${Uri.encode(artistId)}"

fun communityPostRoute(postId: String): String = "$COMMUNITY_ROUTE/post/${Uri.encode(postId)}"

fun NavGraphBuilder.communityDestinations(
    communityViewModel: CommunityViewModel,
    artists: List<FictionalArtist>,
    onOpenArtist: (String) -> Unit,
    onOpenPost: (String) -> Unit,
    onBack: () -> Unit,
) {
    composable(COMMUNITY_ROUTE) {
        CommunityRoute(
            communityViewModel,
            artistId = null,
            onArtist = onOpenArtist,
            onPost = onOpenPost,
            artists = artists,
        )
    }
    composable("$COMMUNITY_ROUTE/artist/{$ARTIST_ID}") { entry ->
        val artistId = requireNotNull(entry.arguments?.getString(ARTIST_ID))
        CommunityRoute(
            communityViewModel,
            artistId,
            onArtist = {},
            onPost = onOpenPost,
            onBack = onBack,
            artists = artists,
        )
    }
    composable("$COMMUNITY_ROUTE/post/{$POST_ID}") { entry ->
        val postId = requireNotNull(entry.arguments?.getString(POST_ID))
        val draftViewModel: PostDraftViewModel = viewModel(viewModelStoreOwner = entry)
        PostDetailRoute(communityViewModel, postId, onBack, draftViewModel)
    }
}
