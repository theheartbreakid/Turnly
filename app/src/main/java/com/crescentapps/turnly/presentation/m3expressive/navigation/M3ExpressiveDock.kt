package com.crescentapps.turnly.presentation.m3expressive.navigation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class M3NavDestination(val route: String, val label: String, val icon: ImageVector) {
    HOME("home", "Today", Icons.Default.Home),
    CALENDAR("calendar", "Calendar", Icons.Default.DateRange),
    SCHEDULES("schedules", "Schedules", Icons.AutoMirrored.Filled.List),
    ROOMS("rooms", "Rooms", Icons.Default.Group),
    HISTORY("history", "History", Icons.Default.History),
    SETTINGS("settings", "Settings", Icons.Default.Settings)
}

/**
 * Custom Shape-Shifting Floating Bottom Dock for Material 3 Expressive.
 * Displays as one coherent floating group control that morphs smoothly
 * with spring physics as the selected tab changes.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun M3ExpressiveDock(
    currentRoute: String?,
    onNavigate: (M3NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .shadow(
                elevation = 12.dp,
                shape = CircleShape,
                clip = false
            ),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            M3NavDestination.entries.forEach { destination ->
                val isSelected = currentRoute == destination.route
                
                M3DockItem(
                    destination = destination,
                    isSelected = isSelected,
                    onClick = { onNavigate(destination) }
                )
            }
        }
    }
}

@Composable
private fun M3DockItem(
    destination: M3NavDestination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0f)
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dock_item_color"
    )

    val contentColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dock_item_content_color"
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (isSelected) 24.dp else 16.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dock_item_corner"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 4.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dock_item_elevation"
    )

    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .shadow(elevation = elevation, shape = RoundedCornerShape(cornerRadius))
            .clip(RoundedCornerShape(cornerRadius))
            .background(containerColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Tab,
                onClick = onClick
            )
            .semantics {
                role = Role.Tab
                selected = isSelected
                contentDescription = destination.label
            }
            .padding(horizontal = if (isSelected) 12.dp else 8.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = destination.icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )

            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = destination.label,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1,
                    fontSize = 12.sp
                )
            }
        }
    }
}
