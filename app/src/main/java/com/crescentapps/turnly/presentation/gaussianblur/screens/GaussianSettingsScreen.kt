package com.crescentapps.turnly.presentation.gaussianblur.screens

import android.app.TimePickerDialog
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.core.model.FirstDayOfWeek
import com.crescentapps.turnly.core.model.ThemeMode
import com.crescentapps.turnly.core.model.UiSystemMode
import com.crescentapps.turnly.presentation.components.HorizontalDivider
import com.crescentapps.turnly.presentation.components.Icon
import com.crescentapps.turnly.presentation.components.Text
import com.crescentapps.turnly.presentation.gaussianblur.components.*
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianBlurColors
import com.crescentapps.turnly.presentation.screens.settings.SettingsViewModel
import java.util.Locale

import com.crescentapps.turnly.presentation.gaussianblur.theme.GaussianOptics
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianOptics
import androidx.compose.runtime.CompositionLocalProvider


@Composable
fun GaussianSettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.effectivePreferences.collectAsState()
    val context = LocalContext.current
    val colors = LocalGaussianBlurColors.current
    val listState = rememberLazyListState()

    var showUiSystemDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showFirstDayDialog by remember { mutableStateOf(false) }
    var showResetAllSettingsDialog by remember { mutableStateOf(false) }
    var showResetDataDialog by remember { mutableStateOf(false) }

    val backgroundLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                viewModel.setBackgroundImageUri(it.toString())
            } catch (_: Exception) {
                viewModel.setBackgroundImageUri(it.toString())
            }
        }
    }


    var localBlurRadius by remember(prefs.gaussianBlurRadius) { mutableFloatStateOf(prefs.gaussianBlurRadius) }
    var localSurfaceOpacity by remember(prefs.gaussianSurfaceOpacity) { mutableFloatStateOf(prefs.gaussianSurfaceOpacity) }
    var localRefractionStrength by remember(prefs.gaussianRefractionStrength) { mutableFloatStateOf(prefs.gaussianRefractionStrength) }
    var localDynamicHighlightsIntensity by remember(prefs.gaussianDynamicHighlightsIntensity) { mutableFloatStateOf(prefs.gaussianDynamicHighlightsIntensity) }
    var localSpecularIntensity by remember(prefs.gaussianSpecularIntensity) { mutableFloatStateOf(prefs.gaussianSpecularIntensity) }
    var localCondensedLightRadius by remember(prefs.gaussianCondensedLightRadius) { mutableFloatStateOf(prefs.gaussianCondensedLightRadius) }

    val liveOptics = GaussianOptics(
        blurRadiusDp = localBlurRadius,
        surfaceOpacity = localSurfaceOpacity,
        refractionEnabled = prefs.gaussianRefractionEnabled,
        refractionHeightDp = localRefractionStrength * 24f,
        refractionAmountDp = localRefractionStrength * 48f,
        specularEnabled = prefs.gaussianSpecularEnabled,
        specularIntensity = localSpecularIntensity,
        dynamicHighlightsEnabled = prefs.gaussianDynamicHighlightsEnabled,
        dynamicHighlightsIntensity = localDynamicHighlightsIntensity,
        condensedLightEnabled = prefs.gaussianCondensedLightEnabled,
        condensedLightRadius = localCondensedLightRadius,
        reduceMotion = prefs.isReduceMotion
    )

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = 700.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item(key = "title_settings") {
                Text(
                    text = "Settings",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.textPrimary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            // LIVE GAUSSIAN GLASS OPTICS PREVIEW SHOWCASE
            item(key = "header_gaussian_preview") { GaussianSettingsSectionHeader("Live Gaussian Blur Optics Preview") }
            item(key = "card_gaussian_preview") {
                CompositionLocalProvider(LocalGaussianOptics provides liveOptics) {
                    GaussianCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        shape = RoundedCornerShape(24.dp),
                        surfaceOpacity = liveOptics.surfaceOpacity,
                        blurRadiusDp = liveOptics.blurRadiusDp
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
                                    tint = colors.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "GaussianBlur Lens Renderer",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }
                            Text(
                                text = "Active Blur: ${prefs.gaussianBlurRadius.toInt()}dp  ·  Opacity: ${(prefs.gaussianSurfaceOpacity * 100).toInt()}%  ·  Refraction: ${if (prefs.gaussianRefractionEnabled) "On" else "Off"}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textSecondary
                            )
                            Text(
                                text = "Real backdrop blur sampling from host canvas",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textMuted
                            )
                        }
                    }
                }
                }
            }

            // UI SYSTEM MODE SELECTION
            item(key = "header_ui_system") { GaussianSettingsSectionHeader("UI System Mode") }
            item(key = "card_ui_system") {
                GaussianCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column {
                        GaussianSettingsNavRow(
                            title = "Active UI System",
                            value = prefs.uiSystemMode.displayName,
                            icon = Icons.Outlined.Dashboard,
                            onClick = { showUiSystemDialog = true }
                        )
                    }
                }
            }

            // GAUSSIAN BLUR GLASS OPTICS CONTROLS
            item(key = "header_blur_optics") { GaussianSettingsSectionHeader("GaussianBlur Glass Optics") }
            item(key = "card_blur_optics") {
                GaussianCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column {
                        GaussianSettingsToggleRow(
                            title = "Liquid Glass",
                            subtitle = "Real refracting glass on elements. Android 12+ only; uses more GPU.",
                            icon = Icons.Outlined.AutoAwesome,
                            checked = prefs.isLiquidGlassEnabled,
                            onCheckedChange = { viewModel.setLiquidGlassEnabled(it) }
                        )

                        HorizontalDivider(color = colors.divider)

                        GaussianOpticsSliderRow(
                            title = "Backdrop Blur Radius",
                            value = localBlurRadius,
                            range = 0f..80f,
                            unit = "dp",
                            onValueChange = { localBlurRadius = it },
                            onValueChangeFinished = { viewModel.setGaussianBlurRadius(localBlurRadius) }
                        )

                        HorizontalDivider(color = colors.divider)

                        GaussianOpticsSliderRow(
                            title = "Surface Opacity",
                            value = localSurfaceOpacity * 100f,
                            range = 5f..95f,
                            unit = "%",
                            onValueChange = { localSurfaceOpacity = it / 100f },
                            onValueChangeFinished = { viewModel.setGaussianSurfaceOpacity(localSurfaceOpacity) }
                        )

                        HorizontalDivider(color = colors.divider)

                        GaussianOpticsSliderRow(
                            title = "Lens Refraction",
                            value = localRefractionStrength * 100f,
                            range = 0f..200f,
                            unit = "%",
                            onValueChange = { localRefractionStrength = it / 100f },
                            onValueChangeFinished = { viewModel.setGaussianRefractionStrength(localRefractionStrength) }
                        )

                        HorizontalDivider(color = colors.divider)

                        GaussianOpticsSliderRow(
                            title = "Dynamic Highlights",
                            value = localDynamicHighlightsIntensity * 100f,
                            range = 0f..200f,
                            unit = "%",
                            onValueChange = { localDynamicHighlightsIntensity = it / 100f },
                            onValueChangeFinished = { viewModel.setGaussianDynamicHighlightsIntensity(localDynamicHighlightsIntensity) }
                        )

                        HorizontalDivider(color = colors.divider)

                        GaussianOpticsSliderRow(
                            title = "Specular Reflection Rim",
                            value = localSpecularIntensity * 100f,
                            range = 0f..200f,
                            unit = "%",
                            onValueChange = { localSpecularIntensity = it / 100f },
                            onValueChangeFinished = { viewModel.setGaussianSpecularIntensity(localSpecularIntensity) }
                        )

                        HorizontalDivider(color = colors.divider)

                        GaussianOpticsSliderRow(
                            title = "Condensed Interaction Light",
                            value = localCondensedLightRadius,
                            range = 10f..200f,
                            unit = "dp",
                            onValueChange = { localCondensedLightRadius = it },
                            onValueChangeFinished = { viewModel.setGaussianCondensedLightRadius(localCondensedLightRadius) }
                        )
                    }
                }
            }

            // APPEARANCE SECTION
            item(key = "header_appearance") { GaussianSettingsSectionHeader("Appearance") }
            item(key = "card_appearance") {
                GaussianCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column {
                        GaussianSettingsNavRow(
                            title = "Theme Mode",
                            value = prefs.themeMode.name.lowercase().replaceFirstChar { it.uppercase() },
                            icon = Icons.Outlined.Palette,
                            onClick = { showThemeDialog = true }
                        )

                        HorizontalDivider(color = colors.divider)

                        GaussianSettingsToggleRow(
                            title = "AMOLED Pitch Black",
                            subtitle = "Pure black background in dark theme",
                            icon = Icons.Outlined.DarkMode,
                            checked = prefs.isAmoled,
                            onCheckedChange = { viewModel.setAmoled(it) }
                        )

                        HorizontalDivider(color = colors.divider)

                        GaussianSettingsToggleRow(
                            title = "Reduce Motion",
                            subtitle = "Disable spring deformations for power efficiency",
                            icon = Icons.Outlined.MotionPhotosOff,
                            checked = prefs.isReduceMotion,
                            onCheckedChange = { viewModel.setReduceMotion(it) }
                        )
                    }
                }
            }

            // PREFERENCES SECTION
            item(key = "header_preferences") { GaussianSettingsSectionHeader("Preferences & Notifications") }
            item(key = "card_preferences") {
                GaussianCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column {
                        GaussianSettingsToggleRow(
                            title = "Sound Feedback",
                            subtitle = "Audio cues for turn actions",
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            checked = prefs.soundFeedbackEnabled,
                            onCheckedChange = { viewModel.setSoundFeedbackEnabled(it) }
                        )

                        HorizontalDivider(color = colors.divider)

                        GaussianSettingsToggleRow(
                            title = "Haptic Feedback",
                            subtitle = "Tactile vibration responses",
                            icon = Icons.Outlined.Vibration,
                            checked = prefs.hapticFeedbackEnabled,
                            onCheckedChange = { viewModel.setHapticFeedbackEnabled(it) }
                        )

                        HorizontalDivider(color = colors.divider)

                        GaussianSettingsToggleRow(
                            title = "Daily Turn Reminders",
                            subtitle = "Notify when it is your turn",
                            icon = Icons.Outlined.Notifications,
                            checked = prefs.masterNotificationsEnabled,
                            onCheckedChange = { viewModel.setMasterNotificationsEnabled(it) }
                        )

                        if (prefs.masterNotificationsEnabled) {
                            HorizontalDivider(color = colors.divider)
                            GaussianSettingsNavRow(
                                title = "Reminder Time",
                                value = String.format(Locale.ROOT, "%02d:%02d", prefs.defaultNotificationHour, prefs.defaultNotificationMinute),
                                icon = Icons.Outlined.Schedule,
                                onClick = {
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
                            )
                        }

                        HorizontalDivider(color = colors.divider)

                        GaussianSettingsNavRow(
                            title = "First Day of Week",
                            value = prefs.firstDayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                            icon = Icons.Outlined.CalendarToday,
                            onClick = { showFirstDayDialog = true }
                        )
                    }
                }
            }

            // BACKGROUND SECTION
            item(key = "header_background") { GaussianSettingsSectionHeader("Background Wallpaper") }
            item(key = "card_background") {
                GaussianCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column {
                        GaussianSettingsNavRow(
                            title = "Change Background Image",
                            value = if (prefs.backgroundImageUri.isNotBlank()) "Custom Active" else "Ambient Mesh",
                            icon = Icons.Outlined.Image,
                            onClick = {
                                backgroundLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        )

                        if (prefs.backgroundImageUri.isNotBlank()) {
                            HorizontalDivider(color = colors.divider)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.setBackgroundImageUri("") }
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.RestartAlt, null, tint = colors.statusMissed, modifier = Modifier.size(22.dp))
                                Spacer(Modifier.width(16.dp))
                                Text("Reset to Ambient Gradient Mesh", color = colors.statusMissed, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            // DATA MANAGEMENT
            item(key = "header_management") { GaussianSettingsSectionHeader("Data & Management") }
            item(key = "card_management") {
                GaussianCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        GaussianButton(
                            onClick = { viewModel.exportData(context) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.FileDownload, null, modifier = Modifier.size(20.dp), tint = colors.textPrimary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Turnly Data (JSON)", fontWeight = FontWeight.Bold, color = colors.textPrimary)
                        }

                        GaussianButton(
                            onClick = { viewModel.createDemoData() },
                            modifier = Modifier.fillMaxWidth(),
                            accentTint = colors.accent
                        ) {
                            Icon(Icons.Outlined.AutoFixHigh, null, modifier = Modifier.size(20.dp), tint = colors.textOnAccent)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Load Demo Schedules", fontWeight = FontWeight.Bold, color = colors.textOnAccent)
                        }

                        GaussianButton(
                            onClick = { showResetAllSettingsDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.SettingsBackupRestore, null, modifier = Modifier.size(20.dp), tint = colors.statusMissed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset All Preferences", color = colors.statusMissed, fontWeight = FontWeight.Bold)
                        }

                        GaussianButton(
                            onClick = { showResetDataDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.DeleteForever, null, modifier = Modifier.size(20.dp), tint = colors.statusMissed)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset Database", color = colors.statusMissed, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(100.dp)) }
        }
    }

    // UI System Switcher Dialog
    if (showUiSystemDialog) {
        GaussianDialog(
            onDismissRequest = { showUiSystemDialog = false },
            title = "Select UI Visual System",
            positiveText = "Done",
            negativeText = null,
            onPositive = { showUiSystemDialog = false }
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(UiSystemMode.GAUSSIAN_BLUR, UiSystemMode.LIQUID).forEach { mode ->
                    val isSelected = prefs.uiSystemMode == mode
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) colors.accent.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { viewModel.setUiSystemMode(mode) }
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                mode.displayName,
                                color = colors.textPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            val desc = when (mode) {
                                UiSystemMode.GAUSSIAN_BLUR -> "BitChord-inspired physical glass with real backdrop blur"
                                UiSystemMode.LIQUID -> "Prismal OpenGL shader liquid glass"
                            }
                            Text(desc, fontSize = 11.sp, color = colors.textMuted)
                        }
                        if (isSelected) {
                            Icon(Icons.Default.Check, null, tint = colors.accent)
                        }
                    }
                }
            }
        }
    }

    // Theme Mode Dialog
    if (showThemeDialog) {
        GaussianDialog(
            onDismissRequest = { showThemeDialog = false },
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
                            .background(if (isSelected) colors.accent.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { viewModel.setThemeMode(mode) }
                            .padding(vertical = 12.dp, horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            mode.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = colors.textPrimary,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                        if (isSelected) {
                            Icon(Icons.Default.Check, null, tint = colors.accent)
                        }
                    }
                }
            }
        }
    }

    // First Day of Week Dialog
    if (showFirstDayDialog) {
        GaussianDialog(
            onDismissRequest = { showFirstDayDialog = false },
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
                            .background(if (isSelected) colors.accent.copy(alpha = 0.15f) else Color.Transparent)
                            .clickable { viewModel.setFirstDayOfWeek(day) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(day.name.lowercase().replaceFirstChar { it.uppercase() }, color = colors.textPrimary)
                        if (isSelected) Icon(Icons.Default.Check, null, tint = colors.accent)
                    }
                }
            }
        }
    }

    // Reset All Settings Confirmation
    if (showResetAllSettingsDialog) {
        GaussianDialog(
            onDismissRequest = { showResetAllSettingsDialog = false },
            title = "Reset All Preferences",
            message = "Restore all user preferences and visual styling to defaults? Database schedules will NOT be deleted.",
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
        GaussianDialog(
            onDismissRequest = { showResetDataDialog = false },
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
}

@Composable
fun GaussianSettingsSectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Black,
        color = LocalGaussianBlurColors.current.accent,
        modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
    )
}

@Composable
fun GaussianOpticsSliderRow(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    unit: String,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: (() -> Unit)? = null
) {
    val colors = LocalGaussianBlurColors.current
    Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            val displayValue = when (unit) {
                "%" -> "${value.toInt()}%"
                "dp" -> "${value.toInt()} dp"
                else -> "${value.toInt()}"
            }
            Text(displayValue, fontSize = 12.sp, color = colors.textMuted)
        }
        GaussianSlider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = range
        )
    }
}
