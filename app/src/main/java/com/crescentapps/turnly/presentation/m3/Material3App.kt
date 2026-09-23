package com.crescentapps.turnly.presentation.m3

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.crescentapps.turnly.TurnlyApplication
import com.crescentapps.turnly.core.model.Schedule
import com.crescentapps.turnly.data.preferences.UserPreferences
import com.crescentapps.turnly.presentation.m3.navigation.M3BottomNavigationBar
import com.crescentapps.turnly.presentation.m3.navigation.M3NavigationRail
import com.crescentapps.turnly.presentation.m3.screens.calendar.M3CalendarScreen
import com.crescentapps.turnly.presentation.m3.screens.history.M3HistoryScreen
import com.crescentapps.turnly.presentation.m3.screens.home.M3HomeScreen
import com.crescentapps.turnly.presentation.m3.screens.room.*
import com.crescentapps.turnly.presentation.m3.screens.schedule.*
import com.crescentapps.turnly.presentation.m3.screens.settings.M3SettingsScreen
import com.crescentapps.turnly.presentation.m3.theme.TurnlyM3Theme
import com.crescentapps.turnly.presentation.navigation.Screen
import com.crescentapps.turnly.presentation.navigation.mainScreens
import com.crescentapps.turnly.presentation.screens.calendar.CalendarViewModel
import com.crescentapps.turnly.presentation.screens.history.HistoryViewModel
import com.crescentapps.turnly.presentation.screens.home.HomeViewModel
import com.crescentapps.turnly.presentation.screens.room.RoomDetailViewModel
import com.crescentapps.turnly.presentation.screens.room.RoomViewModel
import com.crescentapps.turnly.presentation.screens.schedule.ScheduleDetailViewModel
import com.crescentapps.turnly.presentation.screens.schedule.ScheduleFormViewModel
import com.crescentapps.turnly.presentation.screens.settings.SettingsViewModel

/**
 * Pure Material 3 Presentation Root for Turnly.
 * Built with standard Jetpack Compose Material 3 components:
 * - TurnlyM3Theme (Light, Dark, Amoled, Dynamic)
 * - NavigationBar (compact) & NavigationRail (expanded / tablet / landscape)
 * - Standard M3 Scaffolding & responsive layout adaptation
 * - Completely free of PrismalScene, GLSurfaceView, LiquidCard, or Liquid shaders.
 */
@Composable
fun Material3App(
    app: TurnlyApplication,
    prefs: UserPreferences,
    deepLinkCode: String? = null
) {
    TurnlyM3Theme(
        themeMode = prefs.themeMode,
        isAmoled = prefs.isAmoled,
        dynamicColor = prefs.dynamicColors
    ) {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        LaunchedEffect(deepLinkCode) {
            if (!deepLinkCode.isNullOrBlank()) {
                navController.navigate(Screen.JoinRoom.createRoute(deepLinkCode))
            }
        }

        val config = LocalConfiguration.current
        val isExpanded = config.screenWidthDp >= 600
        val isMainScreen = currentRoute in mainScreens.map { it.route }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                // Responsive Navigation Rail for tablets & landscape
                if (isExpanded && isMainScreen) {
                    M3NavigationRail(
                        currentRoute = currentRoute,
                        onNavigate = { screen ->
                            navController.navigate(screen.route) {
                                popUpTo(Screen.Home.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }

                // Main Navigation Scaffold
                Scaffold(
                    modifier = Modifier.weight(1f),
                    bottomBar = {
                        if (!isExpanded && isMainScreen) {
                            M3BottomNavigationBar(
                                currentRoute = currentRoute,
                                onNavigate = { screen ->
                                    navController.navigate(screen.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        composable(Screen.Home.route) {
                            val homeViewModel: HomeViewModel = viewModel {
                                HomeViewModel(app.repository, app.roomRepository)
                            }
                            M3HomeScreen(
                                viewModel = homeViewModel,
                                onCreateSchedule = { navController.navigate(Screen.CreateSchedule.route) },
                                onScheduleClick = { sId ->
                                    navController.navigate(Screen.ScheduleDetail.createRoute(sId))
                                }
                            )
                        }

                        composable(Screen.Calendar.route) {
                            val calendarViewModel: CalendarViewModel = viewModel {
                                CalendarViewModel(app.repository)
                            }
                            M3CalendarScreen(
                                viewModel = calendarViewModel,
                                onScheduleClick = { sId ->
                                    navController.navigate(Screen.ScheduleDetail.createRoute(sId))
                                }
                            )
                        }

                        composable(Screen.Schedules.route) {
                            val schedules by app.repository.activeSchedules.collectAsState(initial = emptyList<Schedule>())
                            M3ScheduleListScreen(
                                schedules = schedules,
                                onCreateSchedule = { navController.navigate(Screen.CreateSchedule.route) },
                                onScheduleClick = { sId ->
                                    navController.navigate(Screen.ScheduleDetail.createRoute(sId))
                                }
                            )
                        }

                        composable(Screen.Rooms.route) {
                            val roomViewModel: RoomViewModel = viewModel {
                                RoomViewModel(app.roomRepository, app.repository)
                            }
                            M3RoomListScreen(
                                viewModel = roomViewModel,
                                onCreateRoom = { navController.navigate(Screen.CreateRoom.route) },
                                onJoinRoom = { navController.navigate(Screen.JoinRoom.createRoute()) },
                                onRoomClick = { rId ->
                                    navController.navigate(Screen.RoomDetail.createRoute(rId))
                                }
                            )
                        }

                        composable(Screen.CreateRoom.route) {
                            val roomViewModel: RoomViewModel = viewModel {
                                RoomViewModel(app.roomRepository, app.repository)
                            }
                            M3CreateRoomScreen(
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
                            val roomViewModel: RoomViewModel = viewModel {
                                RoomViewModel(app.roomRepository, app.repository)
                            }
                            M3JoinRoomScreen(
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
                            val detailViewModel: RoomDetailViewModel = viewModel(key = "m3_room_detail_$rId") {
                                RoomDetailViewModel(app.roomRepository, app.repository, rId)
                            }
                            M3RoomDetailScreen(
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
                            val detailViewModel: RoomDetailViewModel = viewModel(key = "m3_room_settings_$rId") {
                                RoomDetailViewModel(app.roomRepository, app.repository, rId)
                            }
                            M3RoomSettingsScreen(
                                viewModel = detailViewModel,
                                onNavigateBack = { navController.popBackStack() },
                                onRoomExited = {
                                    navController.popBackStack(Screen.Rooms.route, false)
                                }
                            )
                        }

                        composable(Screen.History.route) {
                            val historyViewModel: HistoryViewModel = viewModel {
                                HistoryViewModel(app.repository)
                            }
                            M3HistoryScreen(viewModel = historyViewModel)
                        }

                        composable(Screen.Settings.route) {
                            val settingsViewModel: SettingsViewModel = viewModel {
                                SettingsViewModel(app.userPreferencesRepository, app.repository, app.updateManager)
                            }
                            M3SettingsScreen(viewModel = settingsViewModel)
                        }

                        composable(Screen.CreateSchedule.route) {
                            val formViewModel: ScheduleFormViewModel = viewModel {
                                ScheduleFormViewModel(app.repository, app.roomRepository)
                            }
                            M3CreateScheduleScreen(
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
                            val detailViewModel: ScheduleDetailViewModel = viewModel(key = "m3_schedule_detail_$sId") {
                                ScheduleDetailViewModel(app.repository, sId)
                            }
                            M3ScheduleDetailScreen(
                                viewModel = detailViewModel,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
