package com.crescentapps.turnly.presentation.m3.screens.room

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.crescentapps.turnly.core.model.*
import com.crescentapps.turnly.core.util.DateUtils
import com.crescentapps.turnly.core.util.QRCodeUtils
import com.crescentapps.turnly.presentation.m3.components.M3ConfirmDialog
import com.crescentapps.turnly.presentation.m3.components.M3ParticipantAvatar
import com.crescentapps.turnly.presentation.m3.components.M3ScheduleTypeBadge
import com.crescentapps.turnly.presentation.screens.room.RoomDetailViewModel
import com.crescentapps.turnly.presentation.screens.room.RoomViewModel

/**
 * Material 3 Room List Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3RoomListScreen(
    viewModel: RoomViewModel,
    onCreateRoom: () -> Unit,
    onJoinRoom: () -> Unit,
    onRoomClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Shared Schedules", fontWeight = FontWeight.Bold) },
                actions = {
                    TextButton(onClick = onJoinRoom) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Join")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateRoom,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Room")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 88.dp)
        ) {
            // Conflict warning banner
            if (uiState.activeConflicts.isNotEmpty()) {
                val conflict = uiState.activeConflicts.first()
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = null)
                                Text("Sync Discrepancy", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Conflict on \"${conflict.scheduleName}\" for ${DateUtils.formatDisplay(conflict.date)}.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            if (uiState.rooms.isEmpty()) {
                item {
                    OutlinedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(40.dp)
                            )
                            Text("No Shared Rooms", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(
                                "Create a shared room to synchronize rotation schedules across family, roommates, or team members.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Button(onClick = onCreateRoom) {
                                Text("Create Room")
                            }
                        }
                    }
                }
            } else {
                items(uiState.rooms, key = { it.id }) { room ->
                    ElevatedCard(
                        onClick = { onRoomClick(room.id) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                        ),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = room.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "Code: ${room.roomCode}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            if (room.description.isNotBlank()) {
                                Text(
                                    text = room.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Synced via P2P · Turnly Network",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                                Icon(
                                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Material 3 Create Room Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3CreateRoomScreen(
    viewModel: RoomViewModel,
    onNavigateBack: () -> Unit,
    onRoomCreated: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var currentStep by remember { mutableStateOf(1) }
    var roomName by remember { mutableStateOf("") }
    var roomDescription by remember { mutableStateOf("") }
    var selectedScheduleIds by remember { mutableStateOf(setOf<Long>()) }

    var createdRoom by remember { mutableStateOf<P2PRoom?>(null) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(if (createdRoom != null) "Room Created" else "Step $currentStep of 2", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (createdRoom != null) onRoomCreated(createdRoom!!.id)
                        else if (currentStep > 1) currentStep--
                        else onNavigateBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (createdRoom != null) {
                val room = createdRoom!!
                LazyColumn(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    item {
                        Text("Room is Ready!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(
                            "Share the 6-character code or QR code with other members.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (qrBitmap != null) {
                        item {
                            ElevatedCard(shape = RoundedCornerShape(16.dp)) {
                                Box(modifier = Modifier.padding(16.dp)) {
                                    Image(
                                        bitmap = qrBitmap!!.asImageBitmap(),
                                        contentDescription = "Room QR Code",
                                        modifier = Modifier.size(200.dp)
                                    )
                                }
                            }
                        }
                    }

                    item {
                        ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = room.roomCode,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 4.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Turnly Room Code", room.roomCode))
                                    Toast.makeText(context, "Code copied", Toast.LENGTH_SHORT).show()
                                }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = { onRoomCreated(room.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Done")
                        }
                    }
                }
            } else if (currentStep == 1) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.weight(1f)) {
                    Text("Name your Shared Room", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Enter a name for this household, apartment, or team room.", style = MaterialTheme.typography.bodyMedium)

                    OutlinedTextField(
                        value = roomName,
                        onValueChange = { roomName = it },
                        label = { Text("Room Name") },
                        placeholder = { Text("e.g. Apartment 4B") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = roomDescription,
                        onValueChange = { roomDescription = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Button(
                    onClick = { currentStep = 2 },
                    enabled = roomName.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text("Next: Select Schedules")
                }
            } else {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Choose Schedules to Share", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Only selected schedules synchronize. Unselected schedules remain private.", style = MaterialTheme.typography.bodyMedium)

                    Spacer(modifier = Modifier.height(16.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(uiState.availableSchedules, key = { it.id }) { schedule ->
                            val isChecked = selectedScheduleIds.contains(schedule.id)
                            OutlinedCard(
                                onClick = {
                                    selectedScheduleIds = if (isChecked) selectedScheduleIds - schedule.id else selectedScheduleIds + schedule.id
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(schedule.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                        Text(schedule.type.displayName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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

                Button(
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Text(if (uiState.isCreating) "Creating Room..." else "Create Room")
                }
            }
        }
    }
}

/**
 * Material 3 Join Room Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3JoinRoomScreen(
    viewModel: RoomViewModel,
    initialRoomCode: String? = null,
    onNavigateBack: () -> Unit,
    onJoinedSuccessfully: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var roomCodeInput by remember { mutableStateOf(initialRoomCode.orEmpty()) }
    var userDisplayNameInput by remember { mutableStateOf("") }
    var isScanningMode by remember { mutableStateOf(false) }

    LaunchedEffect(initialRoomCode) {
        if (!initialRoomCode.isNullOrBlank()) {
            viewModel.previewRoom(initialRoomCode) {}
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Join a Room", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isScanningMode) {
                            isScanningMode = false
                        } else {
                            onNavigateBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val preview = uiState.previewRoomData
            if (preview != null) {
                ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Join Room?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(preview.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        if (preview.description.isNotBlank()) {
                            Text(preview.description, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("Owner: ${preview.ownerDisplayName}", style = MaterialTheme.typography.bodySmall)

                        OutlinedTextField(
                            value = userDisplayNameInput,
                            onValueChange = { userDisplayNameInput = it },
                            label = { Text("Your Display Name in this Room") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }

                            Button(
                                onClick = {
                                    viewModel.joinRoom(
                                        roomCode = preview.roomCode,
                                        displayName = userDisplayNameInput.ifBlank { "Member" },
                                        onSuccess = { room -> onJoinedSuccessfully(room.id) }
                                    )
                                },
                                enabled = !uiState.isJoining,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (uiState.isJoining) "Joining..." else "Join Room")
                            }
                        }
                    }
                }
            } else if (isScanningMode) {
                ElevatedCard(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
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
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(340.dp)
                                .clip(RoundedCornerShape(16.dp))
                        ) {
                            com.crescentapps.turnly.presentation.components.camera.QRScannerView(
                                onQRCodeScanned = { rawPayload, extractedCode ->
                                    isScanningMode = false
                                    roomCodeInput = extractedCode
                                    viewModel.previewRoom(extractedCode) {}
                                },
                                onClose = { isScanningMode = false },
                                frameBorderColor = MaterialTheme.colorScheme.primary
                            )
                        }

                        OutlinedButton(
                            onClick = { isScanningMode = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Enter Code Manually")
                        }
                    }
                }
            } else {
                Text("Enter 6-Character Room Code", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Ask the room owner for their code or scan their QR code.", style = MaterialTheme.typography.bodyMedium)

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
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(
                    onClick = { viewModel.previewRoom(roomCodeInput) {} },
                    enabled = roomCodeInput.length == 6 && !uiState.isJoining,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (uiState.isJoining) "Finding Room..." else "Find Room")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Text(
                        text = "OR",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                OutlinedButton(
                    onClick = { isScanningMode = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Scan QR Code")
                }
            }
        }
    }
}

/**
 * Material 3 Room Detail Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
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
    val room = uiState.room

    if (room == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(room.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToSettings(room.id) }) {
                        Icon(Icons.Default.Settings, contentDescription = "Room Settings")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 48.dp)
        ) {
            // Room Status Card
            item {
                ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Room Code: ${room.roomCode}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Your Role: ${room.role.name}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Button(onClick = {
                            viewModel.syncNow(context)
                            Toast.makeText(context, "Syncing...", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync")
                        }
                    }
                }
            }

            // Members Header
            item {
                Text(
                    text = "MEMBERS (${uiState.members.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(uiState.members) { member ->
                OutlinedCard(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(member.displayName, fontWeight = FontWeight.SemiBold) },
                        supportingContent = { Text(member.role.name) },
                        leadingContent = {
                            M3ParticipantAvatar(participant = Participant(name = member.displayName), size = 36.dp)
                        }
                    )
                }
            }
        }
    }
}

/**
 * Material 3 Room Settings Screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun M3RoomSettingsScreen(
    viewModel: RoomDetailViewModel,
    onNavigateBack: () -> Unit,
    onRoomExited: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Room Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Room Information", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(room.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Code: ${room.roomCode}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            OutlinedButton(
                onClick = { showLeaveConfirm = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Leave Room")
            }

            if (isOwner) {
                Button(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete Room for Everyone")
                }
            }
        }
    }

    if (showLeaveConfirm) {
        M3ConfirmDialog(
            title = "Leave ${room.name}?",
            message = "You will stop synchronizing with this room. Your local schedules will be kept safely.",
            confirmText = "Leave",
            isDestructive = true,
            onConfirm = {
                showLeaveConfirm = false
                viewModel.leaveRoom(keepLocalCopy = true, onComplete = onRoomExited)
            },
            onDismiss = { showLeaveConfirm = false }
        )
    }

    if (showDeleteConfirm) {
        M3ConfirmDialog(
            title = "Delete Room?",
            message = "This permanently ends synchronization for all room members. Local schedules remain intact.",
            confirmText = "Delete Room",
            isDestructive = true,
            onConfirm = {
                showDeleteConfirm = false
                viewModel.deleteRoom(keepLocalCopy = true, onComplete = onRoomExited)
            },
            onDismiss = { showDeleteConfirm = false }
        )
    }
}
