package com.crescentapps.turnly.presentation.gaussianblur.backdrop

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import com.crescentapps.turnly.presentation.gaussianblur.theme.GaussianBlurTokens
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianOptics
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.liquidLens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightElement
import com.kyant.backdrop.highlight.HighlightStyle
import com.kyant.backdrop.internal.ShapeProvider
import com.kyant.backdrop.shadow.Shadow
import androidx.compose.ui.unit.dp

/**
 * Whether real refracting liquid glass is enabled — see [com.crescentapps.turnly.data.preferences.UserPreferences.isLiquidGlassEnabled].
 */
val LocalLiquidGlassEnabled = staticCompositionLocalOf { true }

fun isGaussianGlassSupported(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

/**
 * Primary GaussianBlur Glass Modifier adapted from BitChord's Modifier.liquidGlass.
 * Renders backdrop blur, vibrancy, lens refraction, dynamic highlight, specular edge, and surface tint.
 */
@Composable
fun Modifier.gaussianGlass(
    shape: CornerBasedShape,
    backdrop: Backdrop?,
    blurRadiusDp: Float = LocalGaussianOptics.current.blurRadiusDp,
    surfaceOpacity: Float = LocalGaussianOptics.current.surfaceOpacity,
    refractionEnabled: Boolean = LocalGaussianOptics.current.refractionEnabled,
    refractionHeightDp: Float = LocalGaussianOptics.current.refractionHeightDp,
    refractionAmountDp: Float = LocalGaussianOptics.current.refractionAmountDp,
    specularEnabled: Boolean = LocalGaussianOptics.current.specularEnabled,
    specularIntensity: Float = LocalGaussianOptics.current.specularIntensity,
    dynamicHighlightsEnabled: Boolean = LocalGaussianOptics.current.dynamicHighlightsEnabled,
    dynamicHighlightsIntensity: Float = LocalGaussianOptics.current.dynamicHighlightsIntensity,
    reduceMotion: Boolean = LocalGaussianOptics.current.reduceMotion,
    isDark: Boolean = true,
    isLiquidGlassEnabled: Boolean = LocalLiquidGlassEnabled.current
): Modifier {
    if (!isGaussianGlassSupported() || backdrop == null) {
        return lightweightGaussianGlass(
            shape = shape,
            surfaceOpacity = surfaceOpacity,
            isDark = isDark
        )
    }

    val density = LocalDensity.current
    val blurPx = with(density) { blurRadiusDp.dp.toPx() } * GaussianBlurTokens.GlassResolutionScale
    val lensHeightPx = with(density) { refractionHeightDp.dp.toPx() } * GaussianBlurTokens.GlassResolutionScale
    val lensAmountPx = with(density) { refractionAmountDp.dp.toPx() } * GaussianBlurTokens.GlassResolutionScale

    val surfaceTintColor = if (isDark) {
        Color(0xFF121212)
    } else {
        Color(0xFFFAFAFA)
    }

    return drawBackdrop(
        backdrop = backdrop,
        shape = { shape },
        effects = {
            if (isLiquidGlassEnabled) {
                colorControls(saturation = 1.25f)
            }
            blur(blurPx)
            if (isLiquidGlassEnabled && refractionEnabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !reduceMotion) {
                liquidLens(
                    refractionHeight = lensHeightPx,
                    refractionAmount = lensAmountPx,
                    depthEffect = true,
                    chromaticAberration = if (specularEnabled) 0.005f * specularIntensity else 0f
                )
            }
        },
        highlight = {
            if (isLiquidGlassEnabled) {
                val baseSpecular = if (specularEnabled) specularIntensity else 0f
                if (baseSpecular > 0f || dynamicHighlightsEnabled) {
                    val baseColor = Color.White.copy(alpha = 0.5f * (baseSpecular + (if(dynamicHighlightsEnabled) dynamicHighlightsIntensity else 0f)) / 2f)
                    val activeFalloff = if (dynamicHighlightsEnabled) {
                        (2f - dynamicHighlightsIntensity).coerceIn(0.1f, 2f)
                    } else 1.0f
                    Highlight(
                        alpha = 1.0f.coerceAtMost(baseSpecular.coerceAtLeast(dynamicHighlightsIntensity)),
                        style = HighlightStyle.Default(
                            color = baseColor,
                            falloff = activeFalloff
                        )
                    )
                } else {
                    null
                }
            } else {
                null
            }
        },
        shadow = { Shadow.Default },
        onDrawSurface = {
            drawRect(
                color = surfaceTintColor.copy(alpha = surfaceOpacity.coerceIn(0.05f, 0.95f)),
                size = size
            )
        }
    )
}

/**
 * Lightweight visual match for Gaussian Glass when backdrop capture is paused or unsupported.
 */
@Composable
fun Modifier.lightweightGaussianGlass(
    shape: CornerBasedShape,
    surfaceOpacity: Float = 0.50f,
    isDark: Boolean = true
): Modifier {
    val glassTint = if (isDark) Color(0xFF18181B) else Color(0xFFF4F4F5)
    val shapeProvider = ShapeProvider { shape }

    return clip(shape)
        .background(
            color = glassTint.copy(alpha = surfaceOpacity.coerceIn(0.1f, 0.95f)),
            shape = shape
        )
        .then(
            HighlightElement(
                shapeProvider = shapeProvider,
                highlight = { Highlight.Default }
            )
        )
        .border(
            width = GaussianBlurTokens.GlassEdgeWidth,
            color = if (isDark) GaussianBlurTokens.GlassEdgeColorDark else GaussianBlurTokens.GlassEdgeColor,
            shape = shape
        )
}
