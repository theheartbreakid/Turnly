package com.crescentapps.turnly.presentation.components.liquid

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.crescentapps.turnly.presentation.components.AdaptiveIcon
import com.crescentapps.turnly.presentation.components.AdaptiveLuminanceProvider
import com.crescentapps.turnly.presentation.components.LocalGlassSettings
import com.crescentapps.turnly.presentation.components.LocalPrismalAdaptiveColor
import com.crescentapps.turnly.presentation.components.PrismalDialog
import com.crescentapps.turnly.presentation.components.rememberPrismalHaptic
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.BackdropEffectScope
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.liquidLens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle

/**
 * DHIKRCOUNTER LIQUID DIALOG
 * Exact implementation from DhikrCounter master.
 * Uses backdrop refraction when available, falling back safely to PrismalDialog.
 */
@Composable
fun LiquidDialog(
    onDismissRequest: () -> Unit,
    backdrop: Backdrop? = null,
    title: String = "",
    message: String = "",
    positiveText: String = "Confirm",
    negativeText: String? = "Cancel",
    onPositive: () -> Unit = {},
    icon: ImageVector? = null,
    iconTint: Color? = null,
    accentColor: Color? = null,
    content: @Composable (ColumnScope.() -> Unit)? = null
) {
    if (backdrop == null) {
        PrismalDialog(
            onDismissRequest = onDismissRequest,
            tonalColor = if (accentColor != null) accentColor.copy(alpha = 0.15f) else null,
            title = title,
            message = message,
            positiveText = positiveText,
            negativeText = negativeText,
            onPositive = onPositive,
            content = content
        )
        return
    }

    val turnlyColors = com.crescentapps.turnly.presentation.theme.LocalTurnlyColors.current
    val isLightTheme = turnlyColors.isLight
    val glassSettings = LocalGlassSettings.current
    val haptic = rememberPrismalHaptic()
    val containerColor = if (isLightTheme) Color(0xFFFAFAFA).copy(0.4f) else Color(0xFF121212).copy(0.4f)
    val finalAccentColor = accentColor ?: androidx.compose.material3.MaterialTheme.colorScheme.primary

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        var showDialog by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { showDialog = true }

        val scale by animateFloatAsState(
            targetValue = if (showDialog) 1f else 0.95f,
            animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
            label = "LiquidDialogScale"
        )
        val alpha by animateFloatAsState(
            targetValue = if (showDialog) 1f else 0f,
            animationSpec = tween(250),
            label = "LiquidDialogAlpha"
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f * alpha))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismissRequest
                ),
            contentAlignment = Alignment.Center
        ) {
            val layer = rememberGraphicsLayer()
            val effectsBlock: BackdropEffectScope.() -> Unit = remember(glassSettings, isLightTheme) {
                val block: BackdropEffectScope.() -> Unit = {
                    colorControls(
                        brightness = if (isLightTheme) 0.1f else 0f,
                        saturation = 1.2f
                    )
                    blur(glassSettings.blurRadius.dp.toPx())
                    liquidLens(
                        refractionHeight = glassSettings.refractionHeight.dp.toPx(),
                        refractionAmount = glassSettings.refractionAmount.dp.toPx(),
                        chromaticAberration = glassSettings.chromaticAberration,
                        depthEffect = true
                    )
                }
                block
            }
            val dialogShape = remember(glassSettings.cornerRadius) {
                RoundedRectangle(glassSettings.cornerRadius.dp)
            }

            val onDrawBackdropBlock: DrawScope.(drawBackdrop: DrawScope.() -> Unit) -> Unit = remember(containerColor, layer) {
                val block: DrawScope.(drawBackdrop: DrawScope.() -> Unit) -> Unit = { drawBackdrop ->
                    drawBackdrop()
                    layer.record(
                        density = this,
                        layoutDirection = layoutDirection,
                        size = androidx.compose.ui.unit.IntSize(size.width.toInt(), size.height.toInt())
                    ) { 
                        drawBackdrop()
                        drawRect(containerColor)
                    }
                }
                block
            }

            val showButtons = positiveText.isNotBlank() || negativeText != null

            AdaptiveLuminanceProvider(layer = layer, enabled = true) {
                Column(
                    Modifier
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                        .widthIn(min = 280.dp, max = 380.dp)
                        .fillMaxWidth(0.92f)
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            this.alpha = alpha
                        }
                        .drawBackdrop(
                            backdrop = backdrop,
                            shape = { dialogShape },
                            effects = effectsBlock,
                            highlight = { Highlight.Plain },
                            onDrawBackdrop = onDrawBackdropBlock,
                            onDrawSurface = { drawRect(containerColor) }
                        )
                        .clickable(enabled = false) {}
                ) {
                    val columnScope = this

                    if (icon != null) {
                        Box(
                            modifier = Modifier
                                .padding(top = 24.dp)
                                .size(48.dp)
                                .align(Alignment.CenterHorizontally)
                                .clip(Capsule())
                                .background(iconTint?.copy(alpha = 0.1f) ?: LocalPrismalAdaptiveColor.current.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AdaptiveIcon(
                                icon = icon,
                                modifier = Modifier.size(24.dp),
                                tint = iconTint ?: LocalPrismalAdaptiveColor.current
                            )
                        }
                    }

                    // Content Area (Scrollable to prevent overflow on short windows)
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (title.isNotEmpty()) {
                            BasicText(
                                title,
                                Modifier.padding(20.dp, if (icon != null) 12.dp else 20.dp, 20.dp, 8.dp),
                                style = TextStyle(LocalPrismalAdaptiveColor.current, 20.sp, FontWeight.Bold)
                            )
                        }

                        if (message.isNotEmpty()) {
                            BasicText(
                                message,
                                Modifier
                                    .then(if (isLightTheme) Modifier else Modifier.graphicsLayer(blendMode = BlendMode.Plus))
                                    .padding(20.dp, 4.dp, 20.dp, 12.dp),
                                style = TextStyle(LocalPrismalAdaptiveColor.current.copy(0.7f), 14.sp)
                            )
                        }

                        if (content != null) {
                            Box(Modifier.padding(horizontal = 20.dp, vertical = if (title.isEmpty() && message.isEmpty()) 16.dp else 4.dp)) {
                                columnScope.content()
                            }
                        }
                    }

                    if (showButtons) {
                        Row(
                            Modifier
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (negativeText != null) {
                                Row(
                                    Modifier
                                        .clip(Capsule())
                                        .background(LocalPrismalAdaptiveColor.current.copy(0.08f))
                                        .clickable { 
                                            haptic()
                                            onDismissRequest() 
                                        }
                                        .height(48.dp)
                                        .weight(1f),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BasicText(negativeText, style = TextStyle(LocalPrismalAdaptiveColor.current, 15.sp, FontWeight.Medium))
                                }
                            }
                            if (positiveText.isNotBlank()) {
                                Row(
                                    Modifier
                                        .clip(Capsule())
                                        .background(finalAccentColor)
                                        .clickable {
                                            haptic()
                                            onPositive()
                                            onDismissRequest()
                                        }
                                        .height(48.dp)
                                        .weight(1f),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BasicText(
                                        positiveText,
                                        style = TextStyle(
                                            if (finalAccentColor.luminance() > 0.5f) Color.Black else Color.White,
                                            15.sp,
                                            FontWeight.Bold
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
