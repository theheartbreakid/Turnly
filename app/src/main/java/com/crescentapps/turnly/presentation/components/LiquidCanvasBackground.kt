package com.crescentapps.turnly.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import kotlin.math.cos
import kotlin.math.sin

/**
 * DHIKRCOUNTER FLUID BACKDROP CANVAS - STABILIZED
 * Provides true AGSL backdrop-compatible layer for refractive liquid rendering.
 * Optimized to prevent flickering during visibility or motion state changes.
 */
@Composable
fun TurnlyBackdropBackground(
    backdrop: LayerBackdrop,
    modifier: Modifier = Modifier,
    isDark: Boolean = isSystemInDarkTheme(),
    isAmoled: Boolean = false,
    isReduceMotion: Boolean = false
) {
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var isAppVisible by remember { mutableStateOf(true) }

    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            isAppVisible = event == androidx.lifecycle.Lifecycle.Event.ON_RESUME || event == androidx.lifecycle.Lifecycle.Event.ON_START
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val baseColor = when {
        isAmoled && isDark -> Color(0xFF000000)
        isDark -> Color(0xFF0F172A)
        else -> Color(0xFFF8FAFC)
    }

    // Dynamic fluid oscillation for refractive backdrop layers
    val infiniteTransition = rememberInfiniteTransition(label = "LiquidFlow")
    
    val phase by if (isReduceMotion || !isAppVisible) {
        remember { mutableFloatStateOf(0f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 16000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "phase"
        )
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .layerBackdrop(backdrop)
    ) {
        drawRect(color = baseColor)

        val w = size.width
        val h = size.height

        val blob1 = if (isDark) Color(0x556366F1) else Color(0x38818CF8) // Indigo
        val blob2 = if (isDark) Color(0x45A855F7) else Color(0x30C084FC) // Purple
        val blob3 = if (isDark) Color(0x4006B6D4) else Color(0x2E06B6D4) // Cyan
        val blob4 = if (isDark) Color(0x35EC4899) else Color(0x28F472B6) // Pink

        // Fluid Blob 1 - Top Left
        val x1 = w * (0.28f + 0.10f * sin(phase.toDouble()).toFloat())
        val y1 = h * (0.20f + 0.08f * cos(phase.toDouble()).toFloat())
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(blob1, Color.Transparent),
                center = Offset(x1, y1),
                radius = w * 0.7f
            ),
            center = Offset(x1, y1),
            radius = w * 0.7f
        )

        // Fluid Blob 2 - Bottom Right
        val x2 = w * (0.75f - 0.10f * cos(phase * 0.85).toFloat())
        val y2 = h * (0.75f - 0.08f * sin(phase * 0.85).toFloat())
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(blob2, Color.Transparent),
                center = Offset(x2, y2),
                radius = w * 0.75f
            ),
            center = Offset(x2, y2),
            radius = w * 0.75f
        )

        // Fluid Blob 3 - Center
        val x3 = w * (0.80f + 0.08f * sin(phase * 1.15).toFloat())
        val y3 = h * (0.32f + 0.10f * cos(phase * 1.15).toFloat())
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(blob3, Color.Transparent),
                center = Offset(x3, y3),
                radius = w * 0.6f
            ),
            center = Offset(x3, y3),
            radius = w * 0.6f
        )

        // Fluid Blob 4 - Bottom Left
        val x4 = w * (0.22f + 0.08f * cos(phase * 0.9).toFloat())
        val y4 = h * (0.82f + 0.06f * sin(phase * 0.9).toFloat())
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(blob4, Color.Transparent),
                center = Offset(x4, y4),
                radius = w * 0.65f
            ),
            center = Offset(x4, y4),
            radius = w * 0.65f
        )
    }
}
