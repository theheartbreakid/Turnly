package com.crescentapps.turnly.presentation.m3expressive

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.crescentapps.turnly.TurnlyApplication
import com.crescentapps.turnly.core.model.Schedule
import com.crescentapps.turnly.core.model.ThemeMode
import com.crescentapps.turnly.data.preferences.UserPreferences
import com.crescentapps.turnly.presentation.m3expressive.navigation.M3ExpressiveDock
import com.crescentapps.turnly.presentation.m3expressive.screens.calendar.M3CalendarScreen
import com.crescentapps.turnly.presentation.m3expressive.screens.history.M3HistoryScreen
import com.crescentapps.turnly.presentation.m3expressive.screens.home.M3HomeScreen
import com.crescentapps.turnly.presentation.m3expressive.screens.room.M3RoomsScreen
import com.crescentapps.turnly.presentation.m3expressive.screens.schedule.M3CreateScheduleScreen
import com.crescentapps.turnly.presentation.m3expressive.screens.schedule.M3SchedulesScreen
import com.crescentapps.turnly.presentation.m3expressive.screens.settings.M3SettingsScreen
import com.crescentapps.turnly.presentation.m3expressive.theme.TurnlyExpressiveTheme
import com.crescentapps.turnly.presentation.navigation.Screen
import com.crescentapps.turnly.presentation.screens.calendar.CalendarViewModel
import com.crescentapps.turnly.presentation.screens.history.HistoryViewModel
import com.crescentapps.turnly.presentation.screens.home.HomeViewModel
import com.crescentapps.turnly.presentation.screens.room.RoomViewModel
import com.crescentapps.turnly.presentation.screens.schedule.ScheduleFormViewModel
import com.crescentapps.turnly.presentation.screens.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Material3ExpressiveApp(
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

    TurnlyExpressiveTheme(
        isDark = isDark,
        dynamicColor = prefs.m3DynamicColor,
        useExpressiveMotion = prefs.m3UseExpressiveMotion && !prefs.isReduceMotion,
        prefs = prefs
    ) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        // Main top-level routes that show the floating dock
        val mainRoutes = listOf(
            Screen.Home.route,
            Screen.Calendar.route,
            Screen.Schedules.route,
            Screen.Rooms.route,
            Screen.History.route,
            Screen.Settings.route
        )
        val showDock = currentRoute in mainRoutes

        Scaffold(
            bottomBar = {
                if (showDock) {
                    M3ExpressiveDock(
                        currentRoute = currentRoute,
                        onNavigate = { destination ->
                            navController.navigate(destination.route) {
                                popUpTo(Screen.Home.route) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = if (showDock) innerPadding.calculateBottomPadding() else ScaffoldDefaults.contentWindowInsets.asPaddingValues().calculateBottomPadding())
            ) {
                composable(Screen.Home.route) {
                    val homeViewModel: HomeViewModel = viewModel { HomeViewModel(app.repository, app.roomRepository) }
                    M3HomeScreen(
                        viewModel = homeViewModel,
                        onCreateSchedule = { navController.navigate(Screen.CreateSchedule.route) },
                        onScheduleClick = { sId ->
                            navController.navigate(Screen.ScheduleDetail.createRoute(sId))
                        }
                    )
                }
                composable(Screen.Calendar.route) {
                    val calendarViewModel: CalendarViewModel = viewModel { CalendarViewModel(app.repository) }
                    M3CalendarScreen(
                        viewModel = calendarViewModel,
                        onScheduleClick = { sId ->
                            navController.navigate(Screen.ScheduleDetail.createRoute(sId))
                        }
                    )
                }
                composable(Screen.Schedules.route) {
                    val schedules by app.repository.activeSchedules.collectAsState(initial = emptyList<Schedule>())
                    M3SchedulesScreen(
                        schedules = schedules,
                        onCreateSchedule = { navController.navigate(Screen.CreateSchedule.route) },
                        onScheduleClick = { sId ->
                            navController.navigate(Screen.ScheduleDetail.createRoute(sId))
                        }
                    )
                }
                composable(Screen.Rooms.route) {
                    val roomViewModel: RoomViewModel = viewModel { RoomViewModel(app.roomRepository, app.repository) }
                    M3RoomsScreen(
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
                    com.crescentapps.turnly.presentation.m3expressive.screens.room.M3CreateRoomScreen(
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
                    com.crescentapps.turnly.presentation.m3expressive.screens.room.M3JoinRoomScreen(
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
                    val detailViewModel: com.crescentapps.turnly.presentation.screens.room.RoomDetailViewModel = viewModel(key = "room_detail_$rId") {
                        com.crescentapps.turnly.presentation.screens.room.RoomDetailViewModel(app.roomRepository, app.repository, rId)
                    }
                    com.crescentapps.turnly.presentation.m3expressive.screens.room.M3RoomDetailScreen(
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
                    val detailViewModel: com.crescentapps.turnly.presentation.screens.room.RoomDetailViewModel = viewModel(key = "room_settings_$rId") {
                        com.crescentapps.turnly.presentation.screens.room.RoomDetailViewModel(app.roomRepository, app.repository, rId)
                    }
                    com.crescentapps.turnly.presentation.m3expressive.screens.room.M3RoomSettingsScreen(
                        viewModel = detailViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onRoomExited = {
                            navController.popBackStack(Screen.Rooms.route, false)
                        }
                    )
                }
                composable(
                    route = Screen.ScheduleDetail.route,
                    arguments = listOf(navArgument("scheduleId") { type = NavType.LongType })
                ) { backStackEntry ->
                    val sId = backStackEntry.arguments?.getLong("scheduleId") ?: return@composable
                    val detailViewModel: com.crescentapps.turnly.presentation.screens.schedule.ScheduleDetailViewModel = viewModel(key = "schedule_detail_$sId") {
                        com.crescentapps.turnly.presentation.screens.schedule.ScheduleDetailViewModel(app.repository, sId)
                    }
                    com.crescentapps.turnly.presentation.m3expressive.screens.schedule.M3ScheduleDetailScreen(
                        viewModel = detailViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.History.route) {
                    val historyViewModel: HistoryViewModel = viewModel { HistoryViewModel(app.repository) }
                    M3HistoryScreen(viewModel = historyViewModel)
                }
                composable(Screen.Settings.route) {
                    val settingsViewModel: SettingsViewModel = viewModel {
                        SettingsViewModel(app.userPreferencesRepository, app.repository, app.updateManager)
                    }
                    M3SettingsScreen(viewModel = settingsViewModel)
                }
                composable(Screen.CreateSchedule.route) {
                    val formViewModel: ScheduleFormViewModel = viewModel { ScheduleFormViewModel(app.repository, app.roomRepository) }
                    M3CreateScheduleScreen(
                        viewModel = formViewModel,
                        onNavigateBack = { navController.popBackStack() },
                        onCreatedSuccessfully = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
