package com.crescentapps.turnly.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.crescentapps.turnly.core.model.FirstDayOfWeek
import com.crescentapps.turnly.core.model.ThemeMode
import com.crescentapps.turnly.core.model.UiMode
import com.crescentapps.turnly.core.model.UpdateFrequency
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "turnly_preferences")

/**
 * COMPLETE TURNLY USER PREFERENCES & SETTINGS MODEL
 * Directly migrated from DhikrCounter settings architecture.
 * Holds all appearance, optics, typography, feedback, dock, and background parameters.
 */
data class UserPreferences(
    // UI Visual System Mode
    val uiMode: UiMode = UiMode.MATERIAL_3,
    val hasAcceptedLiquidWarning: Boolean = false,

    // Appearance
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val isAmoled: Boolean = false,
    val dynamicColors: Boolean = true,
    val adaptiveLuminance: Boolean = true,
    val adaptiveLuminanceInterval: Int = 1000,
    val fontTintFallbackMode: Int = 0,
    val fontTintPaletteColor: Int = 0xFF6366F1.toInt(),
    val fontTintCustomColor: Int = 0xFF6366F1.toInt(),
    val glassIntensity: Float = 1.0f,
    val isReduceMotion: Boolean = false,

    // Typography
    val appFontFamily: String = "SF Pro Text",
    val appFontWeight: String = "Regular",
    val appFontTilt: Boolean = false,

    // Universal Glass Optics
    val glassCornerRadius: Float = 28f,
    val glassBlurRadius: Float = 2.8f,
    val glassRefractionHeight: Float = 12f,
    val glassRefractionAmount: Float = 24f,
    val glassChromaticAberration: Float = 0.00f,
    val glassIOR: Float = 1.52f,
    val glassThickness: Float = 18f,
    val glassNormalStrength: Float = 1.1f,
    val glassBrightness: Float = 1.0f,
    val glassRimIntensity: Float = 0.85f,
    val glassSpecularIntensity: Float = 1.0f,
    val glassShininess: Float = 56f,
    val glassDisplacementScale: Float = 0.9f,
    val glassMinSmoothing: Float = 1.8f,
    val glassHighlightWidth: Float = 3.5f,
    val glassCausticIntensity: Float = 0.1f,
    val glassLiquidDome: Float = 0.7f,
    val glassTransmittance: Float = 1.0f,
    val glassLightDirX: Float = -0.5f,
    val glassLightDirY: Float = -0.8f,
    val glassShadowColor: Int = 0x00000000,
    val glassShadowIntensity: Float = 0.18f,
    val glassShadowSoftness: Float = 0.2f,
    val glassCaptureDownsample: String = "balanced",

    // Dock Glass Optics
    val dockCornerRadius: Float = 32f,
    val dockBlurRadius: Float = 8f,
    val dockRefractionHeight: Float = 24f,
    val dockRefractionAmount: Float = 24f,
    val dockChromaticAberration: Float = 0.01f,

    // Feedback
    val soundFeedbackEnabled: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true,
    val hapticIntensity: Float = 1.0f,

    // Background Image
    val backgroundImageUri: String = "",

    // System & Notifications
    val masterNotificationsEnabled: Boolean = true,
    val defaultNotificationHour: Int = 9,
    val defaultNotificationMinute: Int = 0,
    val firstDayOfWeek: FirstDayOfWeek = FirstDayOfWeek.MONDAY,
    val hasCompletedOnboarding: Boolean = false,
    val userDisplayName: String = "User",
    val deviceMemberId: String = "",
    val syncOverMobileData: Boolean = true,
    val onlineSyncEnabled: Boolean = true,

    // App Updates
    val updateCheckFrequency: UpdateFrequency = UpdateFrequency.DAILY,
    val lastUpdateCheckTimestamp: Long = 0L
)

class UserPreferencesRepository(private val context: Context) {

