package dev.fanstage.android.feature.community

import dev.fanstage.android.core.model.CommunityPost
import dev.fanstage.android.core.model.CommunityRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoadCancellationTest {
    @Test fun cancelledCommunityFailureCannotReplaceNewContent() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val release = CompletableDeferred<Unit>()
            var calls = 0
            val vm = CommunityViewModel(object : CommunityRepository {
                override fun toggleLike(id: String) = emptyList<CommunityPost>()
                override fun addComment(id: String, text: String) = emptyList<CommunityPost>()
                override suspend fun posts(): List<CommunityPost> {
                    calls++
                    if (calls == 1) withContext(NonCancellable) { release.await(); throw IllegalStateException() }
                    return emptyList()
                }
            })
            runCurrent(); vm.onAction(CommunityUiAction.RetryClicked); runCurrent()
            assertEquals(CommunityState.Ready(emptyList()), vm.state.value)
            release.complete(Unit); runCurrent()
            assertEquals(CommunityState.Ready(emptyList()), vm.state.value)
        } finally { Dispatchers.resetMain() }
    }
}
