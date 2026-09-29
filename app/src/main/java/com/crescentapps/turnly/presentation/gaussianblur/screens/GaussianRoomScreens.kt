package com.crescentapps.turnly.presentation.gaussianblur.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.crescentapps.turnly.core.model.*
import com.crescentapps.turnly.core.util.DateUtils
import com.crescentapps.turnly.core.util.QRCodeUtils
import com.crescentapps.turnly.presentation.components.Checkbox
import com.crescentapps.turnly.presentation.components.HorizontalDivider
import com.crescentapps.turnly.presentation.components.Icon
import com.crescentapps.turnly.presentation.components.Text
import com.crescentapps.turnly.presentation.components.TurnlyRoleBadge
import com.crescentapps.turnly.presentation.components.TurnlyRoomStatusBadge
import com.crescentapps.turnly.presentation.components.camera.QRScannerView
import com.crescentapps.turnly.presentation.gaussianblur.components.*
import com.crescentapps.turnly.presentation.gaussianblur.theme.LocalGaussianBlurColors
import com.crescentapps.turnly.presentation.screens.room.RoomDetailViewModel
import com.crescentapps.turnly.presentation.screens.room.RoomViewModel

@Composable
fun GaussianRoomListScreen(
    viewModel: RoomViewModel,
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
    onRoomClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalGaussianBlurColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Shared Schedules",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.textPrimary
                )
                Text(
                    text = "Keep in sync with family, roommates, or team members",
                    fontSize = 14.sp,
                    color = colors.textMuted
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                GaussianButton(
                    onClick = onJoinRoom,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("Join", fontSize = 12.sp, color = colors.textPrimary)
                }
                GaussianButton(
                    onClick = onCreateRoom,
                    accentTint = colors.accent,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = colors.textOnAccent, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Share", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = colors.textOnAccent)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (uiState.activeConflicts.isNotEmpty()) {
            val conflict = uiState.activeConflicts.first()
            GaussianCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = colors.statusMissed)
                        Text(text = "Turn Update Discrepancy", fontWeight = FontWeight.Bold, color = colors.statusMissed, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Both you and another member updated \"${conflict.scheduleName}\" for ${DateUtils.formatDisplay(conflict.date)}. Which update should we keep?",
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        GaussianButton(
                            onClick = { viewModel.resolveConflict(conflict.id, keepRemote = true) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Keep Their Update", fontSize = 12.sp, color = colors.textPrimary)
                        }
                        GaussianButton(
                            onClick = { viewModel.resolveConflict(conflict.id, keepRemote = false) },
                            modifier = Modifier.weight(1f),
                            accentTint = colors.accent
                        ) {
                            Text("Keep My Update", fontSize = 12.sp, color = colors.textOnAccent)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (uiState.rooms.isEmpty()) {
            GaussianCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(colors.accent.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, tint = colors.accent, modifier = Modifier.size(32.dp))
                    }
                    Text(
                        text = "No P2P Rooms Connected",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Create a room to synchronize selected schedules across multiple phones using QR or room codes without an account.",
                        fontSize = 14.sp,
                        color = colors.textMuted,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        GaussianButton(onClick = onCreateRoom, accentTint = colors.accent) {
                            Icon(Icons.Default.Add, null, tint = colors.textOnAccent, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Create Room", fontWeight = FontWeight.Bold, color = colors.textOnAccent)
                        }
                        GaussianButton(onClick = onJoinRoom) {
                            Text("Join Room", color = colors.textPrimary)
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                items(uiState.rooms, key = { it.id }) { room ->
                    GaussianCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onRoomClick(room.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = room.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Code: ${room.roomCode} · ${room.role.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                    fontSize = 12.sp,
                                    color = colors.textMuted
                                )
                            }
                            val syncText = when (room.syncState) {
                                SyncState.SYNCED -> "✓ Synced"
                                SyncState.SYNCING -> "↻ Syncing"
                                SyncState.OFFLINE -> "⚠ Offline"
                                SyncState.CONFLICT -> "! Conflict"
                            }
                            GaussianChip(
                                selected = room.syncState == SyncState.SYNCED,
                                label = syncText,
                                onClick = { onRoomClick(room.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GaussianCreateRoomScreen(
    viewModel: RoomViewModel,
    onNavigateBack: () -> Unit,
    onRoomCreated: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val colors = LocalGaussianBlurColors.current

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

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GaussianIconButton(
                onClick = {
                    if (createdRoom != null) {
                        onRoomCreated(createdRoom!!.id)
                    } else if (currentStep > 1) {
                        currentStep--
                    } else {
                        onNavigateBack()
                    }
                }
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
            }

            Text(
                text = if (createdRoom != null) "Room Created" else "Step $currentStep of 2",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.size(44.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (createdRoom != null) {
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
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Invite members by sharing the room code or QR code.",
                        fontSize = 14.sp,
                        color = colors.textMuted
                    )
                }

                item {
                    GaussianCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = room.name,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )

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

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(colors.accent.copy(alpha = 0.15f))
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = room.roomCode,
                                    fontSize = 26.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 4.sp,
                                    color = colors.accent
                                )
                                GaussianIconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Turnly Room Code", room.roomCode))
                                        Toast.makeText(context, "Room code copied", Toast.LENGTH_SHORT).show()
                                    },
                                    size = 38.dp
                                ) {
                                    Icon(Icons.Default.ContentCopy, null, tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                GaussianButton(
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
                                    accentTint = colors.accent,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Share, null, tint = colors.textOnAccent, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text("Share Invite", color = colors.textOnAccent, fontWeight = FontWeight.Bold)
                                }

                                GaussianButton(
                                    onClick = { onRoomCreated(room.id) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Done", color = colors.textPrimary)
                                }
                            }
                        }
                    }
                }
            }
        } else if (currentStep == 1) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Name your P2P Room",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Create a room for your household, roommates, or team.",
                        fontSize = 14.sp,
                        color = colors.textMuted
                    )

                    GaussianTextField(
                        value = roomName,
                        onValueChange = { roomName = it },
                        label = "Room Name",
                        placeholder = "e.g. Family Turns",
                        modifier = Modifier.fillMaxWidth()
                    )

                    GaussianTextField(
                        value = roomDescription,
                        onValueChange = { roomDescription = it },
                        label = "Description (Optional)",
                        placeholder = "e.g. Shared turns for chores and savings",
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = false
                    )
                }

                GaussianButton(
                    onClick = { currentStep = 2 },
                    enabled = roomName.isNotBlank(),
                    accentTint = colors.accent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp)
                ) {
                    Text("Next: Select Schedules", fontWeight = FontWeight.Bold, color = colors.textOnAccent)
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Choose Schedules to Share",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Only selected schedules will synchronize. Unselected schedules remain private.",
                        fontSize = 14.sp,
                        color = colors.textMuted
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (uiState.availableSchedules.isEmpty()) {
                        Text(
                            text = "No local schedules found. You can still create the room now.",
                            fontSize = 14.sp,
                            color = colors.textMuted
                        )
                    } else {
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(uiState.availableSchedules, key = { it.id }) { schedule ->
                                val isChecked = selectedScheduleIds.contains(schedule.id)
                                GaussianCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = {
                                        selectedScheduleIds = if (isChecked) selectedScheduleIds - schedule.id else selectedScheduleIds + schedule.id
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
                                                color = colors.textPrimary
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = schedule.type.displayName,
                                                fontSize = 12.sp,
                                                color = colors.textMuted
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

                GaussianButton(
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
                    accentTint = colors.accent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp, top = 16.dp)
                ) {
                    Text(
                        text = if (uiState.isCreating) "Creating Room..." else "Create P2P Room",
                        fontWeight = FontWeight.Bold,
                        color = colors.textOnAccent
                    )
                }
            }
        }
    }
}

@Composable
fun GaussianJoinRoomScreen(
    viewModel: RoomViewModel,
    initialRoomCode: String? = null,
    onNavigateBack: () -> Unit,
    onJoinedSuccessfully: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val colors = LocalGaussianBlurColors.current

    var roomCodeInput by remember { mutableStateOf(initialRoomCode.orEmpty()) }
    var userDisplayNameInput by remember { mutableStateOf("") }
    var isScanningMode by remember { mutableStateOf(false) }

    LaunchedEffect(initialRoomCode) {
        if (!initialRoomCode.isNullOrBlank()) {
            viewModel.previewRoom(initialRoomCode) {}
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GaussianIconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
            }

            Text(
                text = "Join a P2P Room",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.size(44.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        val preview = uiState.previewRoomData
        if (preview != null) {
            GaussianCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Join Room?",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary
                    )
                    Text(
                        text = preview.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )

                    HorizontalDivider(color = colors.divider)

                    GaussianTextField(
                        value = userDisplayNameInput,
                        onValueChange = { userDisplayNameInput = it },
                        label = "What's your name?",
                        placeholder = "e.g. Alex",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        GaussianButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = colors.textPrimary)
                        }

                        GaussianButton(
                            onClick = {
                                viewModel.joinRoom(
                                    roomCode = preview.roomCode,
                                    displayName = userDisplayNameInput.ifBlank { "Member" },
                                    onSuccess = { room -> onJoinedSuccessfully(room.id) }
                                )
                            },
                            enabled = !uiState.isJoining,
                            accentTint = colors.accent,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Join Room", fontWeight = FontWeight.Bold, color = colors.textOnAccent)
                        }
                    }
                }
            }
        } else if (isScanningMode) {
            GaussianCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Scan Turnly QR Code", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        QRScannerView(
                            onQRCodeScanned = { rawPayload, extractedCode ->
                                isScanningMode = false
                                roomCodeInput = extractedCode
                                viewModel.previewRoom(extractedCode) {}
                            },
                            onClose = { isScanningMode = false },
                            frameBorderColor = colors.accent
                        )
                    }

                    GaussianButton(
                        onClick = { isScanningMode = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Enter Code Manually", color = colors.textPrimary)
                    }
                }
            }
        } else {
            GaussianCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("Enter 6-Character Room Code", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)

                    GaussianTextField(
                        value = roomCodeInput,
                        onValueChange = { input ->
                            if (input.length <= 6) {
                                roomCodeInput = input.uppercase().filter { it.isLetterOrDigit() }
                            }
                        },
                        label = "Room Code",
                        placeholder = "e.g. X7K9P2",
                        modifier = Modifier.fillMaxWidth()
                    )

                    GaussianButton(
                        onClick = { viewModel.previewRoom(roomCodeInput) {} },
                        enabled = roomCodeInput.length == 6 && !uiState.isJoining,
                        accentTint = colors.accent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (uiState.isJoining) "Finding Room..." else "Find Room", fontWeight = FontWeight.Bold, color = colors.textOnAccent)
                    }

                    GaussianButton(
                        onClick = { isScanningMode = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.QrCodeScanner, null, tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Scan QR Code", color = colors.textPrimary)
                    }
                }
            }
        }
    }
}

