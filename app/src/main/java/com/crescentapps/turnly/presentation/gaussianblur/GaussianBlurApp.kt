package com.crescentapps.turnly.presentation.gaussianblur

import androidx.compose.animation.*
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.crescentapps.turnly.TurnlyApplication
import com.crescentapps.turnly.core.model.Schedule
import com.crescentapps.turnly.data.preferences.UserPreferences
import com.crescentapps.turnly.presentation.gaussianblur.backdrop.GaussianBlurBackdropHost
import com.crescentapps.turnly.presentation.gaussianblur.navigation.GaussianDock
import com.crescentapps.turnly.presentation.gaussianblur.navigation.GaussianNavDestination
import com.crescentapps.turnly.presentation.gaussianblur.screens.*
import com.crescentapps.turnly.presentation.gaussianblur.theme.GaussianBlurTheme
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

@Composable
fun GaussianBlurApp(
    app: TurnlyApplication,
    prefs: UserPreferences,
    deepLinkCode: String? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val homeViewModel: HomeViewModel = viewModel { HomeViewModel(app.repository, app.roomRepository) }
    val calendarViewModel: CalendarViewModel = viewModel { CalendarViewModel(app.repository) }
    val historyViewModel: HistoryViewModel = viewModel { HistoryViewModel(app.repository) }
    val roomViewModel: RoomViewModel = viewModel { RoomViewModel(app.roomRepository, app.repository) }
    val settingsViewModel: SettingsViewModel = viewModel { SettingsViewModel(app.userPreferencesRepository, app.repository, app.updateManager) }
    val scheduleFormViewModel: ScheduleFormViewModel = viewModel { ScheduleFormViewModel(app.repository, app.roomRepository) }

    val schedulesState = app.repository.allSchedules.collectAsState(initial = emptyList())
    val schedules = schedulesState.value

    // Map current route to dock destination
    val currentDockDestination = when {
        currentRoute == "today" -> GaussianNavDestination.TODAY
        currentRoute == "calendar" -> GaussianNavDestination.CALENDAR
        currentRoute == "schedules" || currentRoute?.startsWith("schedule") == true -> GaussianNavDestination.SCHEDULES
        currentRoute == "history" -> GaussianNavDestination.HISTORY
        currentRoute == "rooms" || currentRoute?.startsWith("room") == true || currentRoute?.startsWith("create_room") == true || currentRoute?.startsWith("join_room") == true -> GaussianNavDestination.ROOMS
        currentRoute == "settings" -> GaussianNavDestination.SETTINGS
        else -> GaussianNavDestination.TODAY
    }

    LaunchedEffect(deepLinkCode) {
        if (!deepLinkCode.isNullOrBlank()) {
            navController.navigate("join_room?code=$deepLinkCode")
        }
    }

    GaussianBlurTheme(prefs = prefs) {
        GaussianBlurBackdropHost(prefs = prefs) {
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = "today",
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = { fadeIn() },
                    exitTransition = { fadeOut() }
                ) {
                    composable("today") {
                        GaussianHomeScreen(
                            viewModel = homeViewModel,
                            onCreateSchedule = { navController.navigate("create_schedule") },
                            onScheduleClick = { id -> navController.navigate("schedule_detail/$id") }
                        )
                    }

                    composable("calendar") {
                        GaussianCalendarScreen(
                            viewModel = calendarViewModel,
                            onScheduleClick = { id -> navController.navigate("schedule_detail/$id") }
                        )
                    }

                    composable("schedules") {
                        GaussianScheduleListScreen(
                            schedules = schedules,
                            onCreateSchedule = { navController.navigate("create_schedule") },
                            onScheduleClick = { id -> navController.navigate("schedule_detail/$id") }
                        )
                    }

                    composable("create_schedule") {
                        GaussianCreateScheduleScreen(
                            viewModel = scheduleFormViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onCreatedSuccessfully = { navController.popBackStack() }
                        )
                    }

                    composable(
                        route = "schedule_detail/{scheduleId}",
                        arguments = listOf(navArgument("scheduleId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val scheduleId = backStackEntry.arguments?.getLong("scheduleId") ?: 0L
                        val detailViewModel: ScheduleDetailViewModel = viewModel(key = "schedule_detail_$scheduleId") {
                            ScheduleDetailViewModel(app.repository, scheduleId)
                        }
                        GaussianScheduleDetailScreen(
                            viewModel = detailViewModel,
                            onNavigateBack = { navController.popBackStack() }
                        )
                    }

                    composable("history") {
                        GaussianHistoryScreen(viewModel = historyViewModel)
                    }

                    composable("rooms") {
                        GaussianRoomListScreen(
                            viewModel = roomViewModel,
                            onCreateRoom = { navController.navigate("create_room") },
                            onJoinRoom = { navController.navigate("join_room") },
                            onRoomClick = { id -> navController.navigate("room_detail/$id") }
                        )
                    }

                    composable("create_room") {
                        GaussianCreateRoomScreen(
                            viewModel = roomViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onRoomCreated = { id ->
                                navController.popBackStack()
                                navController.navigate("room_detail/$id")
                            }
                        )
                    }

                    composable(
                        route = "join_room?code={code}",
                        arguments = listOf(navArgument("code") {
                            type = NavType.StringType
                            nullable = true
                            defaultValue = null
                        })
                    ) { backStackEntry ->
                        val code = backStackEntry.arguments?.getString("code")
                        GaussianJoinRoomScreen(
                            viewModel = roomViewModel,
                            initialRoomCode = code,
                            onNavigateBack = { navController.popBackStack() },
                            onJoinedSuccessfully = { id ->
                                navController.popBackStack()
                                navController.navigate("room_detail/$id")
                            }
                        )
                    }

                    composable(
                        route = "room_detail/{roomId}",
                        arguments = listOf(navArgument("roomId") { type = NavType.LongType })
                    ) { backStackEntry ->
                        val roomId = backStackEntry.arguments?.getLong("roomId") ?: 0L
                        val roomDetailViewModel: RoomDetailViewModel = viewModel(key = "room_detail_$roomId") {
                            RoomDetailViewModel(app.roomRepository, app.repository, roomId)
                        }
                        GaussianRoomDetailScreen(
                            viewModel = roomDetailViewModel,
                            onNavigateBack = { navController.popBackStack() },
                            onNavigateToSettings = { id -> },
                            onScheduleClick = { id -> navController.navigate("schedule_detail/$id") }
                        )
                    }

                    composable("settings") {
                        GaussianSettingsScreen(viewModel = settingsViewModel)
                    }
                }

                // Gaussian Floating Dock
                GaussianDock(
                    currentDestination = currentDockDestination,
                    onNavigate = { dest ->
                        val route = when (dest) {
                            GaussianNavDestination.TODAY -> "today"
                            GaussianNavDestination.CALENDAR -> "calendar"
                            GaussianNavDestination.SCHEDULES -> "schedules"
                            GaussianNavDestination.HISTORY -> "history"
                            GaussianNavDestination.ROOMS -> "rooms"
                            GaussianNavDestination.SETTINGS -> "settings"
                        }
                        navController.navigate(route) {
                            popUpTo("today") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }
        }
    }
}
