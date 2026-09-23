package com.crescentapps.turnly.presentation.m3.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.core.model.OccurrenceStatus
import com.crescentapps.turnly.core.model.ResolvedTurn
import com.crescentapps.turnly.core.model.ScheduleType
import com.crescentapps.turnly.core.util.CurrencyUtils
import com.crescentapps.turnly.core.util.DateUtils
import com.crescentapps.turnly.presentation.m3.components.M3EmptyTurnState
import com.crescentapps.turnly.presentation.m3.components.M3LinearWavyProgressIndicator
import com.crescentapps.turnly.presentation.m3.components.M3ParticipantAvatar
import com.crescentapps.turnly.presentation.m3.components.M3TurnCard
import com.crescentapps.turnly.presentation.screens.home.HomeFilter
import com.crescentapps.turnly.presentation.screens.home.HomeViewModel

/**
 * Pixel-grade Material 3 Home Screen for Turnly.
 * Built with standard M3 Expressive scaffolding, hero turn banner, and haptic response.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3HomeScreen(
    viewModel: HomeViewModel,
    onCreateSchedule: () -> Unit,
    onScheduleClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val uiState by viewModel.uiState.collectAsState()
    var turnForAmountDialog by remember { mutableStateOf<ResolvedTurn?>(null) }
    var enteredAmount by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Today's Turns",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = DateUtils.formatDisplay(DateUtils.todayString()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onCreateSchedule()
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("New Rotation", style = MaterialTheme.typography.labelLarge)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Expressive Wavy Progress when loading
            if (uiState.isLoading) {
                M3LinearWavyProgressIndicator(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (!uiState.isLoading && uiState.todayTurns.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    M3EmptyTurnState(
                        title = "All clear for today!",
                        description = "No turns are due right now. Set up a rotation schedule for shared expenses, chores, pet care, or team responsibilities.",
                        actionText = "Create Rotation",
                        onAction = onCreateSchedule
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Hero Card: Priority Next Turn Banner
                    val firstPending = uiState.todayTurns.firstOrNull { it.status == OccurrenceStatus.PENDING }
                    if (firstPending != null) {
                        item(key = "m3_hero_pending") {
                            Surface(
                                shape = RoundedCornerShape(22.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                tonalElevation = 2.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(22.dp))
                                    .clickable { onScheduleClick(firstPending.schedule.id) }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(18.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "NEXT UP TODAY",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            letterSpacing = 1.2.sp,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = firstPending.schedule.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                                    ) {
                                        M3ParticipantAvatar(participant = firstPending.participant, size = 52.dp)

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${firstPending.participant.name}'s turn",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                            if (firstPending.schedule.type == ScheduleType.MONEY && firstPending.schedule.defaultAmount != null) {
                                                Text(
                                                    text = CurrencyUtils.formatAmount(
                                                        firstPending.schedule.defaultAmount,
                                                        firstPending.schedule.currencyCode
                                                    ),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            } else {
                                                Text(
                                                    text = "Scheduled for today",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                                )
                                            }
                                        }
                                    }

                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            if (firstPending.schedule.type == ScheduleType.MONEY) {
                                                turnForAmountDialog = firstPending
                                                enteredAmount = firstPending.schedule.defaultAmount?.toString() ?: ""
                                            } else {
                                                viewModel.markTurnComplete(firstPending)
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary,
                                            contentColor = MaterialTheme.colorScheme.onPrimary
                                        )
                                    ) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (firstPending.schedule.type == ScheduleType.MONEY) "Record & Done" else "Mark as Completed",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Section Heading & Filter Chips
                    item(key = "m3_filter_chips") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "All Rotations",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${uiState.totalPending} pending · ${uiState.totalCompleted} done",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val filters = listOf(
                                    HomeFilter.ALL to "All (${uiState.todayTurns.size})",
                                    HomeFilter.LOCAL to "My Rotations",
                                    HomeFilter.SHARED to "Shared Rooms"
                                )
                                filters.forEach { (filter, label) ->
                                    val isSelected = uiState.currentFilter == filter
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            viewModel.setFilter(filter)
                                        },
                                        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Turn Cards List
                    items(uiState.filteredTurns, key = { it.schedule.id }) { turn ->
                        M3TurnCard(
                            resolvedTurn = turn,
                            onMarkDone = { _, _ ->
                                if (turn.schedule.type == ScheduleType.MONEY) {
                                    turnForAmountDialog = turn
                                    enteredAmount = turn.schedule.defaultAmount?.toString() ?: ""
                                } else {
                                    viewModel.markTurnComplete(turn)
                                }
                            },
                            onSkip = { /* Handled in calendar/detail */ },
                            onClick = { onScheduleClick(turn.schedule.id) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(64.dp)) }
                }
            }
        }
    }

    // Material 3 Dialog for Recording Money Amount
    turnForAmountDialog?.let { turn ->
        AlertDialog(
            onDismissRequest = { turnForAmountDialog = null },
            title = { Text("Record Expense", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        "Enter the amount paid for \"${turn.schedule.name}\":",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = enteredAmount,
                        onValueChange = { enteredAmount = it },
                        label = { Text("Amount (${turn.schedule.currencyCode})") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = enteredAmount.toDoubleOrNull()
                        viewModel.markTurnComplete(turn, amount)
                        turnForAmountDialog = null
                    },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save & Complete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { turnForAmountDialog = null },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
