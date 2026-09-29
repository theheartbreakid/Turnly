package com.crescentapps.turnly.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.crescentapps.turnly.core.model.ThemeMode

@Composable
fun TurnlyAppTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    isAmoled: Boolean = false,
    appFontFamily: String = "SF Pro Text",
    appFontWeight: String = "Regular",
    appFontTilt: Boolean = false,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val turnlyColors = when {
        darkTheme && isAmoled -> AmoledDarkTurnlyColors
        darkTheme -> DarkTurnlyColors
        else -> LightTurnlyColors
    }

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
        LocalTurnlyColors provides turnlyColors,
        com.crescentapps.turnly.presentation.components.LocalPrismalAdaptiveColor provides turnlyColors.textPrimary
    ) {
        content()
    }
}
