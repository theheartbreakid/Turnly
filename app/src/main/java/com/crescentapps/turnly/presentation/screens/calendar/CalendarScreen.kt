package com.crescentapps.turnly.presentation.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.core.model.OccurrenceStatus
import com.crescentapps.turnly.core.model.Participant
import com.crescentapps.turnly.core.model.ResolvedTurn
import com.crescentapps.turnly.core.model.ScheduleType
import com.crescentapps.turnly.core.util.CurrencyUtils
import com.crescentapps.turnly.core.util.DateUtils
import com.crescentapps.turnly.presentation.catalog.utils.LocalBackdrop
import com.crescentapps.turnly.presentation.components.LocalPrismalAdaptiveColor
import com.crescentapps.turnly.presentation.components.ParticipantAvatar
import com.crescentapps.turnly.presentation.components.TurnlyScheduleTypeBadge
import com.crescentapps.turnly.presentation.components.TurnlyStatusBadge
import com.crescentapps.turnly.presentation.components.liquid.*
import com.crescentapps.turnly.presentation.theme.LiquidColors
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onScheduleClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val backdrop = LocalBackdrop.current ?: rememberLayerBackdrop()
    val adaptiveColor = LocalPrismalAdaptiveColor.current
    var overrideTurnTarget by remember { mutableStateOf<ResolvedTurn?>(null) }

    BoxWithConstraints(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        val isExpanded = maxWidth > 640.dp

        if (isExpanded) {
            // Multi-pane Tablet Layout
            Row(
                modifier = Modifier.fillMaxSize().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    CalendarHeader(viewModel = viewModel, uiState = uiState, backdrop = backdrop)
                    Spacer(modifier = Modifier.height(12.dp))
                    CalendarGrid(viewModel = viewModel, uiState = uiState, backdrop = backdrop)
                }
                Column(modifier = Modifier.weight(1f)) {
                    CalendarDayDetailPane(
                        uiState = uiState,
                        backdrop = backdrop,
                        onMarkDone = { turn -> viewModel.markTurnComplete(turn) },
                        onOverride = { turn -> overrideTurnTarget = turn },
                        onSkip = { turn -> viewModel.skipDate(turn.schedule.id, turn.date) },
                        onScheduleClick = onScheduleClick
                    )
                }
            }
        } else {
            // Phone Vertical Layout
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 110.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    CalendarHeader(viewModel = viewModel, uiState = uiState, backdrop = backdrop)
                }
                item {
                    CalendarGrid(viewModel = viewModel, uiState = uiState, backdrop = backdrop)
                }
                item {
                    CalendarDayDetailPane(
                        uiState = uiState,
                        backdrop = backdrop,
                        onMarkDone = { turn -> viewModel.markTurnComplete(turn) },
                        onOverride = { turn -> overrideTurnTarget = turn },
                        onSkip = { turn -> viewModel.skipDate(turn.schedule.id, turn.date) },
                        onScheduleClick = onScheduleClick
                    )
                }
            }
        }
    }

    // Override Participant Picker Liquid Dialog
    overrideTurnTarget?.let { turn ->
        LiquidDialog(
            onDismissRequest = { overrideTurnTarget = null },
            backdrop = backdrop,
            title = "Assign Participant",
            message = "Select who takes the turn for \"${turn.schedule.name}\":",
            positiveText = "Close",
            negativeText = null,
            onPositive = { overrideTurnTarget = null }
        ) {
            LazyColumn(modifier = Modifier.heightIn(max = 240.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(uiState.availableParticipants) { p ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                viewModel.setManualOverride(turn.schedule.id, turn.date, p.id)
                                overrideTurnTarget = null
                            }
                            .background(Color.White.copy(alpha = 0.05f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ParticipantAvatar(participant = p, size = 32.dp)
                        Text(text = p.name, fontWeight = FontWeight.SemiBold, color = adaptiveColor)
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarHeader(viewModel: CalendarViewModel, uiState: CalendarUiState, backdrop: Backdrop) {
    val adaptiveColor = LocalPrismalAdaptiveColor.current
    val tokens = com.crescentapps.turnly.presentation.theme.TurnlyThemeTokens.colors
    val monthTitle = remember(uiState.currentMonth) {
        val monthName = uiState.currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
        "$monthName ${uiState.currentMonth.year}"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = monthTitle,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                color = adaptiveColor
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            LiquidIconButton(
                onClick = { viewModel.prevMonth() },
                backdrop = backdrop,
                iconSize = 36.dp
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month", tint = adaptiveColor, modifier = Modifier.size(18.dp))
            }
            LiquidButton(
                onClick = { viewModel.jumpToToday() },
                backdrop = backdrop,
                surfaceColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
            ) {
                Text("Today", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = adaptiveColor)
            }
            LiquidIconButton(
                onClick = { viewModel.nextMonth() },
                backdrop = backdrop,
                iconSize = 36.dp
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = adaptiveColor, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun CalendarGrid(viewModel: CalendarViewModel, uiState: CalendarUiState, backdrop: Backdrop) {
    val adaptiveColor = LocalPrismalAdaptiveColor.current
    val tokens = com.crescentapps.turnly.presentation.theme.TurnlyThemeTokens.colors

    LiquidCard(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Weekday labels
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                weekdays.forEach { day ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = day,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = adaptiveColor.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Days Grid
            val firstOfMonth = uiState.currentMonth.atDay(1)
            val firstDayOffset = firstOfMonth.dayOfWeek.value - 1
            val daysInMonth = uiState.currentMonth.lengthOfMonth()
            val totalCells = ((firstDayOffset + daysInMonth + 6) / 7) * 7

            for (week in 0 until (totalCells / 7)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
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
                                isSelected -> tokens.calendarDaySelectedBg
                                isToday -> tokens.calendarDayTodayBorder.copy(alpha = 0.25f)
                                else -> Color.Transparent
                            }
                            val cellBorderColor = when {
                                isSelected -> tokens.calendarDaySelectedBg
                                isToday -> tokens.calendarDayTodayBorder
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(0.85f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(cellBg)
                                    .border(
                                        width = if (isSelected) 1.5.dp else if (isToday) 1.2.dp else 0.dp,
                                        color = cellBorderColor,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.selectDate(cellDate) }
                                    .padding(2.dp),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    val dayTextColor = when {
                                        isSelected -> tokens.calendarDaySelectedText
                                        isToday -> tokens.primary
                                        else -> adaptiveColor
                                    }

                                    Text(
                                        text = dayNumber.toString(),
                                        fontSize = 13.sp,
                                        fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = dayTextColor
                                    )

                                    if (turns.isNotEmpty()) {
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        ) {
                                            turns.take(3).forEach { indicator ->
                                                val dotColor = try {
                                                    Color(android.graphics.Color.parseColor(indicator.participantColorHex))
                                                } catch (e: Exception) {
                                                    tokens.primary
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .size(5.dp)
                                                        .clip(CircleShape)
                                                        .background(dotColor)
                                                        .border(0.5.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                                )
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.height(5.dp))
                                    }
                                }
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f).aspectRatio(0.85f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CalendarDayDetailPane(
    uiState: CalendarUiState,
    backdrop: Backdrop,
    onMarkDone: (ResolvedTurn) -> Unit,
    onOverride: (ResolvedTurn) -> Unit,
    onSkip: (ResolvedTurn) -> Unit,
    onScheduleClick: (Long) -> Unit
) {
    val adaptiveColor = LocalPrismalAdaptiveColor.current
    val tokens = com.crescentapps.turnly.presentation.theme.TurnlyThemeTokens.colors

    LiquidCard(
        backdrop = backdrop,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = DateUtils.formatDisplay(DateUtils.format(uiState.selectedDate)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = tokens.primary
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (uiState.selectedDayTurns.isEmpty()) {
                Text(
                    text = "No rotations active on this day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = adaptiveColor.copy(alpha = 0.6f)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    uiState.selectedDayTurns.forEach { turn ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(tokens.surface.copy(alpha = 0.08f))
                                .clickable { onScheduleClick(turn.schedule.id) }
                                .padding(14.dp)
                        ) {
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
                                    TurnlyScheduleTypeBadge(type = turn.schedule.type, size = 22.dp)
                                    Text(
                                        text = turn.schedule.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = adaptiveColor,
                                        maxLines = 1
                                    )
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                TurnlyStatusBadge(status = turn.status)
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                ParticipantAvatar(participant = turn.participant, size = 36.dp)
                                Column {
                                    Text(
                                        text = turn.participant.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = adaptiveColor
                                    )
                                    if (turn.schedule.type == ScheduleType.MONEY && turn.expectedAmount != null) {
                                        Text(
                                            text = CurrencyUtils.formatAmount(turn.expectedAmount, turn.schedule.currencyCode),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            // Actions
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (turn.status == OccurrenceStatus.PENDING) {
                                    LiquidButton(
                                        onClick = { onMarkDone(turn) },
                                        backdrop = backdrop,
                                        modifier = Modifier.weight(1f),
                                        surfaceColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                    ) {
                                        Text("Mark Done", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = tokens.primary)
                                    }
                                }
                                LiquidButton(
                                    onClick = { onOverride(turn) },
                                    backdrop = backdrop,
                                    surfaceColor = tokens.surface.copy(alpha = 0.1f)
                                ) {
                                    Text("Override", fontSize = 12.sp, color = adaptiveColor)
                                }
                                LiquidButton(
                                    onClick = { onSkip(turn) },
                                    backdrop = backdrop,
                                    surfaceColor = tokens.surface.copy(alpha = 0.1f)
                                ) {
                                    Text("Skip", fontSize = 12.sp, color = adaptiveColor)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
