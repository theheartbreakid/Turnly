package com.crescentapps.turnly.presentation.m3expressive.components

import androidx.compose.animation.core.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.LayoutDirection
import kotlin.math.cos
import kotlin.math.sin
import com.crescentapps.turnly.presentation.m3expressive.theme.LocalUserPreferences

/**
 * Material 3 Expressive Light Ray / Shaft effect.
 * Creates a static, configurable gradient fill that mimics the position and direction of light falling across a physical surface.
 */
fun Modifier.expressiveLightRay(
    shape: Shape = RectangleShape,
    enabled: Boolean = true,
    fallbackEnabled: Boolean = true // Used only if not in composition tree
): Modifier = composed {
    val prefs = LocalUserPreferences.current
    if (!enabled || !prefs.m3LightRaysEnabled || !fallbackEnabled) return@composed this

    val colorScheme = MaterialTheme.colorScheme
    
    val baseColor = when (prefs.m3GradientColorSource) {
        "SECONDARY" -> colorScheme.secondaryContainer
        "TERTIARY" -> colorScheme.tertiaryContainer
        else -> colorScheme.primaryContainer
    }
    
    val originColor = baseColor.copy(alpha = 1.0f * prefs.m3GradientIntensity)
    val midColor = baseColor.copy(alpha = (1.0f - prefs.m3GradientSoftness) * prefs.m3GradientIntensity)
    val falloffColor = baseColor.copy(alpha = 0f)

    this
        .graphicsLayer {
            compositingStrategy = androidx.compose.ui.graphics.CompositingStrategy.Offscreen
        }
        .drawWithCache {
            val radians = Math.toRadians(prefs.m3GradientAngle.toDouble())
            
            // Calculate gradient direction vector
            val dirX = cos(radians).toFloat()
            val dirY = sin(radians).toFloat()
            
            // Map normalized source coordinates to pixel bounds
            val startX = prefs.m3GradientX * size.width
            val startY = prefs.m3GradientY * size.height
            
            // Calculate end point based on length and direction
            // length = 1.0 means it spans the diagonal of the component
            val diagonal = kotlin.math.sqrt(size.width * size.width + size.height * size.height)
            val distance = diagonal * prefs.m3GradientLength
            
            val endX = startX + dirX * distance
            val endY = startY + dirY * distance

            val gradient = Brush.linearGradient(
                colors = listOf(
                    originColor,
                    midColor,
                    falloffColor
                ),
                start = Offset(startX, startY),
                end = Offset(endX, endY)
            )
            
            val outline = shape.createOutline(size, layoutDirection, this)
            
            onDrawWithContent {
                drawContent()
                
                when (outline) {
                    is Outline.Rectangle -> {
                        drawRect(
                            brush = gradient,
                            blendMode = BlendMode.Screen
                        )
                    }
                    is Outline.Rounded -> {
                        val path = Path().apply { addRoundRect(outline.roundRect) }
                        clipPath(path) {
                            drawRect(
                                brush = gradient,
                                blendMode = BlendMode.Screen
                            )
                        }
                    }
                    is Outline.Generic -> {
                        clipPath(outline.path) {
                            drawRect(
                                brush = gradient,
                                blendMode = BlendMode.Screen
                            )
                        }
                    }
                }
            }
        }
}
