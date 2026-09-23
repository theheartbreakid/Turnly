package com.crescentapps.turnly.presentation.screens.room

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.core.model.RoomRole
import com.crescentapps.turnly.presentation.catalog.utils.LocalBackdrop
import com.crescentapps.turnly.presentation.components.ConfirmDialog
import com.crescentapps.turnly.presentation.components.liquid.LiquidButton
import com.crescentapps.turnly.presentation.components.liquid.LiquidCard
import com.crescentapps.turnly.presentation.components.liquid.LiquidIconButton
import com.crescentapps.turnly.presentation.theme.LiquidColors

@Composable
fun RoomSettingsScreen(
    viewModel: RoomDetailViewModel,
    onNavigateBack: () -> Unit,
    onRoomExited: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val backdrop = LocalBackdrop.current

    var showLeaveConfirm by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val room = uiState.room

    if (room == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val isOwner = room.role == RoomRole.OWNER

    val colors = com.crescentapps.turnly.presentation.theme.LocalTurnlyColors.current
    val adaptiveColor = com.crescentapps.turnly.presentation.components.LocalPrismalAdaptiveColor.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Standard Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LiquidIconButton(
                icon = Icons.Default.ArrowBack,
                onClick = onNavigateBack,
                backdrop = backdrop
            )

            Text(
                text = "Room Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = adaptiveColor
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {
            // General Info
            item {
                LiquidCard(
                    modifier = Modifier.fillMaxWidth(),
                    backdrop = backdrop
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "ROOM INFORMATION",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = colors.accent
                        )
                        Text(
                            text = room.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = adaptiveColor
                        )
                        if (room.description.isNotBlank()) {
                            Text(
                                text = room.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = adaptiveColor.copy(alpha = 0.7f)
                            )
                        }
                        Text(
                            text = "Room Code: ${room.roomCode}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = adaptiveColor
                        )
                        Text(
                            text = "Your Role: ${room.role.name}",
                            fontSize = 13.sp,
                            color = adaptiveColor.copy(alpha = 0.65f)
                        )
                    }
                }
            }

            // Shared Data Privacy Disclosure
            item {
                LiquidCard(
                    modifier = Modifier.fillMaxWidth(),
                    backdrop = backdrop
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "SHARED DATA DISCLOSURE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = colors.accent
                        )
                        Text(
                            text = "The following data is synchronized across members of this room over encrypted connections:",
                            fontSize = 13.sp,
                            color = adaptiveColor.copy(alpha = 0.7f)
                        )
                        Text("✓ Schedule names and rotation frequencies", fontSize = 13.sp, color = adaptiveColor)
                        Text("✓ Participant names and avatar selections", fontSize = 13.sp, color = adaptiveColor)
                        Text("✓ Scheduled turn assignments and dates", fontSize = 13.sp, color = adaptiveColor)
                        Text("✓ Completion timestamps and completing member names", fontSize = 13.sp, color = adaptiveColor)
                        Text("✓ Payment amounts and turn notes", fontSize = 13.sp, color = adaptiveColor)
                    }
                }
            }

            // Actions: Leave / Delete
            item {
                LiquidCard(
                    modifier = Modifier.fillMaxWidth(),
                    backdrop = backdrop
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "ROOM ACTIONS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = colors.statusMissed
                        )

                        LiquidButton(
                            onClick = { showLeaveConfirm = true },
                            backdrop = backdrop,
                            surfaceColor = colors.statusAttentionContainer,
                            tint = colors.statusAttention,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ExitToApp, null, modifier = Modifier.size(18.dp), tint = colors.statusAttention)
                            Spacer(Modifier.width(8.dp))
                            Text("Leave Room", color = colors.statusAttention, fontWeight = FontWeight.Bold)
                        }

                        if (isOwner) {
                            LiquidButton(
                                onClick = { showDeleteConfirm = true },
                                backdrop = backdrop,
                                surfaceColor = colors.statusMissedContainer,
                                tint = colors.statusMissed,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.DeleteForever, null, modifier = Modifier.size(18.dp), tint = colors.statusMissed)
                                Spacer(Modifier.width(8.dp))
                                Text("Delete Room for Everyone", color = colors.statusMissed, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLeaveConfirm) {
        ConfirmDialog(
            title = "Leave ${room.name}?",
            message = "You will no longer receive updates from this room. Your local copy of the schedules will be kept safely.",
            confirmText = "Leave Room",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                showLeaveConfirm = false
                viewModel.leaveRoom(keepLocalCopy = true, onComplete = onRoomExited)
            },
            onDismiss = { showLeaveConfirm = false }
        )
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete ${room.name}?",
            message = "This will permanently stop synchronization for all connected members. Local schedule copies will remain intact.",
            confirmText = "Delete Room",
            dismissText = "Cancel",
            isDestructive = true,
            onConfirm = {
                showDeleteConfirm = false
                viewModel.deleteRoom(keepLocalCopy = true, onComplete = onRoomExited)
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
