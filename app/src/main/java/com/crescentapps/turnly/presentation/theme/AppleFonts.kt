package com.crescentapps.turnly.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import com.crescentapps.turnly.R

val SfProText = FontFamily(
    Font(R.font.sf_pro_text_thin, FontWeight.Thin),
    Font(R.font.sf_pro_text_light, FontWeight.Light),
    Font(R.font.sf_pro_text_regular, FontWeight.Normal),
    Font(R.font.sf_pro_text_medium, FontWeight.Medium),
    Font(R.font.sf_pro_text_semibold, FontWeight.SemiBold),
    Font(R.font.sf_pro_text_bold, FontWeight.Bold),
    Font(R.font.sf_pro_text_heavy, FontWeight.ExtraBold)
)

val SfProRounded = FontFamily(
    Font(R.font.sf_pro_rounded_thin, FontWeight.Thin),
    Font(R.font.sf_pro_rounded_light, FontWeight.Light),
    Font(R.font.sf_pro_rounded_regular, FontWeight.Normal),
    Font(R.font.sf_pro_rounded_medium, FontWeight.Medium),
    Font(R.font.sf_pro_rounded_semibold, FontWeight.SemiBold),
    Font(R.font.sf_pro_rounded_bold, FontWeight.Bold),
    Font(R.font.sf_pro_rounded_black, FontWeight.Black)
)

enum class AppleFontStyle(val label: String, val fontFamily: FontFamily) {
    TEXT("SF Pro Text", SfProText),
    ROUNDED("SF Pro Rounded", SfProRounded);

    companion object {
        fun fromLabel(label: String): AppleFontStyle = entries.find { it.label == label } ?: TEXT
    }
}

enum class AppleFontWeight(val label: String, val weight: FontWeight) {
    THIN("Thin", FontWeight.Thin),
    LIGHT("Light", FontWeight.Light),
    REGULAR("Regular", FontWeight.Normal),
    MEDIUM("Medium", FontWeight.Medium),
    SEMIBOLD("Semibold", FontWeight.SemiBold),
    BOLD("Bold", FontWeight.Bold),
    HEAVY("Heavy", FontWeight.ExtraBold),
    BLACK("Black", FontWeight.Black);

    companion object {
        fun fromLabel(label: String): AppleFontWeight = entries.find { it.label == label } ?: REGULAR
    }
}

/**
 * Creates an authoritative Typography suite configuring font family, base weight,
 * and font tilt (synthetic italic/oblique fallback).
 */
fun buildTurnlyTypography(
    fontFamily: FontFamily,
    fontWeight: FontWeight,
    isTilt: Boolean = false
): Typography {
    val fontStyle = if (isTilt) FontStyle.Italic else FontStyle.Normal
    val fontSynthesis = if (isTilt) FontSynthesis.All else FontSynthesis.Weight

    fun styleWithWeight(w: FontWeight): TextStyle = TextStyle(
        fontFamily = fontFamily,
        fontWeight = w,
        fontStyle = fontStyle,
        fontSynthesis = fontSynthesis
    )

    // Base default style respects selected weight & tilt
    val baseStyle = styleWithWeight(fontWeight)

    return Typography(
        displayLarge = styleWithWeight(FontWeight.Bold).copy(fontStyle = fontStyle, fontSynthesis = fontSynthesis),
        displayMedium = styleWithWeight(FontWeight.Bold).copy(fontStyle = fontStyle, fontSynthesis = fontSynthesis),
        displaySmall = styleWithWeight(FontWeight.Bold).copy(fontStyle = fontStyle, fontSynthesis = fontSynthesis),
        headlineLarge = styleWithWeight(FontWeight.Black).copy(fontStyle = fontStyle, fontSynthesis = fontSynthesis),
        headlineMedium = styleWithWeight(FontWeight.Bold).copy(fontStyle = fontStyle, fontSynthesis = fontSynthesis),
        headlineSmall = styleWithWeight(FontWeight.SemiBold).copy(fontStyle = fontStyle, fontSynthesis = fontSynthesis),
        titleLarge = styleWithWeight(FontWeight.Bold).copy(fontStyle = fontStyle, fontSynthesis = fontSynthesis),
        titleMedium = styleWithWeight(FontWeight.SemiBold).copy(fontStyle = fontStyle, fontSynthesis = fontSynthesis),
        titleSmall = styleWithWeight(FontWeight.Medium).copy(fontStyle = fontStyle, fontSynthesis = fontSynthesis),
        bodyLarge = baseStyle,
        bodyMedium = baseStyle,
        bodySmall = styleWithWeight(FontWeight.Normal).copy(fontStyle = fontStyle, fontSynthesis = fontSynthesis),
        labelLarge = styleWithWeight(FontWeight.SemiBold).copy(fontStyle = fontStyle, fontSynthesis = fontSynthesis),
        labelMedium = styleWithWeight(FontWeight.Medium).copy(fontStyle = fontStyle, fontSynthesis = fontSynthesis),
        labelSmall = styleWithWeight(FontWeight.Normal).copy(fontStyle = fontStyle, fontSynthesis = fontSynthesis)
    )
}
