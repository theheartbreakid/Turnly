package com.crescentapps.turnly.presentation.m3.screens.settings

import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crescentapps.turnly.core.model.FirstDayOfWeek
import com.crescentapps.turnly.core.model.ThemeMode
import com.crescentapps.turnly.core.model.UiMode
import com.crescentapps.turnly.presentation.m3.components.M3ConfirmDialog
import com.crescentapps.turnly.presentation.screens.settings.SettingsViewModel
import java.util.Locale

/**
 * Pure Material 3 Settings Screen.
 * Contains:
 * - UI Mode selector: Material 3 (Stable · Recommended) vs Liquid UI (Experimental)
 * - Confirmation warning when enabling Liquid UI for the first time
 * - Standard M3 Appearance settings (ThemeMode, AMOLED, Dynamic Colors)
 * - Standard M3 Preferences (Daily reminders, First day of week, Sync over mobile data)
 * - Standard M3 Data management (JSON export, Demo data, Reset database)
 * - ABSOLUTELY ZERO Liquid/glass sliders, optics, blur, refraction, or shader controls!
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3SettingsScreen(
    viewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.preferences.collectAsState()
    val context = LocalContext.current

    var showThemeDialog by remember { mutableStateOf(false) }
    var showFirstDayDialog by remember { mutableStateOf(false) }
    var showResetDataDialog by remember { mutableStateOf(false) }
    var showLiquidWarningDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
        ) {
            // UI STYLE / MODE SELECTION
            item(key = "m3_header_uimode") { M3SectionHeader("UI Visual Mode") }
            item(key = "m3_card_uimode") {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    )
                ) {
                    Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = "Select Interface Style",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        // Material 3 Option
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (prefs.uiMode == UiMode.MATERIAL_3) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.setUiMode(UiMode.MATERIAL_3) }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RadioButton(
                                    selected = prefs.uiMode == UiMode.MATERIAL_3,
                                    onClick = { viewModel.setUiMode(UiMode.MATERIAL_3) }
                                )
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Material 3", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "Stable · Recommended",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Pixel-grade native design, high performance, zero shader battery drain.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Liquid UI Option
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (prefs.uiMode == UiMode.LIQUID) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (!prefs.hasAcceptedLiquidWarning) {
                                        showLiquidWarningDialog = true
                                    } else {
                                        viewModel.setUiMode(UiMode.LIQUID)
                                    }
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                RadioButton(
                                    selected = prefs.uiMode == UiMode.LIQUID,
                                    onClick = {
                                        if (!prefs.hasAcceptedLiquidWarning) {
                                            showLiquidWarningDialog = true
                                        } else {
                                            viewModel.setUiMode(UiMode.LIQUID)
                                        }
                                    }
                                )
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text("Liquid UI", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f)
                                        ) {
                                            Text(
                                                text = "Experimental",
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.tertiary
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Prismal glass surfaces, optical refraction shaders, and dynamic canvas effects.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // APPEARANCE SECTION
            item(key = "m3_header_appearance") { M3SectionHeader("Appearance") }
            item(key = "m3_card_appearance") {
                ElevatedCard(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        M3SettingsNavigationItem(
                            title = "Theme Mode",
                            value = prefs.themeMode.name.lowercase().replaceFirstChar { it.uppercase() },
                            icon = Icons.Outlined.Palette,
                            onClick = { showThemeDialog = true }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        M3SettingsSwitchItem(
                            title = "AMOLED Pitch Black",
                            subtitle = "Pure black background in dark mode",
                            icon = Icons.Outlined.DarkMode,
                            checked = prefs.isAmoled,
                            onCheckedChange = { viewModel.setAmoled(it) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        M3SettingsSwitchItem(
                            title = "Dynamic Colors",
                            subtitle = "Sample wallpaper palette on Android 12+",
                            icon = Icons.Outlined.ColorLens,
                            checked = prefs.dynamicColors,
                            onCheckedChange = { viewModel.setDynamicColors(it) }
                        )
                    }
                }
            }

            // PREFERENCES SECTION
            item(key = "m3_header_prefs") { M3SectionHeader("Preferences") }
            item(key = "m3_card_prefs") {
                ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column {
                        M3SettingsSwitchItem(
                            title = "Daily Turn Reminders",
                            subtitle = "Notify when it is your turn",
                            icon = Icons.Outlined.Notifications,
                            checked = prefs.masterNotificationsEnabled,
                            onCheckedChange = { viewModel.setMasterNotificationsEnabled(it) }
                        )
                        if (prefs.masterNotificationsEnabled) {
                            HorizontalDivider()
                            M3SettingsNavigationItem(
                                title = "Reminder Time",
                                value = String.format(Locale.getDefault(), "%02d:%02d", prefs.defaultNotificationHour, prefs.defaultNotificationMinute),
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
                        HorizontalDivider()
                        M3SettingsNavigationItem(
                            title = "First Day of Week",
                            value = prefs.firstDayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                            icon = Icons.Outlined.CalendarToday,
                            onClick = { showFirstDayDialog = true }
                        )
                        HorizontalDivider()
                        M3SettingsSwitchItem(
                            title = "Sync Over Mobile Data",
                            subtitle = "Allow P2P room sync without Wi-Fi",
                            icon = Icons.Outlined.SignalCellularAlt,
                            checked = prefs.syncOverMobileData,
                            onCheckedChange = { viewModel.setSyncOverMobileData(it, context) }
                        )
                    }
                }
            }

            // DATA & MANAGEMENT SECTION
            item(key = "m3_header_data") { M3SectionHeader("Data & Management") }
            item(key = "m3_card_data") {
                ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { viewModel.exportData(context) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.FileDownload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export Turnly Data (JSON)")
                        }

                        OutlinedButton(
                            onClick = { viewModel.createDemoData() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Outlined.AutoFixHigh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Load Demo Schedules")
                        }

                        Button(
                            onClick = { showResetDataDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Icon(Icons.Outlined.DeleteForever, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Reset All Data")
                        }
                    }
                }
            }
        }
    }

    // EXPERIMENTAL WARNING DIALOG
    if (showLiquidWarningDialog) {
        AlertDialog(
            onDismissRequest = { showLiquidWarningDialog = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Liquid UI is experimental", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This interface uses advanced visual effects and may cause increased GPU/CPU usage, battery consumption, memory usage, or performance issues. Performance problems may occur even on high-end devices.\n\nEnable only if you understand and accept these risks."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setHasAcceptedLiquidWarning(true)
                        viewModel.setUiMode(UiMode.LIQUID)
                        showLiquidWarningDialog = false
                    }
                ) {
                    Text("Enable Liquid UI")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLiquidWarningDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // THEME MODE DIALOG
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme Mode", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.values().forEach { mode ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setThemeMode(mode)
                                    showThemeDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(selected = prefs.themeMode == mode, onClick = {
                                viewModel.setThemeMode(mode)
                                showThemeDialog = false
                            })
                            Text(mode.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Close") }
            }
        )
    }

    // FIRST DAY OF WEEK DIALOG
    if (showFirstDayDialog) {
        AlertDialog(
            onDismissRequest = { showFirstDayDialog = false },
            title = { Text("First Day of Week", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FirstDayOfWeek.values().forEach { day ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.setFirstDayOfWeek(day)
                                    showFirstDayDialog = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(selected = prefs.firstDayOfWeek == day, onClick = {
                                viewModel.setFirstDayOfWeek(day)
                                showFirstDayDialog = false
                            })
                            Text(day.name.lowercase().replaceFirstChar { it.uppercase() })
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFirstDayDialog = false }) { Text("Close") }
            }
        )
    }

    // RESET DATA CONFIRMATION
    if (showResetDataDialog) {
        M3ConfirmDialog(
            title = "Reset All Data?",
            message = "This permanently clears all local schedules, rotations, participants, and history. This cannot be undone.",
            confirmText = "Clear Everything",
            isDestructive = true,
            onConfirm = {
                viewModel.resetAllData()
                showResetDataDialog = false
            },
            onDismiss = { showResetDataDialog = false }
        )
    }
}

@Composable
private fun M3SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 4.dp)
    )
}

@Composable
private fun M3SettingsSwitchItem(
    title: String,
    subtitle: String? = null,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.SemiBold) },
        supportingContent = if (subtitle != null) { { Text(subtitle) } } else null,
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
}

@Composable
private fun M3SettingsNavigationItem(
    title: String,
    value: String? = null,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title, fontWeight = FontWeight.SemiBold) },
        supportingContent = if (value != null) { { Text(value) } } else null,
        leadingContent = { Icon(icon, contentDescription = null) },
        trailingContent = { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
