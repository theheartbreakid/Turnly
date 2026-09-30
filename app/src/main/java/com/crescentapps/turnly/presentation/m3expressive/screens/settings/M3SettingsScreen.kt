package com.crescentapps.turnly.presentation.m3expressive.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crescentapps.turnly.core.model.UiSystemMode
import com.crescentapps.turnly.presentation.m3expressive.theme.ExpressiveTokens
import com.crescentapps.turnly.presentation.screens.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3SettingsScreen(viewModel: SettingsViewModel) {
    val prefs by viewModel.preferences.collectAsState(
        initial = com.crescentapps.turnly.data.preferences.UserPreferences()
    )

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Settings") }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(ExpressiveTokens.spacing.normal)
        ) {
            item {
                Text(
                    text = "Appearance",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = ExpressiveTokens.spacing.normal)
                )
            }
            
            // UI Style Selector
            item {
                Text("UI Style", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(ExpressiveTokens.spacing.small))
                
                Column(modifier = Modifier.selectableGroup()) {
                    listOf(
                        UiSystemMode.GAUSSIAN_BLUR to "Gaussian Blur",
                        UiSystemMode.LIQUID to "Liquid UI",
                        UiSystemMode.MATERIAL_3_EXPRESSIVE to "Material 3 Expressive"
                    ).forEach { (mode, label) ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .padding(horizontal = ExpressiveTokens.spacing.small),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (prefs.uiSystemMode == mode),
                                onClick = { viewModel.setUiSystemMode(mode) }
                            )
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(start = ExpressiveTokens.spacing.normal)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(ExpressiveTokens.spacing.group))
            }
            
            // M3 Specific settings
            if (prefs.uiSystemMode == UiSystemMode.MATERIAL_3_EXPRESSIVE) {
                item {
                    Text(
                        text = "Material 3 Expressive",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(ExpressiveTokens.spacing.small))
                    
                    ListItem(
                        supportingContent = { Text("Use wallpaper colors for the theme") },
                        trailingContent = {
                            Switch(
                                checked = prefs.m3DynamicColor,
                                onCheckedChange = { viewModel.updateM3DynamicColor(it) }
                            )
                        }
                    ) {
                        Text("Dynamic Color")
                    }
                    
                    ListItem(
                        supportingContent = { Text("Use bouncy spatial springs and morphing") },
                        trailingContent = {
                            Switch(
                                checked = prefs.m3UseExpressiveMotion,
                                onCheckedChange = { viewModel.updateM3UseExpressiveMotion(it) }
                            )
                        }
                    ) {
                        Text("Expressive Motion")
                    }
                }
            }
        }
    }
}
