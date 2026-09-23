package com.crescentapps.turnly.presentation.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.core.model.ScheduleType
import com.crescentapps.turnly.core.util.CurrencyUtils
import com.crescentapps.turnly.core.util.DateUtils
import com.crescentapps.turnly.presentation.catalog.utils.LocalBackdrop
import com.crescentapps.turnly.presentation.components.ConfirmDialog
import com.crescentapps.turnly.presentation.components.LocalPrismalAdaptiveColor
import com.crescentapps.turnly.presentation.components.ParticipantAvatar
import com.crescentapps.turnly.presentation.components.liquid.LiquidButton
import com.crescentapps.turnly.presentation.components.liquid.LiquidCard
import com.crescentapps.turnly.presentation.components.liquid.LiquidIconButton
import com.crescentapps.turnly.presentation.screens.history.StatColumn
import com.crescentapps.turnly.presentation.theme.LiquidColors
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

@Composable
fun ScheduleDetailScreen(
    viewModel: ScheduleDetailViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val backdrop = LocalBackdrop.current ?: rememberLayerBackdrop()
    val adaptiveColor = LocalPrismalAdaptiveColor.current

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showArchiveConfirm by remember { mutableStateOf(false) }

    val schedule = uiState.schedule

    if (schedule == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LiquidIconButton(onClick = onNavigateBack, backdrop = backdrop, iconSize = 40.dp) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = adaptiveColor)
            }
            Text(
                text = schedule.name,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = adaptiveColor
            )
            LiquidIconButton(
                onClick = { viewModel.togglePauseSchedule() },
                backdrop = backdrop,
                iconSize = 40.dp,
                surfaceColor = if (schedule.isPaused) LiquidColors.StatusCompleted.copy(alpha = 0.2f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            ) {
                Icon(
                    imageVector = if (schedule.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (schedule.isPaused) "Resume" else "Pause",
                    tint = if (schedule.isPaused) LiquidColors.StatusCompleted else MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 110.dp)
        ) {
            // Overview Card
            item {
                LiquidCard(
                    backdrop = backdrop,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "SCHEDULE TYPE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = schedule.type.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = adaptiveColor
                                )
                            }

                            if (schedule.isPaused) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(LiquidColors.StatusPending.copy(alpha = 0.2f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "PAUSED",
                                        color = LiquidColors.StatusPending,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        if (schedule.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = schedule.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = adaptiveColor.copy(alpha = 0.7f)
                            )
                        }

                        if (schedule.type == ScheduleType.MONEY && schedule.defaultAmount != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Amount: ${CurrencyUtils.formatAmount(schedule.defaultAmount, schedule.currencyCode)} / turn",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Statistics Card
            item {
                LiquidCard(
                    backdrop = backdrop,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = "STATISTICS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            StatColumn(label = "Total Turns", value = "${uiState.totalTurnsCount}", color = adaptiveColor)
                            StatColumn(label = "Completed", value = "${uiState.completedCount}", color = LiquidColors.StatusCompleted)
                            if (schedule.type == ScheduleType.MONEY) {
                                StatColumn(label = "Recorded", value = CurrencyUtils.formatAmount(uiState.totalRecordedAmount, schedule.currencyCode), color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }

            // Participants List
            item {
                Text(
                    text = "PARTICIPANTS ()",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(uiState.participants) { p ->
                LiquidCard(
                    backdrop = backdrop,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ParticipantAvatar(participant = p, size = 36.dp)
                            Text(text = p.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = adaptiveColor)
                        }

                        val turnsDone = uiState.turnsPerParticipant[p.name] ?: 0
                        Text(
                            text = " turns",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = adaptiveColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            // Upcoming Turns Preview
            item {
                Text(
                    text = "UPCOMING TURNS (NEXT 14)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(uiState.upcomingTurns) { (p, date) ->
                LiquidCard(
                    backdrop = backdrop,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ParticipantAvatar(participant = p, size = 30.dp)
                            Text(text = p.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = adaptiveColor)
                        }
                        Text(
                            text = DateUtils.formatDisplay(DateUtils.format(date)),
                            fontSize = 13.sp,
                            color = adaptiveColor.copy(alpha = 0.65f)
                        )
                    }
                }
            }

            // Management Actions (Archive / Delete)
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    LiquidButton(
                        onClick = { showArchiveConfirm = true },
                        backdrop = backdrop,
                        modifier = Modifier.weight(1f),
                        surfaceColor = Color.White.copy(alpha = 0.08f)
                    ) {
                        Text(if (schedule.isArchived) "Unarchive" else "Archive", color = adaptiveColor, fontWeight = FontWeight.SemiBold)
                    }

                    LiquidButton(
                        onClick = { showDeleteConfirm = true },
                        backdrop = backdrop,
                        modifier = Modifier.weight(1f),
                        surfaceColor = Color.Red.copy(alpha = 0.15f),
                        tint = Color.Red.copy(alpha = 0.35f)
                    ) {
                        Text("Delete", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showArchiveConfirm) {
        ConfirmDialog(
            title = "Archive Schedule?",
            message = "Archiving will hide this schedule from active rotations while preserving all completed historical data.",
            confirmText = "Archive",
            onConfirm = {
                viewModel.archiveSchedule()
                onNavigateBack()
            },
            onDismiss = { showArchiveConfirm = false }
        )
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Permanently Delete Schedule?",
            message = "This will permanently remove '' and all its rotation records. If you want to keep history, choose Archive instead.",
            confirmText = "Delete Permanently",
            isDestructive = true,
            onConfirm = {
                viewModel.deletePermanently()
                onNavigateBack()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
