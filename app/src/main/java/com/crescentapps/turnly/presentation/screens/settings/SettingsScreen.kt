package com.crescentapps.turnly.presentation.screens.settings

import android.app.TimePickerDialog
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import com.crescentapps.turnly.core.model.UpdateFrequency
import com.crescentapps.turnly.core.update.model.UpdateState
import java.text.SimpleDateFormat
import java.util.Date
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.core.model.FirstDayOfWeek
import com.crescentapps.turnly.core.model.ThemeMode
import com.crescentapps.turnly.data.preferences.UserPreferencesRepository
import com.crescentapps.turnly.presentation.catalog.utils.LocalBackdrop
import com.crescentapps.turnly.presentation.components.AdaptiveIcon
import com.crescentapps.turnly.presentation.components.GlassSettings
import com.crescentapps.turnly.presentation.components.LocalAppWindowSizeDetails
import com.crescentapps.turnly.presentation.components.LocalGlassSettings
import com.crescentapps.turnly.presentation.components.LocalPrismalAdaptiveColor
import com.crescentapps.turnly.presentation.components.LocalScrollInProgress
import com.crescentapps.turnly.presentation.components.PrismalCard
import com.crescentapps.turnly.presentation.components.liquid.LiquidButton
import com.crescentapps.turnly.presentation.components.liquid.LiquidCard
import com.crescentapps.turnly.presentation.components.liquid.LiquidDialog
import com.crescentapps.turnly.presentation.components.liquid.LiquidSlider
import com.crescentapps.turnly.presentation.components.liquid.LiquidToggle
import com.crescentapps.turnly.presentation.theme.AppleFontStyle
import com.crescentapps.turnly.presentation.theme.AppleFontWeight
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import java.util.Locale

