package dev.fanstage.android.core.designsystem

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

@Stable
class CollapsingTitleBarState(private val distancePx: Float) {
    var visibleFraction by mutableFloatStateOf(1f)
        private set

    val connection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (source == NestedScrollSource.UserInput && kotlin.math.abs(available.y) > kotlin.math.abs(available.x)) {
                visibleFraction = (visibleFraction + available.y / distancePx).coerceIn(0f, 1f)
            }
            return Offset.Zero
        }
    }
}

val LocalCollapsingTitleBar = compositionLocalOf<CollapsingTitleBarState?> { null }

@Composable
fun rememberCollapsingTitleBarState(key: Any?): CollapsingTitleBarState {
    val distance = with(LocalDensity.current) { 56.dp.toPx() }
    return remember(key, distance) { CollapsingTitleBarState(distance) }
}
