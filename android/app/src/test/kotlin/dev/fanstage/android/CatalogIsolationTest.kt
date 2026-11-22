package dev.fanstage.android

import dev.fanstage.android.core.data.FakeCatalog
import dev.fanstage.android.core.data.FakeCommunityRepository
import dev.fanstage.android.core.data.FakeMessageRepository
import dev.fanstage.android.core.model.CatalogSnapshot
import dev.fanstage.android.core.model.CatalogRepository
import dev.fanstage.android.feature.community.CommunityState
import dev.fanstage.android.feature.community.CommunityViewModel
import dev.fanstage.android.feature.discover.DiscoverState
import dev.fanstage.android.feature.discover.DiscoverViewModel
import dev.fanstage.android.feature.messages.MessagesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogIsolationTest {
    @Test fun discoverFailureDoesNotEraseOtherTabsCatalog() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val discover = DiscoverViewModel(object : CatalogRepository {
                override suspend fun load(): CatalogSnapshot = throw IllegalStateException()
            })
            val community = CommunityViewModel(FakeCommunityRepository())
            val messages = MessagesViewModel(FakeMessageRepository())
            runCurrent()
            assertEquals(DiscoverState.Error, discover.state.value)
            assertEquals(3, FakeCatalog.artists().size)
            assertEquals(3, (community.state.value as CommunityState.Ready).posts.size)
            assertEquals(3, messages.state.value.conversations.size)
        } finally { Dispatchers.resetMain() }
    }
}
