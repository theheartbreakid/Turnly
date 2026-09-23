package com.crescentapps.turnly.presentation.liquid

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.crescentapps.turnly.TurnlyApplication
import com.crescentapps.turnly.core.model.Schedule
import com.crescentapps.turnly.core.model.ThemeMode
import com.crescentapps.turnly.data.preferences.UserPreferences
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

/**
 * LiquidApp: Pure encapsulation of the Prismal/Liquid visual pipeline.
 * Contains:
 * - PrismalScene
 * - TurnlyBackdropBackground / Custom wallpaper
 * - TurnlyLiquidDock & TurnlyLiquidNavigationRail
 * - LocalGlassSettings, LocalDockSettings, LocalAdaptiveLuminance
 * - Full DhikrCounter-derived glass components and settings.
 * 
 * Only instantiated when UiMode == LIQUID.
 */
@Composable
fun LiquidApp(
    app: TurnlyApplication,
    prefs: UserPreferences,
    deepLinkCode: String? = null
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (prefs.themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    TurnlyAppTheme(
        themeMode = prefs.themeMode,
        isAmoled = prefs.isAmoled,
        dynamicColor = prefs.dynamicColors,
        appFontFamily = prefs.appFontFamily,
        appFontWeight = prefs.appFontWeight,
        appFontTilt = prefs.appFontTilt
    ) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        LaunchedEffect(deepLinkCode) {
            if (!deepLinkCode.isNullOrBlank()) {
                navController.navigate(Screen.JoinRoom.createRoute(deepLinkCode))
            }
        }

        val backdrop = rememberLayerBackdrop()

        val config = LocalConfiguration.current
        val widthDp = config.screenWidthDp
        val heightDp = config.screenHeightDp
        val isLandscape = config.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

        val widthClass = when {
            widthDp < 360 -> AppWindowWidthSizeClass.COMPACT
            widthDp < 600 -> AppWindowWidthSizeClass.MEDIUM
            else -> AppWindowWidthSizeClass.EXPANDED
        }

        val heightClass = when {
            heightDp < 400 -> AppWindowHeightSizeClass.COMPACT
            heightDp < 600 -> AppWindowHeightSizeClass.MEDIUM
            else -> AppWindowHeightSizeClass.EXPANDED
        }

        val windowSizeDetails = remember(widthClass, heightClass, isLandscape, widthDp, heightDp) {
            AppWindowSizeDetails(
                widthClass = widthClass,
                heightClass = heightClass,
                isLandscape = isLandscape,
                widthDp = widthDp,
                heightDp = heightDp
            )
        }

        val glassSettings = remember(
            prefs.glassBlurRadius,
            prefs.glassCornerRadius,
            prefs.glassRefractionHeight,
            prefs.glassRefractionAmount,
            prefs.glassChromaticAberration,
            prefs.glassIOR,
            prefs.glassThickness,
            prefs.glassNormalStrength,
            prefs.glassBrightness,
            prefs.glassRimIntensity,
            prefs.glassSpecularIntensity,
            prefs.glassShininess,
            prefs.glassDisplacementScale,
            prefs.glassMinSmoothing,
            prefs.glassHighlightWidth,
            prefs.glassCausticIntensity,
            prefs.glassLiquidDome,
            prefs.glassTransmittance,
            prefs.glassLightDirX,
            prefs.glassLightDirY,
            prefs.glassShadowColor,
            prefs.glassShadowIntensity,
            prefs.glassShadowSoftness,
            prefs.glassCaptureDownsample
        ) {
            GlassSettings(
                blurRadius = prefs.glassBlurRadius,
                cornerRadius = prefs.glassCornerRadius,
                refractionHeight = prefs.glassRefractionHeight,
                refractionAmount = prefs.glassRefractionAmount,
                chromaticAberration = prefs.glassChromaticAberration,
                ior = prefs.glassIOR,
                thickness = prefs.glassThickness,
                normalStrength = prefs.glassNormalStrength,
                brightness = prefs.glassBrightness,
                rimIntensity = prefs.glassRimIntensity,
                specularIntensity = prefs.glassSpecularIntensity,
                shininess = prefs.glassShininess,
                displacementScale = prefs.glassDisplacementScale,
                minSmoothing = prefs.glassMinSmoothing,
                highlightWidth = prefs.glassHighlightWidth,
                causticIntensity = prefs.glassCausticIntensity,
                liquidDome = prefs.glassLiquidDome,
                transmittance = prefs.glassTransmittance,
                lightDirX = prefs.glassLightDirX,
                lightDirY = prefs.glassLightDirY,
                shadowColor = prefs.glassShadowColor,
                shadowIntensity = prefs.glassShadowIntensity,
                shadowSoftness = prefs.glassShadowSoftness,
                captureDownsample = prefs.glassCaptureDownsample
            )
        }

        val dockSettings = remember(
            prefs.dockBlurRadius,
            prefs.dockCornerRadius,
            prefs.dockRefractionHeight,
            prefs.dockRefractionAmount,
            prefs.dockChromaticAberration
        ) {
            DockSettings(
                blurRadius = prefs.dockBlurRadius,
                cornerRadius = prefs.dockCornerRadius,
                refractionHeight = prefs.dockRefractionHeight,
                refractionAmount = prefs.dockRefractionAmount,
                chromaticAberration = prefs.dockChromaticAberration
            )
        }

        PrismalScene {
            CompositionLocalProvider(
                LocalBackdrop provides backdrop,
                LocalAppWindowSizeDetails provides windowSizeDetails,
                LocalGlassSettings provides glassSettings,
                LocalDockSettings provides dockSettings,
                LocalAdaptiveLuminanceEnabled provides prefs.adaptiveLuminance,
                LocalAdaptiveLuminanceInterval provides prefs.adaptiveLuminanceInterval,
                LocalGlassIntensity provides prefs.glassIntensity,
                LocalHapticIntensity provides prefs.hapticIntensity,
                LocalHapticEnabled provides prefs.hapticFeedbackEnabled,
                LocalSoundFeedbackEnabled provides prefs.soundFeedbackEnabled,
                LocalFontTintFallbackMode provides prefs.fontTintFallbackMode,
                LocalFontTintPaletteColor provides prefs.fontTintPaletteColor,
                LocalFontTintCustomColor provides prefs.fontTintCustomColor
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (prefs.backgroundImageUri.isNotBlank()) {
                        coil.compose.AsyncImage(
                            model = prefs.backgroundImageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().layerBackdrop(backdrop),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        TurnlyBackdropBackground(
                            backdrop = backdrop,
                            isDark = isDark,
                            isAmoled = prefs.isAmoled,
                            isReduceMotion = prefs.isReduceMotion
                        )
                    }

                    val isExpanded = widthClass == AppWindowWidthSizeClass.EXPANDED

                    Row(modifier = Modifier.fillMaxSize()) {
                        // Responsive Navigation Rail for tablets
                        if (isExpanded && currentRoute in listOf(
                                Screen.Home.route,
                                Screen.Calendar.route,
                                Screen.Schedules.route,
                                Screen.Rooms.route,
                                Screen.History.route,
                                Screen.Settings.route
                            )
                        ) {
                            TurnlyLiquidNavigationRail(
                                currentRoute = currentRoute,
                                onNavigate = { screen ->
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                backdrop = backdrop
                            )
                        }

                        // Main Navigation Scaffold
                        Scaffold(
                            modifier = Modifier.weight(1f),
                            containerColor = Color.Transparent
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding)
                            ) {
                                NavHost(
                                    navController = navController,
                                    startDestination = Screen.Home.route,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    composable(Screen.Home.route) {
                                        val homeViewModel: HomeViewModel = viewModel { HomeViewModel(app.repository, app.roomRepository) }
                                        HomeScreen(
                                            viewModel = homeViewModel,
                                            onCreateSchedule = { navController.navigate(Screen.CreateSchedule.route) },
                                            onScheduleClick = { sId ->
                                                navController.navigate(Screen.ScheduleDetail.createRoute(sId))
                                            }
                                        )
                                    }

                                    composable(Screen.Calendar.route) {
                                        val calendarViewModel: CalendarViewModel = viewModel { CalendarViewModel(app.repository) }
                                        CalendarScreen(
                                            viewModel = calendarViewModel,
                                            onScheduleClick = { sId ->
                                                navController.navigate(Screen.ScheduleDetail.createRoute(sId))
                                            }
                                        )
                                    }

                                    composable(Screen.Schedules.route) {
                                        val schedules by app.repository.activeSchedules.collectAsState(initial = emptyList<Schedule>())
                                        ScheduleListScreen(
                                            schedules = schedules,
                                            onCreateSchedule = { navController.navigate(Screen.CreateSchedule.route) },
                                            onScheduleClick = { sId ->
                                                navController.navigate(Screen.ScheduleDetail.createRoute(sId))
                                            }
                                        )
                                    }

                                    composable(Screen.Rooms.route) {
                                        val roomViewModel: RoomViewModel = viewModel { RoomViewModel(app.roomRepository, app.repository) }
                                        RoomListScreen(
                                            viewModel = roomViewModel,
                                            onCreateRoom = { navController.navigate(Screen.CreateRoom.route) },
                                            onJoinRoom = { navController.navigate(Screen.JoinRoom.createRoute()) },
                                            onRoomClick = { rId ->
                                                navController.navigate(Screen.RoomDetail.createRoute(rId))
                                            }
                                        )
                                    }

                                    composable(Screen.CreateRoom.route) {
                                        val roomViewModel: RoomViewModel = viewModel { RoomViewModel(app.roomRepository, app.repository) }
                                        CreateRoomScreen(
                                            viewModel = roomViewModel,
                                            onNavigateBack = { navController.popBackStack() },
                                            onRoomCreated = { rId ->
                                                navController.navigate(Screen.RoomDetail.createRoute(rId)) {
                                                    popUpTo(Screen.Rooms.route)
                                                }
                                            }
                                        )
                                    }

                                    composable(
                                        route = Screen.JoinRoom.route,
                                        arguments = listOf(navArgument("code") {
                                            type = NavType.StringType
                                            nullable = true
                                            defaultValue = null
                                        })
                                    ) { backStackEntry ->
                                        val codeArg = backStackEntry.arguments?.getString("code")
                                        val roomViewModel: RoomViewModel = viewModel { RoomViewModel(app.roomRepository, app.repository) }
                                        JoinRoomScreen(
                                            viewModel = roomViewModel,
                                            initialRoomCode = codeArg,
                                            onNavigateBack = { navController.popBackStack() },
                                            onJoinedSuccessfully = { rId ->
                                                navController.navigate(Screen.RoomDetail.createRoute(rId)) {
                                                    popUpTo(Screen.Rooms.route)
                                                }
                                            }
                                        )
                                    }

                                    composable(
                                        route = Screen.RoomDetail.route,
                                        arguments = listOf(navArgument("roomId") { type = NavType.LongType })
                                    ) { backStackEntry ->
                                        val rId = backStackEntry.arguments?.getLong("roomId") ?: return@composable
                                        val detailViewModel: RoomDetailViewModel = viewModel(key = "room_detail_$rId") {
                                            RoomDetailViewModel(app.roomRepository, app.repository, rId)
                                        }
                                        RoomDetailScreen(
                                            viewModel = detailViewModel,
                                            onNavigateBack = { navController.popBackStack() },
                                            onNavigateToSettings = { roomId ->
                                                navController.navigate(Screen.RoomSettings.createRoute(roomId))
                                            },
                                            onScheduleClick = { sId ->
                                                navController.navigate(Screen.ScheduleDetail.createRoute(sId))
                                            }
                                        )
                                    }

                                    composable(
                                        route = Screen.RoomSettings.route,
                                        arguments = listOf(navArgument("roomId") { type = NavType.LongType })
                                    ) { backStackEntry ->
                                        val rId = backStackEntry.arguments?.getLong("roomId") ?: return@composable
                                        val detailViewModel: RoomDetailViewModel = viewModel(key = "room_settings_$rId") {
                                            RoomDetailViewModel(app.roomRepository, app.repository, rId)
                                        }
                                        RoomSettingsScreen(
                                            viewModel = detailViewModel,
                                            onNavigateBack = { navController.popBackStack() },
                                            onRoomExited = {
                                                navController.popBackStack(Screen.Rooms.route, false)
                                            }
                                        )
                                    }

                                    composable(Screen.History.route) {
                                        val historyViewModel: HistoryViewModel = viewModel { HistoryViewModel(app.repository) }
                                        HistoryScreen(viewModel = historyViewModel)
                                    }

                                    composable(Screen.Settings.route) {
                                        val settingsViewModel: SettingsViewModel = viewModel {
                                            SettingsViewModel(app.userPreferencesRepository, app.repository)
                                        }
                                        SettingsScreen(viewModel = settingsViewModel)
                                    }

                                    composable(Screen.CreateSchedule.route) {
                                        val formViewModel: ScheduleFormViewModel = viewModel { ScheduleFormViewModel(app.repository, app.roomRepository) }
                                        CreateScheduleScreen(
                                            viewModel = formViewModel,
                                            onNavigateBack = { navController.popBackStack() },
                                            onCreatedSuccessfully = { navController.popBackStack() }
                                        )
                                    }

                                    composable(
                                        route = Screen.ScheduleDetail.route,
                                        arguments = listOf(navArgument("scheduleId") { type = NavType.LongType })
                                    ) { backStackEntry ->
                                        val sId = backStackEntry.arguments?.getLong("scheduleId") ?: return@composable
                                        val detailViewModel: ScheduleDetailViewModel = viewModel(key = "schedule_detail_$sId") {
                                            ScheduleDetailViewModel(app.repository, sId)
                                        }
                                        ScheduleDetailScreen(
                                            viewModel = detailViewModel,
                                            onNavigateBack = { navController.popBackStack() }
                                        )
                                    }
                                }

                                // Floating DhikrCounter Liquid Bottom Dock
                                if (!isExpanded && currentRoute in listOf(
                                        Screen.Home.route,
                                        Screen.Calendar.route,
                                        Screen.Schedules.route,
                                        Screen.Rooms.route,
                                        Screen.History.route,
                                        Screen.Settings.route
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 16.dp, vertical = 20.dp),
                                        contentAlignment = Alignment.BottomCenter
                                    ) {
                                        TurnlyLiquidDock(
                                            currentRoute = currentRoute,
                                            onNavigate = { screen ->
                                                navController.navigate(screen.route) {
                                                    popUpTo(Screen.Home.route) { saveState = true }
                                                    launchSingleTop = true
                                                    restoreState = true
                                                }
                                            },
                                            backdrop = backdrop
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
