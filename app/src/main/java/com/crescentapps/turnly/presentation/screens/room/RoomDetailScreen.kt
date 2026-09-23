package com.crescentapps.turnly.presentation.screens.room

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crescentapps.turnly.core.model.*
import com.crescentapps.turnly.core.util.QRCodeUtils
import com.crescentapps.turnly.data.repository.RoomRepository
import com.crescentapps.turnly.data.repository.TurnlyRepository
import com.crescentapps.turnly.data.sync.SyncWorker
import com.crescentapps.turnly.presentation.catalog.utils.LocalBackdrop
import com.crescentapps.turnly.presentation.components.TurnlyRoleBadge
import com.crescentapps.turnly.presentation.components.TurnlyRoomStatusBadge
import com.crescentapps.turnly.presentation.components.liquid.LiquidButton
import com.crescentapps.turnly.presentation.components.liquid.LiquidCard
import com.crescentapps.turnly.presentation.components.liquid.LiquidChip
import com.crescentapps.turnly.presentation.components.liquid.LiquidDialog
import com.crescentapps.turnly.presentation.components.liquid.LiquidIconButton
import com.crescentapps.turnly.presentation.theme.LiquidColors
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RoomDetailUiState(
    val room: P2PRoom? = null,
    val members: List<RoomMember> = emptyList(),
    val sharedSchedules: List<SharedScheduleInfo> = emptyList(),
    val localSchedulesMap: Map<Long, Schedule> = emptyMap(),
    val isLoading: Boolean = true
)

class RoomDetailViewModel(
    private val roomRepository: RoomRepository,
    private val turnlyRepository: TurnlyRepository,
    private val roomId: Long
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomDetailUiState())
    val uiState: StateFlow<RoomDetailUiState> = _uiState.asStateFlow()

    init {
        loadRoomData()
    }

    private fun loadRoomData() {
        viewModelScope.launch {
            combine(
                roomRepository.getRoomFlow(roomId),
                roomRepository.getMembersFlow(roomId),
                roomRepository.getSharedSchedulesFlow(roomId),
                turnlyRepository.allSchedules
            ) { room, members, sharedSchedules, allSchedules ->
                val map = allSchedules.associateBy { it.id }
                RoomDetailUiState(
                    room = room,
                    members = members,
                    sharedSchedules = sharedSchedules,
                    localSchedulesMap = map,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun syncNow(context: Context) {
        viewModelScope.launch {
            SyncWorker.triggerImmediateSync(context)
            roomRepository.tryDispatchPendingOperations()
        }
    }

    fun leaveRoom(keepLocalCopy: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            roomRepository.leaveRoom(roomId, keepLocalCopy)
            onComplete()
        }
    }

    fun deleteRoom(keepLocalCopy: Boolean, onComplete: () -> Unit) {
        viewModelScope.launch {
            roomRepository.deleteRoom(roomId, keepLocalCopy)
            onComplete()
        }
    }
}

@Composable
fun RoomDetailScreen(
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
    val adaptiveColor = com.crescentapps.turnly.presentation.components.LocalPrismalAdaptiveColor.current

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
            LiquidIconButton(
                icon = Icons.Default.ArrowBack,
                onClick = onNavigateBack,
                backdrop = backdrop
            )

            Text(
                text = room.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = adaptiveColor
            )

            LiquidIconButton(
                icon = Icons.Default.Settings,
                onClick = { onNavigateToSettings(room.id) },
                backdrop = backdrop
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // Status Card
            item {
                LiquidCard(
                    modifier = Modifier.fillMaxWidth(),
                    backdrop = backdrop
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
                                style = MaterialTheme.typography.bodyMedium,
                                color = adaptiveColor.copy(alpha = 0.75f)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        LiquidButton(
                            text = "Sync",
                            icon = Icons.Default.Sync,
                            onClick = {
                                viewModel.syncNow(context)
                                Toast.makeText(context, "Synchronizing...", Toast.LENGTH_SHORT).show()
                            },
                            backdrop = backdrop
                        )
                    }
                }
            }

            // Action: Invite Member
            item {
                LiquidButton(
                    text = "Invite Member (QR & Code)",
                    icon = Icons.Default.PersonAdd,
                    onClick = { showInviteDialog = true },
                    backdrop = backdrop,
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
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.accent
                )
                Spacer(modifier = Modifier.height(8.dp))
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
                                        style = MaterialTheme.typography.titleMedium,
                                        fontSize = 15.sp,
                                        color = adaptiveColor
                                    )
                                    if (member.role == RoomRole.OWNER) {
                                        LiquidChip(
                                            label = "Owner",
                                            selected = true,
                                            onClick = {},
                                            backdrop = backdrop
                                        )
                                    }
                                }

                                Text(
                                    text = if (member.presence == MemberPresence.ONLINE) "Online" else "Offline",
                                    fontSize = 12.sp,
                                    style = MaterialTheme.typography.bodyMedium,
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
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.accent
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState.sharedSchedules.isEmpty()) {
                    Text(
                        text = "No schedules shared in this room yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = adaptiveColor.copy(alpha = 0.6f)
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        uiState.sharedSchedules.forEach { sInfo ->
                            val localSched = uiState.localSchedulesMap[sInfo.localScheduleId]
                            LiquidCard(
                                modifier = Modifier.fillMaxWidth(),
                                backdrop = backdrop,
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
                                            style = MaterialTheme.typography.titleMedium,
                                            fontSize = 15.sp,
                                            color = adaptiveColor
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = localSched?.type?.displayName ?: "Rotation",
                                            fontSize = 12.sp,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = adaptiveColor.copy(alpha = 0.65f)
                                        )
                                    }
                                    LiquidChip(
                                        label = "☁ Shared",
                                        selected = true,
                                        onClick = { localSched?.let { onScheduleClick(it.id) } },
                                        backdrop = backdrop
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

        LiquidDialog(
            title = "Invite to ${room.name}",
            onDismissRequest = { showInviteDialog = false },
            positiveText = "",
            negativeText = null,
            backdrop = backdrop,
            content = {
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
                            .background(colors.accent.copy(alpha = 0.1f))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = room.roomCode,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 3.sp,
                            color = colors.accent
                        )
                        LiquidIconButton(
                            icon = Icons.Default.ContentCopy,
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Turnly Room Code", room.roomCode))
                                Toast.makeText(context, "Room code copied", Toast.LENGTH_SHORT).show()
                            },
                            backdrop = backdrop,
                            size = 36.dp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                            text = "Close",
                            onClick = { showInviteDialog = false },
                            backdrop = backdrop,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        )
    }
}
