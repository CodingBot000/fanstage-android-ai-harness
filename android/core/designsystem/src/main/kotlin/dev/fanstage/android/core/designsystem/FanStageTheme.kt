package dev.fanstage.android.core.designsystem

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

val Ink = Color(0xFF211B2D)
val Plum = Color(0xFF6341A5)
val Canvas = Color(0xFFFAF8FC)
private val palette = lightColorScheme(primary = Plum, onPrimary = Color.White, background = Canvas, surface = Color.White, onSurface = Ink)
private val typography = Typography().let { it.copy(headlineMedium = it.headlineMedium.copy(fontWeight = FontWeight.Bold), titleLarge = it.titleLarge.copy(fontWeight = FontWeight.Bold), titleMedium = it.titleMedium.copy(fontWeight = FontWeight.SemiBold)) }
private val shapes = Shapes(medium = RoundedCornerShape(16.dp), large = RoundedCornerShape(24.dp))
@Composable fun FanStageTheme(content: @Composable () -> Unit) { MaterialTheme(colorScheme = palette, typography = typography, shapes = shapes, content = content) }
