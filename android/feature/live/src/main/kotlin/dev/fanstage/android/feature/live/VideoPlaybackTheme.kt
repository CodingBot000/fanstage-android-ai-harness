package dev.fanstage.android.feature.live

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat

/** A neutral playback palette; Discover, Community, Media and Messages retain the light theme. */
private val videoColors = darkColorScheme(
    primary = Color(0xFFDDE9ED), onPrimary = Color(0xFF172326),
    primaryContainer = Color(0xFF303A3F), onPrimaryContainer = Color.White,
    secondary = Color(0xFFBECAD0), onSecondary = Color(0xFF182024),
    secondaryContainer = Color(0xFF2D3337), onSecondaryContainer = Color(0xFFEEF2F4),
    tertiary = Color(0xFFB7CBD0), onTertiary = Color(0xFF182024),
    tertiaryContainer = Color(0xFF2D3538), onTertiaryContainer = Color(0xFFF0F3F5),
    background = Color.Black, onBackground = Color(0xFFF1F3F4),
    surface = Color(0xFF1B1D20), onSurface = Color(0xFFF1F3F4),
    surfaceVariant = Color(0xFF292C30), onSurfaceVariant = Color(0xFFCDD2D6),
    surfaceTint = Color.Transparent,
    surfaceContainer = Color(0xFF1B1D20),
    surfaceContainerLow = Color(0xFF141618),
    surfaceContainerHigh = Color(0xFF27292C),
    surfaceContainerHighest = Color(0xFF303337),
    outline = Color(0xFF858C90), outlineVariant = Color(0xFF44494D),
    inverseSurface = Color(0xFFE3E7E9), inverseOnSurface = Color(0xFF202426),
    inversePrimary = Color(0xFF3B555C), error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

private tailrec fun Context.findPlaybackActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findPlaybackActivity()
    else -> null
}

@Composable
internal fun VideoPlaybackTheme(content: @Composable () -> Unit) {
    val activity = LocalContext.current.findPlaybackActivity()
    // Edge-to-edge system bar icons must remain visible against the black surface.
    DisposableEffect(activity) {
        val controller = activity?.window?.let { WindowCompat.getInsetsController(it, it.decorView) }
        val wasLightStatus = controller?.isAppearanceLightStatusBars
        val wasLightNavigation = controller?.isAppearanceLightNavigationBars
        if (controller != null) {
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }
        onDispose {
            if (controller != null) {
                if (wasLightStatus != null) controller.isAppearanceLightStatusBars = wasLightStatus
                if (wasLightNavigation != null) controller.isAppearanceLightNavigationBars = wasLightNavigation
            }
        }
    }
    MaterialTheme(
        colorScheme = videoColors,
        typography = MaterialTheme.typography,
        shapes = MaterialTheme.shapes,
        content = content,
    )
}