@Composable
fun GaussianRoomDetailScreen(
    viewModel: RoomDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: (Long) -> Unit,
    onScheduleClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val colors = LocalGaussianBlurColors.current
    var showInviteDialog by remember { mutableStateOf(false) }

    val room = uiState.room ?: return

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GaussianIconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
            }

            Text(
                text = room.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )

            GaussianIconButton(onClick = { onNavigateToSettings(room.id) }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings", tint = colors.textPrimary)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 120.dp)
        ) {
            item {
                GaussianCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TurnlyRoomStatusBadge(syncState = room.syncState)
                                TurnlyRoleBadge(role = room.role)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Code: ${room.roomCode}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = colors.textSecondary
                            )
                        }

                        GaussianButton(
                            onClick = {
                                viewModel.syncNow(context)
                                Toast.makeText(context, "Synchronizing...", Toast.LENGTH_SHORT).show()
                            },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Sync, null, tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Sync", fontSize = 12.sp, color = colors.textPrimary)
                        }
                    }
                }
            }

            item {
                GaussianButton(
                    onClick = { showInviteDialog = true },
                    accentTint = colors.accent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.PersonAdd, null, tint = colors.textOnAccent, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Invite Member (QR & Code)", fontWeight = FontWeight.Bold, color = colors.textOnAccent)
                }
            }

            item {
                Text(
                    text = "CONNECTED MEMBERS (${uiState.members.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = colors.accent
                )
                Spacer(modifier = Modifier.height(8.dp))
                GaussianCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        uiState.members.forEach { member ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (member.presence == MemberPresence.ONLINE) colors.statusCompleted else colors.statusSkipped
                                            )
                                    )
                                    Text(
                                        text = member.displayName,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 15.sp,
                                        color = colors.textPrimary
                                    )
                                }

                                Text(
                                    text = if (member.presence == MemberPresence.ONLINE) "Online" else "Offline",
                                    fontSize = 12.sp,
                                    color = colors.textMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showInviteDialog) {
        val deepLink = QRCodeUtils.createInviteDeepLink(
            RoomInvitePayload(
                roomId = room.remoteRoomId,
                roomCode = room.roomCode,
                roomName = room.name,
                ownerName = room.ownerDisplayName,
                inviteToken = "token_${room.roomCode}"
            )
        )
        val qrBmp = remember(room.roomCode) {
            QRCodeUtils.generateQRCodeBitmap(deepLink, 400, 400)
        }

        GaussianDialog(
            title = "Invite to ${room.name}",
            onDismissRequest = { showInviteDialog = false },
            positiveText = "Close",
            negativeText = null,
            onPositive = { showInviteDialog = false }
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    bitmap = qrBmp.asImageBitmap(),
                    contentDescription = "Invite QR Code",
                    modifier = Modifier
                        .sizeIn(minWidth = 160.dp, maxWidth = 200.dp, minHeight = 160.dp, maxHeight = 200.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.White)
                        .padding(10.dp)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(colors.accent.copy(alpha = 0.15f))
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = room.roomCode,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 3.sp,
                        color = colors.accent
                    )
                    GaussianIconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Turnly Room Code", room.roomCode))
                            Toast.makeText(context, "Room code copied", Toast.LENGTH_SHORT).show()
                        },
                        size = 36.dp
                    ) {
                        Icon(Icons.Default.ContentCopy, null, tint = colors.textPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}
