package com.crescentapps.turnly.presentation.gaussianblur.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.runtime.setValue
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import com.crescentapps.turnly.presentation.gaussianblur.backdrop.LocalAppBackdrop
import com.crescentapps.turnly.presentation.gaussianblur.backdrop.gaussianGlass
import com.crescentapps.turnly.presentation.gaussianblur.theme.GaussianBlurTokens
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianBlurColors
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianOptics

@Composable
fun GaussianCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    shape: CornerBasedShape = GaussianBlurTokens.ShapeCard,
    surfaceOpacity: Float = GaussianBlurTokens.DefaultSurfaceOpacity,
    blurRadiusDp: Float = GaussianBlurTokens.DefaultBlurRadiusDp,
    content: @Composable BoxScope.() -> Unit
) {
    val backdrop = LocalAppBackdrop.current
    val colors = LocalGaussianBlurColors.current
    val optics = LocalGaussianOptics.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    var pressPosition by remember { mutableStateOf(Offset.Unspecified) }
    LaunchedEffect(interactionSource) {
        interactionSource.interactions.collect { interaction ->
            when (interaction) {
                is PressInteraction.Press -> pressPosition = interaction.pressPosition
                is PressInteraction.Release, is PressInteraction.Cancel -> pressPosition = Offset.Unspecified
            }
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.98f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "cardPressScale"
    )

    val condensedLightAlpha by animateFloatAsState(
        targetValue = if (isPressed && optics.condensedLightEnabled && onClick != null) 1.0f else 0.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "cardCondensedLight"
    )

    Box(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .gaussianGlass(
                shape = shape,
                backdrop = backdrop,
                blurRadiusDp = blurRadiusDp,
                surfaceOpacity = surfaceOpacity,
                isDark = colors.isDark,
                dynamicHighlightsEnabled = optics.condensedLightEnabled
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else {
                    Modifier
                }
            )
    ) {
        if (condensedLightAlpha > 0f && pressPosition != Offset.Unspecified) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.25f * condensedLightAlpha),
                                Color.Transparent
                            ),
                            center = pressPosition,
                            radius = optics.condensedLightRadius.coerceAtLeast(10f)
                        )
                    )
            )
        }
        
        content()
    }
}
