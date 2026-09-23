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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.crescentapps.turnly.core.model.*
import com.crescentapps.turnly.core.util.DateUtils
import com.crescentapps.turnly.data.repository.RoomRepository
import com.crescentapps.turnly.data.repository.TurnlyRepository
import com.crescentapps.turnly.presentation.catalog.utils.LocalBackdrop
import com.crescentapps.turnly.presentation.components.liquid.LiquidButton
import com.crescentapps.turnly.presentation.components.liquid.LiquidCard
import com.crescentapps.turnly.presentation.components.liquid.LiquidChip
import com.crescentapps.turnly.presentation.components.liquid.LiquidIconButton
import com.crescentapps.turnly.presentation.theme.LiquidColors
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RoomUiState(
    val rooms: List<P2PRoom> = emptyList(),
    val availableSchedules: List<Schedule> = emptyList(),
    val activeConflicts: List<SyncConflict> = emptyList(),
    val isCreating: Boolean = false,
    val isJoining: Boolean = false,
    val errorMessage: String? = null,
    val previewRoomData: com.crescentapps.turnly.data.network.RoomMetadataResponse? = null
)

class RoomViewModel(
    private val roomRepository: RoomRepository,
    private val turnlyRepository: TurnlyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoomUiState())
    val uiState: StateFlow<RoomUiState> = _uiState.asStateFlow()

    init {
        observeRooms()
        observeSchedules()
        observeConflicts()
    }

    private fun observeRooms() {
        viewModelScope.launch {
            roomRepository.allRooms.collect { rooms ->
                _uiState.update { it.copy(rooms = rooms) }
            }
        }
    }

    private fun observeSchedules() {
        viewModelScope.launch {
            turnlyRepository.activeSchedules.collect { schedules ->
                _uiState.update { it.copy(availableSchedules = schedules) }
            }
        }
    }

    private fun observeConflicts() {
        viewModelScope.launch {
            roomRepository.activeConflicts.collect { conflicts ->
                _uiState.update { it.copy(activeConflicts = conflicts) }
            }
        }
    }

    fun createRoom(
        name: String,
        description: String,
        selectedScheduleIds: List<Long>,
        onSuccess: (P2PRoom) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, errorMessage = null) }
            val result = roomRepository.createRoom(name, description, selectedScheduleIds)
            _uiState.update { it.copy(isCreating = false) }
            result.onSuccess { room ->
                onSuccess(room)
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Failed to create room") }
            }
        }
    }

    fun previewRoom(roomCode: String, onPreviewReady: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isJoining = true, errorMessage = null) }
            val result = roomRepository.previewRoom(roomCode)
            _uiState.update { it.copy(isJoining = false) }
            result.onSuccess { data ->
                _uiState.update { it.copy(previewRoomData = data) }
                onPreviewReady()
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Room not found") }
            }
        }
    }

    fun joinRoom(
        roomCode: String,
        displayName: String,
        onSuccess: (P2PRoom) -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isJoining = true, errorMessage = null) }
            val result = roomRepository.joinRoom(roomCode, displayName)
            _uiState.update { it.copy(isJoining = false) }
            result.onSuccess { room ->
                onSuccess(room)
            }.onFailure { err ->
                _uiState.update { it.copy(errorMessage = err.message ?: "Failed to join room") }
            }
        }
    }

    fun resolveConflict(conflictId: Long, keepRemote: Boolean) {
        viewModelScope.launch {
            roomRepository.resolveConflict(conflictId, keepRemote)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}

@Composable
fun RoomListScreen(
    viewModel: RoomViewModel,
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
    onRoomClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val backdrop = LocalBackdrop.current
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
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Shared Schedules",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = adaptiveColor
                )
                Text(
                    text = "Keep in sync with family, roommates, or team members",
                    style = MaterialTheme.typography.bodyMedium,
                    color = adaptiveColor.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LiquidButton(
                    text = "Join with Code",
                    onClick = onJoinRoom,
                    backdrop = backdrop,
                    modifier = Modifier.height(44.dp)
                )
                LiquidButton(
                    text = "Share",
                    icon = Icons.Default.Add,
                    onClick = onCreateRoom,
                    backdrop = backdrop,
                    modifier = Modifier.height(44.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Conflicts Banner
        if (uiState.activeConflicts.isNotEmpty()) {
            val conflict = uiState.activeConflicts.first()
            LiquidCard(
                modifier = Modifier.fillMaxWidth(),
                backdrop = backdrop
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = colors.statusMissed
                        )
                        Text(
                            text = "Turn Update Discrepancy",
                            fontWeight = FontWeight.Bold,
                            color = colors.statusMissed,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Both you and another member updated \"${conflict.scheduleName}\" for ${DateUtils.formatDisplay(conflict.date)}. Which update should we keep?",
                        fontSize = 13.sp,
                        color = adaptiveColor.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        LiquidButton(
                            text = "Keep Their Update",
                            onClick = { viewModel.resolveConflict(conflict.id, keepRemote = true) },
                            backdrop = backdrop,
                            modifier = Modifier.weight(1f)
                        )
                        LiquidButton(
                            text = "Keep My Update",
                            onClick = { viewModel.resolveConflict(conflict.id, keepRemote = false) },
                            backdrop = backdrop,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Room List Content
        if (uiState.rooms.isEmpty()) {
            LiquidCard(
                modifier = Modifier.fillMaxWidth(),
                backdrop = backdrop
            ) {
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
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = colors.accent,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Text(
                        text = "No P2P Rooms Connected",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = adaptiveColor
                    )
                    Text(
                        text = "Create a room to synchronize selected schedules across multiple phones using QR or room codes without an account.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = adaptiveColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 12.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        LiquidButton(
                            text = "Create Room",
                            icon = Icons.Default.Add,
                            onClick = onCreateRoom,
                            backdrop = backdrop
                        )
                        LiquidButton(
                            text = "Join Room",
                            onClick = onJoinRoom,
                            backdrop = backdrop
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                items(uiState.rooms, key = { it.id }) { room ->
                    LiquidCard(
                        modifier = Modifier.fillMaxWidth(),
                        backdrop = backdrop,
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
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = adaptiveColor
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Code: ${room.roomCode} · ${room.role.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                    fontSize = 12.sp,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = adaptiveColor.copy(alpha = 0.65f)
                                )
                            }
                            val syncText = when (room.syncState) {
                                SyncState.SYNCED -> "✓ Synced"
                                SyncState.SYNCING -> "↻ Syncing"
                                SyncState.OFFLINE -> "⚠ Offline"
                                SyncState.CONFLICT -> "! Conflict"
                            }
                            LiquidChip(
                                label = syncText,
                                selected = room.syncState == SyncState.SYNCED,
                                onClick = { onRoomClick(room.id) },
                                backdrop = backdrop
                            )
                        }
                    }
                }
            }
        }
    }
}
