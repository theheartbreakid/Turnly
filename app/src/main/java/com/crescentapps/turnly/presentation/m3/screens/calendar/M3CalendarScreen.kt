package com.crescentapps.turnly.presentation.m3.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.core.model.OccurrenceStatus
import com.crescentapps.turnly.core.model.Participant
import com.crescentapps.turnly.core.model.ResolvedTurn
import com.crescentapps.turnly.core.model.ScheduleType
import com.crescentapps.turnly.core.util.CurrencyUtils
import com.crescentapps.turnly.core.util.DateUtils
import com.crescentapps.turnly.presentation.m3.components.M3ParticipantAvatar
import com.crescentapps.turnly.presentation.m3.components.M3ScheduleTypeBadge
import com.crescentapps.turnly.presentation.m3.components.M3StatusBadge
import com.crescentapps.turnly.presentation.screens.calendar.CalendarUiState
import com.crescentapps.turnly.presentation.screens.calendar.CalendarViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * Pure Material 3 Calendar Screen.
 * Uses standard M3 components: Scaffold, TopAppBar, ElevatedCard, OutlinedCard, Button, Date selection.
 * Adapts to multi-pane on wide screens (>= 600dp).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3CalendarScreen(
    viewModel: CalendarViewModel,
    onScheduleClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var overrideTurnTarget by remember { mutableStateOf<ResolvedTurn?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Calendar", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            val isExpanded = maxWidth >= 600.dp

            if (isExpanded) {
                // Wide / Tablet Multi-pane layout
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1.2f)) {
                        M3CalendarHeader(viewModel = viewModel, uiState = uiState)
                        Spacer(modifier = Modifier.height(12.dp))
                        M3CalendarGrid(viewModel = viewModel, uiState = uiState)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        M3CalendarDayDetailPane(
                            uiState = uiState,
                            onMarkDone = { turn -> viewModel.markTurnComplete(turn) },
                            onOverride = { turn -> overrideTurnTarget = turn },
                            onSkip = { turn -> viewModel.skipDate(turn.schedule.id, turn.date) },
                            onScheduleClick = onScheduleClick
                        )
                    }
                }
            } else {
                // Phone single-column layout
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 88.dp)
                ) {
                    item(key = "m3_cal_header") {
                        M3CalendarHeader(viewModel = viewModel, uiState = uiState)
                    }
                    item(key = "m3_cal_grid") {
                        M3CalendarGrid(viewModel = viewModel, uiState = uiState)
                    }
                    item(key = "m3_cal_details") {
                        M3CalendarDayDetailPane(
                            uiState = uiState,
                            onMarkDone = { turn -> viewModel.markTurnComplete(turn) },
                            onOverride = { turn -> overrideTurnTarget = turn },
                            onSkip = { turn -> viewModel.skipDate(turn.schedule.id, turn.date) },
                            onScheduleClick = onScheduleClick
                        )
                    }
                }
            }
        }
    }

    // Override Rotation Dialog
    overrideTurnTarget?.let { turn ->
        M3OverrideTurnDialog(
            turn = turn,
            availableParticipants = uiState.availableParticipants,
            onDismiss = { overrideTurnTarget = null },
            onConfirm = { newPersonId ->
                viewModel.setManualOverride(turn.schedule.id, turn.date, newPersonId)
                overrideTurnTarget = null
            }
        )
    }
}

@Composable
private fun M3CalendarHeader(viewModel: CalendarViewModel, uiState: CalendarUiState) {
    val haptic = LocalHapticFeedback.current
    val monthTitle = "${uiState.currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${uiState.currentMonth.year}"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = monthTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Tap any date to inspect rotation turns",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.prevMonth()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
            }
            IconButton(onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                viewModel.nextMonth()
            }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month")
            }
        }
    }
}

