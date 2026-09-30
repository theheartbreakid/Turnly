package com.crescentapps.turnly.presentation.m3expressive.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class TurnlySpacing(
    val micro: Dp = 4.dp,
    val small: Dp = 8.dp,
    val component: Dp = 12.dp,
    val normal: Dp = 16.dp,
    val group: Dp = 24.dp,
    val section: Dp = 32.dp,
    val hero: Dp = 48.dp
)

fun getSpacingForDensity(density: String): TurnlySpacing = when (density) {
    "COMFORTABLE" -> TurnlySpacing(
        micro = 6.dp,
        small = 10.dp,
        component = 14.dp,
        normal = 20.dp,
        group = 28.dp,
        section = 36.dp,
        hero = 56.dp
    )
    "COMPACT" -> TurnlySpacing(
        micro = 3.dp,
        small = 6.dp,
        component = 10.dp,
        normal = 12.dp,
        group = 18.dp,
        section = 24.dp,
        hero = 36.dp
    )
    else -> TurnlySpacing(
        micro = 4.dp,
        small = 8.dp,
        component = 12.dp,
        normal = 16.dp,
        group = 24.dp,
        section = 32.dp,
        hero = 48.dp
    )
}

object ExpressiveShapes {
    val None = RoundedCornerShape(0.dp)
    val ExtraSmall = RoundedCornerShape(4.dp)
    val Small = RoundedCornerShape(8.dp)
    val Medium = RoundedCornerShape(12.dp)
    val Large = RoundedCornerShape(16.dp)
    val LargeIncreased = RoundedCornerShape(20.dp)
    val ExtraLarge = RoundedCornerShape(28.dp)
    val ExtraLargeIncreased = RoundedCornerShape(32.dp)
    val ExtraExtraLarge = RoundedCornerShape(48.dp)
    val Full = RoundedCornerShape(100)
}

fun getShapesForPersonality(personality: String, variation: String): Shapes {
    val factor = when (variation) {
        "HIGH" -> 1.25f
        "LOW" -> 0.75f
        else -> 1.0f
    }
    return when (personality) {
        "ROUNDED" -> Shapes(
            extraSmall = RoundedCornerShape((8 * factor).dp),
            small = RoundedCornerShape((12 * factor).dp),
            medium = RoundedCornerShape((18 * factor).dp),
            large = RoundedCornerShape((24 * factor).dp),
            extraLarge = RoundedCornerShape((36 * factor).dp)
        )
        "STRUCTURED" -> Shapes(
            extraSmall = RoundedCornerShape((2 * factor).dp),
            small = RoundedCornerShape((4 * factor).dp),
            medium = RoundedCornerShape((8 * factor).dp),
            large = RoundedCornerShape((12 * factor).dp),
            extraLarge = RoundedCornerShape((16 * factor).dp)
        )
        "BALANCED" -> Shapes(
            extraSmall = RoundedCornerShape((4 * factor).dp),
            small = RoundedCornerShape((8 * factor).dp),
            medium = RoundedCornerShape((12 * factor).dp),
            large = RoundedCornerShape((16 * factor).dp),
            extraLarge = RoundedCornerShape((24 * factor).dp)
        )
        else -> Shapes( // EXPRESSIVE
            extraSmall = RoundedCornerShape((6 * factor).dp),
            small = RoundedCornerShape((10 * factor).dp),
            medium = RoundedCornerShape((16 * factor).dp),
            large = RoundedCornerShape((24 * factor).dp),
            extraLarge = RoundedCornerShape((32 * factor).dp)
        )
    }
}

val ExpressiveMaterialShapes = getShapesForPersonality("EXPRESSIVE", "MEDIUM")

fun getTypographyForStyle(fontStyle: String, emphasis: String): Typography {
    val family = when (fontStyle) {
        "GOOGLE_SANS", "GOOGLE_SANS_FLEX" -> FontFamily.SansSerif
        else -> FontFamily.Default
    }
    val titleWeight = when (emphasis) {
        "STRONG" -> FontWeight.ExtraBold
        "STANDARD" -> FontWeight.SemiBold
        else -> FontWeight.Bold // EXPRESSIVE
    }
    val headlineWeight = when (emphasis) {
        "STRONG" -> FontWeight.Black
        "STANDARD" -> FontWeight.Medium
        else -> FontWeight.ExtraBold // EXPRESSIVE
    }

    return Typography(
        displayLarge = TextStyle(
            fontFamily = family,
            fontWeight = headlineWeight,
            fontSize = 57.sp,
            lineHeight = 64.sp,
            letterSpacing = (-0.25).sp
        ),
        displayMedium = TextStyle(
            fontFamily = family,
            fontWeight = headlineWeight,
            fontSize = 45.sp,
            lineHeight = 52.sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = family,
            fontWeight = headlineWeight,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp
        ),
        headlineLarge = TextStyle(
            fontFamily = family,
            fontWeight = headlineWeight,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = family,
            fontWeight = titleWeight,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = family,
            fontWeight = titleWeight,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ),
        titleLarge = TextStyle(
            fontFamily = family,
            fontWeight = titleWeight,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        bodyLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        labelLarge = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = family,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
    )
}

val ExpressiveTypography = getTypographyForStyle("SYSTEM", "EXPRESSIVE")

val LocalTurnlySpacing = staticCompositionLocalOf { TurnlySpacing() }

object ExpressiveTokens {
    val spacing: TurnlySpacing
        @Composable
        get() = LocalTurnlySpacing.current
}
