package com.crescentapps.turnly.presentation.gaussianblur.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.Composable
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
import com.crescentapps.turnly.presentation.components.EmptyTurnState
import com.crescentapps.turnly.presentation.components.Icon
import com.crescentapps.turnly.presentation.components.Text
import com.crescentapps.turnly.presentation.gaussianblur.components.GaussianCard
import com.crescentapps.turnly.presentation.gaussianblur.components.GaussianIconButton
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianBlurColors

@Composable
fun GaussianScheduleListScreen(
    schedules: List<Schedule>,
    onCreateSchedule: () -> Unit,
    onScheduleClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = LocalGaussianBlurColors.current

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
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = colors.textPrimary
            )

            GaussianIconButton(
                onClick = onCreateSchedule,
                size = 42.dp
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Schedule", tint = colors.accent)
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
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(schedules, key = { it.id }) { schedule ->
                    GaussianCard(
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
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "${schedule.frequencyType.displayName} · ${schedule.type.displayName}",
                                        fontSize = 13.sp,
                                        color = colors.textMuted
                                    )
                                }
                            }

                            if (schedule.type == ScheduleType.MONEY && schedule.defaultAmount != null) {
                                Text(
                                    text = CurrencyUtils.formatAmount(schedule.defaultAmount, schedule.currencyCode),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = colors.accent
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
