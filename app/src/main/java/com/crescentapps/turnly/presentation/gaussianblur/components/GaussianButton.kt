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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.crescentapps.turnly.presentation.gaussianblur.backdrop.LocalAppBackdrop
import com.crescentapps.turnly.presentation.gaussianblur.backdrop.gaussianGlass
import com.crescentapps.turnly.presentation.gaussianblur.theme.GaussianBlurTokens
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianBlurColors
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianOptics

@Composable
fun GaussianButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: CornerBasedShape = GaussianBlurTokens.ShapeButton,
    accentTint: Color? = null,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit
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
        targetValue = if (isPressed && enabled) 0.96f else 1.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "buttonPressScale"
    )

    val condensedLightAlpha by animateFloatAsState(
        targetValue = if (isPressed && optics.condensedLightEnabled && enabled) 1.0f else 0.0f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "buttonCondensedLight"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .defaultMinSize(minHeight = GaussianBlurTokens.MinimumTouchTarget)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1.0f else 0.5f
            }
            .gaussianGlass(
                shape = shape,
                backdrop = backdrop,
                blurRadiusDp = 18f,
                surfaceOpacity = if (accentTint != null) 0.35f else 0.25f,
                isDark = colors.isDark,
                dynamicHighlightsEnabled = optics.condensedLightEnabled
            )
            .then(
                if (enabled) {
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
        // Condensed Interaction Light
        if (condensedLightAlpha > 0f && pressPosition != Offset.Unspecified) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.40f * condensedLightAlpha),
                                Color.Transparent
                            ),
                            center = pressPosition,
                            radius = optics.condensedLightRadius.coerceAtLeast(10f)
                        )
                    )
            )
        }

        Row(
            modifier = Modifier.padding(contentPadding),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            content()
        }
    }
}
