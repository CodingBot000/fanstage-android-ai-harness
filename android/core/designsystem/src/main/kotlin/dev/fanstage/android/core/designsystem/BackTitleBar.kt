package dev.fanstage.android.core.designsystem

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.dp

@Composable
fun BackTitleBar(title: String, onBack: () -> Unit, modifier: Modifier = Modifier, backTag: String = "back_button") {
    val fraction = LocalCollapsingTitleBar.current?.visibleFraction ?: 1f
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val style = MaterialTheme.typography.titleLarge
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val maxTextWidth = with(density) { (maxWidth - 48.dp).coerceAtLeast(0.dp).roundToPx() }
        val textHeight = with(density) {
            measurer.measure(title, style = style, maxLines = 2, constraints = Constraints(maxWidth = maxTextWidth)).size.height.toDp()
        }
        val expandedHeight = maxOf(56.dp, textHeight)
        Row(Modifier.fillMaxWidth().height(expandedHeight * fraction).clipToBounds(), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack, modifier = Modifier.testTag(backTag)) {
                Icon(painterResource(R.drawable.ic_arrow_back), contentDescription = stringResource(R.string.navigation_back))
            }
            Text(title, modifier = Modifier.weight(1f), style = style, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}
