package com.crescentapps.turnly.presentation.screens.room

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.core.model.P2PRoom
import com.crescentapps.turnly.core.model.RoomInvitePayload
import com.crescentapps.turnly.core.model.Schedule
import com.crescentapps.turnly.core.util.QRCodeUtils
import com.crescentapps.turnly.presentation.catalog.utils.LocalBackdrop
import com.crescentapps.turnly.presentation.components.liquid.AdaptiveGlassTextField
import com.crescentapps.turnly.presentation.components.liquid.LiquidButton
import com.crescentapps.turnly.presentation.components.liquid.LiquidCard
import com.crescentapps.turnly.presentation.components.liquid.LiquidIconButton
import com.crescentapps.turnly.presentation.theme.LiquidColors

@Composable
fun CreateRoomScreen(
    viewModel: RoomViewModel,
    onNavigateBack: () -> Unit,
    onRoomCreated: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val backdrop = LocalBackdrop.current
    val colors = com.crescentapps.turnly.presentation.theme.LocalTurnlyColors.current
    val adaptiveColor = com.crescentapps.turnly.presentation.components.LocalPrismalAdaptiveColor.current

    var currentStep by remember { mutableStateOf(1) }
    var roomName by remember { mutableStateOf("") }
    var roomDescription by remember { mutableStateOf("") }
    var selectedScheduleIds by remember { mutableStateOf(setOf<Long>()) }

    var createdRoom by remember { mutableStateOf<P2PRoom?>(null) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Standard Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            LiquidIconButton(
                icon = Icons.Default.ArrowBack,
                onClick = {
                    if (createdRoom != null) {
                        onRoomCreated(createdRoom!!.id)
                    } else if (currentStep > 1) {
                        currentStep--
                    } else {
                        onNavigateBack()
                    }
                },
                backdrop = backdrop
            )

            Text(
                text = if (createdRoom != null) "Room Created" else "Step $currentStep of 2",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = adaptiveColor
            )

            // Equal spacing anchor
            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (createdRoom != null) {
            // STEP 3: SUCCESS & INVITE
            val room = createdRoom!!
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {
                item {
                    Text(
                        text = "Room Ready!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = adaptiveColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Invite members by sharing the room code or QR code.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = adaptiveColor.copy(alpha = 0.7f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }

                item {
                    LiquidCard(
                        modifier = Modifier.fillMaxWidth(),
                        backdrop = backdrop
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = room.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = adaptiveColor
                            )

                            // High-contrast QR Code with responsive clamping
                            qrBitmap?.let { bmp ->
                                Image(
                                    bitmap = bmp.asImageBitmap(),
                                    contentDescription = "Room QR Code",
                                    modifier = Modifier
                                        .sizeIn(minWidth = 160.dp, maxWidth = 220.dp, minHeight = 160.dp, maxHeight = 220.dp)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White)
                                        .padding(12.dp)
                                )
                            }

                            // Room Code chip with copy button
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.accent.copy(alpha = 0.1f))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = room.roomCode,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 4.sp,
                                    color = colors.accent
                                )
                                LiquidIconButton(
                                    icon = Icons.Default.ContentCopy,
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Turnly Room Code", room.roomCode))
                                        Toast.makeText(context, "Room code copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    backdrop = backdrop,
                                    size = 38.dp
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                LiquidButton(
                                    text = "Share Invite",
                                    icon = Icons.Default.Share,
                                    onClick = {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "Join my Turnly room: ${room.name}\nRoom Code: ${room.roomCode}\nOpen Turnly and choose Join Room."
                                            )
                                            type = "text/plain"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share Room Invitation"))
                                    },
                                    backdrop = backdrop,
                                    modifier = Modifier.weight(1f)
                                )

                                LiquidButton(
                                    text = "Done",
                                    onClick = { onRoomCreated(room.id) },
                                    backdrop = backdrop,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        } else if (currentStep == 1) {
            // STEP 1: ROOM NAME & DESCRIPTION
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Name your P2P Room",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = adaptiveColor
                    )
                    Text(
                        text = "Create a room for your household, roommates, or team (e.g. 'Family Turns').",
                        style = MaterialTheme.typography.bodyMedium,
                        color = adaptiveColor.copy(alpha = 0.7f)
                    )

                    AdaptiveGlassTextField(
                        value = roomName,
                        onValueChange = { roomName = it },
                        label = "Room Name",
                        placeholder = "e.g. Family Turns",
                        modifier = Modifier.fillMaxWidth()
                    )

                    AdaptiveGlassTextField(
                        value = roomDescription,
                        onValueChange = { roomDescription = it },
                        label = "Description (Optional)",
                        placeholder = "e.g. Shared turns for chores and savings",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false
                    )
                }

                LiquidButton(
                    text = "Next: Select Schedules",
                    onClick = { currentStep = 2 },
                    enabled = roomName.isNotBlank(),
                    backdrop = backdrop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                )
            }
        } else {
            // STEP 2: SELECT SCHEDULES TO SHARE
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Choose Schedules to Share",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = adaptiveColor
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Only selected schedules will synchronize. Unselected schedules remain 100% private to your device.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = adaptiveColor.copy(alpha = 0.7f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Privacy Note
                    LiquidCard(
                        modifier = Modifier.fillMaxWidth(),
                        backdrop = backdrop
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "ⓘ Shared schedules synchronize participant names, turn assignments, completion status, notes, and payment amounts with connected room members.",
                                fontSize = 12.sp,
                                style = MaterialTheme.typography.bodySmall,
                                color = adaptiveColor.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.availableSchedules.isEmpty()) {
                        Text(
                            text = "No local schedules found. You can still create the room now and share schedules later.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = adaptiveColor.copy(alpha = 0.7f)
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(uiState.availableSchedules, key = { it.id }) { schedule ->
                                val isChecked = selectedScheduleIds.contains(schedule.id)
                                LiquidCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    backdrop = backdrop,
                                    onClick = {
                                        selectedScheduleIds = if (isChecked) {
                                            selectedScheduleIds - schedule.id
                                        } else {
                                            selectedScheduleIds + schedule.id
                                        }
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = schedule.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp,
                                                style = MaterialTheme.typography.titleMedium,
                                                color = adaptiveColor
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = schedule.type.displayName,
                                                fontSize = 12.sp,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = adaptiveColor.copy(alpha = 0.65f)
                                            )
                                        }
                                        Checkbox(
                                            checked = isChecked,
                                            onCheckedChange = { checked ->
                                                selectedScheduleIds = if (checked) selectedScheduleIds + schedule.id else selectedScheduleIds - schedule.id
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                LiquidButton(
                    text = if (uiState.isCreating) "Creating Room..." else "Create P2P Room",
                    onClick = {
                        viewModel.createRoom(
                            name = roomName,
                            description = roomDescription,
                            selectedScheduleIds = selectedScheduleIds.toList(),
                            onSuccess = { room ->
                                createdRoom = room
                                val deepLink = QRCodeUtils.createInviteDeepLink(
                                    RoomInvitePayload(
                                        roomId = room.remoteRoomId,
                                        roomCode = room.roomCode,
                                        roomName = room.name,
                                        ownerName = room.ownerDisplayName,
                                        inviteToken = "token_${room.roomCode}"
                                    )
                                )
                                qrBitmap = QRCodeUtils.generateQRCodeBitmap(deepLink, 512, 512)
                            }
                        )
                    },
                    enabled = !uiState.isCreating,
                    backdrop = backdrop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, top = 16.dp)
                )
            }
        }
    }
}