    object Defaults {
        const val DEFAULT_GLASS_CORNER_RADIUS = 28f
        const val DEFAULT_GLASS_BLUR_RADIUS = 2.8f
        const val DEFAULT_GLASS_REFRACTION_HEIGHT = 12f
        const val DEFAULT_GLASS_REFRACTION_AMOUNT = 24f
        const val DEFAULT_GLASS_CHROMATIC_ABERRATION = 0.00f
        const val DEFAULT_GLASS_INTENSITY = 1.0f
        const val DEFAULT_GLASS_IOR = 1.52f
        const val DEFAULT_GLASS_THICKNESS = 18f
        const val DEFAULT_GLASS_NORMAL_STRENGTH = 1.1f
        const val DEFAULT_GLASS_BRIGHTNESS = 1.0f
        const val DEFAULT_GLASS_RIM_INTENSITY = 0.85f
        const val DEFAULT_GLASS_SPECULAR_INTENSITY = 1.0f
        const val DEFAULT_GLASS_SHININESS = 56f
        const val DEFAULT_GLASS_DISPLACEMENT_SCALE = 0.9f
        const val DEFAULT_GLASS_MIN_SMOOTHING = 1.8f
        const val DEFAULT_GLASS_HIGHLIGHT_WIDTH = 3.5f
        const val DEFAULT_GLASS_CAUSTIC_INTENSITY = 0.1f
        const val DEFAULT_GLASS_LIQUID_DOME = 0.7f
        const val DEFAULT_GLASS_TRANSMITTANCE = 1.0f
        const val DEFAULT_GLASS_LIGHT_DIR_X = -0.5f
        const val DEFAULT_GLASS_LIGHT_DIR_Y = -0.8f
        const val DEFAULT_GLASS_SHADOW_COLOR = 0x00000000
        const val DEFAULT_GLASS_SHADOW_INTENSITY = 0.18f
        const val DEFAULT_GLASS_SHADOW_SOFTNESS = 0.2f
        const val DEFAULT_GLASS_CAPTURE_DOWNSAMPLE = "balanced"

        const val DEFAULT_DOCK_CORNER_RADIUS = 32f
        const val DEFAULT_DOCK_BLUR_RADIUS = 8f
        const val DEFAULT_DOCK_REFRACTION_HEIGHT = 24f
        const val DEFAULT_DOCK_REFRACTION_AMOUNT = 24f
        const val DEFAULT_DOCK_CHROMATIC_ABERRATION = 0.01f

        const val DEFAULT_ADAPTIVE_LUMINANCE_INTERVAL = 1000
        const val DEFAULT_FONT_FAMILY = "SF Pro Text"
        const val DEFAULT_FONT_WEIGHT = "Regular"
        const val DEFAULT_HAPTIC_INTENSITY = 1.0f
    }

    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_AMOLED = booleanPreferencesKey("is_amoled")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
        val ADAPTIVE_LUMINANCE = booleanPreferencesKey("adaptive_luminance")
        val ADAPTIVE_LUMINANCE_INTERVAL = intPreferencesKey("adaptive_luminance_interval")
        val FONT_TINT_FALLBACK_MODE = intPreferencesKey("font_tint_fallback_mode")
        val FONT_TINT_PALETTE_COLOR = intPreferencesKey("font_tint_palette_color")
        val FONT_TINT_CUSTOM_COLOR = intPreferencesKey("font_tint_custom_color")
        val GLASS_INTENSITY = floatPreferencesKey("glass_intensity")
        val IS_REDUCE_MOTION = booleanPreferencesKey("is_reduce_motion")

        val APP_FONT_FAMILY = stringPreferencesKey("app_font_family")
        val APP_FONT_WEIGHT = stringPreferencesKey("app_font_weight")
        val APP_FONT_TILT = booleanPreferencesKey("app_font_tilt")

        val GLASS_CORNER_RADIUS = floatPreferencesKey("glass_corner_radius")
        val GLASS_BLUR_RADIUS = floatPreferencesKey("glass_blur_radius")
        val GLASS_REFRACTION_HEIGHT = floatPreferencesKey("glass_refraction_height")
        val GLASS_REFRACTION_AMOUNT = floatPreferencesKey("glass_refraction_amount")
        val GLASS_CHROMATIC_ABERRATION = floatPreferencesKey("glass_chromatic_aberration")
        val GLASS_IOR = floatPreferencesKey("glass_ior")
        val GLASS_THICKNESS = floatPreferencesKey("glass_thickness")
        val GLASS_NORMAL_STRENGTH = floatPreferencesKey("glass_normal_strength")
        val GLASS_BRIGHTNESS = floatPreferencesKey("glass_brightness")
        val GLASS_RIM_INTENSITY = floatPreferencesKey("glass_rim_intensity")
        val GLASS_SPECULAR_INTENSITY = floatPreferencesKey("glass_specular_intensity")
        val GLASS_SHININESS = floatPreferencesKey("glass_shininess")
        val GLASS_DISPLACEMENT_SCALE = floatPreferencesKey("glass_displacement_scale")
        val GLASS_MIN_SMOOTHING = floatPreferencesKey("glass_min_smoothing")
        val GLASS_HIGHLIGHT_WIDTH = floatPreferencesKey("glass_highlight_width")
        val GLASS_CAUSTIC_INTENSITY = floatPreferencesKey("glass_caustic_intensity")
        val GLASS_LIQUID_DOME = floatPreferencesKey("glass_liquid_dome")
        val GLASS_TRANSMITTANCE = floatPreferencesKey("glass_transmittance")
        val GLASS_LIGHT_DIR_X = floatPreferencesKey("glass_light_dir_x")
        val GLASS_LIGHT_DIR_Y = floatPreferencesKey("glass_light_dir_y")
        val GLASS_SHADOW_COLOR = intPreferencesKey("glass_shadow_color")
        val GLASS_SHADOW_INTENSITY = floatPreferencesKey("glass_shadow_intensity")
        val GLASS_SHADOW_SOFTNESS = floatPreferencesKey("glass_shadow_softness")
        val GLASS_CAPTURE_DOWNSAMPLE = stringPreferencesKey("glass_capture_downsample")

