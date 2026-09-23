package com.crescentapps.turnly.presentation.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.presentation.components.LocalAppWindowSizeDetails
import com.crescentapps.turnly.presentation.components.LocalPrismalAdaptiveColor
import com.crescentapps.turnly.presentation.components.liquid.LiquidBottomTab
import com.crescentapps.turnly.presentation.components.liquid.LiquidBottomTabs
import com.crescentapps.turnly.presentation.theme.LocalTurnlyColors
import com.kyant.backdrop.Backdrop

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : Screen("home", "Today", Icons.Filled.Today, Icons.Outlined.Today)
    object Calendar : Screen("calendar", "Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth)
    object Schedules : Screen("schedules", "Schedules", Icons.Filled.Loop, Icons.Outlined.Loop)
    object Rooms : Screen("rooms", "Shared", Icons.Filled.Share, Icons.Outlined.Share)
    object History : Screen("history", "History", Icons.Filled.History, Icons.Outlined.History)
    object Settings : Screen("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)

    // Sub-screens
    object CreateSchedule : Screen("create_schedule", "New Schedule", Icons.Filled.Add, Icons.Outlined.Add)
    object ScheduleDetail : Screen("schedule_detail/{scheduleId}", "Schedule Details", Icons.Filled.Info, Icons.Outlined.Info) {
        fun createRoute(scheduleId: Long) = "schedule_detail/$scheduleId"
    }

    object CreateRoom : Screen("create_room", "New Room", Icons.Filled.Add, Icons.Outlined.Add)
    object JoinRoom : Screen("join_room?code={code}", "Join Room", Icons.Filled.QrCodeScanner, Icons.Outlined.QrCodeScanner) {
        fun createRoute(code: String? = null) = if (code.isNullOrBlank()) "join_room" else "join_room?code=$code"
    }
    object RoomDetail : Screen("room_detail/{roomId}", "Room Details", Icons.Filled.Info, Icons.Outlined.Info) {
        fun createRoute(roomId: Long) = "room_detail/$roomId"
    }
    object RoomSettings : Screen("room_settings/{roomId}", "Room Settings", Icons.Filled.Settings, Icons.Outlined.Settings) {
        fun createRoute(roomId: Long) = "room_settings/$roomId"
    }
}

val mainScreens = listOf(
    Screen.Home,
    Screen.Calendar,
    Screen.Schedules,
    Screen.Rooms,
    Screen.History,
    Screen.Settings
)

/**
 * DhikrCounter Liquid Glass Dock / Bottom Tabs Navigation
 */
@Composable
fun TurnlyLiquidDock(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val selectedIndex = mainScreens.indexOfFirst { it.route == currentRoute }.coerceAtLeast(0)
    val sizeDetails = LocalAppWindowSizeDetails.current
    val isCompact = sizeDetails.widthDp < 400

    LiquidBottomTabs(
        selectedTabIndex = { selectedIndex },
        onTabSelected = { index ->
            if (index in mainScreens.indices) {
                onNavigate(mainScreens[index])
            }
        },
        backdrop = backdrop,
        tabsCount = mainScreens.size,
        accentColor = MaterialTheme.colorScheme.primary,
        modifier = modifier
    ) {
        val turnlyColors = LocalTurnlyColors.current
        mainScreens.forEach { screen ->
            val isSelected = currentRoute == screen.route
            val adaptiveColor = LocalPrismalAdaptiveColor.current
            
            val isDarkDockSurface = adaptiveColor.luminance() > 0.5f
            val selectedColor = if (isDarkDockSurface) {
                if (MaterialTheme.colorScheme.primary.luminance() < 0.4f) Color.White else MaterialTheme.colorScheme.primary
            } else {
                if (MaterialTheme.colorScheme.primary.luminance() > 0.65f) turnlyColors.textPrimary else MaterialTheme.colorScheme.primary
            }

            LiquidBottomTab(
                onClick = { onNavigate(screen) }
            ) {
                Icon(
                    imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                    contentDescription = screen.title,
                    modifier = Modifier.size(if (isCompact) 18.dp else 22.dp),
                    tint = if (isSelected) selectedColor else adaptiveColor
                )
                if (!isCompact) {
                    Text(
                        text = screen.title,
                        fontSize = 9.sp,
                        maxLines = 1,
                        color = if (isSelected) selectedColor else adaptiveColor.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

/**
 * Responsive Navigation Rail using DhikrCounter Liquid styling for Expanded/Tablet viewports
 */
@Composable
fun TurnlyLiquidNavigationRail(
    currentRoute: String?,
    onNavigate: (Screen) -> Unit,
    backdrop: Backdrop,
    modifier: Modifier = Modifier
) {
    val turnlyColors = LocalTurnlyColors.current
    val mainScreens = listOf(
        Screen.Home,
        Screen.Calendar,
        Screen.Schedules,
        Screen.Rooms,
        Screen.History,
        Screen.Settings
    )

    LiquidBottomTabs(
        selectedTabIndex = {
            val idx = mainScreens.indexOfFirst { it.route == currentRoute }
            if (idx >= 0) idx else 0
        },
        onTabSelected = { index ->
            onNavigate(mainScreens[index])
        },
        backdrop = backdrop,
        tabsCount = mainScreens.size,
        accentColor = MaterialTheme.colorScheme.primary,
        modifier = modifier
            .fillMaxHeight()
            .width(80.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically)
        ) {
            mainScreens.forEach { screen ->
                val isSelected = currentRoute == screen.route
                val adaptiveColor = LocalPrismalAdaptiveColor.current

                val isDarkDockSurface = adaptiveColor.luminance() > 0.5f
                val selectedColor = if (isDarkDockSurface) {
                    if (MaterialTheme.colorScheme.primary.luminance() < 0.4f) Color.White else MaterialTheme.colorScheme.primary
                } else {
                    if (MaterialTheme.colorScheme.primary.luminance() > 0.65f) turnlyColors.textPrimary else MaterialTheme.colorScheme.primary
                }

                com.crescentapps.turnly.presentation.components.liquid.LiquidIconButton(
                    onClick = { onNavigate(screen) },
                    backdrop = backdrop,
                    iconSize = 44.dp,
                    tint = if (isSelected) selectedColor.copy(alpha = 0.25f) else Color.Transparent
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) screen.selectedIcon else screen.unselectedIcon,
                            contentDescription = screen.title,
                            modifier = Modifier.size(20.dp),
                            tint = if (isSelected) selectedColor else adaptiveColor
                        )
                        Text(
                            text = screen.title,
                            fontSize = 8.sp,
                            maxLines = 1,
                            color = if (isSelected) selectedColor else adaptiveColor.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}
