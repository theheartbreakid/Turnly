package com.crescentapps.turnly.presentation.components.liquid

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.unit.dp
import com.crescentapps.turnly.presentation.components.AdaptiveLuminanceProvider
import com.crescentapps.turnly.presentation.components.LocalAdaptiveLuminanceActive
import com.crescentapps.turnly.presentation.components.LocalGlassIntensity
import com.crescentapps.turnly.presentation.components.LocalGlassSettings
import com.crescentapps.turnly.presentation.components.PrismalCard
import com.crescentapps.turnly.presentation.components.rememberPrismalHaptic
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.liquidLens
import com.kyant.backdrop.effects.vibrancy

/**
 * DHIKRCOUNTER LIQUID CARD
 * Exact implementation from DhikrCounter master.
 * Uses backdrop refraction when backdrop is available, falling back safely to PrismalCard.
 */
@Composable
fun LiquidCard(
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    shape: Shape? = null,
    onClick: (() -> Unit)? = null,
    tint: Color = Color.Unspecified,
    adaptiveLuminance: Boolean = true,
    content: @Composable () -> Unit
) {
    if (backdrop == null) {
        PrismalCard(
            modifier = modifier,
            shape = shape ?: RoundedCornerShape(24.dp),
            tonalColor = if (tint == Color.Unspecified) null else tint,
            onClick = onClick,
            content = content
        )
        return
    }

    val glassSettings = LocalGlassSettings.current
    val glassIntensity = LocalGlassIntensity.current
    val actualShape = remember(shape, glassSettings.cornerRadius) {
        shape ?: RoundedCornerShape(glassSettings.cornerRadius.dp)
    }
    val isLuminanceRoot = !LocalAdaptiveLuminanceActive.current && adaptiveLuminance
    val layer = if (isLuminanceRoot) rememberGraphicsLayer() else null
    val haptic = rememberPrismalHaptic()

    val effectsBlock: BackdropEffectScope.() -> Unit = remember(glassSettings, glassIntensity) {
        val block: BackdropEffectScope.() -> Unit = {
            vibrancy()
            blur(glassSettings.blurRadius.dp.toPx() * glassIntensity)
            liquidLens(
                refractionHeight = glassSettings.refractionHeight.dp.toPx(),
                refractionAmount = glassSettings.refractionAmount.dp.toPx(),
                chromaticAberration = glassSettings.chromaticAberration
            )
        }
        block
    }

    val onDrawBackdropBlock: DrawScope.(drawBackdrop: DrawScope.() -> Unit) -> Unit = remember(tint, isLuminanceRoot, layer) {
        val block: DrawScope.(drawBackdrop: DrawScope.() -> Unit) -> Unit = { drawBackdrop ->
            drawBackdrop()
            if (tint.isSpecified) {
                drawRect(tint, blendMode = BlendMode.Hue)
                drawRect(tint.copy(alpha = 0.05f))
            }
            if (isLuminanceRoot) {
                layer?.record(
                    density = this,
                    layoutDirection = layoutDirection,
                    size = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt())
                ) {
                    drawBackdrop()
                    if (tint.isSpecified) {
                        drawRect(tint, blendMode = BlendMode.Hue)
                        drawRect(tint.copy(alpha = 0.05f))
                    }
                }
            }
        }
        block
    }

    AdaptiveLuminanceProvider(layer = layer, enabled = isLuminanceRoot) {
        Box(
            modifier
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { actualShape },
                    effects = effectsBlock,
                    onDrawBackdrop = onDrawBackdropBlock,
                    onDrawSurface = {}
                )
                .clip(actualShape)
                .then(
                    if (onClick != null) Modifier.clickable {
                        haptic()
                        onClick()
                    } else Modifier
                ),
            contentAlignment = Alignment.CenterStart
        ) {
            content()
        }
    }
}
