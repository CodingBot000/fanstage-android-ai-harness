package dev.fanstage.android.feature.media

import dev.fanstage.android.core.model.CatalogSnapshot
import dev.fanstage.android.core.model.CatalogRepository
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
    @Test fun cancelledMediaFailureCannotReplaceNewContent() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val release = CompletableDeferred<Unit>()
            var calls = 0
            val latest = CatalogSnapshot(emptyList(), emptyList())
            val vm = MediaViewModel(object : CatalogRepository {
                override suspend fun load(): CatalogSnapshot {
                    calls++
                    if (calls == 1) withContext(NonCancellable) { release.await(); throw IllegalStateException() }
                    return latest
                }
            })
            runCurrent(); vm.onAction(MediaUiAction.RetryClicked); runCurrent()
            assertEquals(MediaState.Ready(latest), vm.state.value)
            release.complete(Unit); runCurrent()
            assertEquals(MediaState.Ready(latest), vm.state.value)
        } finally { Dispatchers.resetMain() }
    }
}
