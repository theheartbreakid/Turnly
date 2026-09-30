package com.crescentapps.turnly.presentation.m3expressive.screens.room

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.material3.*
import com.crescentapps.turnly.presentation.m3expressive.components.TurnlyExpressiveButton
import com.crescentapps.turnly.presentation.screens.room.RoomViewModel
import com.crescentapps.turnly.presentation.catalog.utils.LocalBackdrop

@Composable
fun M3JoinRoomScreen(
    viewModel: RoomViewModel,
    initialRoomCode: String? = null,
    onNavigateBack: () -> Unit,
    onJoinedSuccessfully: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val backdrop = LocalBackdrop.current
    val colors = com.crescentapps.turnly.presentation.theme.LocalTurnlyColors.current
    val adaptiveColor = MaterialTheme.colorScheme.onBackground

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

        // Standard Top App Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = "Join a P2P Room",
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                fontWeight = FontWeight.Bold,
                color = adaptiveColor
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Confirmation Screen if Room Preview Data is Ready
        val preview = uiState.previewRoomData
        if (preview != null) {
            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth(),
                
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Join Room?",
                        style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.Bold),
                        fontWeight = FontWeight.Black,
                        color = adaptiveColor
                    )
                    Text(
                        text = preview.name,
                        style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                        fontWeight = FontWeight.Bold,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                    )
                    if (preview.description.isNotBlank()) {
                        Text(
                            text = preview.description,
                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
                            color = adaptiveColor.copy(alpha = 0.7f)
                        )
                    }

                    HorizontalDivider(color = colors.divider)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Room Owner:", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal), color = adaptiveColor.copy(alpha = 0.7f))
                        Text(preview.ownerDisplayName, style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold), fontWeight = FontWeight.Bold, color = adaptiveColor)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Members:", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal), color = adaptiveColor.copy(alpha = 0.7f))
                        Text("${preview.members.size}", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold), fontWeight = FontWeight.Bold, color = adaptiveColor)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Shared Schedules:", style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal), color = adaptiveColor.copy(alpha = 0.7f))
                        Text("${preview.sharedSchedules.size}", style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold), fontWeight = FontWeight.Bold, color = adaptiveColor)
                    }

                    if (preview.sharedSchedules.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            preview.sharedSchedules.forEach { s ->
                                Text(
                                    text = "• ${s.schedule.name} (${s.schedule.type.displayName})",
                                    fontSize = 13.sp,
                                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal),
                                    color = adaptiveColor.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = colors.divider)

                    OutlinedTextField(
                        value = userDisplayNameInput,
                        onValueChange = { userDisplayNameInput = it },
                        label = { Text("What's your name?") },
                        placeholder = { Text("e.g. Mohsin") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        TurnlyExpressiveButton(
                            text = "Cancel",
                            onClick = onNavigateBack,
                            isPrimary = false,
                            modifier = Modifier.weight(1f)
                        )

                        TurnlyExpressiveButton(
                            text = if (uiState.isJoining) "Joining..." else "Join Room",
                            onClick = {
                                viewModel.joinRoom(
                                    roomCode = preview.roomCode,
                                    displayName = userDisplayNameInput.ifBlank { "Member" },
                                    onSuccess = { room ->
                                        onJoinedSuccessfully(room.id)
                                    }
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        } else if (isScanningMode) {
            // Live Scanner View
            androidx.compose.material3.Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false),
                
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Scan Turnly QR Code",
                        style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                        fontWeight = FontWeight.Bold,
                        color = adaptiveColor
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(340.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        com.crescentapps.turnly.presentation.components.camera.QRScannerView(
                            onQRCodeScanned = { rawPayload, extractedCode ->
                                isScanningMode = false
                                roomCodeInput = extractedCode
                                viewModel.previewRoom(extractedCode) {}
                            },
                            onClose = { isScanningMode = false },
                            frameBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.primary
                        )
                    }

                    TurnlyExpressiveButton(
                        text = "Enter Code Manually",
                        onClick = { isScanningMode = false },
                        isPrimary = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            // MODE A: ENTER CODE MANUALLY
            androidx.compose.material3.Card(
                modifier = Modifier.fillMaxWidth(),
                
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Enter 6-Character Room Code",
                        style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
                        fontWeight = FontWeight.Bold,
                        color = adaptiveColor
                    )
                    Text(
                        text = "Ask the room owner for their 6-character room code or scan their QR code.",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
                        color = adaptiveColor.copy(alpha = 0.7f)
                    )

                    OutlinedTextField(
                        value = roomCodeInput,
                        onValueChange = { input ->
                            if (input.length <= 6) {
                                roomCodeInput = input.uppercase().filter { it.isLetterOrDigit() }
                            }
                        },
                        label = { Text("Room Code") },
                        placeholder = { Text("e.g. X7K9P2") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = colors.statusMissed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    TurnlyExpressiveButton(
                        text = if (uiState.isJoining) "Finding Room..." else "Find Room",
                        onClick = {
                            viewModel.previewRoom(roomCodeInput) {}
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = colors.divider)
                        Text(
                            text = "OR",
                            modifier = Modifier.padding(horizontal = 12.dp),
                            color = adaptiveColor.copy(alpha = 0.5f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = colors.divider)
                    }

                    // Scan QR Button
                    TurnlyExpressiveButton(
                        text = "Scan QR Code",
                        icon = { Icon(Icons.Default.QrCodeScanner, contentDescription = null) },
                        onClick = { isScanningMode = true },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

