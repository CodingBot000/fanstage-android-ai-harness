package dev.fanstage.android

import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dev.fanstage.android.core.data.FakeCommunityRepository
import dev.fanstage.android.core.data.FakeDiscoverRepository
import dev.fanstage.android.core.data.FakeMessageRepository
import dev.fanstage.android.core.model.CommunityRepository
import dev.fanstage.android.core.model.DiscoverRepository
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
    @BindValue @JvmField val discover: DiscoverRepository = FakeDiscoverRepository()
    @BindValue @JvmField val community: CommunityRepository = FakeCommunityRepository()
    @BindValue @JvmField val messages: MessageRepository = FakeMessageRepository()
    @Inject lateinit var injectedDiscover: DiscoverRepository
    @Inject lateinit var injectedCommunity: CommunityRepository
    @Inject lateinit var injectedMessages: MessageRepository

    @Test fun replacedBindingsAreInjected() {
        hiltRule.inject()
        assertSame(discover, injectedDiscover)
        assertSame(community, injectedCommunity)
        assertSame(messages, injectedMessages)
    }
}
