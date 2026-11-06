package dev.fanstage.android

import dev.fanstage.android.core.data.FakeCommunityRepository
import dev.fanstage.android.feature.community.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CommunityActionTest {
    @Test fun actionsUpdateOnlyTargetAndSurviveReload() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val model = CommunityViewModel(FakeCommunityRepository())
            runCurrent()
            val initial = (model.state.value as CommunityState.Ready).posts
            val id = initial.first().id
            model.onAction(CommunityUiAction.LikeClicked(id))
            assertTrue((model.state.value as CommunityState.Ready).posts.first().liked)
            model.onAction(CommunityUiAction.LikeClicked(id))
            model.onAction(CommunityUiAction.CommentSubmitted(id, "  "))
            model.onAction(CommunityUiAction.CommentSubmitted(id, " hello "))
            model.onAction(CommunityUiAction.RetryClicked)
            runCurrent()
            val result = (model.state.value as CommunityState.Ready).posts
            assertFalse(result.first().liked)
            assertEquals(listOf("hello"), result.first().comments)
            assertEquals(initial.last(), result.last())
        } finally { Dispatchers.resetMain() }
    }
}
