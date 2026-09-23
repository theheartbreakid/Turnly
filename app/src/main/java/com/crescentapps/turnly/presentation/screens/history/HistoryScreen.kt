package com.crescentapps.turnly.presentation.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.core.model.OccurrenceStatus
import com.crescentapps.turnly.core.util.CurrencyUtils
import com.crescentapps.turnly.core.util.DateUtils
import com.crescentapps.turnly.presentation.catalog.utils.LocalBackdrop
import com.crescentapps.turnly.presentation.components.LocalPrismalAdaptiveColor
import com.crescentapps.turnly.presentation.components.ParticipantAvatar
import com.crescentapps.turnly.presentation.components.liquid.LiquidCard
import com.crescentapps.turnly.presentation.components.liquid.LiquidChip
import com.crescentapps.turnly.presentation.theme.LiquidColors
import com.crescentapps.turnly.presentation.theme.LocalTurnlyColors
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val backdrop = LocalBackdrop.current ?: rememberLayerBackdrop()
    val adaptiveColor = LocalPrismalAdaptiveColor.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "History & Records",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            color = adaptiveColor
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Statistics Card
        LiquidCard(
            backdrop = backdrop,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StatColumn(label = "Total Turns", value = uiState.totalCount.toString(), color = adaptiveColor)
                    StatColumn(label = "Completed", value = uiState.completedCount.toString(), color = LiquidColors.StatusCompleted)
                    StatColumn(label = "Skipped", value = uiState.skippedCount.toString(), color = LiquidColors.StatusSkipped)
                    StatColumn(label = "Missed", value = uiState.missedCount.toString(), color = LiquidColors.StatusMissed)
                }

                if (uiState.totalRecordedAmount > 0.0 || uiState.totalExpectedAmount > 0.0) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = adaptiveColor.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Recorded: ${CurrencyUtils.formatAmount(uiState.totalRecordedAmount, "USD")}",
                            fontWeight = FontWeight.Bold,
                            color = LiquidColors.StatusCompleted,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Expected: ${CurrencyUtils.formatAmount(uiState.totalExpectedAmount, "USD")}",
                            fontWeight = FontWeight.SemiBold,
                            color = adaptiveColor.copy(alpha = 0.65f),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Filter Chips
        val colors = LocalTurnlyColors.current
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                val isSelected = uiState.selectedStatus == null
                LiquidChip(
                    onClick = { viewModel.filterByStatus(null) },
                    backdrop = backdrop,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent,
                    surfaceColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else colors.surface.copy(alpha = 0.08f)
                ) {
                    Text(
                        text = "All Statuses",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else adaptiveColor.copy(alpha = 0.8f)
                    )
                }
            }
            items(OccurrenceStatus.values()) { status ->
                val isSelected = uiState.selectedStatus == status
                LiquidChip(
                    onClick = { viewModel.filterByStatus(status) },
                    backdrop = backdrop,
                    tint = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.4f) else Color.Transparent,
                    surfaceColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else colors.surface.copy(alpha = 0.08f)
                ) {
                    Text(
                        text = status.displayName,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else adaptiveColor.copy(alpha = 0.8f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // History Timeline List
        if (uiState.filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No occurrences recorded yet.",
                    color = adaptiveColor.copy(alpha = 0.6f)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 110.dp)
            ) {
                items(uiState.filteredItems, key = { "${it.occurrence.id}_${it.occurrence.date}" }) { item ->
                    val pColor = LiquidColors.getParticipantColor(item.participantColorHex)

                    LiquidCard(
                        backdrop = backdrop,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp)
                    ) {
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
                                        text = item.participantName.take(2).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                }

                                Column {
                                    Text(
                                        text = item.participantName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = adaptiveColor
                                    )
                                    val completedBy = item.occurrence.completedByMemberName?.let { " · by $it" }.orEmpty()
                                    Text(
                                        text = "${item.scheduleName} · ${DateUtils.formatDisplay(item.occurrence.date)}$completedBy",
                                        fontSize = 12.sp,
                                        color = adaptiveColor.copy(alpha = 0.65f)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                val statusBg = when (item.occurrence.status) {
                                    OccurrenceStatus.COMPLETED -> LiquidColors.StatusCompleted.copy(alpha = 0.2f)
                                    OccurrenceStatus.SKIPPED -> LiquidColors.StatusSkipped.copy(alpha = 0.2f)
                                    else -> LiquidColors.StatusPending.copy(alpha = 0.2f)
                                }
                                val statusFg = when (item.occurrence.status) {
                                    OccurrenceStatus.COMPLETED -> LiquidColors.StatusCompleted
                                    OccurrenceStatus.SKIPPED -> LiquidColors.StatusSkipped
                                    else -> LiquidColors.StatusPending
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(statusBg)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = item.occurrence.status.name,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = statusFg
                                    )
                                }

                                val amt = item.occurrence.actualAmount ?: item.occurrence.expectedAmount
                                if (amt != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = CurrencyUtils.formatAmount(amt, item.currencyCode),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = if (item.occurrence.status == OccurrenceStatus.COMPLETED) LiquidColors.StatusCompleted else adaptiveColor
                                    )
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
fun StatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = color)
        Text(text = label, fontSize = 11.sp, color = color.copy(alpha = 0.7f))
    }
}
