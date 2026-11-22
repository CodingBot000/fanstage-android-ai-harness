package dev.fanstage.android

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createEmptyComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.test.core.app.ActivityScenario
import dagger.hilt.android.testing.BindValue
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.UninstallModules
import dev.fanstage.android.core.data.FakeCatalog
import dev.fanstage.android.core.data.FakeCommunityRepository
import dev.fanstage.android.core.data.FakeMessageRepository
import dev.fanstage.android.core.model.ArtistCatalog
import dev.fanstage.android.core.model.CatalogContent
import dev.fanstage.android.core.model.CatalogRepository
import dev.fanstage.android.core.model.CatalogSnapshot
import dev.fanstage.android.core.model.CommunityRepository
import dev.fanstage.android.core.model.MessageRepository
import dev.fanstage.android.di.RepositoryModule
import dev.fanstage.android.feature.discover.DiscoverState
import dev.fanstage.android.feature.discover.DiscoverViewModel
import dev.fanstage.android.feature.media.MediaState
import dev.fanstage.android.feature.media.MediaViewModel
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
@UninstallModules(RepositoryModule::class)
class RepositoryBindingsTest {
    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val composeRule = createEmptyComposeRule()

    private val failContent = AtomicBoolean()
    private val replacementArtist = "Hilt Paper Lantern"

    @BindValue @JvmField val catalog: CatalogRepository = object : CatalogRepository {
        override suspend fun load(): CatalogSnapshot {
            check(!failContent.get()) { "Synthetic catalog load failure" }
            return CatalogSnapshot(
                artists = emptyList(),
                content = listOf(CatalogContent("hilt-di-probe", "DI probe", "Synthetic test")),
            )
        }
    }
    @BindValue @JvmField val artists: ArtistCatalog = ArtistCatalog {
        listOf(FakeCatalog.artists().first().copy(name = replacementArtist))
    }
    @BindValue @JvmField val community: CommunityRepository = FakeCommunityRepository()
    @BindValue @JvmField val messages: MessageRepository = FakeMessageRepository()
    @Inject lateinit var injectedCatalog: CatalogRepository
    @Inject lateinit var injectedArtists: ArtistCatalog
    @Inject lateinit var injectedCommunity: CommunityRepository
    @Inject lateinit var injectedMessages: MessageRepository

    @Test fun replacedCatalogBindingDrivesFeatureState() = runBlocking {
        hiltRule.inject()
        val store = ViewModelStore()
        try {
            val factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    modelClass.cast(DiscoverViewModel(injectedCatalog))!!
            }
            val viewModel = ViewModelProvider(store, factory)[DiscoverViewModel::class.java]
            val ready = withTimeout(5_000) {
                viewModel.state.first { it is DiscoverState.Ready }
            } as DiscoverState.Ready
            assertEquals(listOf("hilt-di-probe"), ready.data.content.map { it.id })
        } finally {
            store.clear()
        }
    }

    @Test fun activityHiltViewModelsUseReplacementCatalog() = runBlocking {
        hiltRule.inject()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            val models = AtomicReference<Pair<DiscoverViewModel, MediaViewModel>>()
            scenario.onActivity { activity ->
                val provider = ViewModelProvider(activity)
                models.set(provider[DiscoverViewModel::class.java] to provider[MediaViewModel::class.java])
            }
            val (discover, media) = models.get()
            val discoverReady = withTimeout(5_000) {
                discover.state.first { it is DiscoverState.Ready }
            } as DiscoverState.Ready
            val mediaReady = withTimeout(5_000) {
                media.state.first { it is MediaState.Ready }
            } as MediaState.Ready
            assertEquals(listOf("hilt-di-probe"), discoverReady.data.content.map { it.id })
            assertEquals(discoverReady.data, mediaReady.data)
        }
    }

    @Test fun artistCatalogStillDrivesCommunityAndMessagesWhenContentFails() {
        hiltRule.inject()
        failContent.set(true)
        ActivityScenario.launch(MainActivity::class.java).use {
            composeRule.waitUntil(10_000) {
                composeRule.onAllNodesWithTag("discover_error").fetchSemanticsNodes().isNotEmpty()
            }
            composeRule.onNodeWithTag("tab_community").performClick()
            composeRule.onNodeWithText(replacementArtist).assertIsDisplayed()
            composeRule.onNodeWithTag("tab_messages").performClick()
            composeRule.onNodeWithText(replacementArtist).assertIsDisplayed()
        }
    }

    @Test fun replacedBindingsAreInjected() {
        hiltRule.inject()
        assertSame(catalog, injectedCatalog)
        assertSame(artists, injectedArtists)
        assertSame(community, injectedCommunity)
        assertSame(messages, injectedMessages)
    }
}
