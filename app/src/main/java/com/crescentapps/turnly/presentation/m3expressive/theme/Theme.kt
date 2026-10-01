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
import androidx.core.graphics.ColorUtils
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

/**
 * Adjusts the saturation and lightness of a color.
 */
private fun Color.adjust(saturationMultiplier: Float, lightnessMultiplier: Float = 1f): Color {
    val hsl = FloatArray(3)
    ColorUtils.colorToHSL(this.toArgb(), hsl)
    hsl[1] = (hsl[1] * saturationMultiplier).coerceIn(0f, 1f)
    hsl[2] = (hsl[2] * lightnessMultiplier).coerceIn(0f, 1f)
    return Color(ColorUtils.HSLToColor(hsl))
}

/**
 * Applies color personality transformations to an existing scheme (useful for dynamic color).
 */
private fun applyPersonalityToScheme(scheme: ColorScheme, personality: String): ColorScheme {
    val (satMult, lightMult) = when (personality) {
        "VIBRANT" -> 1.4f to 1.0f
        "CALM" -> 0.6f to 0.95f
        "PLAYFUL" -> 1.2f to 1.05f
        "ELEGANT" -> 0.45f to 1.0f
        "MONOCHROME" -> 0.0f to 1.0f
        "NATURE" -> 0.75f to 1.0f
        else -> 1.0f to 1.0f // BALANCED
    }

    if (satMult == 1.0f && lightMult == 1.0f) return scheme

    return scheme.copy(
        primary = scheme.primary.adjust(satMult, lightMult),
        primaryContainer = scheme.primaryContainer.adjust(satMult, lightMult),
        secondary = scheme.secondary.adjust(satMult, lightMult),
        secondaryContainer = scheme.secondaryContainer.adjust(satMult, lightMult),
        tertiary = scheme.tertiary.adjust(satMult, lightMult),
        tertiaryContainer = scheme.tertiaryContainer.adjust(satMult, lightMult),
        background = scheme.background.adjust(satMult, lightMult),
        surface = scheme.surface.adjust(satMult, lightMult),
        surfaceVariant = scheme.surfaceVariant.adjust(satMult, lightMult),
        surfaceTint = scheme.surfaceTint.adjust(satMult, lightMult),
        inverseSurface = scheme.inverseSurface.adjust(satMult, lightMult),
        outline = scheme.outline.adjust(satMult, lightMult),
        outlineVariant = scheme.outlineVariant.adjust(satMult, lightMult),
        surfaceBright = scheme.surfaceBright.adjust(satMult, lightMult),
        surfaceContainer = scheme.surfaceContainer.adjust(satMult, lightMult),
        surfaceContainerHigh = scheme.surfaceContainerHigh.adjust(satMult, lightMult),
        surfaceContainerHighest = scheme.surfaceContainerHighest.adjust(satMult, lightMult),
        surfaceContainerLow = scheme.surfaceContainerLow.adjust(satMult, lightMult),
        surfaceContainerLowest = scheme.surfaceContainerLowest.adjust(satMult, lightMult),
        surfaceDim = scheme.surfaceDim.adjust(satMult, lightMult),
    )
}

// Playful Color Schemes
private val LightPlayfulColors = applyPersonalityToScheme(LightVibrantColors, "PLAYFUL").copy(
    primary = Color(0xFFF94144),
    primaryContainer = Color(0xFFFFD6D6),
    secondary = Color(0xFFF3722C),
    tertiary = Color(0xFFF9C74F)
)
private val DarkPlayfulColors = applyPersonalityToScheme(DarkVibrantColors, "PLAYFUL").copy(
    primary = Color(0xFFFF7A7A),
    primaryContainer = Color(0xFF7A0000),
    secondary = Color(0xFFFF9E70),
    tertiary = Color(0xFFFFE070)
)

// Elegant Color Schemes
private val LightElegantColors = applyPersonalityToScheme(LightBalancedColors, "ELEGANT").copy(
    primary = Color(0xFF5E5470),
    primaryContainer = Color(0xFFE4DEF0),
    secondary = Color(0xFF8C7A6B),
    tertiary = Color(0xFF6B7A8C)
)
private val DarkElegantColors = applyPersonalityToScheme(DarkBalancedColors, "ELEGANT").copy(
    primary = Color(0xFFBDB2D1),
    primaryContainer = Color(0xFF3F3551),
    secondary = Color(0xFFD4BFA9),
    tertiary = Color(0xFFA9BFD4)
)

// Monochrome Color Schemes
private val LightMonochromeColors = applyPersonalityToScheme(LightBalancedColors, "MONOCHROME")
private val DarkMonochromeColors = applyPersonalityToScheme(DarkBalancedColors, "MONOCHROME")

// Nature Color Schemes
private val LightNatureColors = applyPersonalityToScheme(LightCalmColors, "NATURE").copy(
    primary = Color(0xFF4A6B53),
    primaryContainer = Color(0xFFD3EADD),
    secondary = Color(0xFF6B654A),
    tertiary = Color(0xFF4A5F6B)
)
private val DarkNatureColors = applyPersonalityToScheme(DarkCalmColors, "NATURE").copy(
    primary = Color(0xFF90B59B),
    primaryContainer = Color(0xFF2A4A33),
    secondary = Color(0xFFB5AE90),
    tertiary = Color(0xFF90A5B5)
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
    
    val rawColorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val baseDynamic = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            applyPersonalityToScheme(baseDynamic, prefs.m3ColorPersonality)
        }
        else -> when (prefs.m3ColorPersonality) {
            "VIBRANT" -> if (isDark) DarkVibrantColors else LightVibrantColors
            "CALM" -> if (isDark) DarkCalmColors else LightCalmColors
            "PLAYFUL" -> if (isDark) DarkPlayfulColors else LightPlayfulColors
            "ELEGANT" -> if (isDark) DarkElegantColors else LightElegantColors
            "MONOCHROME" -> if (isDark) DarkMonochromeColors else LightMonochromeColors
            "NATURE" -> if (isDark) DarkNatureColors else LightNatureColors
            else -> if (isDark) DarkBalancedColors else LightBalancedColors
        }
    }

    // Apply color intensity scaling to container colors and primary tones
    val intensity = prefs.m3ColorIntensity.coerceIn(0.2f, 1.2f)
    val baseColorScheme = if (intensity != 1.0f) {
        rawColorScheme.copy(
            primaryContainer = rawColorScheme.primaryContainer.copy(
                alpha = (rawColorScheme.primaryContainer.alpha * intensity).coerceIn(0.3f, 1.0f)
            ),
            secondaryContainer = rawColorScheme.secondaryContainer.copy(
                alpha = (rawColorScheme.secondaryContainer.alpha * intensity).coerceIn(0.3f, 1.0f)
            ),
            tertiaryContainer = rawColorScheme.tertiaryContainer.copy(
                alpha = (rawColorScheme.tertiaryContainer.alpha * intensity).coerceIn(0.3f, 1.0f)
            )
        )
    } else {
        rawColorScheme
    }

    val motionScheme = if (useExpressiveMotion && !prefs.isReduceMotion) {
        MotionScheme.expressive()
    } else {
        MotionScheme.standard()
    }

    val spacing = getSpacingForDensity(prefs.m3Density)
    val shapes = getShapesForPersonality(prefs.m3ShapePersonality, prefs.m3ShapeVariation)
    val typography = getTypographyForStyle(prefs.m3TypographyStyle, prefs.m3TypographyEmphasis, prefs.m3ShapePersonality)

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
        LocalTurnlySpacing provides spacing,
        LocalUserPreferences provides prefs
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
