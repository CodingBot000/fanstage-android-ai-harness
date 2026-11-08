package dev.fanstage.android

import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dev.fanstage.android.core.data.FakeCommunityRepository
import dev.fanstage.android.core.data.FakeMessageRepository
import dev.fanstage.android.core.model.CommunityRepository
import dev.fanstage.android.core.model.DiscoverRepository
import dev.fanstage.android.core.model.DiscoverData
import dev.fanstage.android.core.model.DiscoverContent
import dev.fanstage.android.feature.discover.DiscoverState
import dev.fanstage.android.feature.discover.DiscoverViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import dev.fanstage.android.core.model.MessageRepository
import dev.fanstage.android.di.RepositoryModule
import javax.inject.Inject
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(RepositoryModule::class)
class RepositoryBindingsTest {
    @get:Rule val hiltRule = HiltAndroidRule(this)
    // A unique marker makes the replacement observably different from the production Fake.
    @BindValue @JvmField val discover: DiscoverRepository = object : DiscoverRepository {
        override suspend fun load(): DiscoverData = DiscoverData(
            artists = emptyList(),
            content = listOf(DiscoverContent("hilt-di-probe", "DI probe", "Synthetic test")),
        )
    }
    @BindValue @JvmField val community: CommunityRepository = FakeCommunityRepository()
    @BindValue @JvmField val messages: MessageRepository = FakeMessageRepository()
    @Inject lateinit var injectedDiscover: DiscoverRepository
    @Inject lateinit var injectedCommunity: CommunityRepository
    @Inject lateinit var injectedMessages: MessageRepository

    @Test fun replacedDiscoverBindingDrivesFeatureState() = runBlocking {
        hiltRule.inject()
        val viewModel = DiscoverViewModel(injectedDiscover)
        val ready = withTimeout(5_000) {
            viewModel.state.first { it is DiscoverState.Ready }
        } as DiscoverState.Ready
        assertEquals(listOf("hilt-di-probe"), ready.data.content.map { it.id })
    }

    @Test fun replacedBindingsAreInjected() {
        hiltRule.inject()
        assertSame(discover, injectedDiscover)
        assertSame(community, injectedCommunity)
        assertSame(messages, injectedMessages)
    }
}
