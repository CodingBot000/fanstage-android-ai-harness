package dev.fanstage.android.core.designsystem

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow

/** A card label occupies one line at the current font scale. */
@Composable
fun CardSingleLineText(text: String, style: TextStyle, modifier: Modifier = Modifier, onTextLayout: (TextLayoutResult) -> Unit = {}) {
    Text(text, modifier.fillMaxWidth(), style = style, maxLines = 1, softWrap = false, overflow = TextOverflow.Ellipsis, onTextLayout = onTextLayout)
}
