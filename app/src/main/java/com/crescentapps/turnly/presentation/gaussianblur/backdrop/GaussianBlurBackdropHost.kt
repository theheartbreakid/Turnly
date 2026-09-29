package com.crescentapps.turnly.presentation.gaussianblur.backdrop

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.crescentapps.turnly.data.preferences.UserPreferences
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianBlurColors
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/**
 * CompositionLocal providing the root App Backdrop source to all child glass surfaces.
 */
val LocalAppBackdrop = staticCompositionLocalOf<Backdrop?> { null }

/**
 * Root GaussianBlur Environmental Host.
 * Renders an animated gradient or custom background image inside a LayerBackdrop recording scope,
 * making it available for real backdrop sampling by every glass component.
 */
@Composable
fun GaussianBlurBackdropHost(
    prefs: UserPreferences,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val backdrop = rememberLayerBackdrop()
    val colors = LocalGaussianBlurColors.current
    val context = LocalContext.current

    val bgGradientColors = remember(colors.isDark) {
        if (colors.isDark) {
            listOf(
                Color(0xFF0F172A),
                Color(0xFF1E1B4B),
                Color(0xFF31103F),
                Color(0xFF0F172A)
            )
        } else {
            listOf(
                Color(0xFFEEF2FF),
                Color(0xFFE0E7FF),
                Color(0xFFF3E8FF),
                Color(0xFFF8FAFC)
            )
        }
    }

    CompositionLocalProvider(
        LocalAppBackdrop provides backdrop,
        com.crescentapps.turnly.presentation.catalog.utils.LocalBackdrop provides backdrop
    ) {
        Box(modifier = modifier.fillMaxSize()) {
            // Backdrop Source Layer — Captured by backdrop engine
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .layerBackdrop(backdrop)
            ) {
                if (prefs.backgroundImageUri.isNotBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(prefs.backgroundImageUri)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Background Wallpaper",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        // Radial/Linear ambient gradient mesh
                        drawRect(
                            brush = Brush.radialGradient(
                                colors = bgGradientColors,
                                center = Offset(width * 0.3f, height * 0.2f),
                                radius = width * 1.2f
                            )
                        )

                        // Secondary ambient accent glow
                        drawCircle(
                            color = colors.accent.copy(alpha = if (colors.isDark) 0.25f else 0.15f),
                            radius = width * 0.6f,
                            center = Offset(width * 0.8f, height * 0.75f)
                        )
                    }
                }
            }

            // Interactive Foreground Content
            content()
        }
    }
}
