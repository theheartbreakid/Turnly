package com.crescentapps.turnly.presentation.gaussianblur.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.core.model.OccurrenceStatus
import com.crescentapps.turnly.core.model.ResolvedTurn
import com.crescentapps.turnly.core.model.ScheduleType
import com.crescentapps.turnly.core.util.CurrencyUtils
import com.crescentapps.turnly.core.util.DateUtils
import com.crescentapps.turnly.presentation.components.Icon
import com.crescentapps.turnly.presentation.components.ParticipantAvatar
import com.crescentapps.turnly.presentation.components.Text
import com.crescentapps.turnly.presentation.components.TurnlyScheduleTypeBadge
import com.crescentapps.turnly.presentation.components.TurnlyStatusBadge
import com.crescentapps.turnly.presentation.gaussianblur.components.*
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianBlurColors
import com.crescentapps.turnly.presentation.screens.calendar.CalendarUiState
import com.crescentapps.turnly.presentation.screens.calendar.CalendarViewModel
import java.time.LocalDate
import java.util.Locale

@Composable
fun GaussianCalendarScreen(
    viewModel: CalendarViewModel,
    onScheduleClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalGaussianBlurColors.current
    var overrideTurnTarget by remember { mutableStateOf<ResolvedTurn?>(null) }

    BoxWithConstraints(modifier = modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        val isExpanded = maxWidth > 640.dp

        if (isExpanded) {
            Row(
                modifier = Modifier.fillMaxSize().padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1.2f)) {
                    GaussianCalendarHeader(viewModel = viewModel, uiState = uiState)
                    Spacer(modifier = Modifier.height(12.dp))
                    GaussianCalendarGrid(viewModel = viewModel, uiState = uiState)
                }
                Column(modifier = Modifier.weight(1f)) {
                    GaussianCalendarDayDetailPane(
                        uiState = uiState,
                        onMarkDone = { turn -> viewModel.markTurnComplete(turn) },
                        onOverride = { turn -> overrideTurnTarget = turn },
                        onSkip = { turn -> viewModel.skipDate(turn.schedule.id, turn.date) },
                        onScheduleClick = onScheduleClick
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 16.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    GaussianCalendarHeader(viewModel = viewModel, uiState = uiState)
                }
                item {
                    GaussianCalendarGrid(viewModel = viewModel, uiState = uiState)
                }
                item {
                    GaussianCalendarDayDetailPane(
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

    // Override Participant Picker Dialog
    overrideTurnTarget?.let { turn ->
        GaussianDialog(
            onDismissRequest = { overrideTurnTarget = null },
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
                            .background(colors.textPrimary.copy(alpha = 0.05f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ParticipantAvatar(participant = p, size = 32.dp)
                        Text(text = p.name, fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun GaussianCalendarHeader(viewModel: CalendarViewModel, uiState: CalendarUiState) {
    val colors = LocalGaussianBlurColors.current
    val monthTitle = remember(uiState.currentMonth) {
        val monthName = uiState.currentMonth.month.getDisplayName(java.time.format.TextStyle.FULL, Locale.getDefault())
        "$monthName ${uiState.currentMonth.year}"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = monthTitle,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = colors.textPrimary
        )

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
            GaussianIconButton(
                onClick = { viewModel.prevMonth() },
                size = 36.dp
            ) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Month", tint = colors.textPrimary, modifier = Modifier.size(18.dp))
            }
            GaussianButton(
                onClick = { viewModel.jumpToToday() },
                accentTint = colors.accent,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text("Today", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.textOnAccent)
            }
            GaussianIconButton(
                onClick = { viewModel.nextMonth() },
                size = 36.dp
            ) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Next Month", tint = colors.textPrimary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun GaussianCalendarGrid(viewModel: CalendarViewModel, uiState: CalendarUiState) {
    val colors = LocalGaussianBlurColors.current

    GaussianCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                weekdays.forEach { day ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = day,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                                isSelected -> colors.accent
                                isToday -> colors.accent.copy(alpha = 0.25f)
                                else -> Color.Transparent
                            }
                            val cellBorderColor = when {
                                isSelected -> colors.accent
                                isToday -> colors.accent
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
                                        isSelected -> colors.textOnAccent
                                        isToday -> colors.accent
                                        else -> colors.textPrimary
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
                                                    colors.accent
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
fun GaussianCalendarDayDetailPane(
    uiState: CalendarUiState,
    onMarkDone: (ResolvedTurn) -> Unit,
    onOverride: (ResolvedTurn) -> Unit,
    onSkip: (ResolvedTurn) -> Unit,
    onScheduleClick: (Long) -> Unit
) {
    val colors = LocalGaussianBlurColors.current

    GaussianCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = DateUtils.formatDisplay(DateUtils.format(uiState.selectedDate)),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.accent
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (uiState.selectedDayTurns.isEmpty()) {
                Text(
                    text = "No rotations active on this day.",
                    fontSize = 14.sp,
                    color = colors.textMuted
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    uiState.selectedDayTurns.forEach { turn ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(18.dp))
                                .background(colors.textPrimary.copy(alpha = 0.05f))
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
                                        color = colors.textPrimary,
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
                                        color = colors.textPrimary
                                    )
                                    if (turn.schedule.type == ScheduleType.MONEY && turn.expectedAmount != null) {
                                        Text(
                                            text = CurrencyUtils.formatAmount(turn.expectedAmount, turn.schedule.currencyCode),
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 13.sp,
                                            color = colors.accent
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                if (turn.status == OccurrenceStatus.PENDING) {
                                    GaussianButton(
                                        onClick = { onMarkDone(turn) },
                                        modifier = Modifier.weight(1f),
                                        accentTint = colors.accent,
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Mark Done", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.textOnAccent)
                                    }
                                }
                                GaussianButton(
                                    onClick = { onOverride(turn) },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Override", fontSize = 12.sp, color = colors.textPrimary)
                                }
                                GaussianButton(
                                    onClick = { onSkip(turn) },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("Skip", fontSize = 12.sp, color = colors.textPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
