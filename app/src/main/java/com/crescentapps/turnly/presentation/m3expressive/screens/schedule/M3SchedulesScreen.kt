package com.crescentapps.turnly.presentation.m3expressive.screens.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.crescentapps.turnly.core.model.Schedule
import com.crescentapps.turnly.presentation.m3expressive.components.TurnlyExpressiveCard
import com.crescentapps.turnly.presentation.m3expressive.theme.ExpressiveTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3SchedulesScreen(
    schedules: List<Schedule>,
    onCreateSchedule: () -> Unit,
    onScheduleClick: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Schedules") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateSchedule) {
                Icon(Icons.Default.Add, contentDescription = "Create Schedule")
            }
        }
    ) { padding ->
        if (schedules.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No schedules yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(ExpressiveTokens.spacing.normal),
                verticalArrangement = Arrangement.spacedBy(ExpressiveTokens.spacing.component)
            ) {
                items(schedules) { schedule ->
                    TurnlyExpressiveCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onScheduleClick(schedule.id) }
                    ) {
                        Column(Modifier.padding(ExpressiveTokens.spacing.normal)) {
                            Text(text = schedule.name, style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(ExpressiveTokens.spacing.small))
                            Text(text = schedule.description, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
