package com.crescentapps.turnly.presentation.m3expressive.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.crescentapps.turnly.data.preferences.UserPreferences

// Vibrant Color Schemes
private val LightVibrantColors = lightColorScheme(
    primary = Color(0xFF8C1D40),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFD9E2),
    onPrimaryContainer = Color(0xFF3E0016),
    secondary = Color(0xFF9C4148),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDADA),
    onSecondaryContainer = Color(0xFF40000A),
    tertiary = Color(0xFF805600),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDEAC),
    onTertiaryContainer = Color(0xFF281900),
    background = Color(0xFFFFF8F7),
    onBackground = Color(0xFF22191B),
    surface = Color(0xFFFFF8F7),
    onSurface = Color(0xFF22191B),
    surfaceContainer = Color(0xFFF7EBEF),
    surfaceContainerLow = Color(0xFFFDF1F5),
    surfaceContainerHigh = Color(0xFFF1E5E9),
    surfaceContainerHighest = Color(0xFFEBDFE3)
)

private val DarkVibrantColors = darkColorScheme(
    primary = Color(0xFFFFB1C5),
    onPrimary = Color(0xFF560021),
    primaryContainer = Color(0xFF70042B),
    onPrimaryContainer = Color(0xFFFFD9E2),
    secondary = Color(0xFFFFB3B8),
    onSecondary = Color(0xFF5F121C),
    secondaryContainer = Color(0xFF7D2A31),
    onSecondaryContainer = Color(0xFFFFDADA),
    tertiary = Color(0xFFFFBA52),
    onTertiary = Color(0xFF432C00),
    tertiaryContainer = Color(0xFF614000),
    onTertiaryContainer = Color(0xFFFFDEAC),
    background = Color(0xFF191113),
    onBackground = Color(0xFFF0DFE2),
    surface = Color(0xFF191113),
    onSurface = Color(0xFFF0DFE2),
    surfaceContainer = Color(0xFF261D1F),
    surfaceContainerLow = Color(0xFF22191B),
    surfaceContainerHigh = Color(0xFF31282A),
    surfaceContainerHighest = Color(0xFF3C3235)
)

// Balanced Color Schemes (Default matching reference design)
private val LightBalancedColors = lightColorScheme(
    primary = Color(0xFF7D2B42),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFAD8DF),
    onPrimaryContainer = Color(0xFF3D0014),
    secondary = Color(0xFF77535B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD9E2),
    onSecondaryContainer = Color(0xFF2C1119),
    tertiary = Color(0xFF7C5635),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCC1),
    onTertiaryContainer = Color(0xFF2E1500),
    background = Color(0xFFFFF8F7),
    onBackground = Color(0xFF201A1B),
    surface = Color(0xFFFFF8F7),
    onSurface = Color(0xFF201A1B),
    surfaceContainer = Color(0xFFF6ECEE),
    surfaceContainerLow = Color(0xFFFCF1F3),
    surfaceContainerHigh = Color(0xFFF0E6E8),
    surfaceContainerHighest = Color(0xFFEAE0E2)
)

private val DarkBalancedColors = darkColorScheme(
    primary = Color(0xFFEBB8C4),
    onPrimary = Color(0xFF49192A),
    primaryContainer = Color(0xFF632135),
    onPrimaryContainer = Color(0xFFFAD8DF),
    secondary = Color(0xFFE6BDC6),
    onSecondary = Color(0xFF44252D),
    secondaryContainer = Color(0xFF5D3B43),
    onSecondaryContainer = Color(0xFFFFD9E2),
    tertiary = Color(0xFFEFB992),
    onTertiary = Color(0xFF47280C),
    tertiaryContainer = Color(0xFF613E20),
    onTertiaryContainer = Color(0xFFFFDCC1),
    background = Color(0xFF181213),
    onBackground = Color(0xFFECDFE1),
    surface = Color(0xFF181213),
    onSurface = Color(0xFFECDFE1),
    surfaceContainer = Color(0xFF251E20),
    surfaceContainerLow = Color(0xFF201A1B),
    surfaceContainerHigh = Color(0xFF30282A),
    surfaceContainerHighest = Color(0xFF3B3335)
)

// Calm Color Schemes
private val LightCalmColors = lightColorScheme(
    primary = Color(0xFF6B424D),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFF4D8DF),
    onPrimaryContainer = Color(0xFF261017),
    secondary = Color(0xFF6B585C),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF4DDE1),
    onSecondaryContainer = Color(0xFF24161A),
    tertiary = Color(0xFF6E5C4E),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF8DFCE),
    onTertiaryContainer = Color(0xFF26190F),
    background = Color(0xFFFAF7F7),
    onBackground = Color(0xFF1D1B1C),
    surface = Color(0xFFFAF7F7),
    onSurface = Color(0xFF1D1B1C),
    surfaceContainer = Color(0xFFF1EEEE),
    surfaceContainerLow = Color(0xFFF7F4F4),
    surfaceContainerHigh = Color(0xFFEBE8E8),
    surfaceContainerHighest = Color(0xFFE5E2E2)
)

private val DarkCalmColors = darkColorScheme(
    primary = Color(0xFFD7BFC5),
    onPrimary = Color(0xFF3B2B31),
    primaryContainer = Color(0xFF523B42),
    onPrimaryContainer = Color(0xFFF4D8DF),
    secondary = Color(0xFFD7C1C5),
    onSecondary = Color(0xFF3B2B2F),
    secondaryContainer = Color(0xFF524145),
    onSecondaryContainer = Color(0xFFF4DDE1),
    tertiary = Color(0xFFDAC4B3),
    onTertiary = Color(0xFF3C2E23),
    tertiaryContainer = Color(0xFF554438),
    onTertiaryContainer = Color(0xFFF8DFCE),
    background = Color(0xFF151314),
    onBackground = Color(0xFFE7E1E2),
    surface = Color(0xFF151314),
    onSurface = Color(0xFFE7E1E2),
    surfaceContainer = Color(0xFF211F20),
    surfaceContainerLow = Color(0xFF1D1B1C),
    surfaceContainerHigh = Color(0xFF2B292A),
    surfaceContainerHighest = Color(0xFF363435)
)

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun TurnlyExpressiveTheme(
    isDark: Boolean,
    dynamicColor: Boolean = true,
    useExpressiveMotion: Boolean = true,
    prefs: UserPreferences = UserPreferences(),
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    val baseColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        else -> when (prefs.m3ColorPersonality) {
            "VIBRANT" -> if (isDark) DarkVibrantColors else LightVibrantColors
            "CALM" -> if (isDark) DarkCalmColors else LightCalmColors
            else -> if (isDark) DarkBalancedColors else LightBalancedColors
        }
    }

    val motionScheme = if (useExpressiveMotion && !prefs.isReduceMotion) {
        MotionScheme.expressive()
    } else {
        MotionScheme.standard()
    }

    val spacing = getSpacingForDensity(prefs.m3Density)
    val shapes = getShapesForPersonality(prefs.m3ShapePersonality, prefs.m3ShapeVariation)
    val typography = getTypographyForStyle(prefs.m3TypographyStyle, prefs.m3TypographyEmphasis)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()
                WindowCompat.setDecorFitsSystemWindows(window, false)
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !isDark
            }
        }
    }

    CompositionLocalProvider(
        LocalTurnlySpacing provides spacing
    ) {
        MaterialExpressiveTheme(
            colorScheme = baseColorScheme,
            motionScheme = motionScheme,
            shapes = shapes,
            typography = typography,
            content = content
        )
    }
}
