package com.crescentapps.turnly.presentation.m3expressive.screens.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.crescentapps.turnly.presentation.m3expressive.components.TurnlyExpressiveCard
import com.crescentapps.turnly.presentation.m3expressive.theme.ExpressiveTokens
import com.crescentapps.turnly.presentation.screens.calendar.CalendarViewModel

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
                title = { Text("Calendar") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Very simple calendar layout logic for now
            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize().weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentPadding = PaddingValues(ExpressiveTokens.spacing.normal),
                    verticalArrangement = Arrangement.spacedBy(ExpressiveTokens.spacing.component)
                ) {
                    items(uiState.selectedDayTurns) { turn ->
                        TurnlyExpressiveCard(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onScheduleClick(turn.schedule.id) }
                        ) {
                            Column(Modifier.padding(ExpressiveTokens.spacing.normal)) {
                                Text(text = turn.schedule.name, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(ExpressiveTokens.spacing.small))
                                Text(
                                    text = "Turn: ${turn.participant.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
