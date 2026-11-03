package dev.fanstage.android.feature.live

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import dev.fanstage.android.core.model.Room
import dev.fanstage.android.core.model.RoomRepository
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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RoomListViewModelTest {
    private lateinit var store: ViewModelStore
    private val rooms = listOf(Room("test-room", "Test room", "Test host", 1))

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
        store = ViewModelStore()
    }

    @After
    fun tearDown() {
        store.clear()
        Dispatchers.resetMain()
    }

    private fun createViewModel(repository: RoomRepository): RoomListViewModel =
        ViewModelProvider(store, RoomListViewModel.Factory(repository))[RoomListViewModel::class.java]

    private fun repository(fetch: suspend () -> List<Room>) = object : RoomRepository {
        override suspend fun getRooms(): List<Room> = fetch()
    }

    @Test
    fun `starts loading then shows fake repository content`() = runTest {
        val viewModel = createViewModel(FakeRoomRepository())
        assertEquals(RoomUiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()

        assertEquals(RoomUiState.Content(FakeRoomRepository().getRooms()), viewModel.uiState.value)
    }

    @Test
    fun `empty fetch is successful empty state`() = runTest {
        val viewModel = createViewModel(repository { emptyList() })
        assertEquals(RoomUiState.Loading, viewModel.uiState.value)

        advanceUntilIdle()

        assertEquals(RoomUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `error is safe and retry loads content`() = runTest {
        var calls = 0
        val retryResult = CompletableDeferred<List<Room>>()
        val viewModel = createViewModel(repository {
            calls += 1
            if (calls == 1) error("Internal repository detail")
            retryResult.await()
        })
        advanceUntilIdle()
        assertEquals(RoomUiState.Error("Rooms could not be loaded."), viewModel.uiState.value)

        viewModel.retry()
        runCurrent()
        assertEquals(RoomUiState.Loading, viewModel.uiState.value)
        retryResult.complete(rooms)
        advanceUntilIdle()

        assertEquals(2, calls)
        assertEquals(RoomUiState.Content(rooms), viewModel.uiState.value)
    }

    @Test
    fun `retry cancels in flight load and prevents stale result from replacing content`() = runTest {
        var calls = 0
        val staleResult = CompletableDeferred<List<Room>>()
        val viewModel = createViewModel(repository {
            calls += 1
            if (calls == 1) {
                // Simulate a dependency that completes even after cancellation.
                withContext(NonCancellable) { staleResult.await() }
            } else {
                rooms
            }
        })
        runCurrent()

        viewModel.retry()
        runCurrent()
        assertEquals(RoomUiState.Content(rooms), viewModel.uiState.value)
        staleResult.complete(emptyList())
        advanceUntilIdle()

        assertEquals(2, calls)
        assertEquals(RoomUiState.Content(rooms), viewModel.uiState.value)
    }

    @Test
    fun `clearing ViewModel cancels repository without publishing error`() = runTest {
        var started = false
        var cancelled = false
        val viewModel = createViewModel(repository {
            started = true
            try {
                awaitCancellation()
            } finally {
                cancelled = true
            }
        })
        runCurrent()
        assertTrue(started)
        assertFalse(cancelled)

        store.clear()
        advanceUntilIdle()

        assertTrue(cancelled)
        assertEquals(RoomUiState.Loading, viewModel.uiState.value)
    }
}
