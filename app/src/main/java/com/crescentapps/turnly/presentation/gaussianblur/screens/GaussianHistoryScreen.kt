package com.crescentapps.turnly.presentation.gaussianblur.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.crescentapps.turnly.presentation.components.HorizontalDivider
import com.crescentapps.turnly.presentation.components.Text
import com.crescentapps.turnly.presentation.gaussianblur.components.GaussianCard
import com.crescentapps.turnly.presentation.gaussianblur.components.GaussianChip
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianBlurColors
import com.crescentapps.turnly.presentation.screens.history.HistoryViewModel

@Composable
fun GaussianHistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalGaussianBlurColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "History & Records",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Summary Statistics Card
        GaussianCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    GaussianStatColumn(label = "Total Turns", value = uiState.totalCount.toString(), color = colors.textPrimary)
                    GaussianStatColumn(label = "Completed", value = uiState.completedCount.toString(), color = colors.statusCompleted)
                    GaussianStatColumn(label = "Skipped", value = uiState.skippedCount.toString(), color = colors.statusSkipped)
                    GaussianStatColumn(label = "Missed", value = uiState.missedCount.toString(), color = colors.statusMissed)
                }

                if (uiState.totalRecordedAmount > 0.0 || uiState.totalExpectedAmount > 0.0) {
                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = colors.divider)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Recorded: ${CurrencyUtils.formatAmount(uiState.totalRecordedAmount, "USD")}",
                            fontWeight = FontWeight.Bold,
                            color = colors.statusCompleted,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Expected: ${CurrencyUtils.formatAmount(uiState.totalExpectedAmount, "USD")}",
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Status Filter Chips
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                val isSelected = uiState.selectedStatus == null
                GaussianChip(
                    selected = isSelected,
                    label = "All Statuses",
                    onClick = { viewModel.filterByStatus(null) }
                )
            }
            items(OccurrenceStatus.values()) { status ->
                val isSelected = uiState.selectedStatus == status
                GaussianChip(
                    selected = isSelected,
                    label = status.displayName,
                    onClick = { viewModel.filterByStatus(status) }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (uiState.filteredItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No occurrences recorded yet.",
                    color = colors.textMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(uiState.filteredItems, key = { "${it.occurrence.id}_${it.occurrence.date}" }) { item ->
                    val pColor = try {
                        Color(android.graphics.Color.parseColor(item.participantColorHex))
                    } catch (_: Exception) {
                        colors.accent
                    }

                    GaussianCard(
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
                                        color = colors.textPrimary
                                    )
                                    val completedBy = item.occurrence.completedByMemberName?.let { " · by $it" }.orEmpty()
                                    Text(
                                        text = "${item.scheduleName} · ${DateUtils.formatDisplay(item.occurrence.date)}$completedBy",
                                        fontSize = 12.sp,
                                        color = colors.textMuted
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                val statusBg = when (item.occurrence.status) {
                                    OccurrenceStatus.COMPLETED -> colors.statusCompleted.copy(alpha = 0.2f)
                                    OccurrenceStatus.SKIPPED -> colors.statusSkipped.copy(alpha = 0.2f)
                                    else -> colors.statusPending.copy(alpha = 0.2f)
                                }
                                val statusFg = when (item.occurrence.status) {
                                    OccurrenceStatus.COMPLETED -> colors.statusCompleted
                                    OccurrenceStatus.SKIPPED -> colors.statusSkipped
                                    else -> colors.statusPending
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
                                        color = if (item.occurrence.status == OccurrenceStatus.COMPLETED) colors.statusCompleted else colors.textPrimary
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
fun GaussianStatColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.Black, fontSize = 18.sp, color = color)
        Text(text = label, fontSize = 11.sp, color = color.copy(alpha = 0.7f))
    }
}
