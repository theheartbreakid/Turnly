package com.crescentapps.turnly.presentation.m3expressive.screens.room

import android.widget.Toast
import androidx.compose.foundation.layout.*
import com.crescentapps.turnly.presentation.components.HorizontalDivider
import com.crescentapps.turnly.presentation.components.CircularProgressIndicator
import com.crescentapps.turnly.presentation.components.LinearProgressIndicator
import com.crescentapps.turnly.presentation.components.RadioButton
import com.crescentapps.turnly.presentation.components.Checkbox

import com.crescentapps.turnly.presentation.components.Text
import com.crescentapps.turnly.presentation.components.Icon
import com.crescentapps.turnly.presentation.theme.LocalTurnlyColors
import androidx.compose.ui.text.TextStyle

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.core.model.RoomRole
import androidx.compose.material3.*
import com.crescentapps.turnly.presentation.m3expressive.components.TurnlyExpressiveButton
import com.crescentapps.turnly.presentation.screens.room.RoomDetailViewModel
import com.crescentapps.turnly.presentation.catalog.utils.LocalBackdrop
import com.crescentapps.turnly.presentation.components.ConfirmDialog

@Composable
fun M3RoomSettingsScreen(
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
    val adaptiveColor = MaterialTheme.colorScheme.onBackground

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
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "Room Settings",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
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
                androidx.compose.material3.Card(
                    modifier = Modifier.fillMaxWidth(),
                    
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
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = room.name,
                            style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                            fontWeight = FontWeight.Bold,
                            color = adaptiveColor
                        )
                        if (room.description.isNotBlank()) {
                            Text(
                                text = room.description,
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
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
                androidx.compose.material3.Card(
                    modifier = Modifier.fillMaxWidth(),
                    
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
                            color = androidx.compose.material3.MaterialTheme.colorScheme.primary
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
                androidx.compose.material3.Card(
                    modifier = Modifier.fillMaxWidth(),
                    
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

                        TurnlyExpressiveButton(
                            onClick = { showLeaveConfirm = true },
                            text = "Leave Room",
                            icon = { Icon(Icons.Default.ExitToApp, contentDescription = null) },
                            isDestructive = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (isOwner) {
                            TurnlyExpressiveButton(
                                onClick = { showDeleteConfirm = true },
                                text = "Delete Room for Everyone",
                                icon = { Icon(Icons.Default.DeleteForever, contentDescription = null) },
                                isDestructive = true,
                                modifier = Modifier.fillMaxWidth()
                            )
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

