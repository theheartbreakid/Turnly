package com.crescentapps.turnly.presentation.gaussianblur.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.crescentapps.turnly.presentation.gaussianblur.backdrop.LocalAppBackdrop
import com.crescentapps.turnly.presentation.gaussianblur.backdrop.gaussianGlass
import com.crescentapps.turnly.presentation.gaussianblur.theme.GaussianBlurTokens
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianBlurColors

/**
 * Fundamental GaussianBlur Surface.
 * Renders backdrop blur sampling from [LocalAppBackdrop], lens refraction, and specular highlight.
 */
@Composable
fun GaussianSurface(
    modifier: Modifier = Modifier,
    shape: CornerBasedShape = GaussianBlurTokens.ShapeCard,
    surfaceOpacity: Float = GaussianBlurTokens.DefaultSurfaceOpacity,
    blurRadiusDp: Float = GaussianBlurTokens.DefaultBlurRadiusDp,
    content: @Composable () -> Unit
) {
    val backdrop = LocalAppBackdrop.current
    val colors = LocalGaussianBlurColors.current

    Box(
        modifier = modifier.gaussianGlass(
            shape = shape,
            backdrop = backdrop,
            blurRadiusDp = blurRadiusDp,
            surfaceOpacity = surfaceOpacity,
            isDark = colors.isDark
        )
    ) {
        content()
    }
}
