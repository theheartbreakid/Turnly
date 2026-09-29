package com.crescentapps.turnly.presentation.gaussianblur.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.crescentapps.turnly.presentation.gaussianblur.backdrop.LocalAppBackdrop
import com.crescentapps.turnly.presentation.gaussianblur.backdrop.gaussianGlass
import com.crescentapps.turnly.presentation.gaussianblur.theme.GaussianBlurTokens
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianBlurColors

@Composable
fun GaussianSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    onValueChangeFinished: (() -> Unit)? = null,
    trackHeight: Dp = 10.dp,
    thumbSize: Dp = 22.dp
) {
    val backdrop = LocalAppBackdrop.current
    val colors = LocalGaussianBlurColors.current
    val density = LocalDensity.current

    val currentOnValueChange by rememberUpdatedState(onValueChange)
    val currentOnValueChangeFinished by rememberUpdatedState(onValueChangeFinished)
    val currentRange by rememberUpdatedState(valueRange)

    var trackWidthPx by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var activeFraction by remember { mutableFloatStateOf(0f) }

    val rangeSpan = currentRange.endInclusive - currentRange.start
    val resolvedFraction = if (isDragging) {
        activeFraction
    } else {
        if (rangeSpan <= 0f) 0f else ((value - currentRange.start) / rangeSpan).coerceIn(0f, 1f)
    }

    fun updateFromX(x: Float) {
        val thumbPx = with(density) { thumbSize.toPx() }
        val maxOffsetPx = (trackWidthPx - thumbPx).coerceAtLeast(1f)
        val touchX = (x - thumbPx / 2f).coerceIn(0f, maxOffsetPx)
        val newFraction = touchX / maxOffsetPx
        activeFraction = newFraction
        val newValue = currentRange.start + newFraction * rangeSpan
        currentOnValueChange(newValue)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .onSizeChanged { trackWidthPx = it.width.toFloat() }
            .pointerInput(currentRange) {
                detectHorizontalDragGestures(
                    onDragStart = { offset ->
                        isDragging = true
                        updateFromX(offset.x)
                    },
                    onDragEnd = {
                        isDragging = false
                        currentOnValueChangeFinished?.invoke()
                    },
                    onDragCancel = {
                        isDragging = false
                        currentOnValueChangeFinished?.invoke()
                    },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        updateFromX(change.position.x)
                    }
                )
            }
            .pointerInput(currentRange, "tap") {
                detectTapGestures(
                    onTap = { offset ->
                        isDragging = true
                        updateFromX(offset.x)
                        isDragging = false
                        currentOnValueChangeFinished?.invoke()
                    }
                )
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Track Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(trackHeight)
                .gaussianGlass(
                    shape = CircleShape,
                    backdrop = backdrop,
                    blurRadiusDp = 12f,
                    surfaceOpacity = 0.25f,
                    isDark = colors.isDark,
                    refractionEnabled = false
                )
        ) {
            // Active Progress Fill
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(resolvedFraction.coerceIn(0.01f, 1f))
                    .clip(CircleShape)
                    .background(colors.accent)
            )
        }

        // Thumb
        val thumbPx = with(density) { thumbSize.toPx() }
        val maxOffsetPx = (trackWidthPx - thumbPx).coerceAtLeast(0f)
        val currentOffsetDp = with(density) { (resolvedFraction * maxOffsetPx).toDp() }

        Box(
            modifier = Modifier
                .offset(x = currentOffsetDp)
                .size(thumbSize)
                .clip(CircleShape)
                .background(Color.White)
                .border(
                    width = GaussianBlurTokens.GlassEdgeWidth,
                    color = Color.Black.copy(alpha = 0.2f),
                    shape = CircleShape
                )
        )
    }
}
