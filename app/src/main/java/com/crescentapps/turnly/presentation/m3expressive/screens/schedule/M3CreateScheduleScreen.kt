package com.crescentapps.turnly.presentation.m3expressive.screens.schedule

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.crescentapps.turnly.presentation.m3expressive.components.TurnlyExpressiveButton
import com.crescentapps.turnly.presentation.m3expressive.components.TurnlyExpressiveCard
import com.crescentapps.turnly.presentation.m3expressive.theme.ExpressiveTokens
import com.crescentapps.turnly.presentation.screens.schedule.ScheduleFormViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3CreateScheduleScreen(
    viewModel: ScheduleFormViewModel,
    onNavigateBack: () -> Unit,
    onCreatedSuccessfully: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onCreatedSuccessfully()
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("New Schedule") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = ExpressiveTokens.spacing.normal)
        ) {
            // Very simple step representation
            when (uiState.currentStep) {
                1 -> {
                    Text("Basic Info", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(ExpressiveTokens.spacing.normal))
                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.updateName(it) },
                        label = { Text("Schedule Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(ExpressiveTokens.spacing.group))
                    TurnlyExpressiveButton(
                        onClick = { viewModel.nextStep() },
                        text = "Next",
                        modifier = Modifier.align(Alignment.End)
                    )
                }
                2 -> {
                    Text("Participants", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(ExpressiveTokens.spacing.normal))
                    var newName by remember { mutableStateOf("") }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Name") },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(ExpressiveTokens.spacing.small))
                        TurnlyExpressiveButton(
                            onClick = {
                                viewModel.addParticipant(newName, null, "")
                                newName = ""
                            },
                            text = "Add",
                            isPrimary = false
                        )
                    }
                    Spacer(Modifier.height(ExpressiveTokens.spacing.normal))
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        itemsIndexed(uiState.participants) { index, p ->
                            ListItem(
                                trailingContent = {
                                    IconButton(onClick = { viewModel.removeParticipant(index) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove")
                                    }
                                }
                            ) {
                                Text(p.name)
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = ExpressiveTokens.spacing.normal),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TurnlyExpressiveButton(onClick = { viewModel.previousStep() }, text = "Back", isPrimary = false)
                        TurnlyExpressiveButton(onClick = { viewModel.nextStep() }, text = "Next")
                    }
                }
                3 -> {
                    Text("Review & Save", style = MaterialTheme.typography.titleLarge)
                    Spacer(Modifier.height(ExpressiveTokens.spacing.normal))
                    TurnlyExpressiveCard(modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(ExpressiveTokens.spacing.normal)) {
                            Text("Name: ${uiState.name}", style = MaterialTheme.typography.bodyLarge)
                            Text("Participants: ${uiState.participants.size}", style = MaterialTheme.typography.bodyLarge)
                            Text("Frequency: ${uiState.frequencyType}", style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    Spacer(Modifier.weight(1f))
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = ExpressiveTokens.spacing.normal),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TurnlyExpressiveButton(onClick = { viewModel.previousStep() }, text = "Back", isPrimary = false)
                        TurnlyExpressiveButton(onClick = { viewModel.saveSchedule() }, text = "Create Schedule")
                    }
                }
            }
        }
    }
}
