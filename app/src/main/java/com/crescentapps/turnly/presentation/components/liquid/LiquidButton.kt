package com.crescentapps.turnly.presentation.components.liquid

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceAtMost
import androidx.compose.ui.util.lerp
import com.crescentapps.turnly.presentation.catalog.utils.InteractiveHighlight
import com.crescentapps.turnly.presentation.components.AdaptiveLuminanceProvider
import com.crescentapps.turnly.presentation.components.LocalAdaptiveLuminanceActive
import com.crescentapps.turnly.presentation.components.LocalGlassSettings
import com.crescentapps.turnly.presentation.components.PrismalButton
import com.crescentapps.turnly.presentation.components.rememberPrismalHaptic
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.liquidLens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.shapes.Capsule
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tanh

/**
 * DHIKRCOUNTER LIQUID BUTTON
 * Exact implementation from DhikrCounter master.
 * Uses backdrop refraction with interactive highlight when backdrop is provided,
 * and falls back safely to PrismalButton if backdrop is null.
 */
@Composable
fun LiquidButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    backdrop: Backdrop? = null,
    isInteractive: Boolean = true,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified,
    adaptiveLuminance: Boolean = true,
    content: @Composable RowScope.() -> Unit
) {
    if (backdrop == null) {
        PrismalButton(
            onClick = onClick,
            modifier = modifier,
            tonalColor = if (surfaceColor != Color.Unspecified) surfaceColor else (if (tint != Color.Unspecified) tint.copy(alpha = 0.15f) else null),
            shape = CircleShape,
            content = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    content = content
                )
            }
        )
        return
    }

    val haptic = rememberPrismalHaptic()
    val animationScope = rememberCoroutineScope()

    val interactiveHighlight = remember(animationScope) {
        InteractiveHighlight(animationScope = animationScope)
    }
    val glassSettings = LocalGlassSettings.current
    val isLuminanceRoot = !LocalAdaptiveLuminanceActive.current && adaptiveLuminance
    val layer = if (isLuminanceRoot) rememberGraphicsLayer() else null

    AdaptiveLuminanceProvider(layer = layer, enabled = isLuminanceRoot) {
        Row(
            modifier
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { Capsule() },
                    effects = {
                        vibrancy()
                        blur(glassSettings.blurRadius.dp.toPx())
                        liquidLens(
                            refractionHeight = glassSettings.refractionHeight.dp.toPx(),
                            refractionAmount = glassSettings.refractionAmount.dp.toPx(),
                            chromaticAberration = glassSettings.chromaticAberration
                        )
                    },
                    layerBlock = if (isInteractive) {
                        {
                            val width = size.width
                            val height = size.height

                            val progress = interactiveHighlight.pressProgress
                            val scale = lerp(1f, 1f + 4f.dp.toPx() / size.height, progress)

                            val maxOffset = size.minDimension
                            val initialDerivative = 0.05f
                            val offset = interactiveHighlight.offset
                            translationX = maxOffset * tanh(initialDerivative * offset.x / maxOffset)
                            translationY = maxOffset * tanh(initialDerivative * offset.y / maxOffset)

                            val maxDragScale = 4f.dp.toPx() / size.height
                            val offsetAngle = atan2(offset.y, offset.x)
                            scaleX = scale + maxDragScale * abs(cos(offsetAngle) * offset.x / size.maxDimension) * (width / height).fastCoerceAtMost(1f)
                            scaleY = scale + maxDragScale * abs(sin(offsetAngle) * offset.y / size.maxDimension) * (height / width).fastCoerceAtMost(1f)
                        }
                    } else {
                        null
                    },
                    onDrawBackdrop = { drawBackdrop ->
                        drawBackdrop()
                        if (tint.isSpecified) {
                            drawRect(tint, blendMode = BlendMode.Hue)
                            drawRect(tint.copy(alpha = 0.75f))
                        }
                        if (surfaceColor.isSpecified) {
                            drawRect(surfaceColor)
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
                                    drawRect(tint.copy(alpha = 0.75f))
                                }
                                if (surfaceColor.isSpecified) {
                                    drawRect(surfaceColor)
                                }
                            }
                        }
                    },
                    onDrawSurface = {}
                )
                .clickable(
                    interactionSource = null,
                    indication = if (isInteractive) null else LocalIndication.current,
                    role = Role.Button,
                    onClick = {
                        haptic()
                        onClick()
                    }
                )
                .then(
                    if (isInteractive) {
                        Modifier
                            .then(interactiveHighlight.modifier)
                            .then(interactiveHighlight.gestureModifier)
                    } else {
                        Modifier
                    }
                )
                .height(48.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun LiquidButton(
    text: String,
    onClick: () -> Unit,
    backdrop: Backdrop?,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    isInteractive: Boolean = true,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified
) {
    LiquidButton(
        modifier = modifier,
        onClick = { if (enabled) onClick() },
        backdrop = backdrop,
        isInteractive = isInteractive && enabled,
        tint = tint,
        surfaceColor = surfaceColor
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = text,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.labelLarge
        )
    }
}
