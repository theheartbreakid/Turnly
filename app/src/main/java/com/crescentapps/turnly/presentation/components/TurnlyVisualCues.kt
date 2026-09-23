package com.crescentapps.turnly.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.core.model.OccurrenceStatus
import com.crescentapps.turnly.core.model.RoomRole
import com.crescentapps.turnly.core.model.ScheduleType
import com.crescentapps.turnly.core.model.SyncState
import com.crescentapps.turnly.presentation.theme.TurnlyThemeTokens

/**
 * TURNLY VISUAL CUE SYSTEM
 * Expressive status badges, role indicators, schedule type glyphs, and sync states
 * that allow instant comprehension through shape, icon, and tint without reading English text.
 */

@Composable
fun TurnlyStatusBadge(
    status: OccurrenceStatus,
    modifier: Modifier = Modifier,
    showLabel: Boolean = true
) {
    val tokens = TurnlyThemeTokens.colors
    val (icon, tint, containerColor, label) = when (status) {
        OccurrenceStatus.COMPLETED -> StatusVisualInfo(
            icon = Icons.Default.CheckCircle,
            tint = tokens.statusCompleted,
            container = tokens.statusCompletedContainer,
            label = "Completed"
        )
        OccurrenceStatus.SKIPPED -> StatusVisualInfo(
            icon = Icons.Default.FastForward,
            tint = tokens.statusSkipped,
            container = tokens.statusSkippedContainer,
            label = "Skipped"
        )
        OccurrenceStatus.MISSED -> StatusVisualInfo(
            icon = Icons.Default.Warning,
            tint = tokens.statusMissed,
            container = tokens.statusMissedContainer,
            label = "Missed"
        )
        OccurrenceStatus.PENDING -> StatusVisualInfo(
            icon = Icons.Default.Schedule,
            tint = tokens.statusPending,
            container = tokens.statusPendingContainer,
            label = "Pending"
        )
        OccurrenceStatus.CANCELLED -> StatusVisualInfo(
            icon = Icons.Default.Cancel,
            tint = tokens.statusSkipped,
            container = tokens.statusSkippedContainer,
            label = "Cancelled"
        )
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(containerColor)
            .border(width = 1.dp, color = tint.copy(alpha = 0.35f), shape = RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(13.dp),
            tint = tint
        )
        if (showLabel) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.3.sp,
                color = tint
            )
        }
    }
}

private data class StatusVisualInfo(
    val icon: ImageVector,
    val tint: Color,
    val container: Color,
    val label: String
)

@Composable
fun TurnlyRoomStatusBadge(
    syncState: SyncState,
    modifier: Modifier = Modifier
) {
    val tokens = TurnlyThemeTokens.colors
    val infiniteTransition = rememberInfiniteTransition(label = "SyncRotation")
    val rotation by if (syncState == SyncState.SYNCING) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "rotateSync"
        )
    } else {
        remember { mutableFloatStateOf(0f) }
    }

    val (icon, tint, container, text) = when (syncState) {
        SyncState.SYNCED -> Quadruple(Icons.Default.CloudDone, tokens.statusCompleted, tokens.statusCompletedContainer, "Synced")
        SyncState.SYNCING -> Quadruple(Icons.Default.Sync, tokens.accent, tokens.accentContainer.copy(alpha = 0.3f), "Syncing")
        SyncState.OFFLINE -> Quadruple(Icons.Default.CloudOff, tokens.statusPending, tokens.statusPendingContainer, "Offline")
        SyncState.CONFLICT -> Quadruple(Icons.Default.SyncProblem, tokens.statusMissed, tokens.statusMissedContainer, "Conflict")
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(container)
            .border(0.8.dp, tint.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = tint,
            modifier = Modifier
                .size(14.dp)
                .then(if (syncState == SyncState.SYNCING) Modifier.rotate(rotation) else Modifier)
        )
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = tint
        )
    }
}

@Composable
fun TurnlyRoleBadge(
    role: RoomRole,
    modifier: Modifier = Modifier
) {
    val tokens = TurnlyThemeTokens.colors
    val isOwner = role == RoomRole.OWNER
    val tint = if (isOwner) tokens.statusPending else tokens.textSecondary
    val container = if (isOwner) tokens.statusPendingContainer else tokens.glassCard

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(container)
            .border(0.5.dp, tint.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = if (isOwner) Icons.Default.Stars else Icons.Default.Person,
            contentDescription = role.name,
            tint = tint,
            modifier = Modifier.size(12.dp)
        )
        Text(
            text = if (isOwner) "Owner" else "Member",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = tint
        )
    }
}

@Composable
fun TurnlyScheduleTypeBadge(
    type: ScheduleType,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    val (icon, color) = when (type) {
        ScheduleType.MONEY -> Icons.Default.Payments to Color(0xFF10B981)
        ScheduleType.TASK -> Icons.Default.Assignment to Color(0xFF3B82F6)
        ScheduleType.RESPONSIBILITY -> Icons.Default.CleaningServices to Color(0xFF8B5CF6)
        ScheduleType.GENERAL -> Icons.Default.Repeat to Color(0xFFF59E0B)
        ScheduleType.CUSTOM -> Icons.Default.Category to Color(0xFFEC4899)
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.2f))
            .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = type.name,
            tint = color,
            modifier = Modifier.size(size * 0.58f)
        )
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
