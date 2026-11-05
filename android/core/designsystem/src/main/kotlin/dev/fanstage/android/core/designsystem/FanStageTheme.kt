package dev.fanstage.android.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

val Ink = Color(0xFF21152C)
val Plum = Color(0xFF653D75)
val Canvas = Color(0xFFFAF8FC)
val PaperLanternAccent = Color(0xFF3A234B)
val BluePencilAccent = Color(0xFF285DAD)
val DiceGardenAccent = Color(0xFF183F3B)
object FanStageSpacing {
    val small = 8.dp
    val medium = 12.dp
    val large = 16.dp
    val page = 20.dp
    val section = 24.dp
}
private val palette = lightColorScheme(primary = Plum, onPrimary = Color.White, background = Canvas, surface = Color.White, onSurface = Ink)
private val typography = Typography().let { it.copy(headlineMedium = it.headlineMedium.copy(fontWeight = FontWeight.Bold), titleLarge = it.titleLarge.copy(fontWeight = FontWeight.Bold), titleMedium = it.titleMedium.copy(fontWeight = FontWeight.SemiBold)) }
private val shapes = Shapes(small = RoundedCornerShape(12.dp), medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(24.dp))
@Composable fun FanStageTheme(content: @Composable () -> Unit) { MaterialTheme(colorScheme = palette, typography = typography, shapes = shapes, content = content) }
