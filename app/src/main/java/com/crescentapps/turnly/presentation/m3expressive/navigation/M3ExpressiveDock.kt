package com.crescentapps.turnly.presentation.m3expressive.navigation

import androidx.compose.animation.animateContentSize
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
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
                    .animateContentSize(animationSpec = MaterialTheme.motionScheme.defaultSpatialSpec()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
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
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun M3DockItem(
    destination: M3NavDestination,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0f)
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "dock_item_color"
    )

    val contentColor by androidx.compose.animation.animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        animationSpec = MaterialTheme.motionScheme.defaultEffectsSpec(),
        label = "dock_item_content_color"
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (isSelected) 24.dp else 16.dp,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "dock_item_corner"
    )

    val elevation by animateDpAsState(
        targetValue = if (isSelected) 4.dp else 0.dp,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
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
            .padding(horizontal = if (isSelected) 16.dp else 10.dp, vertical = 10.dp)
            .animateContentSize(animationSpec = MaterialTheme.motionScheme.fastSpatialSpec()),
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
                modifier = Modifier.size(24.dp)
            )

            if (isSelected) {
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = destination.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = contentColor,
                    maxLines = 1,
                    fontSize = 14.sp
                )
            }
        }
    }
}
