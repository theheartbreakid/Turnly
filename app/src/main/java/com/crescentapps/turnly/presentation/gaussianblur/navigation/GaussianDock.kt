package com.crescentapps.turnly.presentation.gaussianblur.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.presentation.components.Icon
import com.crescentapps.turnly.presentation.components.Text
import com.crescentapps.turnly.presentation.gaussianblur.backdrop.LocalAppBackdrop
import com.crescentapps.turnly.presentation.gaussianblur.backdrop.gaussianGlass
import com.crescentapps.turnly.presentation.gaussianblur.theme.GaussianBlurTokens
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianBlurColors

enum class GaussianNavDestination(val label: String, val icon: ImageVector) {
    TODAY("Today", Icons.Outlined.Today),
    CALENDAR("Calendar", Icons.Outlined.CalendarMonth),
    SCHEDULES("Schedules", Icons.Outlined.ListAlt),
    HISTORY("History", Icons.Outlined.History),
    ROOMS("Rooms", Icons.Outlined.Groups),
    SETTINGS("Settings", Icons.Outlined.Settings)
}

@Composable
fun GaussianDock(
    currentDestination: GaussianNavDestination,
    onNavigate: (GaussianNavDestination) -> Unit,
    modifier: Modifier = Modifier,
    scrollConnection: FloatingTabBarScrollConnection = rememberFloatingTabBarScrollConnection()
) {
    val backdrop = LocalAppBackdrop.current
    val colors = LocalGaussianBlurColors.current
    val pillShape = remember { RoundedCornerShape(percent = 50) }

    val glassSurface: @Composable () -> Modifier = {
        Modifier.gaussianGlass(
            shape = pillShape,
            backdrop = backdrop,
            blurRadiusDp = 28f,
            surfaceOpacity = 0.45f,
            isDark = colors.isDark
        )
    }

    val destinations = GaussianNavDestination.entries

    FloatingTabBar(
        selectedTabKey = currentDestination,
        scrollConnection = scrollConnection,
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 12.dp)
            .padding(bottom = 6.dp)
            .fillMaxWidth(),
        tabBarContentModifier = glassSurface,
        colors = FloatingTabBarDefaults.colors(
            backgroundColor = Color.Transparent,
            accessoryBackgroundColor = Color.Transparent,
            indicatorColor = colors.accent.copy(alpha = 0.35f)
        ),
        contentKey = currentDestination
    ) {
        destinations.forEach { dest ->
            val isSelected = dest == currentDestination
            val tint = if (isSelected) colors.textPrimary else colors.textMuted

            tab(
                key = dest,
                icon = {
                    Icon(
                        imageVector = dest.icon,
                        contentDescription = dest.label,
                        tint = tint,
                        modifier = Modifier.size(22.dp)
                    )
                },
                title = {
                    Text(
                        text = dest.label,
                        fontSize = 10.sp,
                        color = tint,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                },
                onClick = { onNavigate(dest) }
            )
        }
    }
}
