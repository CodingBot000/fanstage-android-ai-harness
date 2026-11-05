package dev.fanstage.android

import dev.fanstage.android.core.model.*
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoadCancellationTest {
    @Test fun cancelledDiscoverFailureCannotReplaceNewContent() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val release = CompletableDeferred<Unit>()
            var calls = 0
            val latest = DiscoverData(emptyList(), emptyList())
            val vm = DiscoverViewModel(object : DiscoverRepository {
                override suspend fun load(): DiscoverData {
                    calls++
                    if (calls == 1) withContext(NonCancellable) { release.await(); throw IllegalStateException() }
                    return latest
                }
            })
            runCurrent(); vm.retry(); runCurrent()
            assertEquals(DiscoverState.Ready(latest), vm.state.value)
            release.complete(Unit); runCurrent()
            assertEquals(DiscoverState.Ready(latest), vm.state.value)
        } finally { Dispatchers.resetMain() }
    }
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
            runCurrent(); vm.retry(); runCurrent()
            assertEquals(CommunityState.Ready(emptyList()), vm.state.value)
            release.complete(Unit); runCurrent()
            assertEquals(CommunityState.Ready(emptyList()), vm.state.value)
        } finally { Dispatchers.resetMain() }
    }
}
