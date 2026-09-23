package com.crescentapps.turnly.presentation.components.liquid

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.presentation.components.AdaptiveLuminanceProvider
import com.crescentapps.turnly.presentation.components.LocalAdaptiveLuminanceActive
import com.crescentapps.turnly.presentation.components.LocalGlassSettings
import com.crescentapps.turnly.presentation.components.LocalPrismalAdaptiveColor
import com.crescentapps.turnly.presentation.components.PrismalChip
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.liquidLens
import com.kyant.backdrop.effects.vibrancy

/**
 * DHIKRCOUNTER LIQUID CHIP
 * Exact implementation from DhikrCounter master.
 * Uses backdrop refraction when backdrop is available, falling back safely to PrismalChip.
 */
@Composable
fun LiquidChip(
    onClick: (() -> Unit)?,
    backdrop: Backdrop? = null,
    modifier: Modifier = Modifier,
    tint: Color = Color.Unspecified,
    surfaceColor: Color = Color.Unspecified,
    adaptiveLuminance: Boolean = true,
    content: @Composable () -> Unit
) {
    if (backdrop == null) {
        PrismalChip(
            modifier = modifier,
            tonalColor = if (surfaceColor != Color.Unspecified) surfaceColor else (if (tint != Color.Unspecified) tint.copy(alpha = 0.2f) else null),
            onClick = onClick,
            content = {
                Box(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    content()
                }
            }
        )
        return
    }

    val glassSettings = LocalGlassSettings.current
    val shape = RoundedCornerShape(16.dp)
    val isLuminanceRoot = !LocalAdaptiveLuminanceActive.current && adaptiveLuminance
    val layer = if (isLuminanceRoot) rememberGraphicsLayer() else null

    AdaptiveLuminanceProvider(layer = layer, enabled = isLuminanceRoot) {
        Box(
            modifier
                .drawBackdrop(
                    backdrop = backdrop,
                    shape = { shape },
                    effects = {
                        vibrancy()
                        blur(glassSettings.blurRadius.dp.toPx() * 0.4f)
                        liquidLens(
                            refractionHeight = glassSettings.refractionHeight.dp.toPx() * 0.4f,
                            refractionAmount = glassSettings.refractionAmount.dp.toPx() * 0.4f,
                            chromaticAberration = glassSettings.chromaticAberration * 0.4f
                        )
                    },
                    onDrawBackdrop = { drawBackdrop ->
                        drawBackdrop()
                        if (tint.isSpecified) {
                            drawRect(tint, blendMode = BlendMode.Hue)
                            drawRect(tint.copy(alpha = 0.2f))
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
                                    drawRect(tint.copy(alpha = 0.2f))
                                }
                                if (surfaceColor.isSpecified) {
                                    drawRect(surfaceColor)
                                }
                            }
                        }
                    },
                    onDrawSurface = {}
                )
                .clip(shape)
                .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                content()
            }
        }
    }
}

@Composable
fun LiquidChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    backdrop: Backdrop?,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    LiquidChip(
        modifier = modifier,
        onClick = onClick,
        backdrop = backdrop,
        tint = if (selected) primaryColor.copy(alpha = 0.4f) else Color.Transparent,
        surfaceColor = if (selected) primaryColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) primaryColor else LocalPrismalAdaptiveColor.current
        )
    }
}
