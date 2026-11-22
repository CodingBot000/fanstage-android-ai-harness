package dev.fanstage.android.feature.discover

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import dev.fanstage.android.core.data.FakeCatalogRepository
import dev.fanstage.android.core.model.CatalogContent
import dev.fanstage.android.core.model.CatalogSnapshot
import dev.fanstage.android.core.model.CatalogRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/** Loading/cancellation contracts migrated from the retired standalone room list. */
@OptIn(ExperimentalCoroutinesApi::class)
class DiscoverViewModelTest {
    private lateinit var store: ViewModelStore
    private val content = CatalogSnapshot(emptyList(), listOf(CatalogContent("test-room", "Test room", "Test artist")))

    @Before fun setUp() { Dispatchers.setMain(StandardTestDispatcher()); store = ViewModelStore() }
    @After fun tearDown() { store.clear(); Dispatchers.resetMain() }

    private fun create(repository: CatalogRepository): DiscoverViewModel =
        ViewModelProvider(store, object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                modelClass.cast(DiscoverViewModel(repository))!!
        })[DiscoverViewModel::class.java]

    private fun repository(load: suspend () -> CatalogSnapshot) = object : CatalogRepository {
        override suspend fun load() = load.invoke()
    }

    @Test fun startsLoadingThenShowsCatalogContent() = runTest {
        val vm = create(FakeCatalogRepository())
        assertEquals(DiscoverState.Loading, vm.state.value)
        advanceUntilIdle()
        assertEquals(DiscoverState.Ready(FakeCatalogRepository().load()), vm.state.value)
    }

    @Test fun emptyCatalogIsSuccessfulContent() = runTest {
        val empty = CatalogSnapshot(emptyList(), emptyList())
        val vm = create(repository { empty })
        assertEquals(DiscoverState.Loading, vm.state.value)
        advanceUntilIdle()
        assertEquals(DiscoverState.Ready(empty), vm.state.value)
    }

    @Test fun repositoryErrorIsSafeAndRetryLoadsContent() = runTest {
        var calls = 0
        val result = CompletableDeferred<CatalogSnapshot>()
        val vm = create(repository {
            calls++
            if (calls == 1) error("Internal repository detail")
            result.await()
        })
        advanceUntilIdle()
        assertEquals(DiscoverState.Error, vm.state.value)
        vm.retry()
        runCurrent()
        assertEquals(DiscoverState.Loading, vm.state.value)
        result.complete(content)
        advanceUntilIdle()
        assertEquals(2, calls)
        assertEquals(DiscoverState.Ready(content), vm.state.value)
    }

    @Test fun retryCancelsLoadAndLateSuccessCannotReplaceLatestContent() = runTest {
        var calls = 0
        val stale = CompletableDeferred<CatalogSnapshot>()
        val vm = create(repository {
            if (++calls == 1) withContext(NonCancellable) { stale.await() } else content
        })
        runCurrent()
        vm.retry()
        runCurrent()
        assertEquals(DiscoverState.Ready(content), vm.state.value)
        stale.complete(CatalogSnapshot(emptyList(), emptyList()))
        advanceUntilIdle()
        assertEquals(2, calls)
        assertEquals(DiscoverState.Ready(content), vm.state.value)
    }

    @Test fun clearingViewModelCancelsRepositoryWithoutPublishingError() = runTest {
        var started = false
        var cancelled = false
        val vm = create(repository {
            started = true
            try { awaitCancellation() } finally { cancelled = true }
        })
        runCurrent()
        assertTrue(started)
        assertFalse(cancelled)
        store.clear()
        advanceUntilIdle()
        assertTrue(cancelled)
        assertEquals(DiscoverState.Loading, vm.state.value)
    }
}
