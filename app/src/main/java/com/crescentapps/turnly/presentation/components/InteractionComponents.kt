package com.crescentapps.turnly.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.core.model.OccurrenceStatus
import com.crescentapps.turnly.core.model.Participant
import com.crescentapps.turnly.presentation.theme.TurnlyThemeTokens
import com.crescentapps.turnly.core.model.ResolvedTurn
import com.crescentapps.turnly.core.model.ScheduleType
import com.crescentapps.turnly.core.util.CurrencyUtils
import com.crescentapps.turnly.core.util.DateUtils
import com.crescentapps.turnly.presentation.catalog.utils.LocalBackdrop
import com.crescentapps.turnly.presentation.components.liquid.LiquidButton
import com.crescentapps.turnly.presentation.components.liquid.LiquidCard
import com.crescentapps.turnly.presentation.components.liquid.LiquidChip
import com.crescentapps.turnly.presentation.components.liquid.LiquidDialog
import com.crescentapps.turnly.presentation.theme.LiquidColors
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

/**
 * TURNLY TURN CARD
 * Built with DhikrCounter LiquidCard architecture & adaptive typography.
 */
@Composable
fun TurnCard(
    resolvedTurn: ResolvedTurn,
    onMarkDone: (Long, Double?) -> Unit,
    onSkip: (Long) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val schedule = resolvedTurn.schedule
    val currentPerson = resolvedTurn.participant
    val isPending = resolvedTurn.status == OccurrenceStatus.PENDING
    val isDone = resolvedTurn.status == OccurrenceStatus.COMPLETED
    val isSkipped = resolvedTurn.status == OccurrenceStatus.SKIPPED
    val occurrence = null as Any?
    
    val backdrop = LocalBackdrop.current ?: rememberLayerBackdrop()
    val adaptiveColor = LocalPrismalAdaptiveColor.current

    LiquidCard(
        backdrop = backdrop,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Row: Schedule Type & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    TurnlyScheduleTypeBadge(type = schedule.type, size = 26.dp)

                    Text(
                        text = schedule.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = adaptiveColor,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // High-contrast, language-free visual status badge
                TurnlyStatusBadge(status = resolvedTurn.status)
            }

            // Participant Avatar + Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                ParticipantAvatar(
                    participant = currentPerson,
                    size = 52.dp
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Turn Today",
                        fontSize = 12.sp,
                        color = adaptiveColor.copy(alpha = 0.6f),
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = currentPerson?.name ?: "Unassigned",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = adaptiveColor
                    )
                    if (schedule.type == ScheduleType.MONEY && schedule.defaultAmount != null) {
                        Text(
                            text = CurrencyUtils.formatAmount(schedule.defaultAmount, schedule.currencyCode),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Action Buttons Row (Mark Done / Skip)
            if (isPending) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val tokens = TurnlyThemeTokens.colors
                    LiquidButton(
                        onClick = { onSkip(schedule.id) },
                        backdrop = backdrop,
                        modifier = Modifier.weight(0.4f),
                        surfaceColor = tokens.glassCard
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Skip",
                            modifier = Modifier.size(15.dp),
                            tint = adaptiveColor.copy(alpha = 0.85f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Skip",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = adaptiveColor.copy(alpha = 0.85f)
                        )
                    }

                    LiquidButton(
                        onClick = { onMarkDone(schedule.id, null) },
                        backdrop = backdrop,
                        modifier = Modifier.weight(0.6f),
                        tint = tokens.statusCompleted.copy(alpha = 0.35f),
                        surfaceColor = tokens.statusCompleted.copy(alpha = 0.25f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Done",
                            modifier = Modifier.size(16.dp),
                            tint = tokens.statusCompleted
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Mark Done",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = tokens.statusCompleted
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantAvatar(
    participant: Participant?,
    size: androidx.compose.ui.unit.Dp = 44.dp
) {
    val initial = participant?.name?.take(1)?.uppercase() ?: "?"
    val colorHex = participant?.colorHex ?: "#3B82F6"
    val avatarBg = try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color(0xFF3B82F6)
    }
    val textColor = if (avatarBg.luminance() > 0.45f) Color(0xFF0F172A) else Color.White

    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(avatarBg.copy(alpha = 0.9f))
            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initial,
            fontWeight = FontWeight.Black,
            fontSize = (size.value * 0.42f).sp,
            color = textColor
        )
    }
}

/**
 * EMPTY STATE COMPONENT
 * Using DhikrCounter LiquidCard architecture.
 */
@Composable
fun EmptyTurnState(
    title: String = "No turn schedules yet",
    description: String = "Create your first rotation for family expenses, chores, team duties, or pet care.",
    actionText: String = "+ New Turn Schedule",
    onAction: () -> Unit
) {
    val backdrop = LocalBackdrop.current ?: rememberLayerBackdrop()
    val adaptiveColor = LocalPrismalAdaptiveColor.current

    LiquidCard(
        backdrop = backdrop,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = adaptiveColor
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = adaptiveColor.copy(alpha = 0.7f),
                modifier = Modifier.padding(horizontal = 12.dp),
                textAlign = TextAlign.Center
            )

            LiquidButton(
                onClick = onAction,
                backdrop = backdrop,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                surfaceColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
) {
                Text(
                    text = actionText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = adaptiveColor
                )
            }
        }
    }
}

/**
 * CONFIRMATION DIALOG
 * Uses DhikrCounter LiquidDialog.
 */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val backdrop = LocalBackdrop.current ?: rememberLayerBackdrop()

    LiquidDialog(
        onDismissRequest = onDismiss,
        backdrop = backdrop,
        title = title,
        message = message,
        positiveText = confirmText,
        negativeText = dismissText,
        onPositive = onConfirm,
        accentColor = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    )
}
