package com.crescentapps.turnly.presentation.m3expressive.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.crescentapps.turnly.core.model.ResolvedTurn
import com.crescentapps.turnly.core.model.ScheduleType
import com.crescentapps.turnly.core.model.OccurrenceStatus
import com.crescentapps.turnly.presentation.m3expressive.components.TurnlyExpressiveButton
import com.crescentapps.turnly.presentation.m3expressive.components.TurnlyExpressiveCard
import com.crescentapps.turnly.presentation.m3expressive.theme.ExpressiveTokens
import com.crescentapps.turnly.presentation.screens.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun M3HomeScreen(
    viewModel: HomeViewModel,
    onCreateSchedule: () -> Unit,
    onScheduleClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var turnForAmountDialog by remember { mutableStateOf<ResolvedTurn?>(null) }
    var enteredAmount by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Today") },
                actions = {
                    if (uiState.todayTurns.isNotEmpty()) {
                        IconButton(onClick = onCreateSchedule) {
                            Icon(Icons.Default.Add, contentDescription = "New Schedule")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (uiState.todayTurns.isEmpty()) {
                FloatingActionButton(onClick = onCreateSchedule) {
                    Icon(Icons.Default.Add, contentDescription = "New Schedule")
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                LoadingIndicator()
            }
        } else if (uiState.todayTurns.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text(
                    text = "No turns are due right now.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(ExpressiveTokens.spacing.normal),
                verticalArrangement = Arrangement.spacedBy(ExpressiveTokens.spacing.component)
            ) {
                item {
                    Text(
                        text = "Next Turn",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = ExpressiveTokens.spacing.small)
                    )
                }

                // Emphasize the first pending turn
                val firstPending = uiState.todayTurns.firstOrNull { it.status == OccurrenceStatus.PENDING }
                if (firstPending != null) {
                    item {
                        TurnlyExpressiveCard(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            onClick = { onScheduleClick(firstPending.schedule.id) }
                        ) {
                            Column(Modifier.padding(ExpressiveTokens.spacing.group)) {
                                Text(
                                    text = firstPending.schedule.name,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Spacer(Modifier.height(ExpressiveTokens.spacing.small))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(ExpressiveTokens.spacing.group))
                                    Spacer(Modifier.width(ExpressiveTokens.spacing.micro))
                                    Text(
                                        text = firstPending.participant.name,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }
                                Spacer(Modifier.height(ExpressiveTokens.spacing.normal))
                                TurnlyExpressiveButton(
                                    onClick = { 
                                        if (firstPending.schedule.type == ScheduleType.MONEY) {
                                            turnForAmountDialog = firstPending
                                            enteredAmount = firstPending.schedule.defaultAmount?.toString() ?: ""
                                        } else {
                                            viewModel.markTurnComplete(firstPending)
                                        }
                                    },
                                    text = if (firstPending.schedule.type == ScheduleType.MONEY) "Record & Done" else "Mark Complete",
                                    icon = { Icon(Icons.Default.Check, contentDescription = null) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                if (uiState.todayTurns.size > 1) {
                    item {
                        Spacer(Modifier.height(ExpressiveTokens.spacing.group))
                        Text(
                            text = "Later Today",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(Modifier.height(ExpressiveTokens.spacing.small))
                    }

                    items(uiState.todayTurns.filter { it != firstPending }) { turn ->
                        ListItem(
                            supportingContent = { Text("Turn: ${turn.participant.name}") },
                            trailingContent = {
                                TurnlyExpressiveButton(
                                    onClick = { 
                                        if (turn.schedule.type == ScheduleType.MONEY) {
                                            turnForAmountDialog = turn
                                            enteredAmount = turn.schedule.defaultAmount?.toString() ?: ""
                                        } else {
                                            viewModel.markTurnComplete(turn)
                                        }
                                    },
                                    text = "Done",
                                    isPrimary = false
                                )
                            },
                            colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
                        ) {
                            Text(turn.schedule.name, style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
            }
        }
    }

    if (turnForAmountDialog != null) {
        AlertDialog(
            onDismissRequest = { turnForAmountDialog = null },
            title = { Text("Record Expense") },
            text = {
                OutlinedTextField(
                    value = enteredAmount,
                    onValueChange = { enteredAmount = it },
                    label = { Text("Amount (${turnForAmountDialog!!.schedule.currencyCode})") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val amount = enteredAmount.toDoubleOrNull()
                    viewModel.markTurnComplete(turnForAmountDialog!!, amount)
                    turnForAmountDialog = null
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { turnForAmountDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
