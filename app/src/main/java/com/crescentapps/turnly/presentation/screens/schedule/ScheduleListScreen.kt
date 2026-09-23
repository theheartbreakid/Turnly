package com.crescentapps.turnly.presentation.screens.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.core.model.Schedule
import com.crescentapps.turnly.core.model.ScheduleType
import com.crescentapps.turnly.core.util.CurrencyUtils
import com.crescentapps.turnly.presentation.catalog.utils.LocalBackdrop
import com.crescentapps.turnly.presentation.components.EmptyTurnState
import com.crescentapps.turnly.presentation.components.LocalPrismalAdaptiveColor
import com.crescentapps.turnly.presentation.components.liquid.LiquidCard
import com.crescentapps.turnly.presentation.components.liquid.LiquidIconButton
import com.crescentapps.turnly.presentation.theme.LiquidColors
import com.kyant.backdrop.backdrops.rememberLayerBackdrop

@Composable
fun ScheduleListScreen(
    schedules: List<Schedule>,
    onCreateSchedule: () -> Unit,
    onScheduleClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val backdrop = LocalBackdrop.current ?: rememberLayerBackdrop()
    val adaptiveColor = LocalPrismalAdaptiveColor.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Turn Schedules",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = adaptiveColor
            )

            LiquidIconButton(
                onClick = onCreateSchedule,
                backdrop = backdrop,
                iconSize = 42.dp,
                surfaceColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Schedule", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (schedules.isEmpty()) {
            EmptyTurnState(
                title = "No turn schedules",
                description = "Set up turn schedules for shared money, chores, car use, care, or team duties.",
                onAction = onCreateSchedule
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(bottom = 110.dp)
            ) {
                items(schedules, key = { it.id }) { schedule ->
                    LiquidCard(
                        backdrop = backdrop,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        onClick = { onScheduleClick(schedule.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val typeColor = when (schedule.type) {
                                    ScheduleType.MONEY -> Color(0xFF10B981)
                                    ScheduleType.TASK -> Color(0xFF3B82F6)
                                    ScheduleType.RESPONSIBILITY -> Color(0xFF8B5CF6)
                                    ScheduleType.GENERAL -> Color(0xFFF59E0B)
                                    ScheduleType.CUSTOM -> Color(0xFFEC4899)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(typeColor)
                                )

                                Column {
                                    Text(
                                        text = schedule.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 17.sp,
                                        color = adaptiveColor
                                    )
                                    Text(
                                        text = " � ",
                                        fontSize = 13.sp,
                                        color = adaptiveColor.copy(alpha = 0.65f)
                                    )
                                }
                            }

                            if (schedule.type == ScheduleType.MONEY && schedule.defaultAmount != null) {
                                Text(
                                    text = CurrencyUtils.formatAmount(schedule.defaultAmount, schedule.currencyCode),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
