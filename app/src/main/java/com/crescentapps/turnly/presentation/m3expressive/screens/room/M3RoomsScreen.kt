package com.crescentapps.turnly.presentation.m3expressive.screens.room

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.crescentapps.turnly.core.model.SyncState
import com.crescentapps.turnly.presentation.m3expressive.components.TurnlyExpressiveCard
import com.crescentapps.turnly.presentation.m3expressive.components.TurnlyExpressiveButton
import com.crescentapps.turnly.presentation.m3expressive.theme.ExpressiveTokens
import com.crescentapps.turnly.presentation.screens.room.RoomViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3RoomsScreen(
    viewModel: RoomViewModel,
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
    onRoomClick: (Long) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text("Rooms") },
                actions = {
                    TurnlyExpressiveButton(
                        onClick = onJoinRoom,
                        text = "Join",
                        icon = { Icon(Icons.AutoMirrored.Filled.Login, contentDescription = null) },
                        isPrimary = false
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateRoom) {
                Icon(Icons.Default.Add, contentDescription = "Create Room")
            }
        }
    ) { padding ->
        if (uiState.rooms.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Group,
                        contentDescription = null,
                        modifier = Modifier.size(ExpressiveTokens.spacing.hero),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(Modifier.height(ExpressiveTokens.spacing.normal))
                    Text(
                        "No P2P Rooms Connected",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(ExpressiveTokens.spacing.small))
                    Text(
                        "Sync schedules with family using room codes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(ExpressiveTokens.spacing.normal),
                verticalArrangement = Arrangement.spacedBy(ExpressiveTokens.spacing.component)
            ) {
                items(uiState.rooms) { room ->
                    TurnlyExpressiveCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onRoomClick(room.id) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(ExpressiveTokens.spacing.normal),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = room.name, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(ExpressiveTokens.spacing.micro))
                                Text(
                                    text = "Code: ${room.roomCode} · ${room.role.name.lowercase().replaceFirstChar { it.uppercase() }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            val (syncText, syncColor) = when (room.syncState) {
                                SyncState.SYNCED -> "Synced" to MaterialTheme.colorScheme.primary
                                SyncState.SYNCING -> "Syncing" to MaterialTheme.colorScheme.secondary
                                SyncState.OFFLINE -> "Offline" to MaterialTheme.colorScheme.error
                                SyncState.CONFLICT -> "Conflict" to MaterialTheme.colorScheme.error
                            }
                            
                            AssistChip(
                                onClick = {},
                                label = { Text(syncText) },
                                colors = AssistChipDefaults.assistChipColors(
                                    labelColor = syncColor,
                                    leadingIconContentColor = syncColor
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
