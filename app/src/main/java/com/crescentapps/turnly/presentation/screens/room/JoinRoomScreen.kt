package com.crescentapps.turnly.presentation.screens.room

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.crescentapps.turnly.presentation.catalog.utils.LocalBackdrop
import com.crescentapps.turnly.presentation.components.liquid.AdaptiveGlassTextField
import com.crescentapps.turnly.presentation.components.liquid.LiquidButton
import com.crescentapps.turnly.presentation.components.liquid.LiquidCard
import com.crescentapps.turnly.presentation.components.liquid.LiquidIconButton
import com.crescentapps.turnly.presentation.theme.LiquidColors

@Composable
fun JoinRoomScreen(
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
    val adaptiveColor = com.crescentapps.turnly.presentation.components.LocalPrismalAdaptiveColor.current

    var roomCodeInput by remember { mutableStateOf(initialRoomCode.orEmpty()) }
    var userDisplayNameInput by remember { mutableStateOf("") }
    var isScanningMode by remember { mutableStateOf(false) }
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (isGranted) {
            isScanningMode = true
        }
    }

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
            LiquidIconButton(
                icon = Icons.Default.ArrowBack,
                onClick = onNavigateBack,
                backdrop = backdrop
            )

            Text(
                text = "Join a P2P Room",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = adaptiveColor
            )

            Spacer(modifier = Modifier.size(48.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Confirmation Screen if Room Preview Data is Ready
        val preview = uiState.previewRoomData
        if (preview != null) {
            LiquidCard(
                modifier = Modifier.fillMaxWidth(),
                backdrop = backdrop
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Join Room?",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = adaptiveColor
                    )
                    Text(
                        text = preview.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = colors.accent
                    )
                    if (preview.description.isNotBlank()) {
                        Text(
                            text = preview.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = adaptiveColor.copy(alpha = 0.7f)
                        )
                    }

                    HorizontalDivider(color = colors.divider)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Room Owner:", style = MaterialTheme.typography.bodyMedium, color = adaptiveColor.copy(alpha = 0.7f))
                        Text(preview.ownerDisplayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = adaptiveColor)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Members:", style = MaterialTheme.typography.bodyMedium, color = adaptiveColor.copy(alpha = 0.7f))
                        Text("${preview.members.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = adaptiveColor)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Shared Schedules:", style = MaterialTheme.typography.bodyMedium, color = adaptiveColor.copy(alpha = 0.7f))
                        Text("${preview.sharedSchedules.size}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = adaptiveColor)
                    }

                    if (preview.sharedSchedules.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            preview.sharedSchedules.forEach { s ->
                                Text(
                                    text = "• ${s.schedule.name} (${s.schedule.type.displayName})",
                                    fontSize = 13.sp,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = adaptiveColor.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }

                    HorizontalDivider(color = colors.divider)

                    AdaptiveGlassTextField(
                        value = userDisplayNameInput,
                        onValueChange = { userDisplayNameInput = it },
                        label = "What's your name?",
                        placeholder = "e.g. Mohsin",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        LiquidButton(
                            text = "Cancel",
                            onClick = onNavigateBack,
                            backdrop = backdrop,
                            modifier = Modifier.weight(1f)
                        )

                        LiquidButton(
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
                            enabled = !uiState.isJoining,
                            backdrop = backdrop,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        } else if (isScanningMode) {
            // Live Scanner View
            LiquidCard(
                modifier = Modifier.fillMaxWidth(),
                backdrop = backdrop
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        text = "Scan Turnly QR Code",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = adaptiveColor
                    )

                    // Responsive Scanner Viewport Frame
                    Box(
                        modifier = Modifier
                            .sizeIn(minWidth = 200.dp, maxWidth = 260.dp, minHeight = 200.dp, maxHeight = 260.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.Black.copy(alpha = 0.75f))
                            .border(2.dp, colors.accent, RoundedCornerShape(24.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = colors.accent,
                                modifier = Modifier.size(56.dp)
                            )
                            Text(
                                text = "Point camera at QR code",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    LiquidButton(
                        text = "Enter Code Manually",
                        onClick = { isScanningMode = false },
                        backdrop = backdrop
                    )
                }
            }
        } else {
            // MODE A: ENTER CODE MANUALLY
            LiquidCard(
                modifier = Modifier.fillMaxWidth(),
                backdrop = backdrop
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Enter 6-Character Room Code",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = adaptiveColor
                    )
                    Text(
                        text = "Ask the room owner for their 6-character room code or scan their QR code.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = adaptiveColor.copy(alpha = 0.7f)
                    )

                    AdaptiveGlassTextField(
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

                    if (uiState.errorMessage != null) {
                        Text(
                            text = uiState.errorMessage!!,
                            color = colors.statusMissed,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    LiquidButton(
                        text = if (uiState.isJoining) "Finding Room..." else "Find Room",
                        onClick = {
                            viewModel.previewRoom(roomCodeInput) {}
                        },
                        enabled = roomCodeInput.length == 6 && !uiState.isJoining,
                        backdrop = backdrop,
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
                    LiquidButton(
                        text = "Scan QR Code",
                        icon = Icons.Default.QrCodeScanner,
                        onClick = {
                            if (hasCameraPermission) {
                                isScanningMode = true
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        backdrop = backdrop,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
