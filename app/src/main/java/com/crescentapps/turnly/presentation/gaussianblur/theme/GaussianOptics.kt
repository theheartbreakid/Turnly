package com.crescentapps.turnly.presentation.gaussianblur.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import com.crescentapps.turnly.data.preferences.UserPreferences

@Immutable
data class GaussianOptics(
    val blurRadiusDp: Float,
    val surfaceOpacity: Float,
    val refractionEnabled: Boolean,
    val refractionHeightDp: Float,
    val refractionAmountDp: Float,
    val specularEnabled: Boolean,
    val specularIntensity: Float,
    val dynamicHighlightsEnabled: Boolean,
    val dynamicHighlightsIntensity: Float,
    val condensedLightEnabled: Boolean,
    val condensedLightRadius: Float,
    val reduceMotion: Boolean
) {
    companion object {
        fun fromPreferences(prefs: UserPreferences): GaussianOptics {
            return GaussianOptics(
                blurRadiusDp = prefs.gaussianBlurRadius,
                surfaceOpacity = prefs.gaussianSurfaceOpacity,
                refractionEnabled = prefs.gaussianRefractionEnabled,
                refractionHeightDp = prefs.gaussianRefractionStrength * 24f, // Scale strength to Dp
                refractionAmountDp = prefs.gaussianRefractionStrength * 48f,
                specularEnabled = prefs.gaussianSpecularEnabled,
                specularIntensity = prefs.gaussianSpecularIntensity,
                dynamicHighlightsEnabled = prefs.gaussianDynamicHighlightsEnabled,
                dynamicHighlightsIntensity = prefs.gaussianDynamicHighlightsIntensity,
                condensedLightEnabled = prefs.gaussianCondensedLightEnabled,
                condensedLightRadius = prefs.gaussianCondensedLightRadius,
                reduceMotion = prefs.isReduceMotion
            )
        }
    }
}

val LocalGaussianOptics = staticCompositionLocalOf {
    GaussianOptics(
        blurRadiusDp = 24f,
        surfaceOpacity = 0.40f,
        refractionEnabled = true,
        refractionHeightDp = 12f,
        refractionAmountDp = 24f,
        specularEnabled = true,
        specularIntensity = 1.0f,
        dynamicHighlightsEnabled = true,
        dynamicHighlightsIntensity = 1.0f,
        condensedLightEnabled = true,
        condensedLightRadius = 48f,
        reduceMotion = false
    )
}
