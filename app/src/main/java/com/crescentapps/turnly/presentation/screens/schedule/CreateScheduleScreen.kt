package com.crescentapps.turnly.presentation.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.core.model.FrequencyType
import com.crescentapps.turnly.core.model.OverrideStrategy
import com.crescentapps.turnly.core.model.ScheduleType
import com.crescentapps.turnly.presentation.catalog.utils.LocalBackdrop
import com.crescentapps.turnly.presentation.components.LocalPrismalAdaptiveColor
import com.crescentapps.turnly.presentation.components.liquid.*
import com.crescentapps.turnly.presentation.theme.LiquidColors
import com.crescentapps.turnly.presentation.theme.LocalTurnlyColors
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

@Composable
fun CreateScheduleScreen(
    viewModel: ScheduleFormViewModel,
    onNavigateBack: () -> Unit,
    onCreatedSuccessfully: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val backdrop = LocalBackdrop.current ?: rememberLayerBackdrop()
    val adaptiveColor = LocalPrismalAdaptiveColor.current

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onCreatedSuccessfully()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LiquidIconButton(
                onClick = {
                    if (uiState.currentStep > 1) viewModel.previousStep()
                    else onNavigateBack()
                },
                backdrop = backdrop,
                iconSize = 40.dp
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = adaptiveColor)
            }

            Text(
                text = "Step ${uiState.currentStep} of 3",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = adaptiveColor
            )

            Box(modifier = Modifier.size(40.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Step Content Area
        Box(modifier = Modifier.weight(1f)) {
            when (uiState.currentStep) {
                1 -> StepOneBasicInfo(viewModel, uiState, backdrop)
                2 -> StepTwoPeople(viewModel, uiState, backdrop)
                3 -> StepThreeRotation(viewModel, uiState, backdrop)
            }
        }

        // Navigation Action Buttons at Bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.currentStep > 1) {
                LiquidButton(
                    onClick = { viewModel.previousStep() },
                    backdrop = backdrop,
                    modifier = Modifier.weight(1f),
                    surfaceColor = Color.White.copy(alpha = 0.08f)
                ) {
                    Text("Previous", color = adaptiveColor, fontWeight = FontWeight.SemiBold)
                }
            }

            val colors = LocalTurnlyColors.current
            if (uiState.currentStep < 3) {
                LiquidButton(
                    onClick = { viewModel.nextStep() },
                    backdrop = backdrop,
                    modifier = Modifier.weight(1f),
                    surfaceColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                ) {
                    Text("Next", color = colors.primary, fontWeight = FontWeight.Bold)
                }
            } else {
                LiquidButton(
                    onClick = { if (!uiState.isSaving) viewModel.saveSchedule() },
                    backdrop = backdrop,
                    modifier = Modifier.weight(1f),
                    surfaceColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                ) {
                    Text(if (uiState.isSaving) "Creating..." else "Create Schedule", color = colors.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun StepOneBasicInfo(viewModel: ScheduleFormViewModel, uiState: CreateScheduleUiState, backdrop: Backdrop) {
    val adaptiveColor = LocalPrismalAdaptiveColor.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            LiquidCard(
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "WHAT ARE YOU ORGANIZING?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Give your schedule a recognizable name so everyone knows what it's for.",
                        fontSize = 13.sp,
                        color = adaptiveColor.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    AdaptiveGlassTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.updateName(it) },
                        label = "Schedule Name (e.g. Electricity Bill, Coffee Run)",
                        backdrop = backdrop
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    AdaptiveGlassTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        label = "Description (Optional)",
                        backdrop = backdrop,
                        singleLine = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Schedule Category",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = adaptiveColor
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ScheduleType.values()) { type ->
                            val isSelected = uiState.type == type
                            LiquidChip(
                                onClick = { viewModel.updateType(type) },
                                backdrop = backdrop,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent,
                                surfaceColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
                            ) {
                                Text(
                                    text = type.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else adaptiveColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    if (uiState.type == ScheduleType.MONEY) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                AdaptiveGlassTextField(
                                    value = uiState.currencyCode,
                                    onValueChange = { viewModel.updateCurrency(it.uppercase()) },
                                    label = "Currency",
                                    backdrop = backdrop
                                )
                            }
                            Box(modifier = Modifier.weight(1.5f)) {
                                AdaptiveGlassTextField(
                                    value = uiState.defaultAmount,
                                    onValueChange = { viewModel.updateDefaultAmount(it) },
                                    label = "Default Amount (e.g. 500)",
                                    backdrop = backdrop
                                )
                            }
                        }
                    }
                }
            }
        }

        if (uiState.availableRooms.isNotEmpty()) {
            item {
                LiquidCard(backdrop = backdrop, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = "Share with P2P Room", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = adaptiveColor)
                                Text(text = "Synchronize this rotation with a connected room", fontSize = 12.sp, color = adaptiveColor.copy(alpha = 0.6f))
                            }
                            LiquidToggle(
                                selected = { uiState.shareWithRoom },
                                onSelect = { viewModel.updateShareWithRoom(it) },
                                backdrop = backdrop
                            )
                        }

                        if (uiState.shareWithRoom) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Select Room:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = adaptiveColor)
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(uiState.availableRooms) { room ->
                                    val isSelected = uiState.selectedRoomId == room.id
                                    LiquidChip(
                                        onClick = { viewModel.updateSelectedRoomId(room.id) },
                                        backdrop = backdrop,
                                        tint = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent,
                                        surfaceColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
                                    ) {
                                        Text(text = room.name, color = if (isSelected) MaterialTheme.colorScheme.primary else adaptiveColor)
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
fun StepTwoPeople(viewModel: ScheduleFormViewModel, uiState: CreateScheduleUiState, backdrop: Backdrop) {
    var newPersonName by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(LiquidColors.ParticipantPalette[0]) }
    val adaptiveColor = LocalPrismalAdaptiveColor.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            LiquidCard(backdrop = backdrop, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Add People to this Turn", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = adaptiveColor)
                    Text(text = "Add at least 2 people who will alternate turns.", fontSize = 12.sp, color = adaptiveColor.copy(alpha = 0.65f))
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            AdaptiveGlassTextField(
                                value = newPersonName,
                                onValueChange = { newPersonName = it },
                                label = "Name (e.g. Mohsin, Mom)",
                                backdrop = backdrop
                            )
                        }
                        LiquidButton(
                            onClick = {
                                if (newPersonName.isNotBlank()) {
                                    viewModel.addParticipant(newPersonName, null, selectedColor)
                                    newPersonName = ""
                                    val nextIndex = (uiState.participants.size + 1) % LiquidColors.ParticipantPalette.size
                                    selectedColor = LiquidColors.ParticipantPalette[nextIndex]
                                }
                            },
                            backdrop = backdrop,
                            surfaceColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        ) {
                            Text("Add", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    // Color Picker Row
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(LiquidColors.ParticipantPalette) { hex ->
                            val isSelected = selectedColor == hex
                            val parsedColor = try { Color(android.graphics.Color.parseColor(hex)) } catch (e: Exception) { Color(0xFF3B82F6) }
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(parsedColor)
                                    .border(
                                        if (isSelected) 2.5.dp else 1.dp,
                                        if (isSelected) Color.White else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable { selectedColor = hex }
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "PARTICIPANTS ( added - min 2)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        itemsIndexed(uiState.participants) { index, p ->
            val pColor = try { Color(android.graphics.Color.parseColor(p.colorHex)) } catch (e: Exception) { Color(0xFF3B82F6) }
            LiquidCard(backdrop = backdrop, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(pColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = p.name.take(2).uppercase(),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                        Column {
                            Text(text = p.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = adaptiveColor)
                            Text(text = "Position #", fontSize = 12.sp, color = adaptiveColor.copy(alpha = 0.6f))
                        }
                    }

                    LiquidIconButton(
                        onClick = { viewModel.removeParticipant(index) },
                        backdrop = backdrop,
                        iconSize = 36.dp,
                        surfaceColor = Color.Red.copy(alpha = 0.1f)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = Color(0xFFFF5252), modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StepThreeRotation(viewModel: ScheduleFormViewModel, uiState: CreateScheduleUiState, backdrop: Backdrop) {
    val adaptiveColor = LocalPrismalAdaptiveColor.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            LiquidCard(backdrop = backdrop, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "How Often Does It Happen?", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = adaptiveColor)
                    Text(text = "Choose how frequently turns rotate between people.", fontSize = 12.sp, color = adaptiveColor.copy(alpha = 0.65f))
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(FrequencyType.values()) { fType ->
                            val isSelected = uiState.frequencyType == fType
                            LiquidChip(
                                onClick = { viewModel.updateFrequencyType(fType) },
                                backdrop = backdrop,
                                tint = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent,
                                surfaceColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
                            ) {
                                Text(
                                    text = fType.displayName,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else adaptiveColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }

                    if (uiState.frequencyType == FrequencyType.CUSTOM_WEEKDAYS) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Select active weekdays:", fontSize = 13.sp, color = adaptiveColor.copy(alpha = 0.7f))
                        Spacer(modifier = Modifier.height(8.dp))
                        val days = listOf("Mon" to 1, "Tue" to 2, "Wed" to 3, "Thu" to 4, "Fri" to 5, "Sat" to 6, "Sun" to 7)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            days.forEach { (label, dayVal) ->
                                val isSelected = uiState.selectedWeekdays.contains(dayVal)
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primary
                                            else Color.White.copy(alpha = 0.08f)
                                        )
                                        .clickable { viewModel.toggleWeekday(dayVal) },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label.take(1),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else adaptiveColor.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            var showAdvanced by remember { mutableStateOf(false) }
            LiquidCard(backdrop = backdrop, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAdvanced = !showAdvanced },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Advanced Options", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = adaptiveColor)
                            Text(text = "Manual assignment behavior & turn adjustments", fontSize = 12.sp, color = adaptiveColor.copy(alpha = 0.6f))
                        }
                        Icon(
                            imageVector = if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Toggle Advanced",
                            tint = adaptiveColor
                        )
                    }

                    if (showAdvanced) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(text = "When someone takes a turn early or out of order:", fontSize = 12.sp, fontWeight = FontWeight.Medium, color = adaptiveColor)
                        Spacer(modifier = Modifier.height(8.dp))

                        OverrideStrategy.values().forEach { strategy ->
                            val isSelected = uiState.overrideStrategy == strategy
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.updateOverrideStrategy(strategy) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.updateOverrideStrategy(strategy) }
                                )
                                Column(modifier = Modifier.padding(start = 8.dp)) {
                                    Text(text = strategy.displayName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = adaptiveColor)
                                    Text(text = strategy.description, fontSize = 11.sp, color = adaptiveColor.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
