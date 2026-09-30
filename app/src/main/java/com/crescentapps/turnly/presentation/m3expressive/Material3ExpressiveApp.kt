package com.crescentapps.turnly.presentation.m3expressive

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.crescentapps.turnly.TurnlyApplication
import com.crescentapps.turnly.core.model.ThemeMode
import com.crescentapps.turnly.data.preferences.UserPreferences
import com.crescentapps.turnly.presentation.m3expressive.theme.TurnlyExpressiveTheme
import com.crescentapps.turnly.presentation.m3expressive.screens.home.M3HomeScreen
import com.crescentapps.turnly.presentation.m3expressive.screens.settings.M3SettingsScreen
import com.crescentapps.turnly.presentation.m3expressive.screens.calendar.M3CalendarScreen
import com.crescentapps.turnly.presentation.m3expressive.screens.schedule.M3SchedulesScreen
import com.crescentapps.turnly.presentation.m3expressive.screens.room.M3RoomsScreen
import com.crescentapps.turnly.presentation.m3expressive.screens.history.M3HistoryScreen
import com.crescentapps.turnly.presentation.m3expressive.screens.schedule.M3CreateScheduleScreen
import com.crescentapps.turnly.presentation.navigation.Screen
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Group
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crescentapps.turnly.presentation.screens.calendar.CalendarViewModel
import com.crescentapps.turnly.core.model.Schedule
import com.crescentapps.turnly.presentation.screens.home.HomeViewModel
import com.crescentapps.turnly.presentation.screens.settings.SettingsViewModel
import com.crescentapps.turnly.presentation.screens.room.RoomViewModel
import com.crescentapps.turnly.presentation.screens.history.HistoryViewModel
import com.crescentapps.turnly.presentation.screens.schedule.ScheduleFormViewModel

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
        useExpressiveMotion = prefs.m3UseExpressiveMotion && !prefs.isReduceMotion
    ) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        NavigationSuiteScaffold(
            navigationSuiteItems = {
                item(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Today") },
                    label = { Text("Today") },
                    selected = currentRoute == Screen.Home.route,
                    onClick = { navController.navigate(Screen.Home.route) { popUpTo(Screen.Home.route); launchSingleTop = true } }
                )
                item(
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "Calendar") },
                    label = { Text("Calendar") },
                    selected = currentRoute == Screen.Calendar.route,
                    onClick = { navController.navigate(Screen.Calendar.route) { popUpTo(Screen.Home.route); launchSingleTop = true } }
                )
                item(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Schedules") },
                    label = { Text("Schedules") },
                    selected = currentRoute == Screen.Schedules.route,
                    onClick = { navController.navigate(Screen.Schedules.route) { popUpTo(Screen.Home.route); launchSingleTop = true } }
                )
                item(
                    icon = { Icon(Icons.Default.Group, contentDescription = "Rooms") },
                    label = { Text("Rooms") },
                    selected = currentRoute == Screen.Rooms.route,
                    onClick = { navController.navigate(Screen.Rooms.route) { popUpTo(Screen.Home.route); launchSingleTop = true } }
                )
                item(
                    icon = { Icon(Icons.Default.History, contentDescription = "History") },
                    label = { Text("History") },
                    selected = currentRoute == Screen.History.route,
                    onClick = { navController.navigate(Screen.History.route) { popUpTo(Screen.Home.route); launchSingleTop = true } }
                )
                item(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = currentRoute == Screen.Settings.route,
                    onClick = { navController.navigate(Screen.Settings.route) { popUpTo(Screen.Home.route); launchSingleTop = true } }
                )
            }
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.fillMaxSize()
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
