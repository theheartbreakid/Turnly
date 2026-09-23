package com.crescentapps.turnly.presentation.m3.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.crescentapps.turnly.core.model.OccurrenceStatus
import com.crescentapps.turnly.core.util.CurrencyUtils
import com.crescentapps.turnly.core.util.DateUtils
import com.crescentapps.turnly.presentation.m3.components.M3ParticipantAvatar
import com.crescentapps.turnly.presentation.m3.components.M3StatusBadge
import com.crescentapps.turnly.presentation.screens.history.HistoryViewModel

/**
 * Pure Material 3 History Screen.
 * Displays statistics in standard M3 cards and filter chips for statuses.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3HistoryScreen(
    viewModel: HistoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("History & Records", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Summary Statistics Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        M3StatItem("Total", "${uiState.totalCount}")
                        M3StatItem("Completed", "${uiState.completedCount}")
                        M3StatItem("Skipped", "${uiState.skippedCount}")
                        M3StatItem("Missed", "${uiState.missedCount}")
                    }

                    if (uiState.totalRecordedAmount > 0.0 || uiState.totalExpectedAmount > 0.0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Recorded: ${CurrencyUtils.formatAmount(uiState.totalRecordedAmount, "USD")}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Expected: ${CurrencyUtils.formatAmount(uiState.totalExpectedAmount, "USD")}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Status Filter Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    val isSelected = uiState.selectedStatus == null
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.filterByStatus(null) },
                        label = { Text("All", fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
                items(OccurrenceStatus.values()) { status ->
                    val isSelected = uiState.selectedStatus == status
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.filterByStatus(status) },
                        label = { Text(status.displayName, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

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
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 96.dp)
                ) {
                    items(uiState.filteredItems, key = { "${it.occurrence.id}_${it.occurrence.date}" }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                            )
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
                                    M3ParticipantAvatar(
                                        participant = com.crescentapps.turnly.core.model.Participant(
                                            name = item.participantName,
                                            colorHex = item.participantColorHex
                                        ),
                                        size = 40.dp
                                    )

                                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text(
                                            text = item.participantName,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold
                                        )
                                        val completedBy = item.occurrence.completedByMemberName?.let { " · by $it" }.orEmpty()
                                        Text(
                                            text = "${item.scheduleName} · ${DateUtils.formatDisplay(item.occurrence.date)}$completedBy",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Column(
                                    horizontalAlignment = Alignment.End,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    M3StatusBadge(status = item.occurrence.status)
                                    val amt = item.occurrence.actualAmount ?: item.occurrence.expectedAmount
                                    if (amt != null) {
                                        Text(
                                            text = CurrencyUtils.formatAmount(amt, item.currencyCode),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = if (item.occurrence.status == OccurrenceStatus.COMPLETED) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
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
}

@Composable
private fun M3StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
