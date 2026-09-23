package com.crescentapps.turnly

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.crescentapps.turnly.core.model.Schedule
import com.crescentapps.turnly.core.model.ThemeMode
import com.crescentapps.turnly.core.notification.DailyNotificationScheduler
import com.crescentapps.turnly.presentation.catalog.utils.LocalBackdrop
import com.crescentapps.turnly.presentation.components.*
import com.crescentapps.turnly.presentation.navigation.Screen
import com.crescentapps.turnly.presentation.navigation.TurnlyLiquidDock
import com.crescentapps.turnly.presentation.navigation.TurnlyLiquidNavigationRail
import com.crescentapps.turnly.presentation.screens.calendar.CalendarScreen
import com.crescentapps.turnly.presentation.screens.calendar.CalendarViewModel
import com.crescentapps.turnly.presentation.screens.history.HistoryScreen
import com.crescentapps.turnly.presentation.screens.history.HistoryViewModel
import com.crescentapps.turnly.presentation.screens.home.HomeScreen
import com.crescentapps.turnly.presentation.screens.home.HomeViewModel
import com.crescentapps.turnly.presentation.screens.room.*
import com.crescentapps.turnly.presentation.screens.schedule.*
import com.crescentapps.turnly.presentation.screens.settings.SettingsScreen
import com.crescentapps.turnly.presentation.screens.settings.SettingsViewModel
import com.crescentapps.turnly.presentation.theme.TurnlyAppTheme
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

class MainActivity : ComponentActivity() {

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled */ }

    private val incomingDeepLinkCode = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as TurnlyApplication

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        DailyNotificationScheduler.scheduleDailyAlarm(this)

        handleIncomingIntent(intent)

        setContent {
            val prefs by app.userPreferencesRepository.userPreferencesFlow.collectAsState(
                initial = com.crescentapps.turnly.data.preferences.UserPreferences()
            )
            val deepLinkCode by incomingDeepLinkCode.collectAsState()

            com.crescentapps.turnly.presentation.TurnlyApp(
                app = app,
                prefs = prefs,
                deepLinkCode = deepLinkCode
            )
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: android.content.Intent?) {
        val uri = intent?.data ?: return
        val rawUriString = uri.toString()
        val extractedCode = com.crescentapps.turnly.core.util.QRCodeUtils.extractRoomCode(rawUriString)
        if (!extractedCode.isNullOrBlank()) {
            incomingDeepLinkCode.value = extractedCode
        }
    }
}
