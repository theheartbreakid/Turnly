package com.crescentapps.turnly.presentation.gaussianblur.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.crescentapps.turnly.presentation.gaussianblur.backdrop.LocalAppBackdrop
import com.crescentapps.turnly.presentation.gaussianblur.backdrop.gaussianGlass
import com.crescentapps.turnly.presentation.gaussianblur.theme.GaussianBlurTokens
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianBlurColors

@Composable
fun GaussianSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val backdrop = LocalAppBackdrop.current
    val colors = LocalGaussianBlurColors.current
    val interactionSource = remember { MutableInteractionSource() }

    val trackWidth = 52.dp
    val trackHeight = 32.dp
    val thumbSize = 24.dp
    val padding = 4.dp

    val thumbOffset by animateDpAsState(
        targetValue = if (checked) trackWidth - thumbSize - padding else padding,
        animationSpec = spring(),
        label = "switchThumbOffset"
    )

    Box(
        modifier = modifier
            .size(width = trackWidth, height = trackHeight)
            .graphicsLayer { alpha = if (enabled) 1f else 0.5f }
            .gaussianGlass(
                shape = CircleShape,
                backdrop = backdrop,
                blurRadiusDp = 16f,
                surfaceOpacity = if (checked) 0.55f else 0.25f,
                isDark = colors.isDark
            )
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onCheckedChange(!checked) }
                    )
                } else Modifier
            ),
        contentAlignment = Alignment.CenterStart
    ) {
        // Track tint
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    if (checked) colors.accent.copy(alpha = 0.40f) else Color.Transparent
                )
        )

        // Thumb
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(thumbSize)
                .clip(CircleShape)
                .background(
                    if (checked) Color.White else colors.textSecondary
                )
                .border(
                    width = GaussianBlurTokens.GlassEdgeWidth,
                    color = Color.White.copy(alpha = 0.3f),
                    shape = CircleShape
                )
        )
    }
}
