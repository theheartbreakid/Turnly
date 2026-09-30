package com.crescentapps.turnly.presentation.m3expressive.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.presentation.m3expressive.components.TurnlyExpressiveCard
import com.crescentapps.turnly.presentation.m3expressive.theme.ExpressiveTokens
import com.crescentapps.turnly.presentation.screens.calendar.CalendarViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3CalendarScreen(
    viewModel: CalendarViewModel,
    onScheduleClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { 
                    Text(
                        "Calendar",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.jumpToToday() },
                        shape = CircleShape
                    ) {
                        Text("Today", fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    ) { padding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            val isWideScreen = maxWidth > 600.dp

            if (isWideScreen) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left Column: Calendar View
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(16.dp)
                    ) {
                        CalendarGridContent(
                            uiState = uiState,
                            onPrevMonth = { viewModel.prevMonth() },
                            onNextMonth = { viewModel.nextMonth() },
                            onSelectDate = { date -> viewModel.selectDate(date) }
                        )
                    }

                    // Right Column: Selected Date Events
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .padding(16.dp)
                    ) {
                        SelectedDayEventsContent(
                            uiState = uiState,
                            onScheduleClick = onScheduleClick
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Calendar View
                    CalendarGridContent(
                        uiState = uiState,
                        onPrevMonth = { viewModel.prevMonth() },
                        onNextMonth = { viewModel.nextMonth() },
                        onSelectDate = { date -> viewModel.selectDate(date) }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Selected Date Events
                    SelectedDayEventsContent(
                        uiState = uiState,
                        onScheduleClick = onScheduleClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarGridContent(
    uiState: com.crescentapps.turnly.presentation.screens.calendar.CalendarUiState,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthTitle = remember(uiState.currentMonth) {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        uiState.currentMonth.format(formatter)
    }

    Surface(
        shape = RoundedCornerShape(28.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Month Navigation Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPrevMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month"
                    )
                }

                Text(
                    text = monthTitle,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month"
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Weekday Headers
            val weekdays = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                weekdays.forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days Grid
            val firstDayOfMonth = uiState.currentMonth.atDay(1)
            val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.value % 7 // Sun = 0
            val daysInMonth = uiState.currentMonth.lengthOfMonth()
            val totalCells = dayOfWeekOffset + daysInMonth
            val rows = (totalCells + 6) / 7

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                for (r in 0 until rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (c in 0 until 7) {
                            val cellIndex = r * 7 + c
                            val dayNumber = cellIndex - dayOfWeekOffset + 1

                            if (dayNumber in 1..daysInMonth) {
                                val date = uiState.currentMonth.atDay(dayNumber)
                                val isSelected = date == uiState.selectedDate
                                val isToday = date == LocalDate.now()
                                val hasTurns = uiState.dayTurnMap[date]?.isNotEmpty() == true

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                            else MaterialTheme.colorScheme.surfaceContainerHigh
                                        )
                                        .clickable { onSelectDate(date) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = dayNumber.toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                            else if (isToday) MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 14.sp
                                        )

                                        if (hasTurns) {
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Box(
                                                modifier = Modifier
                                                    .size(4.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                                                        else MaterialTheme.colorScheme.primary
                                                    )
                                            )
                                        }
                                    }
                                }
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedDayEventsContent(
    uiState: com.crescentapps.turnly.presentation.screens.calendar.CalendarUiState,
    onScheduleClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatted = remember(uiState.selectedDate) {
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.getDefault())
        uiState.selectedDate.format(formatter)
    }

    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = "Events on $dateFormatted",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.selectedDayTurns.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier.padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No turns scheduled for this date.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(uiState.selectedDayTurns) { turn ->
                    TurnlyExpressiveCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        onClick = { onScheduleClick(turn.schedule.id) }
                    ) {
                        ListItem(
                            leadingContent = {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarMonth,
                                            contentDescription = null,
                                            modifier = Modifier.size(22.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                            },
                            supportingContent = {
                                Text(
                                    text = "Turn: ${turn.participant.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
                        ) {
                            Text(
                                text = turn.schedule.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}