/**
 * COMPLETE TURNLY SETTINGS SCREEN
 * Exact 1:1 DhikrCounter Settings Architecture and Controls
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.effectivePreferences.collectAsState()
    val context = LocalContext.current
    val backdrop = LocalBackdrop.current ?: rememberLayerBackdrop()
    val adaptiveColor = LocalPrismalAdaptiveColor.current
    val listState = rememberLazyListState()

    val sizeDetails = LocalAppWindowSizeDetails.current
    val isWide = sizeDetails.widthClass == com.crescentapps.turnly.presentation.components.AppWindowWidthSizeClass.EXPANDED

    // Dialog visibility states
    var showThemeDialog by remember { mutableStateOf(false) }
    var showFontTintDialog by remember { mutableStateOf(false) }
    var showFontFamilyDialog by remember { mutableStateOf(false) }
    var showFontWeightDialog by remember { mutableStateOf(false) }
    var showFirstDayDialog by remember { mutableStateOf(false) }
    var showResetAppearanceDialog by remember { mutableStateOf(false) }
    var showResetAllSettingsDialog by remember { mutableStateOf(false) }
    var showResetDataDialog by remember { mutableStateOf(false) }
    var showFrequencyDialog by remember { mutableStateOf(false) }

    val updateState by viewModel.updateState.collectAsState()

    val backgroundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                viewModel.setBackgroundImageUri(it.toString())
            } catch (e: Exception) {
                viewModel.setBackgroundImageUri(it.toString())
            }
        }
    }

    val liveGlassSettings = remember(prefs) {
        GlassSettings.fromPreferences(prefs)
    }

    CompositionLocalProvider(
        LocalScrollInProgress provides listState.isScrollInProgress,
        LocalGlassSettings provides liveGlassSettings
    ) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 700.dp)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = if (isWide) 48.dp else 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
            item(key = "title_settings") {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = adaptiveColor,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            // LIVE PRISMAL GLASS PREVIEW SHOWCASE
            if (prefs.uiMode == com.crescentapps.turnly.core.model.UiMode.LIQUID) {
                item(key = "header_prismal_preview") { SettingsSectionHeader("Live Prismal Glass Preview") }
                item(key = "card_prismal_preview") {
                    PrismalCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        shape = RoundedCornerShape(24.dp),
                        tonalColor = adaptiveColor.copy(alpha = 0.08f)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.AutoAwesome,
                                        contentDescription = null,
                                        tint = adaptiveColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Prismal OpenGL Surface",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = adaptiveColor
                                    )
                                }
                                Text(
                                    text = "Active IOR: ${String.format(Locale.ROOT, "%.2f", liveGlassSettings.ior)}  ·  Normal: ${String.format(Locale.ROOT, "%.2f", liveGlassSettings.normalStrength)}  ·  Shininess: ${liveGlassSettings.shininess.toInt()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = adaptiveColor.copy(alpha = 0.85f)
                                )
                                Text(
                                    text = "Uniforms update dynamically on drag with 0 frame drops",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = adaptiveColor.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }

            // UI STYLE / MODE SELECTION
            item(key = "header_uimode") { SettingsSectionHeader("UI Style") }
            item(key = "card_uimode") {
                LiquidCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), backdrop = backdrop) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setUiMode(com.crescentapps.turnly.core.model.UiMode.MATERIAL_3) }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(
                                selected = prefs.uiMode == com.crescentapps.turnly.core.model.UiMode.MATERIAL_3,
                                onClick = { viewModel.setUiMode(com.crescentapps.turnly.core.model.UiMode.MATERIAL_3) }
                            )
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Material 3", fontWeight = FontWeight.Bold, color = adaptiveColor, fontSize = 16.sp)
                                    Text("Stable · Recommended", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                                }
                                Text("Standard Material 3 design, fast performance, accessible contrast", fontSize = 12.sp, color = adaptiveColor.copy(alpha = 0.65f))
                            }
                        }

                        HorizontalDivider(color = adaptiveColor.copy(alpha = 0.05f))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            RadioButton(
                                selected = prefs.uiMode == com.crescentapps.turnly.core.model.UiMode.LIQUID,
                                onClick = { /* already active */ }
                            )
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Liquid UI", fontWeight = FontWeight.Bold, color = adaptiveColor, fontSize = 16.sp)
                                    Text("Experimental", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.tertiary, fontSize = 11.sp)
                                }
                                Text("Prismal glass, refraction shaders, fluid dynamic backgrounds", fontSize = 12.sp, color = adaptiveColor.copy(alpha = 0.65f))
                            }
                        }
                    }
                }
            }

            // APPEARANCE SECTION
            item(key = "header_appearance") { SettingsSectionHeader("Appearance") }
            item(key = "card_appearance") {
                LiquidCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), backdrop = backdrop) {
                    Column {
                        SettingsNavigationItem(
                            title = "Theme Mode",
                            value = prefs.themeMode.name.lowercase().replaceFirstChar { it.uppercase() },
                            icon = Icons.Outlined.Palette
                        ) { showThemeDialog = true }

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        SettingsToggleItem(
                            title = "AMOLED Pitch Black",
                            subtitle = "Pure black in dark theme",
                            icon = Icons.Outlined.DarkMode,
                            checked = prefs.isAmoled,
                            backdrop = backdrop
                        ) { viewModel.setAmoled(it) }

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        SettingsToggleItem(
                            title = "Dynamic Colors",
                            subtitle = "Use Android Material You palette",
                            icon = Icons.Outlined.ColorLens,
                            checked = prefs.dynamicColors,
                            backdrop = backdrop
                        ) { viewModel.setDynamicColors(it) }

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        SettingsToggleItem(
                            title = "Adaptive Luminance",
                            subtitle = "Auto-adjust text contrast from background",
                            icon = Icons.Outlined.BrightnessMedium,
                            checked = prefs.adaptiveLuminance,
                            backdrop = backdrop
                        ) { viewModel.setAdaptiveLuminance(it) }

                        AnimatedVisibility(
                            visible = prefs.adaptiveLuminance,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))
                                GlassEffectSlider(
                                    title = "Luminance Refresh",
                                    value = prefs.adaptiveLuminanceInterval.toFloat(),
                                    range = 100f..3000f,
                                    unit = "ms",
                                    backdrop = backdrop,
                                    defaultValue = UserPreferencesRepository.Defaults.DEFAULT_ADAPTIVE_LUMINANCE_INTERVAL.toFloat(),
                                    onValueChange = { viewModel.updateLiveOverride("adaptiveLuminanceInterval", it.toInt()) },
                                    onValueChangeFinished = { viewModel.setAdaptiveLuminanceInterval(it.toInt()) }
                                )
                            }
                        }

                        AnimatedVisibility(
                            visible = !prefs.adaptiveLuminance,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column {
                                HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))
                                SettingsNavigationItem(
                                    title = "Font Tint (Fallback)",
                                    value = when (prefs.fontTintFallbackMode) {
                                        0 -> "Auto from background"
                                        1 -> "Predefined palette"
                                        else -> "Custom color picker"
                                    },
                                    icon = Icons.Outlined.InvertColors
                                ) { showFontTintDialog = true }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        GlassEffectSlider(
                            title = "Glass Intensity",
                            value = prefs.glassIntensity * 100f,
                            range = 0f..100f,
                            unit = "%",
                            backdrop = backdrop,
                            defaultValue = UserPreferencesRepository.Defaults.DEFAULT_GLASS_INTENSITY * 100f,
                            onValueChange = { viewModel.updateLiveOverride("glassIntensity", it / 100f) },
                            onValueChangeFinished = { viewModel.setGlassIntensity(it / 100f) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        SettingsToggleItem(
                            title = "Reduce Motion",
                            subtitle = "Disable continuous decorative animations for power and comfort",
                            icon = Icons.Outlined.MotionPhotosOff,
                            checked = prefs.isReduceMotion,
                            backdrop = backdrop
                        ) { viewModel.setReduceMotion(it) }
                    }
                }
            }

            // TYPOGRAPHY SECTION
            item(key = "header_typography") { SettingsSectionHeader("Typography") }
            item(key = "card_typography") {
                LiquidCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), backdrop = backdrop) {
                    Column {
                        SettingsNavigationItem(
                            title = "Font Family",
                            value = prefs.appFontFamily,
                            icon = Icons.Outlined.FontDownload
                        ) { showFontFamilyDialog = true }

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        SettingsNavigationItem(
                            title = "Font Weight",
                            value = prefs.appFontWeight,
                            icon = Icons.Outlined.FormatBold
                        ) { showFontWeightDialog = true }

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        SettingsToggleItem(
                            title = "Font Tilt / Fallback",
                            subtitle = "Enable italic styling with synthetic oblique fallback",
                            icon = Icons.Outlined.FormatItalic,
                            checked = prefs.appFontTilt,
                            backdrop = backdrop
                        ) { viewModel.setAppFontTilt(it) }
                    }
                }
            }

            // UNIVERSAL GLASS SETTINGS
            if (prefs.uiMode == com.crescentapps.turnly.core.model.UiMode.LIQUID) {
                item(key = "header_universal_glass") { SettingsSectionHeader("Universal Glass Settings") }
                item(key = "card_universal_glass") {
                    LiquidCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), backdrop = backdrop) {
                    Column {
                        GlassEffectSlider(
                            title = "Corner Radius",
                            value = prefs.glassCornerRadius,
                            range = 0f..100f,
                            unit = "dp",
                            backdrop = backdrop,
                            defaultValue = UserPreferencesRepository.Defaults.DEFAULT_GLASS_CORNER_RADIUS,
                            onValueChange = { viewModel.updateLiveOverride("glassCornerRadius", it) },
                            onValueChangeFinished = { viewModel.setGlassCornerRadius(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        GlassEffectSlider(
                            title = "Blur Radius",
                            value = prefs.glassBlurRadius,
                            range = 0f..100f,
                            unit = "dp",
                            backdrop = backdrop,
                            defaultValue = UserPreferencesRepository.Defaults.DEFAULT_GLASS_BLUR_RADIUS,
                            onValueChange = { viewModel.updateLiveOverride("glassBlurRadius", it) },
                            onValueChangeFinished = { viewModel.setGlassBlurRadius(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        GlassEffectSlider(
                            title = "Refraction Height",
                            value = prefs.glassRefractionHeight,
                            range = 0f..100f,
                            unit = "dp",
                            backdrop = backdrop,
                            defaultValue = UserPreferencesRepository.Defaults.DEFAULT_GLASS_REFRACTION_HEIGHT,
                            onValueChange = { viewModel.updateLiveOverride("glassRefractionHeight", it) },
                            onValueChangeFinished = { viewModel.setGlassRefractionHeight(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        GlassEffectSlider(
                            title = "Refraction Amount",
                            value = prefs.glassRefractionAmount,
                            range = 0f..100f,
                            unit = "dp",
                            backdrop = backdrop,
                            defaultValue = UserPreferencesRepository.Defaults.DEFAULT_GLASS_REFRACTION_AMOUNT,
                            onValueChange = { viewModel.updateLiveOverride("glassRefractionAmount", it) },
                            onValueChangeFinished = { viewModel.setGlassRefractionAmount(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        GlassEffectSlider(
                            title = "Chromatic Aberration",
                            value = prefs.glassChromaticAberration,
                            range = 0f..1f,
                            unit = "normalized",
                            backdrop = backdrop,
                            defaultValue = 0.00f,
                            onValueChange = { viewModel.updateLiveOverride("glassChromaticAberration", it) },
                            onValueChangeFinished = { viewModel.setGlassChromaticAberration(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        GlassEffectSlider(
                            title = "Index of Refraction (IOR)",
                            value = prefs.glassIOR,
                            range = 1f..3f,
                            unit = "normalized",
                            backdrop = backdrop,
                            defaultValue = 1.52f,
                            onValueChange = { viewModel.updateLiveOverride("glassIOR", it) },
                            onValueChangeFinished = { viewModel.setGlassIOR(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        GlassEffectSlider(
                            title = "Glass Thickness",
                            value = prefs.glassThickness,
                            range = 0f..100f,
                            unit = "dp",
                            backdrop = backdrop,
                            defaultValue = 18f,
                            onValueChange = { viewModel.updateLiveOverride("glassThickness", it) },
                            onValueChangeFinished = { viewModel.setGlassThickness(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        GlassEffectSlider(
                            title = "Normal Strength",
                            value = prefs.glassNormalStrength,
                            range = 0f..5f,
                            unit = "normalized",
                            backdrop = backdrop,
                            defaultValue = 1.1f,
                            onValueChange = { viewModel.updateLiveOverride("glassNormalStrength", it) },
                            onValueChangeFinished = { viewModel.setGlassNormalStrength(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        GlassEffectSlider(
                            title = "Brightness Boost",
                            value = prefs.glassBrightness * 100f,
                            range = 0f..200f,
                            unit = "%",
                            backdrop = backdrop,
                            defaultValue = 100f,
                            onValueChange = { viewModel.updateLiveOverride("glassBrightness", it / 100f) },
                            onValueChangeFinished = { viewModel.setGlassBrightness(it / 100f) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        GlassEffectSlider(
                            title = "Rim Intensity",
                            value = prefs.glassRimIntensity * 100f,
                            range = 0f..200f,
                            unit = "%",
                            backdrop = backdrop,
                            defaultValue = 85f,
                            onValueChange = { viewModel.updateLiveOverride("glassRimIntensity", it / 100f) },
                            onValueChangeFinished = { viewModel.setGlassRimIntensity(it / 100f) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        GlassEffectSlider(
                            title = "Specular Intensity",
                            value = prefs.glassSpecularIntensity * 100f,
                            range = 0f..200f,
                            unit = "%",
                            backdrop = backdrop,
                            defaultValue = 100f,
                            onValueChange = { viewModel.updateLiveOverride("glassSpecularIntensity", it / 100f) },
                            onValueChangeFinished = { viewModel.setGlassSpecularIntensity(it / 100f) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        GlassEffectSlider(
                            title = "Shininess",
                            value = prefs.glassShininess,
                            range = 1f..128f,
                            unit = "normalized",
                            backdrop = backdrop,
                            defaultValue = 56f,
                            onValueChange = { viewModel.updateLiveOverride("glassShininess", it) },
                            onValueChangeFinished = { viewModel.setGlassShininess(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        GlassEffectSlider(
                            title = "Displacement Scale",
                            value = prefs.glassDisplacementScale,
                            range = 0f..2f,
                            unit = "normalized",
                            backdrop = backdrop,
                            defaultValue = 0.9f,
                            onValueChange = { viewModel.updateLiveOverride("glassDisplacementScale", it) },
                            onValueChangeFinished = { viewModel.setGlassDisplacementScale(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        GlassEffectSlider(
                            title = "Min Smoothing",
                            value = prefs.glassMinSmoothing,
                            range = 0f..10f,
                            unit = "normalized",
                            backdrop = backdrop,
                            defaultValue = 1.8f,
                            onValueChange = { viewModel.updateLiveOverride("glassMinSmoothing", it) },
                            onValueChangeFinished = { viewModel.setGlassMinSmoothing(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        GlassEffectSlider(
                            title = "Highlight Width",
                            value = prefs.glassHighlightWidth,
                            range = 0f..20f,
                            unit = "normalized",
                            backdrop = backdrop,
                            defaultValue = 3.5f,
                            onValueChange = { viewModel.updateLiveOverride("glassHighlightWidth", it) },
                            onValueChangeFinished = { viewModel.setGlassHighlightWidth(it) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        GlassEffectSlider(
                            title = "Caustic Intensity",
                            value = prefs.glassCausticIntensity * 100f,
                            range = 0f..200f,
                            unit = "%",
                            backdrop = backdrop,
                            defaultValue = 10f,
                            onValueChange = { viewModel.updateLiveOverride("glassCausticIntensity", it / 100f) },
                            onValueChangeFinished = { viewModel.setGlassCausticIntensity(it / 100f) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        GlassEffectSlider(
                            title = "Liquid Dome",
                            value = prefs.glassLiquidDome * 100f,
                            range = 0f..200f,
                            unit = "%",
                            backdrop = backdrop,
                            defaultValue = 70f,
                            onValueChange = { viewModel.updateLiveOverride("glassLiquidDome", it / 100f) },
                            onValueChangeFinished = { viewModel.setGlassLiquidDome(it / 100f) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        GlassEffectSlider(
                            title = "Transmittance",
                            value = prefs.glassTransmittance * 100f,
                            range = 0f..100f,
                            unit = "%",
                            backdrop = backdrop,
                            defaultValue = 100f,
                            onValueChange = { viewModel.updateLiveOverride("glassTransmittance", it / 100f) },
                            onValueChangeFinished = { viewModel.setGlassTransmittance(it / 100f) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        GlassEffectSlider(
                            title = "Shadow Intensity",
                            value = prefs.glassShadowIntensity * 100f,
                            range = 0f..100f,
                            unit = "%",
                            backdrop = backdrop,
                            defaultValue = 18f,
                            onValueChange = { viewModel.updateLiveOverride("glassShadowIntensity", it / 100f) },
                            onValueChangeFinished = { viewModel.setGlassShadowIntensity(it / 100f) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        GlassEffectSlider(
                            title = "Shadow Softness",
                            value = prefs.glassShadowSoftness * 100f,
                            range = 0f..100f,
                            unit = "%",
                            backdrop = backdrop,
                            defaultValue = 20f,
                            onValueChange = { viewModel.updateLiveOverride("glassShadowSoftness", it / 100f) },
                            onValueChangeFinished = { viewModel.setGlassShadowSoftness(it / 100f) }
                        )
                    }
                }
            }
            }

            // PREFERENCES SECTION
            item(key = "header_preferences") { SettingsSectionHeader("Preferences") }
            item(key = "card_preferences") {
                LiquidCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), backdrop = backdrop) {
                    Column {
                        SettingsToggleItem(
                            title = "Sound Feedback",
                            subtitle = "Audio feedback for turn completions and actions",
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            checked = prefs.soundFeedbackEnabled,
                            backdrop = backdrop
                        ) { viewModel.setSoundFeedbackEnabled(it) }

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        SettingsToggleItem(
                            title = "Haptic Feedback",
                            subtitle = "Tactile vibration responses",
                            icon = Icons.Outlined.Vibration,
                            checked = prefs.hapticFeedbackEnabled,
                            backdrop = backdrop
                        ) { viewModel.setHapticFeedbackEnabled(it) }

                        GlassEffectSlider(
                            title = "Haptic Intensity",
                            value = prefs.hapticIntensity * 100f,
                            range = 0f..100f,
                            unit = "%",
                            backdrop = backdrop,
                            defaultValue = UserPreferencesRepository.Defaults.DEFAULT_HAPTIC_INTENSITY * 100f,
                            onValueChange = { viewModel.updateLiveOverride("hapticIntensity", it / 100f) },
                            onValueChangeFinished = { viewModel.setHapticIntensity(it / 100f) }
                        )

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        SettingsToggleItem(
                            title = "Daily Turn Reminders",
                            subtitle = "Notify when it is your turn",
                            icon = Icons.Outlined.Notifications,
                            checked = prefs.masterNotificationsEnabled,
                            backdrop = backdrop
                        ) { viewModel.setMasterNotificationsEnabled(it) }

                        if (prefs.masterNotificationsEnabled) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))
                            SettingsNavigationItem(
                                title = "Reminder Time",
                                value = String.format(Locale.getDefault(), "%02d:%02d", prefs.defaultNotificationHour, prefs.defaultNotificationMinute),
                                icon = Icons.Outlined.Schedule
                            ) {
                                TimePickerDialog(
                                    context,
                                    { _, hourOfDay, minute ->
                                        viewModel.setDefaultNotificationTime(hourOfDay, minute, context)
                                    },
                                    prefs.defaultNotificationHour,
                                    prefs.defaultNotificationMinute,
                                    true
                                ).show()
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        SettingsNavigationItem(
                            title = "First Day of Week",
                            value = prefs.firstDayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                            icon = Icons.Outlined.CalendarToday
                        ) { showFirstDayDialog = true }

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        SettingsToggleItem(
                            title = "Sync Over Mobile Data",
                            subtitle = "Allow P2P room sync without Wi-Fi",
                            icon = Icons.Outlined.SignalCellularAlt,
                            checked = prefs.syncOverMobileData,
                            backdrop = backdrop
                        ) { viewModel.setSyncOverMobileData(it, context) }
                    }
                }
            }

            // UPDATES SECTION
            item(key = "header_updates") { SettingsSectionHeader("Updates") }
            item(key = "card_updates") {
                LiquidCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), backdrop = backdrop) {
                    Column {
                        val lastCheckedText = if (prefs.lastUpdateCheckTimestamp > 0) {
                            val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
                            "Last checked: " + sdf.format(Date(prefs.lastUpdateCheckTimestamp))
                        } else {
                            "Not checked yet"
                        }

                        val checkingSubtitle = when (updateState) {
                            is UpdateState.Checking -> "Checking for updates..."
                            is UpdateState.UpToDate -> "You're up to date · $lastCheckedText"
                            is UpdateState.UpdateAvailable -> "Update available! · $lastCheckedText"
                            is UpdateState.Downloading -> "Downloading update..."
                            else -> lastCheckedText
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                if (updateState is UpdateState.Checking) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp, color = adaptiveColor)
                                } else {
                                    Icon(Icons.Outlined.SystemUpdate, null, tint = adaptiveColor, modifier = Modifier.size(24.dp))
                                }
                                Column {
                                    Text("Check for Updates", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = adaptiveColor)
                                    Text(checkingSubtitle, style = MaterialTheme.typography.bodySmall, color = adaptiveColor.copy(alpha = 0.6f))
                                }
                            }
                            LiquidButton(
                                onClick = { viewModel.checkForUpdates() },
                                backdrop = backdrop,
                                surfaceColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                            ) {
                                Text(
                                    if (updateState is UpdateState.Checking) "Checking..." else "Check Now",
                                    color = adaptiveColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                        SettingsNavigationItem(
                            title = "Automatic Update Checks",
                            value = prefs.updateCheckFrequency.displayName,
                            icon = Icons.Outlined.Update
                        ) { showFrequencyDialog = true }
                    }
                }
            }

            // DOCK SETTINGS SECTION
            if (prefs.uiMode == com.crescentapps.turnly.core.model.UiMode.LIQUID) {
                item(key = "header_dock_settings") { SettingsSectionHeader("Dock Settings") }
                item(key = "card_dock_settings") {
                    LiquidCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), backdrop = backdrop) {
                        Column {
                            GlassEffectSlider(
                                title = "Corner Radius",
                                value = prefs.dockCornerRadius,
                                range = 0f..100f,
                                unit = "dp",
                                backdrop = backdrop,
                                defaultValue = UserPreferencesRepository.Defaults.DEFAULT_DOCK_CORNER_RADIUS,
                                onValueChange = { viewModel.updateLiveOverride("dockCornerRadius", it) },
                                onValueChangeFinished = { viewModel.setDockCornerRadius(it) }
                            )

                            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                            GlassEffectSlider(
                                title = "Blur Radius",
                                value = prefs.dockBlurRadius,
                                range = 0f..100f,
                                unit = "dp",
                                backdrop = backdrop,
                                defaultValue = UserPreferencesRepository.Defaults.DEFAULT_DOCK_BLUR_RADIUS,
                                onValueChange = { viewModel.updateLiveOverride("dockBlurRadius", it) },
                                onValueChangeFinished = { viewModel.setDockBlurRadius(it) }
                            )

                            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                            GlassEffectSlider(
                                title = "Refraction Height",
                                value = prefs.dockRefractionHeight,
                                range = 0f..100f,
                                unit = "dp",
                                backdrop = backdrop,
                                defaultValue = UserPreferencesRepository.Defaults.DEFAULT_DOCK_REFRACTION_HEIGHT,
                                onValueChange = { viewModel.updateLiveOverride("dockRefractionHeight", it) },
                                onValueChangeFinished = { viewModel.setDockRefractionHeight(it) }
                            )

                            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                            GlassEffectSlider(
                                title = "Refraction Amount",
                                value = prefs.dockRefractionAmount,
                                range = 0f..100f,
                                unit = "dp",
                                backdrop = backdrop,
                                defaultValue = UserPreferencesRepository.Defaults.DEFAULT_DOCK_REFRACTION_AMOUNT,
                                onValueChange = { viewModel.updateLiveOverride("dockRefractionAmount", it) },
                                onValueChangeFinished = { viewModel.setDockRefractionAmount(it) }
                            )

                            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))

                            GlassEffectSlider(
                                title = "Chromatic Aberration",
                                value = prefs.dockChromaticAberration,
                                range = 0f..1f,
                                unit = "normalized",
                                backdrop = backdrop,
                                defaultValue = UserPreferencesRepository.Defaults.DEFAULT_DOCK_CHROMATIC_ABERRATION,
                                onValueChange = { viewModel.updateLiveOverride("dockChromaticAberration", it) },
                                onValueChangeFinished = { viewModel.setDockChromaticAberration(it) }
                            )
                        }
                    }
                }
            }

            // BACKGROUND SECTION
            item(key = "header_background") { SettingsSectionHeader("Background") }
            item(key = "card_background") {
                LiquidCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), backdrop = backdrop) {
                    Column {
                        SettingsNavigationItem(
                            title = "Change Background Image",
                            value = if (prefs.backgroundImageUri.isNotBlank()) "Custom Image Active" else "Fluid Canvas",
                            icon = Icons.Outlined.Image,
                            onClick = {
                                backgroundLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )

                        if (prefs.backgroundImageUri.isNotBlank()) {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 20.dp), color = adaptiveColor.copy(alpha = 0.05f))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setBackgroundImageUri("") }
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.RestartAlt, null, tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(16.dp))
                                Text("Reset to Fluid Canvas", color = Color.Red.copy(alpha = 0.7f), fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // DATA & MANAGEMENT SECTION
            item(key = "header_management") { SettingsSectionHeader("Data & Management") }
            item(key = "card_management") {
                LiquidCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), backdrop = backdrop) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        val tokens = com.crescentapps.turnly.presentation.theme.TurnlyThemeTokens.colors
                        
                        LiquidButton(
                            onClick = { viewModel.exportData(context) },
                            modifier = Modifier.fillMaxWidth(),
                            backdrop = backdrop
                        ) {
                            Icon(Icons.Outlined.FileDownload, null, modifier = Modifier.size(20.dp), tint = adaptiveColor)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Turnly Data (JSON)", fontWeight = FontWeight.Bold, color = adaptiveColor)
                        }

                        LiquidButton(
                            onClick = { viewModel.createDemoData() },
                            modifier = Modifier.fillMaxWidth(),
                            backdrop = backdrop
                        ) {
                            Icon(Icons.Outlined.AutoFixHigh, null, modifier = Modifier.size(20.dp), tint = tokens.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Load Demo Schedules", fontWeight = FontWeight.Bold, color = tokens.primary)
                        }

                        LiquidButton(
                            onClick = { showResetAppearanceDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            surfaceColor = tokens.statusAttentionContainer,
                            tint = tokens.statusAttention,
                            backdrop = backdrop
                        ) {
                            Icon(Icons.Outlined.RestartAlt, null, modifier = Modifier.size(20.dp), tint = tokens.statusAttention)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset Appearance Settings", color = tokens.statusAttention, fontWeight = FontWeight.Bold)
                        }

                        LiquidButton(
                            onClick = { showResetAllSettingsDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            surfaceColor = tokens.statusMissedContainer,
                            tint = tokens.statusMissed,
                            backdrop = backdrop
                        ) {
                            Icon(Icons.Outlined.SettingsBackupRestore, null, modifier = Modifier.size(20.dp), tint = tokens.statusMissed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset All Preferences", color = tokens.statusMissed, fontWeight = FontWeight.Bold)
                        }

                        LiquidButton(
                            onClick = { showResetDataDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            surfaceColor = tokens.statusMissed.copy(alpha = 0.22f),
                            tint = tokens.statusMissed,
                            backdrop = backdrop
                        ) {
                            Icon(Icons.Outlined.DeleteForever, null, modifier = Modifier.size(20.dp), tint = tokens.statusMissed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset Database", color = tokens.statusMissed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }
    }

    // Theme Mode Dialog
    if (showThemeDialog) {
        LiquidDialog(
            onDismissRequest = { showThemeDialog = false },
            backdrop = backdrop,
            title = "Theme Mode",
            positiveText = "Done",
            negativeText = null,
            onPositive = { showThemeDialog = false }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(ThemeMode.SYSTEM, ThemeMode.LIGHT, ThemeMode.DARK).forEach { mode ->
                    val isSelected = prefs.themeMode == mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) adaptiveColor.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { viewModel.setThemeMode(mode) }
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            mode.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = adaptiveColor,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    // Font Family Dialog
    if (showFontFamilyDialog) {
        LiquidDialog(
            onDismissRequest = { showFontFamilyDialog = false },
            backdrop = backdrop,
            title = "Font Family",
            positiveText = "Done",
            onPositive = { showFontFamilyDialog = false }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppleFontStyle.entries.forEach { style ->
                    val isSelected = prefs.appFontFamily == style.label
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) adaptiveColor.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { viewModel.setAppFontFamily(style.label) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                style.label,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = style.fontFamily,
                                    fontWeight = AppleFontWeight.fromLabel(prefs.appFontWeight).weight
                                ),
                                color = adaptiveColor
                            )
                            Text(
                                "The quick brown fox jumps over the lazy dog",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = style.fontFamily,
                                    fontWeight = AppleFontWeight.fromLabel(prefs.appFontWeight).weight
                                ),
                                color = adaptiveColor.copy(alpha = 0.6f)
                            )
                        }
                        if (isSelected) {
                            Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    // Font Weight Dialog
    if (showFontWeightDialog) {
        LiquidDialog(
            onDismissRequest = { showFontWeightDialog = false },
            backdrop = backdrop,
            title = "Font Weight",
            positiveText = "Done",
            onPositive = { showFontWeightDialog = false }
        ) {
            LazyColumn(
                modifier = Modifier.heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(AppleFontWeight.entries.size) { index ->
                    val weight = AppleFontWeight.entries[index]
                    val isSelected = prefs.appFontWeight == weight.label
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) adaptiveColor.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { viewModel.setAppFontWeight(weight.label) }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                weight.label,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = AppleFontStyle.fromLabel(prefs.appFontFamily).fontFamily,
                                    fontWeight = weight.weight
                                ),
                                color = adaptiveColor
                            )
                            Text(
                                "The quick brown fox jumps over the lazy dog",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = AppleFontStyle.fromLabel(prefs.appFontFamily).fontFamily,
                                    fontWeight = weight.weight
                                ),
                                color = adaptiveColor.copy(alpha = 0.6f)
                            )
                        }
                        if (isSelected) {
                            Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    // Font Tint Fallback Dialog
    if (showFontTintDialog) {
        LiquidDialog(
            onDismissRequest = { showFontTintDialog = false },
            backdrop = backdrop,
            title = "Font Tint (Fallback)",
            positiveText = "Done",
            onPositive = { showFontTintDialog = false }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Auto", "Palette", "Custom").forEachIndexed { index, label ->
                        LiquidButton(
                            onClick = { viewModel.setFontTintFallbackMode(index) },
                            modifier = Modifier.weight(1f),
                            surfaceColor = if (prefs.fontTintFallbackMode == index) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent,
                            backdrop = backdrop
                        ) {
                            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Black, color = adaptiveColor)
                        }
                    }
                }

                HorizontalDivider(color = adaptiveColor.copy(alpha = 0.05f))

                when (prefs.fontTintFallbackMode) {
                    0 -> {
                        Text(
                            "Automatically derives readable text contrast from your active background wallpaper or liquid scene.",
                            style = MaterialTheme.typography.bodySmall,
                            color = adaptiveColor.copy(alpha = 0.7f),
                            lineHeight = 18.sp
                        )
                    }
                    1 -> {
                        FontTintPalettePicker(
                            selectedColor = Color(prefs.fontTintPaletteColor),
                            onColorSelected = { viewModel.setFontTintPaletteColor(it.toArgb()) },
                            adaptiveColor = adaptiveColor
                        )
                    }
                    2 -> {
                        FontTintPalettePicker(
                            selectedColor = Color(prefs.fontTintCustomColor),
                            onColorSelected = { viewModel.setFontTintCustomColor(it.toArgb()) },
                            adaptiveColor = adaptiveColor,
                            isCustom = true
                        )
                    }
                }
            }
        }
    }

    // First Day of Week Dialog
    if (showFirstDayDialog) {
        LiquidDialog(
            onDismissRequest = { showFirstDayDialog = false },
            backdrop = backdrop,
            title = "First Day of Week",
            positiveText = "Done",
            onPositive = { showFirstDayDialog = false }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(FirstDayOfWeek.MONDAY, FirstDayOfWeek.SUNDAY).forEach { day ->
                    val isSelected = prefs.firstDayOfWeek == day
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) adaptiveColor.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { viewModel.setFirstDayOfWeek(day) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(day.name.lowercase().replaceFirstChar { it.uppercase() }, color = adaptiveColor)
                        if (isSelected) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    // Reset Appearance Confirmation
    if (showResetAppearanceDialog) {
        LiquidDialog(
            onDismissRequest = { showResetAppearanceDialog = false },
            backdrop = backdrop,
            title = "Reset Appearance",
            message = "Restore optical, shader, blur, radius, typography, and wallpaper settings to original defaults? Your schedules and rotations will remain safe.",
            positiveText = "Reset Appearance",
            negativeText = "Cancel",
            onPositive = {
                viewModel.resetAppearanceSettings()
                showResetAppearanceDialog = false
            }
        )
    }

    // Reset All Settings Confirmation
    if (showResetAllSettingsDialog) {
        LiquidDialog(
            onDismissRequest = { showResetAllSettingsDialog = false },
            backdrop = backdrop,
            title = "Reset All Preferences",
            message = "Restore all user preferences, notifications, and visual styling to defaults? Database schedules and participants will NOT be deleted.",
            positiveText = "Reset Preferences",
            negativeText = "Cancel",
            onPositive = {
                viewModel.resetAllPreferences()
                showResetAllSettingsDialog = false
            }
        )
    }

    // Reset Database Confirmation
    if (showResetDataDialog) {
        LiquidDialog(
            onDismissRequest = { showResetDataDialog = false },
            backdrop = backdrop,
            title = "Reset Database",
            message = "Permanently clear all local schedules, rotations, participants, and history? This cannot be undone.",
            positiveText = "Clear Everything",
            negativeText = "Cancel",
            onPositive = {
                viewModel.resetAllData()
                showResetDataDialog = false
            }
        )
    }

    // UPDATE FREQUENCY DIALOG
    if (showFrequencyDialog) {
        LiquidDialog(
            onDismissRequest = { showFrequencyDialog = false },
            backdrop = backdrop,
            title = "Automatic Update Checks",
            positiveText = "Done",
            negativeText = null,
            onPositive = { showFrequencyDialog = false }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                UpdateFrequency.entries.forEach { freq ->
                    val isSelected = prefs.updateCheckFrequency == freq
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) adaptiveColor.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable {
                                viewModel.setUpdateCheckFrequency(freq, context)
                                showFrequencyDialog = false
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(freq.displayName, color = adaptiveColor, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            val desc = when (freq) {
                                UpdateFrequency.OFF -> "Never check automatically"
                                UpdateFrequency.DAILY -> "Check once every 24 hours (Recommended)"
                                UpdateFrequency.WEEKLY -> "Check once every 7 days"
                                UpdateFrequency.MONTHLY -> "Check once every 30 days"
                            }
                            Text(desc, style = MaterialTheme.typography.bodySmall, color = adaptiveColor.copy(alpha = 0.6f))
                        }
                        if (isSelected) Icon(Icons.Default.Check, null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }

    // UPDATE LIFECYCLE DIALOGS (LIQUID STYLED)
    when (val state = updateState) {
        is UpdateState.UpToDate -> {
            LiquidDialog(
                onDismissRequest = { viewModel.dismissUpdateDialog() },
                backdrop = backdrop,
                title = "You're up to date",
                message = "Turnly v${state.currentVersion} is the latest version available.",
                positiveText = "OK",
                negativeText = null,
                icon = Icons.Outlined.CheckCircle,
                onPositive = { viewModel.dismissUpdateDialog() }
            )
        }
        is UpdateState.UpdateAvailable -> {
            LiquidDialog(
                onDismissRequest = { viewModel.dismissUpdateDialog() },
                backdrop = backdrop,
                title = "Update Available",
                icon = Icons.Outlined.NewReleases,
                positiveText = "Download Update",
                negativeText = "Later",
                onPositive = { viewModel.startDownload(state.updateInfo) }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Turnly v${state.updateInfo.versionName} is ready to download.",
                        fontWeight = FontWeight.Bold,
                        color = adaptiveColor
                    )
                    if (state.updateInfo.assetSize > 0) {
                        val mb = state.updateInfo.assetSize / (1024f * 1024f)
                        Text(
                            text = String.format(Locale.ROOT, "Download size: %.1f MB", mb),
                            style = MaterialTheme.typography.bodySmall,
                            color = adaptiveColor.copy(alpha = 0.7f)
                        )
                    }
                    HorizontalDivider(color = adaptiveColor.copy(alpha = 0.08f))
                    Text("What's New", fontWeight = FontWeight.SemiBold, color = adaptiveColor)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(adaptiveColor.copy(alpha = 0.06f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = state.updateInfo.releaseNotes.ifBlank { "No release notes provided." },
                            style = MaterialTheme.typography.bodySmall,
                            color = adaptiveColor
                        )
                    }
                }
            }
        }
        is UpdateState.Downloading -> {
            LiquidDialog(
                onDismissRequest = { /* Require explicit cancel */ },
                backdrop = backdrop,
                title = "Downloading Update",
                icon = Icons.Outlined.CloudDownload,
                positiveText = "Cancel",
                negativeText = null,
                onPositive = { viewModel.cancelDownload() }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    LinearProgressIndicator(
                        progress = { state.progress },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val percent = (state.progress * 100).toInt()
                        Text("$percent%", fontWeight = FontWeight.Bold, color = adaptiveColor)
                        if (state.totalBytes > 0) {
                            val currentMb = state.downloadedBytes / (1024f * 1024f)
                            val totalMb = state.totalBytes / (1024f * 1024f)
                            Text(
                                String.format(Locale.ROOT, "%.1f / %.1f MB", currentMb, totalMb),
                                style = MaterialTheme.typography.bodySmall,
                                color = adaptiveColor.copy(alpha = 0.6f)
                            )
                        }
                    }
                }
            }
        }
        is UpdateState.WaitingForInstallPermission -> {
            LiquidDialog(
                onDismissRequest = { viewModel.dismissUpdateDialog() },
                backdrop = backdrop,
                title = "Install Permission Required",
                icon = Icons.Outlined.Security,
                message = "Turnly downloaded the update, but Android requires you to allow Turnly to install applications from this source.\n\nEnable 'Allow from this source' for Turnly to continue the update.",
                positiveText = "Open Settings",
                negativeText = "Cancel",
                onPositive = {
                    val intent = viewModel.createManageUnknownAppSourcesIntent()
                    if (intent != null) {
                        try {
                            context.startActivity(intent)
                        } catch (_: Exception) {}
                    }
                }
            )
        }
        is UpdateState.ReadyToInstall -> {
            LiquidDialog(
                onDismissRequest = { viewModel.dismissUpdateDialog() },
                backdrop = backdrop,
                title = "Update Downloaded",
                icon = Icons.Outlined.InstallMobile,
                message = "Turnly v${state.updateInfo.versionName} is downloaded and verified. Ready to install.",
                positiveText = "Install Now",
                negativeText = "Later",
                onPositive = { viewModel.installUpdate(state.apkFile, state.updateInfo) }
            )
        }
        is UpdateState.Error -> {
            LiquidDialog(
                onDismissRequest = { viewModel.dismissUpdateDialog() },
                backdrop = backdrop,
                title = "Update Failed",
                icon = Icons.Outlined.ErrorOutline,
                message = state.message,
                positiveText = if (state.canRetry) "Retry" else "OK",
                negativeText = if (state.canRetry) "Dismiss" else null,
                onPositive = {
                    if (state.canRetry) {
                        viewModel.checkForUpdates()
                    } else {
                        viewModel.dismissUpdateDialog()
                    }
                }
            )
        }
        else -> Unit
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Black,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun SettingsToggleItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    checked: Boolean,
    backdrop: Backdrop,
    onCheckedChange: (Boolean) -> Unit
) {
    val adaptiveColor = LocalPrismalAdaptiveColor.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            AdaptiveIcon(icon, modifier = Modifier.size(24.dp), tint = adaptiveColor.copy(alpha = 0.7f))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(title, style = MaterialTheme.typography.bodyLarge, color = adaptiveColor, fontWeight = FontWeight.SemiBold)
                if (subtitle != null) {
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = adaptiveColor.copy(alpha = 0.6f))
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        LiquidToggle(selected = { checked }, onSelect = onCheckedChange, backdrop = backdrop)
    }
}

@Composable
fun SettingsNavigationItem(
    title: String,
    value: String? = null,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val adaptiveColor = LocalPrismalAdaptiveColor.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AdaptiveIcon(icon, modifier = Modifier.size(24.dp), tint = adaptiveColor.copy(alpha = 0.7f))
            Spacer(Modifier.width(16.dp))
            Text(title, style = MaterialTheme.typography.bodyLarge, color = adaptiveColor, fontWeight = FontWeight.SemiBold)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (value != null) {
                Text(value, style = MaterialTheme.typography.bodyMedium, color = adaptiveColor.copy(alpha = 0.6f))
                Spacer(Modifier.width(8.dp))
            }
            AdaptiveIcon(Icons.AutoMirrored.Filled.KeyboardArrowRight, modifier = Modifier.size(24.dp), tint = adaptiveColor.copy(alpha = 0.3f))
        }
    }
}

@Composable
fun GlassEffectSlider(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    backdrop: Backdrop,
    defaultValue: Float? = null,
    onValueChangeFinished: ((Float) -> Unit)? = null,
    onValueChange: (Float) -> Unit
) {
    val adaptiveColor = LocalPrismalAdaptiveColor.current
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.bodyMedium, color = adaptiveColor, fontWeight = FontWeight.Bold)
                if (defaultValue != null && value != defaultValue) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Outlined.RestartAlt,
                        contentDescription = "Reset",
                        modifier = Modifier
                            .size(16.dp)
                            .clickable {
                                onValueChange(defaultValue)
                                onValueChangeFinished?.invoke(defaultValue)
                            },
                        tint = adaptiveColor.copy(alpha = 0.5f)
                    )
                }
            }
            val displayValue = when (unit) {
                "%" -> "${value.toInt()}%"
                "dp" -> String.format(Locale.ROOT, "%d dp", value.toInt())
                "normalized" -> String.format(Locale.ROOT, "%.2f", value)
                "ms" -> String.format(Locale.ROOT, "%d ms", value.toInt())
                else -> String.format(Locale.ROOT, "%d", value.toInt())
            }
            Text(displayValue, style = MaterialTheme.typography.bodySmall, color = adaptiveColor.copy(alpha = 0.6f))
        }
        LiquidSlider(
            value = { value },
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = range,
            visibilityThreshold = if (unit == "normalized") 0.01f else 1f,
            backdrop = backdrop
        )
    }
}

private val CustomPresets = listOf(
    Color(0xFFEF4444), Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFF3B82F6),
    Color(0xFF6366F1), Color(0xFF8B5CF6), Color(0xFFEC4899), Color(0xFF06B6D4),
    Color(0xFF78716C), Color(0xFF451A03), Color(0xFF064E3B), Color(0xFF1E3A8A),
    Color(0xFFFFFFFF), Color(0xFF000000), Color(0xFF94A3B8), Color(0xFF334155)
)

private val PalettePresets = listOf(
    Color(0xFF6366F1), // Indigo
    Color(0xFFEC4899), // Pink
    Color(0xFF10B981), // Emerald
    Color(0xFFF59E0B), // Amber
    Color(0xFF3B82F6), // Blue
    Color(0xFF8B5CF6)  // Violet
)

@Composable
fun FontTintPalettePicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit,
    adaptiveColor: Color,
    isCustom: Boolean = false
) {
    val presets = if (isCustom) CustomPresets else PalettePresets

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        presets.chunked(4).forEach { rowColors ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowColors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(color)
                            .border(
                                width = 2.dp,
                                color = if (selectedColor == color) adaptiveColor else Color.Transparent,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onColorSelected(color) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedColor == color) {
                            Icon(
                                Icons.Default.Check,
                                null,
                                tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
