package dev.fanstage.android

import android.content.res.Configuration
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.fanstage.android.core.data.FakeCatalog
import dev.fanstage.android.core.designsystem.FanStageTheme
import dev.fanstage.android.core.model.DemoVideo
import dev.fanstage.android.core.model.CatalogSnapshot
import dev.fanstage.android.feature.media.MediaScreen
import dev.fanstage.android.feature.media.MediaState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class MediaMissingArtistTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    private fun setEnglishContent(content: @Composable () -> Unit) {
        val config = Configuration(rule.activity.resources.configuration).apply { setLocale(Locale.US) }
        val context = rule.activity.createConfigurationContext(config)
        rule.activity.setContent {
            CompositionLocalProvider(LocalContext provides context, LocalConfiguration provides config, content = content)
        }
    }

    @Test fun emptyArtistCatalogKeepsVideoCardAndClickTarget() {
        val opened = AtomicReference<String>()
        val video = DemoVideo("missing-video", "Available video", "missing-artist", "demo-01.mp4")
        setEnglishContent {
            FanStageTheme {
                MediaScreen(MediaState.Ready(CatalogSnapshot(emptyList(), emptyList())), 0, {}, {}, {}, {}, listOf(video), { opened.set(it) })
            }
        }
        rule.onNodeWithTag("media_missing-video").assertIsDisplayed().assertTextContains("Unknown artist").performClick()
        assertEquals(video.id, opened.get())
    }

    @Test fun missingArtistNeverUsesAnotherArtistsName() {
        val artist = FakeCatalog.artists().first()
        val valid = DemoVideo("valid-video", "Known video", artist.id, "demo-01.mp4")
        val missing = DemoVideo("missing-video", "Other video", "absent", "demo-02.mp4")
        setEnglishContent {
            FanStageTheme {
                MediaScreen(MediaState.Ready(CatalogSnapshot(listOf(artist), emptyList())), 2, {}, {}, {}, {}, listOf(valid, missing))
            }
        }
        rule.onNodeWithTag("media_valid-video").assertTextContains(artist.name).assert(!hasText("Unknown artist"))
        rule.onNodeWithTag("media_missing-video").assertTextContains("Unknown artist").assert(!hasText(artist.name))
    }

    @Test fun koreanFallbackAtDoubleFontKeepsCardHeightAndPlaybackAction() {
        val artist = FakeCatalog.artists().first()
        val videos = listOf(
            DemoVideo("missing-video", "첫 번째 영상", "absent", "demo-01.mp4"),
            DemoVideo("valid-video", "두 번째 영상", artist.id, "demo-02.mp4"),
        )
        val opened = AtomicReference<String>()
        val config = Configuration(rule.activity.resources.configuration).apply { setLocale(Locale.KOREA); fontScale = 2f }
        val context = rule.activity.createConfigurationContext(config)
        rule.activity.setContent {
            CompositionLocalProvider(
                LocalContext provides context,
                LocalConfiguration provides config,
                LocalDensity provides Density(context.resources.displayMetrics.density, config.fontScale),
            ) {
                FanStageTheme {
                    MediaScreen(MediaState.Ready(CatalogSnapshot(listOf(artist), emptyList())), 0, {}, {}, {}, {}, videos, { opened.set(it) })
                }
            }
        }
        val missing = rule.onNodeWithTag("media_missing-video").assertTextContains("알 수 없는 아티스트").getUnclippedBoundsInRoot()
        val valid = rule.onNodeWithTag("media_valid-video").assertTextContains(artist.name).getUnclippedBoundsInRoot()
        assertTrue(abs((missing.bottom - missing.top - valid.bottom + valid.top).value) < 1f)
        rule.onNodeWithTag("media_missing-video").performClick()
        assertEquals("missing-video", opened.get())
    }
}
