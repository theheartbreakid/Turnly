package com.crescentapps.turnly.presentation.gaussianblur.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * BITCHORD-MATCHED VISUAL TOKENS & CONSTANTS
 * Directly derived from D:\Blurs\BitChord-main\app\src\main\java\com\music\bitchord\ui\components\LiquidGlass.kt
 */
@Immutable
object GaussianBlurTokens {
    val GlassEdgeWidth: Dp = 0.5.dp
    val GlassEdgeColor: Color = Color.White.copy(alpha = 0.12f)
    val GlassEdgeColorDark: Color = Color.White.copy(alpha = 0.08f)

    const val GlassResolutionScale: Float = 0.33f
    const val DefaultSurfaceOpacity: Float = 0.42f
    const val DefaultBlurRadiusDp: Float = 24f
    const val DefaultLensHeight: Float = 0.5f
    const val DefaultLensAmount: Float = 0.5f
    const val DefaultLensMaxDp: Float = 48f
    const val DefaultVibrancy: Float = 1.0f

    val ShapeCard: CornerBasedShape = RoundedCornerShape(26.dp)
    val ShapeButton: CornerBasedShape = RoundedCornerShape(20.dp)
    val ShapePill: CornerBasedShape = RoundedCornerShape(percent = 50)
    val ShapeChip: CornerBasedShape = RoundedCornerShape(16.dp)
    val ShapeDialog: CornerBasedShape = RoundedCornerShape(28.dp)
    val ShapeInput: CornerBasedShape = RoundedCornerShape(18.dp)

    val SpringDefault: AnimationSpec<Float> = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow
    )

    val PageGutter: Dp = 16.dp
    val MinimumTouchTarget: Dp = 48.dp
}

/**
 * Icon and text tint for content sitting over a translucent glass backdrop.
 * Matches BitChord's `glassContentColor()` behavior.
 */
@Composable
fun gaussianContentColor(isDark: Boolean = true): Color {
    return if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
}

/**
 * Selected pill / tab indicator color for glass surfaces.
 * Inverse of content color for shaded scrim reading.
 */
@Composable
fun gaussianIndicatorColor(isDark: Boolean = true): Color {
    return if (isDark) Color.Black else Color.White
}

val LocalGaussianBlurTokens = staticCompositionLocalOf { GaussianBlurTokens }
