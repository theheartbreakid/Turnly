package com.crescentapps.turnly.presentation.components.liquid

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.fastRoundToInt
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.crescentapps.turnly.presentation.catalog.utils.DampedDragAnimation
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.liquidLens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule
import com.crescentapps.turnly.presentation.components.LocalGlassSettings
import com.crescentapps.turnly.presentation.theme.LocalTurnlyColors
import kotlinx.coroutines.flow.collectLatest

@Composable
fun LiquidSlider(
    value: () -> Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    visibilityThreshold: Float,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val haptic = com.crescentapps.turnly.presentation.components.rememberPrismalHaptic()
    val turnlyColors = LocalTurnlyColors.current
    val glassSettings = LocalGlassSettings.current
    val accentColor = turnlyColors.sliderTrackActive
    val trackColor = turnlyColors.sliderTrackInactive
    val trackBorderColor = turnlyColors.controlBorder

    val trackBackdrop = rememberLayerBackdrop()
    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentValue by rememberUpdatedState(value)

    BoxWithConstraints(
        modifier
            .fillMaxWidth()
            .height(38.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val trackWidth = constraints.maxWidth
        val currentTrackWidth by rememberUpdatedState(trackWidth)

        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val animationScope = rememberCoroutineScope()
        var isDragging by remember { mutableStateOf(false) }
        var didDrag by remember { mutableStateOf(false) }
        var fraction by remember { mutableStateOf(currentValue()) }

        val dampedDragAnimation = remember(animationScope) {
            DampedDragAnimation(
                animationScope = animationScope,
                initialValue = currentValue(),
                valueRange = valueRange,
                visibilityThreshold = visibilityThreshold,
                initialScale = 1f,
                pressedScale = 1.5f,
                onDragStarted = { position ->
                    isDragging = true
                    val width = currentTrackWidth
                    if (width > 0) {
                        val progress = (position.x / width).coerceIn(0f, 1f)
                        val newValue = if (isLtr) {
                            valueRange.start + progress * (valueRange.endInclusive - valueRange.start)
                        } else {
                            valueRange.endInclusive - progress * (valueRange.endInclusive - valueRange.start)
                        }
                        fraction = newValue
                        currentOnValueChange(newValue)
                    }
                },
                onDragStopped = {
                    isDragging = false
                    if (didDrag) {
                        currentOnValueChange(targetValue)
                        haptic()
                        didDrag = false
                    }
                },
                onDrag = { _, dragAmount ->
                    if (!didDrag) {
                        didDrag = dragAmount.x != 0f
                    }
                    val width = currentTrackWidth
                    if (width > 0) {
                        val delta = (dragAmount.x / width) * (valueRange.endInclusive - valueRange.start)
                        val newValue = (fraction + if (isLtr) delta else -delta).coerceIn(valueRange.start, valueRange.endInclusive)
                        fraction = newValue
                        currentOnValueChange(newValue)
                    }
                }
            )
        }

        LaunchedEffect(dampedDragAnimation) {
            snapshotFlow { fraction }
                .collectLatest { fraction ->
                    dampedDragAnimation.updateValue(fraction)
                }
        }

        LaunchedEffect(currentValue) {
            snapshotFlow { currentValue() }
                .collectLatest { value ->
                    if (!isDragging && value != fraction) {
                        fraction = value
                        dampedDragAnimation.animateToValue(value)
                    }
                }
        }

        Box(
            Modifier
                .fillMaxWidth()
                .then(dampedDragAnimation.modifier) // Apply gestures to the whole area
                .layerBackdrop(trackBackdrop),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                Modifier
                    .clip(Capsule())
                    .background(trackColor)
                    .drawBehind {
                        // High-contrast subtle border
                        drawRect(
                            color = trackBorderColor,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f.dp.toPx())
                        )
                    }
                    .height(6f.dp)
                    .fillMaxWidth()
            )

            Box(
                Modifier
                    .clip(Capsule())
                    .background(accentColor)
                    .height(6f.dp)
                    .layout { measurable, constraints ->
                        val placeable = measurable.measure(constraints)
                        val width = (constraints.maxWidth * dampedDragAnimation.progress)
                            .fastRoundToInt()
                            .coerceIn(0, constraints.maxWidth)
                        layout(width, placeable.height) {
                            placeable.place(0, 0)
                        }
                    }
            )
        }

        Box(
            Modifier
                .graphicsLayer {
                    translationX =
                        (-size.width / 2f + trackWidth * dampedDragAnimation.progress)
                            .fastCoerceIn(-size.width / 4f, trackWidth - size.width * 3f / 4f) * if (isLtr) 1f else -1f
                }
                .drawBackdrop(
                    backdrop = rememberCombinedBackdrop(
                        backdrop,
                        rememberBackdrop(trackBackdrop) { drawBackdrop ->
                            val progress = dampedDragAnimation.pressProgress
                            val scaleX = lerp(2f / 3f, 0.75f, progress)
                            val scaleY = lerp(0f, 0.75f, progress)
                            scale(scaleX, scaleY) {
                                drawBackdrop()
                            }
                        }
                    ),
                    shape = { Capsule() },
                    effects = {
                        val progress = dampedDragAnimation.pressProgress
                        blur(glassSettings.blurRadius.dp.toPx() * (1f - progress))
                        liquidLens(
                            refractionHeight = glassSettings.refractionHeight.dp.toPx() * progress,
                            refractionAmount = glassSettings.refractionAmount.dp.toPx() * progress,
                            chromaticAberration = glassSettings.chromaticAberration * progress
                        )
                    },
                    highlight = {
                        val progress = dampedDragAnimation.pressProgress
                        Highlight.Ambient.copy(
                            width = Highlight.Ambient.width / 1.5f,
                            blurRadius = Highlight.Ambient.blurRadius / 1.5f,
                            alpha = progress
                        )
                    },
                    shadow = {
                        Shadow(
                            radius = 4f.dp,
                            color = turnlyColors.sliderThumbShadow
                        )
                    },
                    innerShadow = {
                        val progress = dampedDragAnimation.pressProgress
                        InnerShadow(
                            radius = 4f.dp * progress,
                            alpha = progress
                        )
                    },
                    layerBlock = {
                        scaleX = dampedDragAnimation.scaleX
                        scaleY = dampedDragAnimation.scaleY
                        val velocity = dampedDragAnimation.velocity / 10f
                        scaleX /= 1f - (velocity * 0.75f).fastCoerceIn(-0.2f, 0.2f)
                        scaleY *= 1f - (velocity * 0.25f).fastCoerceIn(-0.2f, 0.2f)
                    },
                    onDrawSurface = {
                        val progress = dampedDragAnimation.pressProgress
                        drawRect(turnlyColors.sliderThumb.copy(alpha = 1f - progress))
                        // Distinct thumb outline
                        drawRect(
                            color = turnlyColors.sliderThumbBorder,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1f.dp.toPx())
                        )
                    }
                )
                .size(40f.dp, 24f.dp)
        )
    }
}
