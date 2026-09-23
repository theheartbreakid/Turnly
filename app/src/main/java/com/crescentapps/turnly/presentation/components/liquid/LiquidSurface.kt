package com.crescentapps.turnly.presentation.components.liquid

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.crescentapps.turnly.presentation.components.PrismalSurface
import com.kyant.backdrop.Backdrop

/**
 * LIQUID SURFACE - PRISMAL REDIRECT
 * Refactored to use the DhikrCounter Prismal architecture.
 */
@Composable
fun LiquidSurface(
    modifier: Modifier = Modifier,
    backdrop: Backdrop? = null,
    shape: Shape? = null,
    tint: Color = Color.Unspecified,
    adaptiveLuminance: Boolean = true,
    content: @Composable () -> Unit
) {
    PrismalSurface(
        modifier = modifier,
        shape = shape ?: RoundedCornerShape(24.dp),
        tonalColor = if (tint == Color.Unspecified) null else tint,
        adaptiveLuminance = adaptiveLuminance,
        content = content
    )
}
