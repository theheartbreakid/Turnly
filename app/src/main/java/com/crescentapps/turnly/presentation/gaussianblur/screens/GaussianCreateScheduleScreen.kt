package com.crescentapps.turnly.presentation.gaussianblur.screens

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
import com.crescentapps.turnly.presentation.components.Icon
import com.crescentapps.turnly.presentation.components.RadioButton
import com.crescentapps.turnly.presentation.components.Text
import com.crescentapps.turnly.presentation.gaussianblur.components.*
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianBlurColors
import com.crescentapps.turnly.presentation.screens.schedule.CreateScheduleUiState
import com.crescentapps.turnly.presentation.screens.schedule.ScheduleFormViewModel

@Composable
fun GaussianCreateScheduleScreen(
    viewModel: ScheduleFormViewModel,
    onNavigateBack: () -> Unit,
    onCreatedSuccessfully: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalGaussianBlurColors.current

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GaussianIconButton(
                onClick = {
                    if (uiState.currentStep > 1) viewModel.previousStep()
                    else onNavigateBack()
                }
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
            }

            Text(
                text = "Step ${uiState.currentStep} of 3",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.size(44.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            when (uiState.currentStep) {
                1 -> GaussianStepOneBasicInfo(viewModel, uiState)
                2 -> GaussianStepTwoPeople(viewModel, uiState)
                3 -> GaussianStepThreeRotation(viewModel, uiState)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.currentStep > 1) {
                GaussianButton(
                    onClick = { viewModel.previousStep() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Previous", color = colors.textPrimary, fontWeight = FontWeight.SemiBold)
                }
            }

            if (uiState.currentStep < 3) {
                GaussianButton(
                    onClick = { viewModel.nextStep() },
                    accentTint = colors.accent,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Next", color = colors.textOnAccent, fontWeight = FontWeight.Bold)
                }
            } else {
                GaussianButton(
                    onClick = { if (!uiState.isSaving) viewModel.saveSchedule() },
                    accentTint = colors.accent,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (uiState.isSaving) "Creating..." else "Create Schedule", color = colors.textOnAccent, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GaussianStepOneBasicInfo(viewModel: ScheduleFormViewModel, uiState: CreateScheduleUiState) {
    val colors = LocalGaussianBlurColors.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            GaussianCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "WHAT ARE YOU ORGANIZING?",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Give your schedule a recognizable name so everyone knows what it's for.",
                        fontSize = 13.sp,
                        color = colors.textMuted
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    GaussianTextField(
                        value = uiState.name,
                        onValueChange = { viewModel.updateName(it) },
                        label = "Schedule Name (e.g. Electricity Bill, Coffee Run)",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    GaussianTextField(
                        value = uiState.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        label = "Description (Optional)",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Schedule Category",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(ScheduleType.values()) { type ->
                            val isSelected = uiState.type == type
                            GaussianChip(
                                selected = isSelected,
                                label = type.displayName,
                                onClick = { viewModel.updateType(type) }
                            )
                        }
                    }

                    if (uiState.type == ScheduleType.MONEY) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.weight(1f)) {
                                GaussianTextField(
                                    value = uiState.currencyCode,
                                    onValueChange = { viewModel.updateCurrency(it.uppercase()) },
                                    label = "Currency",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Box(modifier = Modifier.weight(1.5f)) {
                                GaussianTextField(
                                    value = uiState.defaultAmount,
                                    onValueChange = { viewModel.updateDefaultAmount(it) },
                                    label = "Default Amount (e.g. 500)",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GaussianStepTwoPeople(viewModel: ScheduleFormViewModel, uiState: CreateScheduleUiState) {
    var newPersonName by remember { mutableStateOf("") }
    val colors = LocalGaussianBlurColors.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GaussianCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "Add People to this Turn", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary)
                    Text(text = "Add at least 2 people who will alternate turns.", fontSize = 12.sp, color = colors.textMuted)
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            GaussianTextField(
                                value = newPersonName,
                                onValueChange = { newPersonName = it },
                                label = "Name (e.g. Alex, Sam)",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        GaussianButton(
                            onClick = {
                                if (newPersonName.isNotBlank()) {
                                    viewModel.addParticipant(newPersonName, null, "#818CF8")
                                    newPersonName = ""
                                }
                            },
                            accentTint = colors.accent
                        ) {
                            Text("Add", color = colors.textOnAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "PARTICIPANTS (${uiState.participants.size} added)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.accent
            )
        }

        itemsIndexed(uiState.participants) { index, p ->
            GaussianCard(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = p.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary)

                    GaussianIconButton(
                        onClick = { viewModel.removeParticipant(index) },
                        size = 36.dp
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove", tint = colors.statusMissed, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun GaussianStepThreeRotation(viewModel: ScheduleFormViewModel, uiState: CreateScheduleUiState) {
    val colors = LocalGaussianBlurColors.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GaussianCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(text = "How Often Does It Happen?", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = colors.textPrimary)
                    Spacer(modifier = Modifier.height(12.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(FrequencyType.values()) { fType ->
                            val isSelected = uiState.frequencyType == fType
                            GaussianChip(
                                selected = isSelected,
                                label = fType.displayName,
                                onClick = { viewModel.updateFrequencyType(fType) }
                            )
                        }
                    }
                }
            }
        }
    }
}
