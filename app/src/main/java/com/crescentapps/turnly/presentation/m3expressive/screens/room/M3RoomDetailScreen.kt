package com.crescentapps.turnly.presentation.m3expressive.screens.room

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crescentapps.turnly.core.model.*
import com.crescentapps.turnly.core.util.QRCodeUtils
import com.crescentapps.turnly.data.repository.RoomRepository
import com.crescentapps.turnly.data.repository.TurnlyRepository
import com.crescentapps.turnly.data.sync.SyncWorker
import androidx.compose.material3.*
import com.crescentapps.turnly.presentation.m3expressive.components.TurnlyExpressiveButton
import com.crescentapps.turnly.presentation.screens.room.RoomDetailViewModel
import com.crescentapps.turnly.presentation.catalog.utils.LocalBackdrop
import com.crescentapps.turnly.presentation.components.TurnlyRoleBadge
import com.crescentapps.turnly.presentation.components.TurnlyRoomStatusBadge
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch



@Composable
fun M3RoomDetailScreen(
    viewModel: RoomDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSettings: (Long) -> Unit,
    onScheduleClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val backdrop = LocalBackdrop.current
    var showInviteDialog by remember { mutableStateOf(false) }

    val room = uiState.room

    if (room == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val colors = com.crescentapps.turnly.presentation.theme.LocalTurnlyColors.current
    val adaptiveColor = MaterialTheme.colorScheme.onBackground

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }

            Text(
                text = room.name,
                style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                fontWeight = FontWeight.Bold,
                color = adaptiveColor
            )

            IconButton(onClick = { onNavigateToSettings(room.id) }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Status Card
            item {
                androidx.compose.material3.Card(
                    modifier = Modifier.fillMaxWidth(),
                    
                ) {
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
                                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
                                color = adaptiveColor.copy(alpha = 0.75f)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        TurnlyExpressiveButton(
                            text = "Sync",
                            icon = { Icon(Icons.Default.Sync, contentDescription = null) },
                            onClick = {
                                viewModel.syncNow(context)
                                Toast.makeText(context, "Synchronizing...", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }

            // Action: Invite Member
            item {
                TurnlyExpressiveButton(
                    text = "Invite Member (QR & Code)",
                    icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                    onClick = { showInviteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Connected Members
            item {
                Text(
                    text = "CONNECTED MEMBERS (${uiState.members.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                androidx.compose.material3.Card(
                    modifier = Modifier.fillMaxWidth(),
                    
                ) {
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
                                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                                        fontSize = 15.sp,
                                        color = adaptiveColor
                                    )
                                    if (member.role == RoomRole.OWNER) {
                                        AssistChip(
                                            onClick = {},
                                            label = { Text("Owner") }
                                        )
                                    }
                                }

                                Text(
                                    text = if (member.presence == MemberPresence.ONLINE) "Online" else "Offline",
                                    fontSize = 12.sp,
                                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
                                    color = adaptiveColor.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
            }

            // Shared Schedules
            item {
                Text(
                    text = "SHARED SCHEDULES (${uiState.sharedSchedules.size})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState.sharedSchedules.isEmpty()) {
                    Text(
                        text = "No schedules shared in this room yet.",
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
                        color = adaptiveColor.copy(alpha = 0.6f)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        uiState.sharedSchedules.forEach { sInfo ->
                            val localSched = uiState.localSchedulesMap[sInfo.localScheduleId]
                            androidx.compose.material3.Card(
                                modifier = Modifier.fillMaxWidth(),
                                
                                onClick = { localSched?.let { onScheduleClick(it.id) } }
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
                                            text = localSched?.name ?: "Shared Schedule",
                                            fontWeight = FontWeight.Bold,
                                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.SemiBold),
                                            fontSize = 15.sp,
                                            color = adaptiveColor
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = localSched?.type?.displayName ?: "Rotation",
                                            fontSize = 12.sp,
                                            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
                                            color = adaptiveColor.copy(alpha = 0.65f)
                                        )
                                    }
                                    AssistChip(
                                        onClick = { localSched?.let { onScheduleClick(it.id) } },
                                        label = { Text("☁ Shared") }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Invite Modal Dialog (Clean layout with no duplicate action buttons)
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

        AlertDialog(
            onDismissRequest = { showInviteDialog = false },
            title = { Text("Invite to ${room.name}") },
            text = {
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = room.roomCode,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Turnly Room Code", room.roomCode))
                                Toast.makeText(context, "Room code copied", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy Code")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
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
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Share Invite")
                        }
                        Button(
                            onClick = { showInviteDialog = false },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Close")
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }
}

