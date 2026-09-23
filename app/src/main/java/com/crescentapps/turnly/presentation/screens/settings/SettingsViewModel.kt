package com.crescentapps.turnly.presentation.screens.settings

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crescentapps.turnly.core.model.*
import com.crescentapps.turnly.core.notification.DailyNotificationScheduler
import com.crescentapps.turnly.core.util.DateUtils
import com.crescentapps.turnly.data.preferences.UserPreferences
import com.crescentapps.turnly.data.preferences.UserPreferencesRepository
import com.crescentapps.turnly.data.repository.TurnlyExportData
import com.crescentapps.turnly.data.repository.TurnlyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * SETTINGS VIEW MODEL
 * Reactive bridge exposing all DhikrCounter visual and preference controls to the UI.
 */
class SettingsViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val repository: TurnlyRepository,
    private val updateManager: com.crescentapps.turnly.core.update.UpdateManager? = null
) : ViewModel() {

    val updateState: StateFlow<com.crescentapps.turnly.core.update.model.UpdateState> =
        updateManager?.updateState ?: MutableStateFlow(com.crescentapps.turnly.core.update.model.UpdateState.Idle)

    val preferences: StateFlow<UserPreferences> = preferencesRepository.userPreferencesFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

    // In-memory live parameter overrides for instantaneous slider updates without waiting for DataStore I/O
    private val liveOverrides = MutableStateFlow<Map<String, Any>>(emptyMap())

    val effectivePreferences: StateFlow<UserPreferences> = combine(preferences, liveOverrides) { prefs, overrides ->
        if (overrides.isEmpty()) {
            prefs
        } else {
            var updated = prefs
            overrides.forEach { (key, value) ->
                when (key) {
                    "glassCornerRadius" -> updated = updated.copy(glassCornerRadius = value as Float)
                    "glassBlurRadius" -> updated = updated.copy(glassBlurRadius = value as Float)
                    "glassRefractionHeight" -> updated = updated.copy(glassRefractionHeight = value as Float)
                    "glassRefractionAmount" -> updated = updated.copy(glassRefractionAmount = value as Float)
                    "glassChromaticAberration" -> updated = updated.copy(glassChromaticAberration = value as Float)
                    "glassIOR" -> updated = updated.copy(glassIOR = value as Float)
                    "glassThickness" -> updated = updated.copy(glassThickness = value as Float)
                    "glassNormalStrength" -> updated = updated.copy(glassNormalStrength = value as Float)
                    "glassBrightness" -> updated = updated.copy(glassBrightness = value as Float)
                    "glassRimIntensity" -> updated = updated.copy(glassRimIntensity = value as Float)
                    "glassSpecularIntensity" -> updated = updated.copy(glassSpecularIntensity = value as Float)
                    "glassShininess" -> updated = updated.copy(glassShininess = value as Float)
                    "glassDisplacementScale" -> updated = updated.copy(glassDisplacementScale = value as Float)
                    "glassMinSmoothing" -> updated = updated.copy(glassMinSmoothing = value as Float)
                    "glassHighlightWidth" -> updated = updated.copy(glassHighlightWidth = value as Float)
                    "glassCausticIntensity" -> updated = updated.copy(glassCausticIntensity = value as Float)
                    "glassLiquidDome" -> updated = updated.copy(glassLiquidDome = value as Float)
                    "glassTransmittance" -> updated = updated.copy(glassTransmittance = value as Float)
                    "glassShadowIntensity" -> updated = updated.copy(glassShadowIntensity = value as Float)
                    "glassShadowSoftness" -> updated = updated.copy(glassShadowSoftness = value as Float)
                    "dockCornerRadius" -> updated = updated.copy(dockCornerRadius = value as Float)
                    "dockBlurRadius" -> updated = updated.copy(dockBlurRadius = value as Float)
                    "dockRefractionHeight" -> updated = updated.copy(dockRefractionHeight = value as Float)
                    "dockRefractionAmount" -> updated = updated.copy(dockRefractionAmount = value as Float)
                    "dockChromaticAberration" -> updated = updated.copy(dockChromaticAberration = value as Float)
                    "glassIntensity" -> updated = updated.copy(glassIntensity = value as Float)
                    "hapticIntensity" -> updated = updated.copy(hapticIntensity = value as Float)
                    "adaptiveLuminanceInterval" -> updated = updated.copy(adaptiveLuminanceInterval = value as Int)
                }
            }
            updated
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, UserPreferences())

    fun updateLiveOverride(key: String, value: Any) {
        liveOverrides.update { it + (key to value) }
    }

    private fun clearLiveOverride(key: String) {
        liveOverrides.update { it - key }
    }

    // UI Visual System Mode
    fun setUiMode(mode: UiMode) {
        viewModelScope.launch { preferencesRepository.setUiMode(mode) }
    }

    fun setHasAcceptedLiquidWarning(accepted: Boolean) {
        viewModelScope.launch { preferencesRepository.setHasAcceptedLiquidWarning(accepted) }
    }

    // Appearance
    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun setAmoled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setAmoled(enabled) }
    }

    fun setDynamicColors(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setDynamicColors(enabled) }
    }

    fun setAdaptiveLuminance(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setAdaptiveLuminance(enabled) }
    }

    fun setAdaptiveLuminanceInterval(interval: Int) {
        viewModelScope.launch { preferencesRepository.setAdaptiveLuminanceInterval(interval) }
    }

    fun setFontTintFallbackMode(mode: Int) {
        viewModelScope.launch { preferencesRepository.setFontTintFallbackMode(mode) }
    }

    fun setFontTintPaletteColor(color: Int) {
        viewModelScope.launch { preferencesRepository.setFontTintPaletteColor(color) }
    }

    fun setFontTintCustomColor(color: Int) {
        viewModelScope.launch { preferencesRepository.setFontTintCustomColor(color) }
    }

    fun setGlassIntensity(intensity: Float) {
        viewModelScope.launch { preferencesRepository.setGlassIntensity(intensity) }
    }

    fun setReduceMotion(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setReduceMotion(enabled) }
    }

    // Typography
    fun setAppFontFamily(family: String) {
        viewModelScope.launch { preferencesRepository.setAppFontFamily(family) }
    }

    fun setAppFontWeight(weight: String) {
        viewModelScope.launch { preferencesRepository.setAppFontWeight(weight) }
    }

    fun setAppFontTilt(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setAppFontTilt(enabled) }
    }

    // Universal Glass
    fun setGlassCornerRadius(radius: Float) {
        viewModelScope.launch { preferencesRepository.setGlassCornerRadius(radius) }
    }

    fun setGlassBlurRadius(radius: Float) {
        viewModelScope.launch { preferencesRepository.setGlassBlurRadius(radius) }
    }

    fun setGlassRefractionHeight(height: Float) {
        viewModelScope.launch { preferencesRepository.setGlassRefractionHeight(height) }
    }

    fun setGlassRefractionAmount(amount: Float) {
        viewModelScope.launch { preferencesRepository.setGlassRefractionAmount(amount) }
    }

    fun setGlassChromaticAberration(aberration: Float) {
        viewModelScope.launch { preferencesRepository.setGlassChromaticAberration(aberration) }
    }

    fun setGlassIOR(ior: Float) {
        viewModelScope.launch { preferencesRepository.setGlassIOR(ior) }
    }

    fun setGlassThickness(thickness: Float) {
        viewModelScope.launch { preferencesRepository.setGlassThickness(thickness) }
    }

    fun setGlassNormalStrength(strength: Float) {
        viewModelScope.launch { preferencesRepository.setGlassNormalStrength(strength) }
    }

    fun setGlassBrightness(brightness: Float) {
        viewModelScope.launch { preferencesRepository.setGlassBrightness(brightness) }
    }

    fun setGlassRimIntensity(intensity: Float) {
        viewModelScope.launch { preferencesRepository.setGlassRimIntensity(intensity) }
    }

    fun setGlassSpecularIntensity(intensity: Float) {
        viewModelScope.launch { preferencesRepository.setGlassSpecularIntensity(intensity) }
    }

    fun setGlassShininess(shininess: Float) {
        viewModelScope.launch { preferencesRepository.setGlassShininess(shininess) }
    }

    fun setGlassDisplacementScale(scale: Float) {
        viewModelScope.launch { preferencesRepository.setGlassDisplacementScale(scale) }
    }

    fun setGlassMinSmoothing(smoothing: Float) {
        viewModelScope.launch { preferencesRepository.setGlassMinSmoothing(smoothing) }
    }

    fun setGlassHighlightWidth(width: Float) {
        viewModelScope.launch { preferencesRepository.setGlassHighlightWidth(width) }
    }

    fun setGlassCausticIntensity(intensity: Float) {
        viewModelScope.launch { preferencesRepository.setGlassCausticIntensity(intensity) }
    }

    fun setGlassLiquidDome(dome: Float) {
        viewModelScope.launch { preferencesRepository.setGlassLiquidDome(dome) }
    }

    fun setGlassTransmittance(transmittance: Float) {
        viewModelScope.launch { preferencesRepository.setGlassTransmittance(transmittance) }
    }

    fun setGlassLightDirection(x: Float, y: Float) {
        viewModelScope.launch { preferencesRepository.setGlassLightDirection(x, y) }
    }

    fun setGlassShadowColor(color: Int) {
        viewModelScope.launch { preferencesRepository.setGlassShadowColor(color) }
    }

    fun setGlassShadowIntensity(intensity: Float) {
        viewModelScope.launch { preferencesRepository.setGlassShadowIntensity(intensity) }
    }

    fun setGlassShadowSoftness(softness: Float) {
        viewModelScope.launch { preferencesRepository.setGlassShadowSoftness(softness) }
    }

    fun setGlassCaptureDownsample(mode: String) {
        viewModelScope.launch { preferencesRepository.setGlassCaptureDownsample(mode) }
    }

    // Dock
    fun setDockCornerRadius(radius: Float) {
        viewModelScope.launch { preferencesRepository.setDockCornerRadius(radius) }
    }

    fun setDockBlurRadius(radius: Float) {
        viewModelScope.launch { preferencesRepository.setDockBlurRadius(radius) }
    }

    fun setDockRefractionHeight(height: Float) {
        viewModelScope.launch { preferencesRepository.setDockRefractionHeight(height) }
    }

    fun setDockRefractionAmount(amount: Float) {
        viewModelScope.launch { preferencesRepository.setDockRefractionAmount(amount) }
    }

    fun setDockChromaticAberration(aberration: Float) {
        viewModelScope.launch { preferencesRepository.setDockChromaticAberration(aberration) }
    }

    // Feedback
    fun setSoundFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setSoundFeedbackEnabled(enabled) }
    }

    fun setHapticFeedbackEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setHapticFeedbackEnabled(enabled) }
    }

    fun setHapticIntensity(intensity: Float) {
        viewModelScope.launch { preferencesRepository.setHapticIntensity(intensity) }
    }

    // Background Image
    fun setBackgroundImageUri(uri: String) {
        viewModelScope.launch { preferencesRepository.setBackgroundImageUri(uri) }
    }

    // Resets
    fun resetAppearanceSettings() {
        liveOverrides.value = emptyMap()
        viewModelScope.launch { preferencesRepository.resetAppearanceSettings() }
    }

    fun resetAllPreferences() {
        liveOverrides.value = emptyMap()
        viewModelScope.launch { preferencesRepository.resetAllSettings() }
    }

    // Notifications & System
    fun setMasterNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setMasterNotificationsEnabled(enabled) }
    }

    fun setDefaultNotificationTime(hour: Int, minute: Int, context: Context) {
        viewModelScope.launch {
            preferencesRepository.setDefaultNotificationTime(hour, minute)
            DailyNotificationScheduler.scheduleDailyAlarm(context, hour, minute)
        }
    }

    fun setFirstDayOfWeek(day: FirstDayOfWeek) {
        viewModelScope.launch { preferencesRepository.setFirstDayOfWeek(day) }
    }

    fun setSyncOverMobileData(enabled: Boolean, context: Context) {
        viewModelScope.launch {
            preferencesRepository.setSyncOverMobileData(enabled)
            com.crescentapps.turnly.data.sync.SyncWorker.schedulePeriodicSync(context, enabled)
        }
    }

    fun setOnlineSyncEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setOnlineSyncEnabled(enabled) }
    }

    fun setUserDisplayName(name: String) {
        viewModelScope.launch { preferencesRepository.setUserDisplayName(name) }
    }

    private val exportJson = Json { prettyPrint = true }

    fun exportData(context: Context) {
        viewModelScope.launch {
            val exportData = repository.exportAllData()
            val jsonString = exportJson.encodeToString<TurnlyExportData>(exportData)

            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, jsonString)
                type = "application/json"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Export Turnly Data")
            shareIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(shareIntent)
        }
    }

    fun createDemoData() {
        viewModelScope.launch {
            val p1Id = repository.saveParticipant(Participant(name = "Mohsin", colorHex = "#3B82F6"))
            val p2Id = repository.saveParticipant(Participant(name = "Mom", colorHex = "#EC4899"))
            val p3Id = repository.saveParticipant(Participant(name = "Dad", colorHex = "#10B981"))

            val schedule1 = Schedule(
                name = "Family Savings",
                description = "Daily contribution rotation",
                type = ScheduleType.MONEY,
                currencyCode = "INR",
                defaultAmount = 500.0,
                frequencyType = FrequencyType.DAILY,
                frequencyInterval = 1,
                startDate = DateUtils.today().minusDays(3).let { DateUtils.format(it) }
            )
            repository.saveScheduleWithDetails(
                schedule = schedule1,
                participantIds = listOf(p1Id, p2Id, p3Id),
                patternParticipantIds = listOf(p1Id, p2Id, p3Id)
            )

            val schedule2 = Schedule(
                name = "Kitchen Cleaning",
                description = "Daily evening dishwashing & counter cleanup",
                type = ScheduleType.TASK,
                frequencyType = FrequencyType.DAILY,
                frequencyInterval = 1,
                startDate = DateUtils.today().minusDays(1).let { DateUtils.format(it) }
            )
            repository.saveScheduleWithDetails(
                schedule = schedule2,
                participantIds = listOf(p2Id, p3Id, p1Id),
                patternParticipantIds = listOf(p2Id, p3Id, p1Id)
            )
        }
    }

    fun resetAllData() {
        viewModelScope.launch {
            repository.resetDatabase()
        }
    }

    // UPDATES ACTIONS
    fun checkForUpdates() {
        viewModelScope.launch {
            updateManager?.checkForUpdates(isManual = true)
            preferencesRepository.setLastUpdateCheckTimestamp(System.currentTimeMillis())
        }
    }

    fun startDownload(updateInfo: com.crescentapps.turnly.core.update.model.UpdateInfo) {
        updateManager?.startDownload(updateInfo)
    }

    fun cancelDownload() {
        updateManager?.cancelDownload()
    }

    fun installUpdate(apkFile: java.io.File, updateInfo: com.crescentapps.turnly.core.update.model.UpdateInfo): Boolean {
        return updateManager?.installApk(apkFile, updateInfo) ?: false
    }

    fun onReturnFromSettings() {
        updateManager?.onReturnFromSettings()
    }

    fun dismissUpdateDialog() {
        updateManager?.dismissState()
    }

    fun createManageUnknownAppSourcesIntent(): Intent? {
        return updateManager?.createManageUnknownAppSourcesIntent()
    }

    fun setUpdateCheckFrequency(frequency: UpdateFrequency, context: Context) {
        viewModelScope.launch {
            preferencesRepository.setUpdateCheckFrequency(frequency)
            com.crescentapps.turnly.core.update.UpdateCheckWorker.schedule(context, frequency)
        }
    }
}
