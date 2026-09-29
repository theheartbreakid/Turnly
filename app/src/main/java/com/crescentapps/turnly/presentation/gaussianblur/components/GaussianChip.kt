package com.crescentapps.turnly.presentation.gaussianblur.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.presentation.components.Text
import com.crescentapps.turnly.presentation.gaussianblur.backdrop.LocalAppBackdrop
import com.crescentapps.turnly.presentation.gaussianblur.backdrop.gaussianGlass
import com.crescentapps.turnly.presentation.gaussianblur.theme.GaussianBlurTokens
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianBlurColors

@Composable
fun GaussianChip(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String,
    leadingIcon: (@Composable () -> Unit)? = null,
    shape: CornerBasedShape = GaussianBlurTokens.ShapeChip
) {
    val backdrop = LocalAppBackdrop.current
    val colors = LocalGaussianBlurColors.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.94f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipPressScale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .gaussianGlass(
                shape = shape,
                backdrop = backdrop,
                blurRadiusDp = 16f,
                surfaceOpacity = if (selected) 0.45f else 0.20f,
                isDark = colors.isDark
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape)
                    .background(colors.accent.copy(alpha = 0.25f))
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (leadingIcon != null) {
                leadingIcon()
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) colors.accent else colors.textSecondary
            )
        }
    }
}
