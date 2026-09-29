package com.crescentapps.turnly.presentation.gaussianblur.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.crescentapps.turnly.core.model.ThemeMode
import com.crescentapps.turnly.data.preferences.UserPreferences
import com.crescentapps.turnly.presentation.gaussianblur.backdrop.LocalLiquidGlassEnabled
import com.crescentapps.turnly.presentation.theme.DarkTurnlyColors
import com.crescentapps.turnly.presentation.theme.LightTurnlyColors
import com.crescentapps.turnly.presentation.theme.LocalTurnlyColors

@Composable
fun GaussianBlurTheme(
    prefs: UserPreferences,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = when (prefs.themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val gaussianColors = if (darkTheme) DarkGaussianBlurColors else LightGaussianBlurColors
    val turnlyColors = if (darkTheme) DarkTurnlyColors else LightTurnlyColors
    val gaussianOptics = GaussianOptics.fromPreferences(prefs)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.setDecorFitsSystemWindows(window, false)
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalLiquidGlassEnabled provides prefs.isLiquidGlassEnabled,
        LocalGaussianOptics provides gaussianOptics,
        LocalGaussianBlurColors provides gaussianColors,
        LocalGaussianBlurTokens provides GaussianBlurTokens,
        LocalTurnlyColors provides turnlyColors,
        com.crescentapps.turnly.presentation.components.LocalPrismalAdaptiveColor provides gaussianColors.textPrimary
    ) {
        content()
    }
}
