package com.crescentapps.turnly.presentation.gaussianblur.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.crescentapps.turnly.presentation.components.ConfirmDialog
import com.crescentapps.turnly.presentation.components.Icon
import com.crescentapps.turnly.presentation.components.ParticipantAvatar
import com.crescentapps.turnly.presentation.components.Text
import com.crescentapps.turnly.presentation.gaussianblur.components.GaussianButton
import com.crescentapps.turnly.presentation.gaussianblur.components.GaussianCard
import com.crescentapps.turnly.presentation.gaussianblur.components.GaussianIconButton
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianBlurColors
import com.crescentapps.turnly.presentation.screens.schedule.ScheduleDetailViewModel

@Composable
fun GaussianScheduleDetailScreen(
    viewModel: ScheduleDetailViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalGaussianBlurColors.current

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showArchiveConfirm by remember { mutableStateOf(false) }

    val schedule = uiState.schedule ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GaussianIconButton(onClick = onNavigateBack, size = 40.dp) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
            }
            Text(
                text = schedule.name,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
            GaussianIconButton(
                onClick = { viewModel.togglePauseSchedule() },
                size = 40.dp
            ) {
                Icon(
                    imageVector = if (schedule.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (schedule.isPaused) "Resume" else "Pause",
                    tint = if (schedule.isPaused) colors.statusCompleted else colors.accent
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                GaussianCard(
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
                                    color = colors.accent
                                )
                                Text(
                                    text = schedule.type.displayName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                            }

                            if (schedule.isPaused) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(colors.statusPending.copy(alpha = 0.2f))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "PAUSED",
                                        color = colors.statusPending,
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
                                fontSize = 14.sp,
                                color = colors.textMuted
                            )
                        }

                        if (schedule.type == ScheduleType.MONEY && schedule.defaultAmount != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Amount: ${CurrencyUtils.formatAmount(schedule.defaultAmount, schedule.currencyCode)} / turn",
                                fontWeight = FontWeight.Bold,
                                color = colors.accent,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "PARTICIPANTS (${uiState.participants.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accent
                )
            }

            items(uiState.participants) { p ->
                GaussianCard(
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
                            Text(text = p.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary)
                        }

                        val turnsDone = uiState.turnsPerParticipant[p.name] ?: 0
                        Text(
                            text = "$turnsDone turns",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textMuted
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GaussianButton(
                        onClick = { showArchiveConfirm = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (schedule.isArchived) "Unarchive" else "Archive", color = colors.textPrimary, fontWeight = FontWeight.SemiBold)
                    }

                    GaussianButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Delete", color = colors.statusMissed, fontWeight = FontWeight.Bold)
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
            message = "This will permanently remove '${schedule.name}' and all its rotation records.",
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
