package com.crescentapps.turnly.presentation.gaussianblur.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.core.model.OccurrenceStatus
import com.crescentapps.turnly.core.model.ResolvedTurn
import com.crescentapps.turnly.core.model.ScheduleType
import com.crescentapps.turnly.core.util.CurrencyUtils
import com.crescentapps.turnly.core.util.DateUtils
import com.crescentapps.turnly.presentation.components.EmptyTurnState
import com.crescentapps.turnly.presentation.components.Icon
import com.crescentapps.turnly.presentation.components.ParticipantAvatar
import com.crescentapps.turnly.presentation.components.Text
import com.crescentapps.turnly.presentation.components.TurnCard
import com.crescentapps.turnly.presentation.gaussianblur.components.*
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianBlurColors
import com.crescentapps.turnly.presentation.screens.home.HomeFilter
import com.crescentapps.turnly.presentation.screens.home.HomeViewModel

@Composable
fun GaussianHomeScreen(
    viewModel: HomeViewModel,
    onCreateSchedule: () -> Unit,
    onScheduleClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalGaussianBlurColors.current

    var turnForAmountDialog by remember { mutableStateOf<ResolvedTurn?>(null) }
    var enteredAmount by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Turnly",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.textPrimary
                )
                Text(
                    text = DateUtils.formatDisplay(DateUtils.todayString()),
                    fontSize = 14.sp,
                    color = colors.textMuted
                )
            }

            if (uiState.todayTurns.isNotEmpty()) {
                GaussianButton(
                    onClick = onCreateSchedule,
                    accentTint = colors.accent,
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Schedule",
                        modifier = Modifier.size(18.dp),
                        tint = colors.textOnAccent
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New", fontWeight = FontWeight.Bold, color = colors.textOnAccent)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (!uiState.isLoading && uiState.todayTurns.isEmpty()) {
            EmptyTurnState(
                title = "All clear for today!",
                description = "No turns are due right now. You can create a schedule to take turns for expenses, chores, pet care, or team duties.",
                actionText = "Create a Schedule",
                onAction = onCreateSchedule
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // Hero Banner if there is a pending turn today
                val firstPending = uiState.todayTurns.firstOrNull { it.status == OccurrenceStatus.PENDING }
                if (firstPending != null) {
                    item(key = "hero_who_is_next") {
                        GaussianCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onScheduleClick(firstPending.schedule.id) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "WHO GOES NEXT?",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        color = colors.accent
                                    )
                                    Text(
                                        text = firstPending.schedule.name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = colors.textSecondary
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    ParticipantAvatar(
                                        participant = firstPending.participant,
                                        size = 56.dp
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "${firstPending.participant.name}'s turn",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Black,
                                            color = colors.textPrimary
                                        )
                                        if (firstPending.schedule.type == ScheduleType.MONEY && firstPending.schedule.defaultAmount != null) {
                                            Text(
                                                text = CurrencyUtils.formatAmount(
                                                    firstPending.schedule.defaultAmount,
                                                    firstPending.schedule.currencyCode
                                                ),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.accent
                                            )
                                        } else {
                                            Text(
                                                text = "Scheduled for today",
                                                fontSize = 13.sp,
                                                color = colors.textMuted
                                            )
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    GaussianButton(
                                        onClick = {
                                            if (firstPending.schedule.type == ScheduleType.MONEY) {
                                                turnForAmountDialog = firstPending
                                                enteredAmount = firstPending.schedule.defaultAmount?.toString() ?: ""
                                            } else {
                                                viewModel.markTurnComplete(firstPending)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        accentTint = colors.accent
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = colors.textOnAccent
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (firstPending.schedule.type == ScheduleType.MONEY) "Record & Done" else "Mark as Done",
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textOnAccent
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Section Summary & Filter Chips
                item(key = "header_filters") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "TODAY'S TURNS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp,
                                color = colors.accent
                            )
                            Text(
                                text = "${uiState.totalPending} left · ${uiState.totalCompleted} done",
                                fontSize = 12.sp,
                                color = colors.textMuted
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val filters = listOf(
                                HomeFilter.ALL to "All Turns",
                                HomeFilter.LOCAL to "My Schedules",
                                HomeFilter.SHARED to "Shared with Others"
                            )
                            filters.forEach { (filter, label) ->
                                val isSelected = uiState.currentFilter == filter
                                GaussianChip(
                                    selected = isSelected,
                                    label = label,
                                    onClick = { viewModel.setFilter(filter) }
                                )
                            }
                        }
                    }
                }

                // Turns list
                items(uiState.filteredTurns, key = { it.schedule.id }) { turn ->
                    TurnCard(
                        resolvedTurn = turn,
                        onMarkDone = { sId, _ ->
                            if (turn.schedule.type == ScheduleType.MONEY) {
                                turnForAmountDialog = turn
                                enteredAmount = turn.schedule.defaultAmount?.toString() ?: ""
                            } else {
                                viewModel.markTurnComplete(turn)
                            }
                        },
                        onSkip = { sId -> },
                        onClick = { onScheduleClick(turn.schedule.id) }
                    )
                }
            }
        }
    }

    // Expense Confirmation Dialog
    turnForAmountDialog?.let { turn ->
        GaussianDialog(
            onDismissRequest = { turnForAmountDialog = null },
            title = "Record Expense",
            message = "Enter the amount paid for \"${turn.schedule.name}\":",
            positiveText = "Save & Mark Done",
            negativeText = "Cancel",
            onPositive = {
                val amount = enteredAmount.toDoubleOrNull()
                viewModel.markTurnComplete(turn, amount)
                turnForAmountDialog = null
            }
        ) {
            GaussianTextField(
                value = enteredAmount,
                onValueChange = { enteredAmount = it },
                label = "Amount (${turn.schedule.currencyCode})",
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
