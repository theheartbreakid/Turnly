package com.crescentapps.turnly.presentation.m3.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.core.model.*
import com.crescentapps.turnly.core.util.CurrencyUtils
import com.crescentapps.turnly.core.util.DateUtils
import com.crescentapps.turnly.presentation.m3.components.*
import com.crescentapps.turnly.presentation.screens.schedule.CreateScheduleUiState
import com.crescentapps.turnly.presentation.screens.schedule.ScheduleDetailViewModel
import com.crescentapps.turnly.presentation.screens.schedule.ScheduleFormViewModel

/**
 * Material 3 Schedule List Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3ScheduleListScreen(
    schedules: List<Schedule>,
    onCreateSchedule: () -> Unit,
    onScheduleClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Turn Schedules", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateSchedule,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Schedule")
            }
        }
    ) { innerPadding ->
        if (schedules.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                M3EmptyTurnState(
                    title = "No turn schedules",
                    description = "Set up turn schedules for shared expenses, chores, car use, care, or team duties.",
                    actionText = "Create Schedule",
                    onAction = onCreateSchedule
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 88.dp)
            ) {
                items(schedules, key = { it.id }) { schedule ->
                    ElevatedCard(
                        onClick = { onScheduleClick(schedule.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f, fill = false)
                            ) {
                                M3ScheduleTypeBadge(type = schedule.type, size = 38.dp)

                                Column {
                                    Text(
                                        text = schedule.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${schedule.type.displayName} · ${schedule.frequencyType.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (schedule.type == ScheduleType.MONEY && schedule.defaultAmount != null) {
                                Text(
                                    text = CurrencyUtils.formatAmount(schedule.defaultAmount, schedule.currencyCode),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Material 3 Schedule Detail Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3ScheduleDetailScreen(
    viewModel: ScheduleDetailViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showArchiveConfirm by remember { mutableStateOf(false) }

    val schedule = uiState.schedule

    if (schedule == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(schedule.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.togglePauseSchedule() }) {
                        Icon(
                            imageVector = if (schedule.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (schedule.isPaused) "Resume" else "Pause",
                            tint = if (schedule.isPaused) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp)
        ) {
            // Overview Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "CATEGORY",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = schedule.type.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (schedule.isPaused) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Text(
                                        text = "PAUSED",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }

                        if (schedule.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = schedule.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (schedule.type == ScheduleType.MONEY && schedule.defaultAmount != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Amount: ${CurrencyUtils.formatAmount(schedule.defaultAmount, schedule.currencyCode)} / turn",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Statistics Card
            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "STATISTICS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            M3StatItem(label = "Total Turns", value = "${uiState.totalTurnsCount}")
                            M3StatItem(label = "Completed", value = "${uiState.completedCount}")
                            if (schedule.type == ScheduleType.MONEY) {
                                M3StatItem(
                                    label = "Recorded",
                                    value = CurrencyUtils.formatAmount(uiState.totalRecordedAmount, schedule.currencyCode)
                                )
                            }
                        }
                    }
                }
            }

            // Participants Header
            item {
                Text(
                    text = "PARTICIPANTS (${uiState.participants.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(uiState.participants) { p ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            M3ParticipantAvatar(participant = p, size = 36.dp)
                            Text(text = p.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        }

                        val turnsDone = uiState.turnsPerParticipant[p.name] ?: 0
                        Text(
                            text = "$turnsDone done",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Upcoming Turns Header
            item {
                Text(
                    text = "UPCOMING TURNS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(uiState.upcomingTurns) { (p, date) ->
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            M3ParticipantAvatar(participant = p, size = 30.dp)
                            Text(text = p.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        }
                        Text(
                            text = DateUtils.formatDisplay(DateUtils.format(date)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Action Buttons
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showArchiveConfirm = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (schedule.isArchived) "Unarchive" else "Archive")
                    }

                    Button(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }

    if (showArchiveConfirm) {
        M3ConfirmDialog(
            title = "Archive Schedule?",
            message = "Archiving will pause this schedule while preserving all history.",
            confirmText = "Archive",
            onConfirm = {
                viewModel.archiveSchedule()
                onNavigateBack()
            },
            onDismiss = { showArchiveConfirm = false }
        )
    }

    if (showDeleteConfirm) {
        M3ConfirmDialog(
            title = "Delete Permanently?",
            message = "This will permanently remove \"${schedule.name}\" and all records. Choose Archive if you wish to keep history.",
            confirmText = "Delete Permanently",
            isDestructive = true,
            onConfirm = {
                viewModel.deletePermanently()
                onNavigateBack()
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}

@Composable
private fun M3StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * Material 3 Create Schedule Screen (3-Step Wizard)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3CreateScheduleScreen(
    viewModel: ScheduleFormViewModel,
    onNavigateBack: () -> Unit,
    onCreatedSuccessfully: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onCreatedSuccessfully()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Step ${uiState.currentStep} of 3", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (uiState.currentStep > 1) viewModel.previousStep()
                        else onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.currentStep > 1) {
                        OutlinedButton(
                            onClick = { viewModel.previousStep() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Previous")
                        }
                    }

                    Button(
                        onClick = {
                            if (uiState.currentStep < 3) viewModel.nextStep()
                            else if (!uiState.isSaving) viewModel.saveSchedule()
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !uiState.isSaving
                    ) {
                        Text(
                            if (uiState.currentStep < 3) "Next"
                            else if (uiState.isSaving) "Creating..."
                            else "Create Schedule"
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            LinearProgressIndicator(
                progress = { uiState.currentStep / 3f },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (uiState.currentStep) {
                1 -> M3StepOne(viewModel, uiState)
                2 -> M3StepTwo(viewModel, uiState)
                3 -> M3StepThree(viewModel, uiState)
            }
        }
    }
}

@Composable
private fun M3StepOne(viewModel: ScheduleFormViewModel, uiState: CreateScheduleUiState) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Basic Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.updateName(it) },
                        label = { Text("Schedule Name") },
                        placeholder = { Text("e.g. Electricity Bill, Coffee Run") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Category", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ScheduleType.values()) { type ->
                            FilterChip(
                                selected = uiState.type == type,
                                onClick = { viewModel.updateType(type) },
                                label = { Text(type.displayName) }
                            )
                        }
                    }

                    if (uiState.type == ScheduleType.MONEY) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = uiState.currencyCode,
                                onValueChange = { viewModel.updateCurrency(it.uppercase()) },
                                label = { Text("Currency") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = uiState.defaultAmount,
                                onValueChange = { viewModel.updateDefaultAmount(it) },
                                label = { Text("Default Amount") },
                                modifier = Modifier.weight(1.5f),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun M3StepTwo(viewModel: ScheduleFormViewModel, uiState: CreateScheduleUiState) {
    var newPersonName by remember { mutableStateOf("") }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text("Add Participants", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "Select or add members who participate in this rotation.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newPersonName,
                    onValueChange = { newPersonName = it },
                    label = { Text("Member Name") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Button(
                    onClick = {
                        if (newPersonName.isNotBlank()) {
                            viewModel.addParticipant(newPersonName.trim(), null, "#3B82F6")
                            newPersonName = ""
                        }
                    },
                    enabled = newPersonName.isNotBlank()
                ) {
                    Text("Add")
                }
            }
        }

        itemsIndexed(uiState.participants) { index, p ->
            OutlinedCard(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        M3ParticipantAvatar(
                            participant = Participant(name = p.name, colorHex = p.colorHex),
                            size = 34.dp
                        )
                        Text(p.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    }

                    IconButton(onClick = { viewModel.removeParticipant(index) }) {
                        Icon(Icons.Default.Close, contentDescription = "Remove")
                    }
                }
            }
        }
    }
}

@Composable
private fun M3StepThree(viewModel: ScheduleFormViewModel, uiState: CreateScheduleUiState) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Text("Rotation Frequency", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }

        item {
            ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("How often do turns rotate?", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(FrequencyType.values()) { freq ->
                            FilterChip(
                                selected = uiState.frequencyType == freq,
                                onClick = { viewModel.updateFrequencyType(freq) },
                                label = { Text(freq.displayName) }
                            )
                        }
                    }

                    if (uiState.frequencyType == FrequencyType.EVERY_X_DAYS) {
                        OutlinedTextField(
                            value = uiState.frequencyInterval.toString(),
                            onValueChange = { it.toIntOrNull()?.let { num -> viewModel.updateFrequencyInterval(num) } },
                            label = { Text("Days between turns") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            }
        }

        item {
            ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Starting Order", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                    uiState.participants.forEachIndexed { index, p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${index + 1}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Text(p.name, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}