@Composable
private fun M3CalendarGrid(viewModel: CalendarViewModel, uiState: CalendarUiState) {
    val haptic = LocalHapticFeedback.current

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Weekday labels
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val weekdays = listOf("M", "T", "W", "T", "F", "S", "S")
                weekdays.forEach { day ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            val firstOfMonth = uiState.currentMonth.atDay(1)
            val firstDayOffset = firstOfMonth.dayOfWeek.value - 1
            val daysInMonth = uiState.currentMonth.lengthOfMonth()
            val totalCells = ((firstDayOffset + daysInMonth + 6) / 7) * 7

            for (week in 0 until (totalCells / 7)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (dayCol in 0..6) {
                        val cellIndex = week * 7 + dayCol
                        val dayNumber = cellIndex - firstDayOffset + 1

                        if (dayNumber in 1..daysInMonth) {
                            val cellDate = uiState.currentMonth.atDay(dayNumber)
                            val isSelected = cellDate == uiState.selectedDate
                            val isToday = cellDate == LocalDate.now()
                            val turns = uiState.dayTurnMap[cellDate].orEmpty()

                            val cellBg = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.primaryContainer
                                else -> Color.Transparent
                            }
                            val dayTextColor = when {
                                isSelected -> MaterialTheme.colorScheme.onPrimary
                                isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                                else -> MaterialTheme.colorScheme.onSurface
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.95f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(cellBg)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        viewModel.selectDate(cellDate)
                                    }
                                    .padding(2.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    Text(
                                        text = dayNumber.toString(),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = dayTextColor
                                    )

                                    if (turns.isNotEmpty()) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            modifier = Modifier.padding(bottom = 2.dp)
                                        ) {
                                            turns.take(3).forEach { indicator ->
                                                val dotColor = try {
                                                    Color(android.graphics.Color.parseColor(indicator.participantColorHex))
                                                } catch (e: Exception) {
                                                    MaterialTheme.colorScheme.primary
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else dotColor)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(5.dp))
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f).aspectRatio(0.9f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun M3CalendarDayDetailPane(
    uiState: CalendarUiState,
    onMarkDone: (ResolvedTurn) -> Unit,
    onOverride: (ResolvedTurn) -> Unit,
    onSkip: (ResolvedTurn) -> Unit,
    onScheduleClick: (Long) -> Unit
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = DateUtils.formatDisplay(DateUtils.format(uiState.selectedDate)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.selectedDayTurns.isEmpty()) {
                Text(
                    text = "No rotations active on this day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    uiState.selectedDayTurns.forEach { turn ->
                        OutlinedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onScheduleClick(turn.schedule.id) },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
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
                                        M3ScheduleTypeBadge(type = turn.schedule.type, size = 22.dp)
                                        Text(
                                            text = turn.schedule.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                    }
                                    M3StatusBadge(status = turn.status)
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    M3ParticipantAvatar(participant = turn.participant, size = 36.dp)
                                    Column {
                                        Text(
                                            text = turn.participant.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        if (turn.schedule.type == ScheduleType.MONEY && turn.expectedAmount != null) {
                                            Text(
                                                text = CurrencyUtils.formatAmount(turn.expectedAmount, turn.schedule.currencyCode),
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }

                                // Actions Row
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    if (turn.status == OccurrenceStatus.PENDING) {
                                        Button(
                                            onClick = { onMarkDone(turn) },
                                            modifier = Modifier.weight(1f),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("Mark Done", style = MaterialTheme.typography.labelMedium)
                                        }
                                    }
                                    OutlinedButton(
                                        onClick = { onOverride(turn) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Override", style = MaterialTheme.typography.labelMedium)
                                    }
                                    OutlinedButton(
                                        onClick = { onSkip(turn) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text("Skip", style = MaterialTheme.typography.labelMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun M3OverrideTurnDialog(
    turn: ResolvedTurn,
    availableParticipants: List<Participant>,
    onDismiss: () -> Unit,
    onConfirm: (Long) -> Unit
) {
    var selectedParticipantId by remember { mutableStateOf<Long?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Override Turn", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Assign someone else to take the turn for \"${turn.schedule.name}\" on ${DateUtils.formatDisplay(turn.date)}:",
                    style = MaterialTheme.typography.bodyMedium
                )
                LazyColumn(
                    modifier = Modifier.heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(availableParticipants) { participant ->
                        val isSelected = selectedParticipantId == participant.id
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedParticipantId = participant.id }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedParticipantId = participant.id }
                            )
                            M3ParticipantAvatar(participant = participant, size = 32.dp)
                            Text(
                                text = participant.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { selectedParticipantId?.let { onConfirm(it) } },
                enabled = selectedParticipantId != null
            ) {
                Text("Confirm Override")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
