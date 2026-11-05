package dev.fanstage.android.core.designsystem

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Reserve the measured height of two lines, including nonlinear system font scaling. */
@Composable
fun CardTwoLineText(text: String, style: TextStyle, modifier: Modifier = Modifier, onTextLayout: (TextLayoutResult) -> Unit = {}) {
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val twoLineHeight = remember(measurer, density, style) {
        with(density) { measurer.measure("가Hg\n가Hg", style = style, maxLines = 2).size.height.toDp() } + 1.dp
    }
    Text(text, modifier.fillMaxWidth().height(twoLineHeight), style = style, maxLines = 2, overflow = TextOverflow.Ellipsis, onTextLayout = onTextLayout)
}