        val DOCK_CORNER_RADIUS = floatPreferencesKey("dock_corner_radius")
        val DOCK_BLUR_RADIUS = floatPreferencesKey("dock_blur_radius")
        val DOCK_REFRACTION_HEIGHT = floatPreferencesKey("dock_refraction_height")
        val DOCK_REFRACTION_AMOUNT = floatPreferencesKey("dock_refraction_amount")
        val DOCK_CHROMATIC_ABERRATION = floatPreferencesKey("dock_chromatic_aberration")

        val SOUND_FEEDBACK_ENABLED = booleanPreferencesKey("sound_feedback_enabled")
        val HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        val HAPTIC_INTENSITY = floatPreferencesKey("haptic_intensity")

        val BACKGROUND_IMAGE_URI = stringPreferencesKey("background_image_uri")

        val UI_MODE = stringPreferencesKey("ui_mode")
        val HAS_ACCEPTED_LIQUID_WARNING = booleanPreferencesKey("has_accepted_liquid_warning")

        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val NOTIFY_HOUR = intPreferencesKey("notify_hour")
        val NOTIFY_MINUTE = intPreferencesKey("notify_minute")
        val FIRST_DAY_OF_WEEK = stringPreferencesKey("first_day_of_week")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val USER_DISPLAY_NAME = stringPreferencesKey("user_display_name")
        val DEVICE_MEMBER_ID = stringPreferencesKey("device_member_id")
        val SYNC_OVER_MOBILE = booleanPreferencesKey("sync_over_mobile")
        val ONLINE_SYNC_ENABLED = booleanPreferencesKey("online_sync_enabled")
        val UPDATE_CHECK_FREQUENCY = stringPreferencesKey("update_check_frequency")
        val LAST_UPDATE_CHECK_TIMESTAMP = longPreferencesKey("last_update_check_timestamp")
    }

    val userPreferencesFlow: Flow<UserPreferences> = context.dataStore.data.map { preferences ->
        val uiModeStr = preferences[PreferencesKeys.UI_MODE] ?: UiMode.MATERIAL_3.name
        val uiMode = runCatching { UiMode.valueOf(uiModeStr) }.getOrDefault(UiMode.MATERIAL_3)
        val hasAcceptedLiquid = preferences[PreferencesKeys.HAS_ACCEPTED_LIQUID_WARNING] ?: false

        val themeStr = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
        val theme = runCatching { ThemeMode.valueOf(themeStr) }.getOrDefault(ThemeMode.SYSTEM)

        val firstDayStr = preferences[PreferencesKeys.FIRST_DAY_OF_WEEK] ?: FirstDayOfWeek.MONDAY.name
        val firstDay = runCatching { FirstDayOfWeek.valueOf(firstDayStr) }.getOrDefault(FirstDayOfWeek.MONDAY)

        var memberId = preferences[PreferencesKeys.DEVICE_MEMBER_ID]
        if (memberId.isNullOrBlank()) {
            memberId = java.util.UUID.randomUUID().toString().replace("-", "").take(12)
        }

        UserPreferences(
            uiMode = uiMode,
            hasAcceptedLiquidWarning = hasAcceptedLiquid,
            themeMode = theme,
            isAmoled = preferences[PreferencesKeys.IS_AMOLED] ?: false,
            dynamicColors = preferences[PreferencesKeys.DYNAMIC_COLORS] ?: true,
            adaptiveLuminance = preferences[PreferencesKeys.ADAPTIVE_LUMINANCE] ?: true,
            adaptiveLuminanceInterval = preferences[PreferencesKeys.ADAPTIVE_LUMINANCE_INTERVAL] ?: Defaults.DEFAULT_ADAPTIVE_LUMINANCE_INTERVAL,
            fontTintFallbackMode = preferences[PreferencesKeys.FONT_TINT_FALLBACK_MODE] ?: 0,
            fontTintPaletteColor = preferences[PreferencesKeys.FONT_TINT_PALETTE_COLOR] ?: 0xFF6366F1.toInt(),
            fontTintCustomColor = preferences[PreferencesKeys.FONT_TINT_CUSTOM_COLOR] ?: 0xFF6366F1.toInt(),
            glassIntensity = (preferences[PreferencesKeys.GLASS_INTENSITY] ?: Defaults.DEFAULT_GLASS_INTENSITY).coerceIn(0f, 1f),
            isReduceMotion = preferences[PreferencesKeys.IS_REDUCE_MOTION] ?: false,

            appFontFamily = preferences[PreferencesKeys.APP_FONT_FAMILY] ?: Defaults.DEFAULT_FONT_FAMILY,
            appFontWeight = preferences[PreferencesKeys.APP_FONT_WEIGHT] ?: Defaults.DEFAULT_FONT_WEIGHT,
            appFontTilt = preferences[PreferencesKeys.APP_FONT_TILT] ?: false,

            glassCornerRadius = (preferences[PreferencesKeys.GLASS_CORNER_RADIUS] ?: Defaults.DEFAULT_GLASS_CORNER_RADIUS).coerceIn(0f, 100f),
            glassBlurRadius = (preferences[PreferencesKeys.GLASS_BLUR_RADIUS] ?: Defaults.DEFAULT_GLASS_BLUR_RADIUS).coerceIn(0f, 100f),
            glassRefractionHeight = (preferences[PreferencesKeys.GLASS_REFRACTION_HEIGHT] ?: Defaults.DEFAULT_GLASS_REFRACTION_HEIGHT).coerceIn(0f, 100f),
            glassRefractionAmount = (preferences[PreferencesKeys.GLASS_REFRACTION_AMOUNT] ?: Defaults.DEFAULT_GLASS_REFRACTION_AMOUNT).coerceIn(0f, 100f),
            glassChromaticAberration = (preferences[PreferencesKeys.GLASS_CHROMATIC_ABERRATION] ?: Defaults.DEFAULT_GLASS_CHROMATIC_ABERRATION).coerceIn(0f, 1f),
            glassIOR = (preferences[PreferencesKeys.GLASS_IOR] ?: Defaults.DEFAULT_GLASS_IOR).coerceIn(1f, 3f),
            glassThickness = (preferences[PreferencesKeys.GLASS_THICKNESS] ?: Defaults.DEFAULT_GLASS_THICKNESS).coerceIn(0f, 100f),
            glassNormalStrength = (preferences[PreferencesKeys.GLASS_NORMAL_STRENGTH] ?: Defaults.DEFAULT_GLASS_NORMAL_STRENGTH).coerceIn(0f, 5f),
            glassBrightness = (preferences[PreferencesKeys.GLASS_BRIGHTNESS] ?: Defaults.DEFAULT_GLASS_BRIGHTNESS).coerceIn(0f, 2f),
            glassRimIntensity = (preferences[PreferencesKeys.GLASS_RIM_INTENSITY] ?: Defaults.DEFAULT_GLASS_RIM_INTENSITY).coerceIn(0f, 2f),
            glassSpecularIntensity = (preferences[PreferencesKeys.GLASS_SPECULAR_INTENSITY] ?: Defaults.DEFAULT_GLASS_SPECULAR_INTENSITY).coerceIn(0f, 2f),
            glassShininess = (preferences[PreferencesKeys.GLASS_SHININESS] ?: Defaults.DEFAULT_GLASS_SHININESS).coerceIn(1f, 128f),
            glassDisplacementScale = (preferences[PreferencesKeys.GLASS_DISPLACEMENT_SCALE] ?: Defaults.DEFAULT_GLASS_DISPLACEMENT_SCALE).coerceIn(0f, 2f),
            glassMinSmoothing = (preferences[PreferencesKeys.GLASS_MIN_SMOOTHING] ?: Defaults.DEFAULT_GLASS_MIN_SMOOTHING).coerceIn(0f, 10f),
            glassHighlightWidth = (preferences[PreferencesKeys.GLASS_HIGHLIGHT_WIDTH] ?: Defaults.DEFAULT_GLASS_HIGHLIGHT_WIDTH).coerceIn(0f, 20f),
            glassCausticIntensity = (preferences[PreferencesKeys.GLASS_CAUSTIC_INTENSITY] ?: Defaults.DEFAULT_GLASS_CAUSTIC_INTENSITY).coerceIn(0f, 2f),
            glassLiquidDome = (preferences[PreferencesKeys.GLASS_LIQUID_DOME] ?: Defaults.DEFAULT_GLASS_LIQUID_DOME).coerceIn(0f, 2f),
            glassTransmittance = (preferences[PreferencesKeys.GLASS_TRANSMITTANCE] ?: Defaults.DEFAULT_GLASS_TRANSMITTANCE).coerceIn(0f, 1f),
            glassLightDirX = (preferences[PreferencesKeys.GLASS_LIGHT_DIR_X] ?: Defaults.DEFAULT_GLASS_LIGHT_DIR_X).coerceIn(-1f, 1f),
            glassLightDirY = (preferences[PreferencesKeys.GLASS_LIGHT_DIR_Y] ?: Defaults.DEFAULT_GLASS_LIGHT_DIR_Y).coerceIn(-1f, 1f),
            glassShadowColor = preferences[PreferencesKeys.GLASS_SHADOW_COLOR] ?: Defaults.DEFAULT_GLASS_SHADOW_COLOR,
            glassShadowIntensity = (preferences[PreferencesKeys.GLASS_SHADOW_INTENSITY] ?: Defaults.DEFAULT_GLASS_SHADOW_INTENSITY).coerceIn(0f, 1f),
            glassShadowSoftness = (preferences[PreferencesKeys.GLASS_SHADOW_SOFTNESS] ?: Defaults.DEFAULT_GLASS_SHADOW_SOFTNESS).coerceIn(0f, 1f),
            glassCaptureDownsample = preferences[PreferencesKeys.GLASS_CAPTURE_DOWNSAMPLE] ?: Defaults.DEFAULT_GLASS_CAPTURE_DOWNSAMPLE,

            dockCornerRadius = (preferences[PreferencesKeys.DOCK_CORNER_RADIUS] ?: Defaults.DEFAULT_DOCK_CORNER_RADIUS).coerceIn(0f, 100f),
            dockBlurRadius = (preferences[PreferencesKeys.DOCK_BLUR_RADIUS] ?: Defaults.DEFAULT_DOCK_BLUR_RADIUS).coerceIn(0f, 100f),
            dockRefractionHeight = (preferences[PreferencesKeys.DOCK_REFRACTION_HEIGHT] ?: Defaults.DEFAULT_DOCK_REFRACTION_HEIGHT).coerceIn(0f, 100f),
            dockRefractionAmount = (preferences[PreferencesKeys.DOCK_REFRACTION_AMOUNT] ?: Defaults.DEFAULT_DOCK_REFRACTION_AMOUNT).coerceIn(0f, 100f),
            dockChromaticAberration = (preferences[PreferencesKeys.DOCK_CHROMATIC_ABERRATION] ?: Defaults.DEFAULT_DOCK_CHROMATIC_ABERRATION).coerceIn(0f, 1f),

            soundFeedbackEnabled = preferences[PreferencesKeys.SOUND_FEEDBACK_ENABLED] ?: false,
            hapticFeedbackEnabled = preferences[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] ?: true,
            hapticIntensity = (preferences[PreferencesKeys.HAPTIC_INTENSITY] ?: Defaults.DEFAULT_HAPTIC_INTENSITY).coerceIn(0f, 1f),

            backgroundImageUri = preferences[PreferencesKeys.BACKGROUND_IMAGE_URI] ?: "",

            masterNotificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
            defaultNotificationHour = preferences[PreferencesKeys.NOTIFY_HOUR] ?: 9,
            defaultNotificationMinute = preferences[PreferencesKeys.NOTIFY_MINUTE] ?: 0,
            firstDayOfWeek = firstDay,
            hasCompletedOnboarding = preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false,
            userDisplayName = preferences[PreferencesKeys.USER_DISPLAY_NAME] ?: "User",
            deviceMemberId = memberId,
            syncOverMobileData = preferences[PreferencesKeys.SYNC_OVER_MOBILE] ?: true,
            onlineSyncEnabled = preferences[PreferencesKeys.ONLINE_SYNC_ENABLED] ?: true,
            updateCheckFrequency = runCatching {
                UpdateFrequency.valueOf(preferences[PreferencesKeys.UPDATE_CHECK_FREQUENCY] ?: UpdateFrequency.DAILY.name)
            }.getOrDefault(UpdateFrequency.DAILY),
            lastUpdateCheckTimestamp = preferences[PreferencesKeys.LAST_UPDATE_CHECK_TIMESTAMP] ?: 0L
        )
    }

    suspend fun setUiMode(mode: UiMode) {
        context.dataStore.edit { it[PreferencesKeys.UI_MODE] = mode.name }
    }

    suspend fun setHasAcceptedLiquidWarning(accepted: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.HAS_ACCEPTED_LIQUID_WARNING] = accepted }
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[PreferencesKeys.THEME_MODE] = mode.name }
    }

    suspend fun setAmoled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.IS_AMOLED] = enabled }
    }

    suspend fun setDynamicColors(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.DYNAMIC_COLORS] = enabled }
    }

    suspend fun setAdaptiveLuminance(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ADAPTIVE_LUMINANCE] = enabled }
    }

    suspend fun setAdaptiveLuminanceInterval(interval: Int) {
        context.dataStore.edit { it[PreferencesKeys.ADAPTIVE_LUMINANCE_INTERVAL] = interval.coerceIn(100, 3000) }
    }

    suspend fun setFontTintFallbackMode(mode: Int) {
        context.dataStore.edit { it[PreferencesKeys.FONT_TINT_FALLBACK_MODE] = mode }
    }

    suspend fun setFontTintPaletteColor(color: Int) {
        context.dataStore.edit { it[PreferencesKeys.FONT_TINT_PALETTE_COLOR] = color }
    }

    suspend fun setFontTintCustomColor(color: Int) {
        context.dataStore.edit { it[PreferencesKeys.FONT_TINT_CUSTOM_COLOR] = color }
    }

    suspend fun setGlassIntensity(intensity: Float) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_INTENSITY] = intensity.coerceIn(0f, 1f) }
    }

    suspend fun setReduceMotion(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.IS_REDUCE_MOTION] = enabled }
    }

    suspend fun setAppFontFamily(family: String) {
        context.dataStore.edit { it[PreferencesKeys.APP_FONT_FAMILY] = family }
    }

    suspend fun setAppFontWeight(weight: String) {
        context.dataStore.edit { it[PreferencesKeys.APP_FONT_WEIGHT] = weight }
    }

    suspend fun setAppFontTilt(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.APP_FONT_TILT] = enabled }
    }

    suspend fun setGlassCornerRadius(radius: Float) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_CORNER_RADIUS] = radius.coerceIn(0f, 100f) }
    }

    suspend fun setGlassBlurRadius(radius: Float) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_BLUR_RADIUS] = radius.coerceIn(0f, 100f) }
    }

    suspend fun setGlassRefractionHeight(height: Float) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_REFRACTION_HEIGHT] = height.coerceIn(0f, 100f) }
    }

    suspend fun setGlassRefractionAmount(amount: Float) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_REFRACTION_AMOUNT] = amount.coerceIn(0f, 100f) }
    }

    suspend fun setGlassChromaticAberration(aberration: Float) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_CHROMATIC_ABERRATION] = aberration.coerceIn(0f, 1f) }
    }

    suspend fun setGlassIOR(ior: Float) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_IOR] = ior.coerceIn(1f, 3f) }
    }

    suspend fun setGlassThickness(thickness: Float) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_THICKNESS] = thickness.coerceIn(0f, 100f) }
    }

    suspend fun setGlassNormalStrength(strength: Float) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_NORMAL_STRENGTH] = strength.coerceIn(0f, 5f) }
    }

    suspend fun setGlassBrightness(brightness: Float) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_BRIGHTNESS] = brightness.coerceIn(0f, 2f) }
    }

    suspend fun setGlassRimIntensity(intensity: Float) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_RIM_INTENSITY] = intensity.coerceIn(0f, 2f) }
    }

    suspend fun setGlassSpecularIntensity(intensity: Float) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_SPECULAR_INTENSITY] = intensity.coerceIn(0f, 2f) }
    }

    suspend fun setGlassShininess(shininess: Float) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_SHININESS] = shininess.coerceIn(1f, 128f) }
    }

    suspend fun setGlassDisplacementScale(scale: Float) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_DISPLACEMENT_SCALE] = scale.coerceIn(0f, 2f) }
    }

    suspend fun setGlassMinSmoothing(smoothing: Float) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_MIN_SMOOTHING] = smoothing.coerceIn(0f, 10f) }
    }

    suspend fun setGlassHighlightWidth(width: Float) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_HIGHLIGHT_WIDTH] = width.coerceIn(0f, 20f) }
    }

    suspend fun setGlassCausticIntensity(intensity: Float) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_CAUSTIC_INTENSITY] = intensity.coerceIn(0f, 2f) }
    }

    suspend fun setGlassLiquidDome(dome: Float) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_LIQUID_DOME] = dome.coerceIn(0f, 2f) }
    }

    suspend fun setGlassTransmittance(transmittance: Float) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_TRANSMITTANCE] = transmittance.coerceIn(0f, 1f) }
    }

    suspend fun setGlassLightDirection(x: Float, y: Float) {
        context.dataStore.edit {
            it[PreferencesKeys.GLASS_LIGHT_DIR_X] = x.coerceIn(-1f, 1f)
            it[PreferencesKeys.GLASS_LIGHT_DIR_Y] = y.coerceIn(-1f, 1f)
        }
    }

    suspend fun setGlassShadowColor(color: Int) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_SHADOW_COLOR] = color }
    }

    suspend fun setGlassShadowIntensity(intensity: Float) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_SHADOW_INTENSITY] = intensity.coerceIn(0f, 1f) }
    }

    suspend fun setGlassShadowSoftness(softness: Float) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_SHADOW_SOFTNESS] = softness.coerceIn(0f, 1f) }
    }

    suspend fun setGlassCaptureDownsample(mode: String) {
        context.dataStore.edit { it[PreferencesKeys.GLASS_CAPTURE_DOWNSAMPLE] = mode }
    }

    suspend fun setDockCornerRadius(radius: Float) {
        context.dataStore.edit { it[PreferencesKeys.DOCK_CORNER_RADIUS] = radius.coerceIn(0f, 100f) }
    }

    suspend fun setDockBlurRadius(radius: Float) {
        context.dataStore.edit { it[PreferencesKeys.DOCK_BLUR_RADIUS] = radius.coerceIn(0f, 100f) }
    }

    suspend fun setDockRefractionHeight(height: Float) {
        context.dataStore.edit { it[PreferencesKeys.DOCK_REFRACTION_HEIGHT] = height.coerceIn(0f, 100f) }
    }

    suspend fun setDockRefractionAmount(amount: Float) {
        context.dataStore.edit { it[PreferencesKeys.DOCK_REFRACTION_AMOUNT] = amount.coerceIn(0f, 100f) }
    }

    suspend fun setDockChromaticAberration(aberration: Float) {
        context.dataStore.edit { it[PreferencesKeys.DOCK_CHROMATIC_ABERRATION] = aberration.coerceIn(0f, 1f) }
    }

    suspend fun setSoundFeedbackEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SOUND_FEEDBACK_ENABLED] = enabled }
    }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.HAPTIC_FEEDBACK_ENABLED] = enabled }
    }

    suspend fun setHapticIntensity(intensity: Float) {
        context.dataStore.edit { it[PreferencesKeys.HAPTIC_INTENSITY] = intensity.coerceIn(0f, 1f) }
    }

    suspend fun setBackgroundImageUri(uri: String) {
        context.dataStore.edit { it[PreferencesKeys.BACKGROUND_IMAGE_URI] = uri }
    }

    suspend fun setMasterNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setDefaultNotificationTime(hour: Int, minute: Int) {
        context.dataStore.edit {
            it[PreferencesKeys.NOTIFY_HOUR] = hour
            it[PreferencesKeys.NOTIFY_MINUTE] = minute
        }
    }

    suspend fun setFirstDayOfWeek(day: FirstDayOfWeek) {
        context.dataStore.edit { it[PreferencesKeys.FIRST_DAY_OF_WEEK] = day.name }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setUserDisplayName(name: String) {
        context.dataStore.edit { it[PreferencesKeys.USER_DISPLAY_NAME] = name }
    }

    suspend fun setSyncOverMobileData(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.SYNC_OVER_MOBILE] = enabled }
    }

    suspend fun setOnlineSyncEnabled(enabled: Boolean) {
        context.dataStore.edit { it[PreferencesKeys.ONLINE_SYNC_ENABLED] = enabled }
    }

    suspend fun resetAppearanceSettings() {
        context.dataStore.edit {
            it.remove(PreferencesKeys.GLASS_CORNER_RADIUS)
            it.remove(PreferencesKeys.GLASS_BLUR_RADIUS)
            it.remove(PreferencesKeys.GLASS_REFRACTION_HEIGHT)
            it.remove(PreferencesKeys.GLASS_REFRACTION_AMOUNT)
            it.remove(PreferencesKeys.GLASS_CHROMATIC_ABERRATION)
            it.remove(PreferencesKeys.GLASS_IOR)
            it.remove(PreferencesKeys.GLASS_THICKNESS)
            it.remove(PreferencesKeys.GLASS_NORMAL_STRENGTH)
            it.remove(PreferencesKeys.GLASS_BRIGHTNESS)
            it.remove(PreferencesKeys.GLASS_RIM_INTENSITY)
            it.remove(PreferencesKeys.GLASS_SPECULAR_INTENSITY)
            it.remove(PreferencesKeys.GLASS_SHININESS)
            it.remove(PreferencesKeys.GLASS_DISPLACEMENT_SCALE)
            it.remove(PreferencesKeys.GLASS_MIN_SMOOTHING)
            it.remove(PreferencesKeys.GLASS_HIGHLIGHT_WIDTH)
            it.remove(PreferencesKeys.GLASS_CAUSTIC_INTENSITY)
            it.remove(PreferencesKeys.GLASS_LIQUID_DOME)
            it.remove(PreferencesKeys.GLASS_TRANSMITTANCE)
            it.remove(PreferencesKeys.GLASS_LIGHT_DIR_X)
            it.remove(PreferencesKeys.GLASS_LIGHT_DIR_Y)
            it.remove(PreferencesKeys.GLASS_SHADOW_COLOR)
            it.remove(PreferencesKeys.GLASS_SHADOW_INTENSITY)
            it.remove(PreferencesKeys.GLASS_SHADOW_SOFTNESS)
            it.remove(PreferencesKeys.GLASS_CAPTURE_DOWNSAMPLE)
            it.remove(PreferencesKeys.GLASS_INTENSITY)
            it.remove(PreferencesKeys.DOCK_CORNER_RADIUS)
            it.remove(PreferencesKeys.DOCK_BLUR_RADIUS)
            it.remove(PreferencesKeys.DOCK_REFRACTION_HEIGHT)
            it.remove(PreferencesKeys.DOCK_REFRACTION_AMOUNT)
            it.remove(PreferencesKeys.DOCK_CHROMATIC_ABERRATION)
            it.remove(PreferencesKeys.DYNAMIC_COLORS)
            it.remove(PreferencesKeys.ADAPTIVE_LUMINANCE)
            it.remove(PreferencesKeys.ADAPTIVE_LUMINANCE_INTERVAL)
            it.remove(PreferencesKeys.FONT_TINT_FALLBACK_MODE)
            it.remove(PreferencesKeys.FONT_TINT_PALETTE_COLOR)
            it.remove(PreferencesKeys.FONT_TINT_CUSTOM_COLOR)
            it.remove(PreferencesKeys.APP_FONT_FAMILY)
            it.remove(PreferencesKeys.APP_FONT_WEIGHT)
            it.remove(PreferencesKeys.APP_FONT_TILT)
            it.remove(PreferencesKeys.BACKGROUND_IMAGE_URI)
        }
    }

    suspend fun resetAllSettings() {
        resetAppearanceSettings()
        context.dataStore.edit {
            it.remove(PreferencesKeys.THEME_MODE)
            it.remove(PreferencesKeys.IS_AMOLED)
            it.remove(PreferencesKeys.IS_REDUCE_MOTION)
            it.remove(PreferencesKeys.SOUND_FEEDBACK_ENABLED)
            it.remove(PreferencesKeys.HAPTIC_FEEDBACK_ENABLED)
            it.remove(PreferencesKeys.HAPTIC_INTENSITY)
            it.remove(PreferencesKeys.NOTIFICATIONS_ENABLED)
            it.remove(PreferencesKeys.NOTIFY_HOUR)
            it.remove(PreferencesKeys.NOTIFY_MINUTE)
            it.remove(PreferencesKeys.FIRST_DAY_OF_WEEK)
            it.remove(PreferencesKeys.SYNC_OVER_MOBILE)
            it.remove(PreferencesKeys.ONLINE_SYNC_ENABLED)
            it.remove(PreferencesKeys.UPDATE_CHECK_FREQUENCY)
            it.remove(PreferencesKeys.LAST_UPDATE_CHECK_TIMESTAMP)
        }
    }

    suspend fun setUpdateCheckFrequency(frequency: UpdateFrequency) {
        context.dataStore.edit { it[PreferencesKeys.UPDATE_CHECK_FREQUENCY] = frequency.name }
    }

    suspend fun setLastUpdateCheckTimestamp(timestamp: Long) {
        context.dataStore.edit { it[PreferencesKeys.LAST_UPDATE_CHECK_TIMESTAMP] = timestamp }
    }
}
