package com.crescentapps.turnly.data.repository

import com.crescentapps.turnly.core.model.*
import com.crescentapps.turnly.data.local.TurnlyDatabase
import com.crescentapps.turnly.data.local.entity.*
import com.crescentapps.turnly.data.network.CreateRoomRequest
import com.crescentapps.turnly.data.network.JoinRoomRequest
import com.crescentapps.turnly.data.network.RealtimeSyncClient
import com.crescentapps.turnly.data.network.RoomService
import com.crescentapps.turnly.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

class RoomRepository(
    private val db: TurnlyDatabase,
    private val turnlyRepository: TurnlyRepository,
    private val roomService: RoomService,
    private val realtimeClient: RealtimeSyncClient,
    private val preferencesRepository: UserPreferencesRepository
) {
    private val roomDao = db.roomDao()
    private val scheduleDao = db.scheduleDao()
    private val participantDao = db.participantDao()
    private val occurrenceDao = db.occurrenceDao()
    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    init {
        // Listen to incoming realtime sync events
        repoScope.launch {
            realtimeClient.incomingEvents.collect { event ->
                handleIncomingSyncEvent(event)
            }
        }
    }

    // -------------------------------------------------------------
    // Rooms
    // -------------------------------------------------------------
    val allRooms: Flow<List<P2PRoom>> = roomDao.getAllRoomsFlow().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getRoomFlow(roomId: Long): Flow<P2PRoom?> =
        roomDao.getRoomFlow(roomId).map { it?.toDomain() }

    fun getMembersFlow(roomId: Long): Flow<List<RoomMember>> =
        roomDao.getMembersForRoomFlow(roomId).map { list -> list.map { it.toDomain() } }

    fun getSharedSchedulesFlow(roomId: Long): Flow<List<SharedScheduleInfo>> =
        roomDao.getSharedSchedulesForRoomFlow(roomId).map { list -> list.map { it.toDomain() } }

    fun getPendingOperationsCountFlow(): Flow<Int> =
        roomDao.getPendingOperationsCountFlow()

    val activeConflicts: Flow<List<SyncConflict>> =
        roomDao.getActiveConflictsFlow().map { list -> list.map { it.toDomain() } }

    // -------------------------------------------------------------
    // Create Room
    // -------------------------------------------------------------
    suspend fun createRoom(
        name: String,
        description: String,
        selectedLocalScheduleIds: List<Long>,
        shareHistory: Boolean = true
    ): Result<P2PRoom> = withContext(Dispatchers.IO) {
        val prefs = preferencesRepository.userPreferencesFlow.first()
        val ownerId = prefs.deviceMemberId.ifBlank { UUID.randomUUID().toString().take(12) }
        val ownerName = prefs.userDisplayName.ifBlank { "Owner" }

        // Gather schedules to share
        val schedulesToShare = selectedLocalScheduleIds.mapNotNull { sId ->
            val s = scheduleDao.getScheduleById(sId)?.toDomain() ?: return@mapNotNull null
            val p = scheduleDao.getParticipantsForSchedule(sId).map { it.toDomain() }
            val rot = scheduleDao.getRotationPattern(sId).map { it.toDomain() }
            ScheduleWithParticipants(s, p, rot)
        }

        val request = CreateRoomRequest(
            name = name,
            description = description,
            ownerMemberId = ownerId,
            ownerDisplayName = ownerName,
            initialSchedules = schedulesToShare
        )

        val result = roomService.createRoom(request)
        if (result.isFailure) {
            return@withContext Result.failure(result.exceptionOrNull() ?: Exception("Room creation failed"))
        }

        val response = result.getOrThrow()

        // Insert Room Entity locally
        val roomEntity = RoomEntity(
            remoteRoomId = response.roomId,
            name = name,
            description = description,
            role = RoomRole.OWNER,
            ownerMemberId = ownerId,
            ownerDisplayName = ownerName,
            roomCode = response.roomCode,
            syncState = SyncState.SYNCED,
            lastSyncedAt = System.currentTimeMillis()
        )
        val localRoomId = roomDao.insertRoom(roomEntity)

        // Add owner member
        val ownerMemberEntity = RoomMemberEntity(
            roomId = localRoomId,
            remoteMemberId = ownerId,
            displayName = ownerName,
            role = RoomRole.OWNER,
            presence = MemberPresence.ONLINE,
            joinedAt = System.currentTimeMillis()
        )
        roomDao.insertMember(ownerMemberEntity)

        // Mark shared schedules
        selectedLocalScheduleIds.forEach { sId ->
            roomDao.insertSharedSchedule(
                SharedScheduleEntity(
                    roomId = localRoomId,
                    localScheduleId = sId,
                    remoteScheduleId = "rem_sched_$sId",
                    sharingState = SharingState.SHARED,
                    permissions = RoomPermission.CAN_COMPLETE
                )
            )
        }

        // Connect realtime
        realtimeClient.connect(response.roomId, ownerId, ownerName)

        Result.success(roomEntity.copy(id = localRoomId).toDomain())
    }

    // -------------------------------------------------------------
    // Join Room
    // -------------------------------------------------------------
    suspend fun previewRoom(roomCode: String) = withContext(Dispatchers.IO) {
        roomService.previewRoom(roomCode)
    }

    suspend fun joinRoom(
        roomCode: String,
        memberDisplayName: String
    ): Result<P2PRoom> = withContext(Dispatchers.IO) {
        val prefs = preferencesRepository.userPreferencesFlow.first()
        val memberId = prefs.deviceMemberId.ifBlank { UUID.randomUUID().toString().take(12) }

        val request = JoinRoomRequest(
            roomCode = roomCode,
            memberId = memberId,
            memberDisplayName = memberDisplayName
        )

        val result = roomService.joinRoom(request)
        if (result.isFailure) {
            return@withContext Result.failure(result.exceptionOrNull() ?: Exception("Failed to join room"))
        }

        val roomData = result.getOrThrow()

        // Check if room already joined locally
        val existingRoom = roomDao.getRoomByRemoteId(roomData.roomId)
        val localRoomId = if (existingRoom != null) {
            existingRoom.id
        } else {
            val roomEntity = RoomEntity(
                remoteRoomId = roomData.roomId,
                name = roomData.name,
                description = roomData.description,
                role = RoomRole.MEMBER,
                ownerMemberId = roomData.ownerMemberId,
                ownerDisplayName = roomData.ownerDisplayName,
                roomCode = roomData.roomCode,
                syncState = SyncState.SYNCED,
                lastSyncedAt = System.currentTimeMillis()
            )
            roomDao.insertRoom(roomEntity)
        }

        // Save members
        roomDao.deleteAllMembersForRoom(localRoomId)
        val memberEntities = roomData.members.map { m ->
            RoomMemberEntity(
                roomId = localRoomId,
                remoteMemberId = m.remoteMemberId,
                displayName = m.displayName,
                role = m.role,
                presence = m.presence,
                joinedAt = m.joinedAt
            )
        }
        roomDao.insertMembers(memberEntities)

        // Merge shared remote schedules into local database
        for (remoteSchedWithP in roomData.sharedSchedules) {
            val remoteSched = remoteSchedWithP.schedule
            val existingLocalSched = scheduleDao.getAllSchedules().find { it.name.equals(remoteSched.name, ignoreCase = true) }

            val targetLocalSchedId = if (existingLocalSched != null) {
                existingLocalSched.id
            } else {
                // Insert participants
                val participantIdMap = mutableMapOf<Long, Long>()
                for (p in remoteSchedWithP.participants) {
                    val pId = participantDao.insertParticipant(p.toEntity().copy(id = 0))
                    participantIdMap[p.id] = pId
                }

                // Insert schedule
                val newSchedEntity = remoteSched.toEntity().copy(id = 0)
                val newSchedId = scheduleDao.insertSchedule(newSchedEntity)

                // Insert schedule participants and pattern
                val spList = participantIdMap.values.mapIndexed { index, pId ->
                    ScheduleParticipantEntity(newSchedId, pId, index, true)
                }
                scheduleDao.insertScheduleParticipants(spList)

                val patternList = remoteSchedWithP.pattern.mapIndexed { index, item ->
                    val mappedPId = participantIdMap[item.participantId] ?: participantIdMap.values.first()
                    RotationPatternEntity(
                        id = 0,
                        scheduleId = newSchedId,
                        participantId = mappedPId,
                        patternPosition = index
                    )
                }
                scheduleDao.insertRotationPatterns(patternList)

                newSchedId
            }

            // Link in shared_schedules
            val remoteId = "rem_sched_${remoteSched.id}"
            roomDao.insertSharedSchedule(
                SharedScheduleEntity(
                    roomId = localRoomId,
                    localScheduleId = targetLocalSchedId,
                    remoteScheduleId = remoteId,
                    sharingState = SharingState.SHARED,
                    permissions = RoomPermission.CAN_COMPLETE
                )
            )
        }

        // Start realtime connection
        realtimeClient.connect(roomData.roomId, memberId, memberDisplayName)

        val updated = roomDao.getRoomById(localRoomId)?.toDomain()
            ?: return@withContext Result.failure(IllegalStateException("Local room save failed"))

        Result.success(updated)
    }

    // -------------------------------------------------------------
    // Schedule Sharing Controls
    // -------------------------------------------------------------
    suspend fun shareScheduleWithRoom(
        roomId: Long,
        localScheduleId: Long,
        shareHistory: Boolean = true
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val room = roomDao.getRoomById(roomId)
            ?: return@withContext Result.failure(IllegalArgumentException("Room not found"))

        val schedule = scheduleDao.getScheduleById(localScheduleId)?.toDomain()
            ?: return@withContext Result.failure(IllegalArgumentException("Schedule not found"))

        val participants = scheduleDao.getParticipantsForSchedule(localScheduleId).map { it.toDomain() }
        val pattern = scheduleDao.getRotationPattern(localScheduleId).map { it.toDomain() }
        val scheduleWithP = ScheduleWithParticipants(schedule, participants, pattern)

        val remoteIdResult = roomService.uploadSchedule(room.remoteRoomId, scheduleWithP, shareHistory)
        if (remoteIdResult.isFailure) {
            return@withContext Result.failure(remoteIdResult.exceptionOrNull() ?: Exception("Upload failed"))
        }

        val remoteSchedId = remoteIdResult.getOrThrow()
        roomDao.insertSharedSchedule(
            SharedScheduleEntity(
                roomId = roomId,
                localScheduleId = localScheduleId,
                remoteScheduleId = remoteSchedId,
                sharingState = SharingState.SHARED,
                permissions = RoomPermission.CAN_COMPLETE
            )
        )
        Result.success(Unit)
    }

    suspend fun stopSharingSchedule(
        roomId: Long,
        localScheduleId: Long,
        keepLocalCopy: Boolean = true
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val room = roomDao.getRoomById(roomId) ?: return@withContext Result.success(Unit)
        val sharedInfo = roomDao.getSharedScheduleForLocalId(localScheduleId) ?: return@withContext Result.success(Unit)

        roomService.stopSharingSchedule(room.remoteRoomId, sharedInfo.remoteScheduleId)
        roomDao.deleteSharedSchedule(roomId, localScheduleId)

        if (!keepLocalCopy) {
            turnlyRepository.deleteSchedulePermanently(localScheduleId)
        }
        Result.success(Unit)
    }

    // -------------------------------------------------------------
    // Synchronized Mutations & Offline Queue
    // -------------------------------------------------------------
    suspend fun markTurnCompleteSynchronized(
        scheduleId: Long,
        date: String,
        participantId: Long,
        actualAmount: Double? = null,
        note: String = ""
    ) = withContext(Dispatchers.IO) {
        val prefs = preferencesRepository.userPreferencesFlow.first()
        val memberName = prefs.userDisplayName.ifBlank { "User" }
        val memberId = prefs.deviceMemberId

        // 1. Mark in local DB immediately (Local-First Guarantee)
        turnlyRepository.markTurnComplete(
            scheduleId = scheduleId,
            date = date,
            participantId = participantId,
            actualAmount = actualAmount,
            note = note,
            completedByMemberName = memberName
        )

        // 2. Check if schedule is shared with an online P2P Room
        val sharedInfo = roomDao.getSharedScheduleForLocalId(scheduleId) ?: return@withContext
        val room = roomDao.getRoomById(sharedInfo.roomId) ?: return@withContext

        val payload = OccurrenceCompletedPayload(
            scheduleIdRemote = sharedInfo.remoteScheduleId,
            date = date,
            participantIdRemote = participantId.toString(),
            completedByMemberId = memberId,
            completedByName = memberName,
            actualAmount = actualAmount,
            note = note,
            completedAt = System.currentTimeMillis()
        )
        val payloadJson = json.encodeToString(payload)

        val event = SyncEvent(
            eventId = UUID.randomUUID().toString(),
            roomId = room.remoteRoomId,
            type = SyncEventType.OCCURRENCE_COMPLETED,
            senderDeviceId = "device_$memberId",
            senderMemberId = memberId,
            senderDisplayName = memberName,
            entityId = "${sharedInfo.remoteScheduleId}_$date",
            payloadJson = payloadJson
        )

        // 3. Queue into sync_operations
        val op = SyncOperationEntity(
            id = event.eventId,
            roomId = room.id,
            entityType = "OCCURRENCE",
            entityId = "${scheduleId}_$date",
            operationType = "OCCURRENCE_COMPLETED",
            payloadJson = payloadJson,
            syncState = OperationSyncState.PENDING
        )
        roomDao.enqueueOperation(op)

        // 4. Try live dispatch if online
        tryDispatchPendingOperations()
    }

    suspend fun tryDispatchPendingOperations() = withContext(Dispatchers.IO) {
        val pendingOps = roomDao.getPendingOperations()
        if (pendingOps.isEmpty()) return@withContext

        val prefs = preferencesRepository.userPreferencesFlow.first()
        if (!prefs.onlineSyncEnabled) return@withContext

        val opsByRoom = pendingOps.groupBy { it.roomId }

        for ((roomId, ops) in opsByRoom) {
            val room = roomDao.getRoomById(roomId) ?: continue
            roomDao.updateRoomSyncState(roomId, SyncState.SYNCING)

            val events = ops.map { op ->
                SyncEvent(
                    eventId = op.id,
                    roomId = room.remoteRoomId,
                    type = when (op.operationType) {
                        "OCCURRENCE_SKIPPED" -> SyncEventType.OCCURRENCE_SKIPPED
                        else -> SyncEventType.OCCURRENCE_COMPLETED
                    },
                    senderDeviceId = "device_${prefs.deviceMemberId}",
                    senderMemberId = prefs.deviceMemberId,
                    senderDisplayName = prefs.userDisplayName,
                    entityId = op.entityId,
                    payloadJson = op.payloadJson
                )
            }

            val pushResult = roomService.pushSyncOperations(room.remoteRoomId, events)
            if (pushResult.isSuccess) {
                // Delete processed operations from queue
                ops.forEach { roomDao.deleteOperation(it.id) }
                roomDao.updateRoomSyncState(roomId, SyncState.SYNCED)
            } else {
                // Back off and mark offline/failed
                ops.forEach { op ->
                    roomDao.updateOperation(op.copy(retryCount = op.retryCount + 1, syncState = OperationSyncState.PENDING))
                }
                roomDao.updateRoomSyncState(roomId, SyncState.OFFLINE)
            }
        }
    }

    // -------------------------------------------------------------
    // Incoming Event Processing & Conflict Handling
    // -------------------------------------------------------------
    private suspend fun handleIncomingSyncEvent(event: SyncEvent) = withContext(Dispatchers.IO) {
        val room = roomDao.getRoomByRemoteId(event.roomId) ?: return@withContext

        when (event.type) {
            SyncEventType.OCCURRENCE_COMPLETED -> {
                val payload = runCatching { json.decodeFromString<OccurrenceCompletedPayload>(event.payloadJson) }.getOrNull() ?: return@withContext
                val sharedInfo = roomDao.getSharedSchedulesForRoom(room.id).find { it.remoteScheduleId == payload.scheduleIdRemote } ?: return@withContext

                val existing = occurrenceDao.getOccurrence(sharedInfo.localScheduleId, payload.date)
                if (existing != null && existing.status == OccurrenceStatus.SKIPPED) {
                    // Conflict detected: Local skipped vs remote completed
                    val conflict = SyncConflictEntity(
                        roomId = room.id,
                        scheduleId = sharedInfo.localScheduleId,
                        scheduleName = scheduleDao.getScheduleById(sharedInfo.localScheduleId)?.name ?: "Schedule",
                        date = payload.date,
                        localVersionDescription = "Skipped locally",
                        remoteVersionDescription = "Completed by ${payload.completedByName}",
                        localStatus = OccurrenceStatus.SKIPPED,
                        remoteStatus = OccurrenceStatus.COMPLETED,
                        completedByNameRemote = payload.completedByName,
                        isResolved = false
                    )
                    roomDao.insertConflict(conflict)
                    roomDao.updateRoomSyncState(room.id, SyncState.CONFLICT)
                    return@withContext
                }

                // Apply update
                val participantId = payload.participantIdRemote.toLongOrNull() ?: existing?.participantId ?: 1L
                val entity = OccurrenceEntity(
                    id = existing?.id ?: 0L,
                    scheduleId = sharedInfo.localScheduleId,
                    date = payload.date,
                    participantId = participantId,
                    status = OccurrenceStatus.COMPLETED,
                    actualAmount = payload.actualAmount,
                    completedAt = payload.completedAt,
                    completedByMemberName = payload.completedByName,
                    note = payload.note,
                    entityVersion = event.entityVersion
                )
                occurrenceDao.insertOccurrence(entity)
                roomDao.updateRoomSyncState(room.id, SyncState.SYNCED)
            }
            SyncEventType.MEMBER_JOINED -> {
                val member = RoomMemberEntity(
                    roomId = room.id,
                    remoteMemberId = event.senderMemberId,
                    displayName = event.senderDisplayName,
                    role = RoomRole.MEMBER,
                    presence = MemberPresence.ONLINE,
                    joinedAt = event.timestamp
                )
                roomDao.insertMember(member)
            }
            SyncEventType.MEMBER_LEFT -> {
                roomDao.deleteMember(room.id, event.senderMemberId)
            }
            SyncEventType.ROOM_DELETED -> {
                roomDao.deleteRoom(room)
            }
            else -> {}
        }
    }

    // -------------------------------------------------------------
    // Conflict Resolution
    // -------------------------------------------------------------
    suspend fun resolveConflict(conflictId: Long, keepRemote: Boolean) = withContext(Dispatchers.IO) {
        val conflict = roomDao.getActiveConflictsFlow().first().find { it.id == conflictId } ?: return@withContext

        if (keepRemote) {
            turnlyRepository.markTurnComplete(
                scheduleId = conflict.scheduleId,
                date = conflict.date,
                participantId = 1L,
                completedByMemberName = conflict.completedByNameRemote
            )
        } else {
            turnlyRepository.markTurnSkipped(
                scheduleId = conflict.scheduleId,
                date = conflict.date,
                participantId = 1L,
                note = "Kept local skipped state"
            )
        }

        roomDao.markConflictResolved(conflictId)
        roomDao.updateRoomSyncState(conflict.roomId, SyncState.SYNCED)
    }

    // -------------------------------------------------------------
    // Leave / Delete Room
    // -------------------------------------------------------------
    suspend fun leaveRoom(roomId: Long, keepLocalCopy: Boolean = true): Result<Unit> = withContext(Dispatchers.IO) {
        val room = roomDao.getRoomById(roomId) ?: return@withContext Result.success(Unit)
        val prefs = preferencesRepository.userPreferencesFlow.first()

        roomService.leaveRoom(room.remoteRoomId, prefs.deviceMemberId)

        if (!keepLocalCopy) {
            val shared = roomDao.getSharedSchedulesForRoom(roomId)
            shared.forEach { s ->
                turnlyRepository.deleteSchedulePermanently(s.localScheduleId)
            }
        }

        roomDao.deleteRoomById(roomId)
        realtimeClient.disconnect()
        Result.success(Unit)
    }

    suspend fun deleteRoom(roomId: Long, keepLocalCopy: Boolean = true): Result<Unit> = withContext(Dispatchers.IO) {
        val room = roomDao.getRoomById(roomId) ?: return@withContext Result.success(Unit)
        val prefs = preferencesRepository.userPreferencesFlow.first()

        val delResult = roomService.deleteRoom(room.remoteRoomId, prefs.deviceMemberId)
        if (delResult.isFailure) {
            return@withContext delResult
        }

        if (!keepLocalCopy) {
            val shared = roomDao.getSharedSchedulesForRoom(roomId)
            shared.forEach { s ->
                turnlyRepository.deleteSchedulePermanently(s.localScheduleId)
            }
        }

        roomDao.deleteRoomById(roomId)
        realtimeClient.disconnect()
        Result.success(Unit)
    }
}

// -------------------------------------------------------------
// Mapping Helpers
// -------------------------------------------------------------
fun RoomEntity.toDomain() = P2PRoom(
    id = id,
    remoteRoomId = remoteRoomId,
    name = name,
    description = description,
    role = role,
    ownerMemberId = ownerMemberId,
    ownerDisplayName = ownerDisplayName,
    roomCode = roomCode,
    syncState = syncState,
    lastSyncedAt = lastSyncedAt,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun RoomMemberEntity.toDomain() = RoomMember(
    id = id,
    roomId = roomId,
    remoteMemberId = remoteMemberId,
    displayName = displayName,
    role = role,
    presence = presence,
    joinedAt = joinedAt,
    lastSeenAt = lastSeenAt
)

fun SharedScheduleEntity.toDomain() = SharedScheduleInfo(
    roomId = roomId,
    localScheduleId = localScheduleId,
    remoteScheduleId = remoteScheduleId,
    sharingState = sharingState,
    permissions = permissions,
    updatedAt = updatedAt
)

fun SyncConflictEntity.toDomain() = SyncConflict(
    id = id,
    roomId = roomId,
    scheduleId = scheduleId,
    scheduleName = scheduleName,
    date = date,
    localVersionDescription = localVersionDescription,
    remoteVersionDescription = remoteVersionDescription,
    localStatus = localStatus,
    remoteStatus = remoteStatus,
    completedByNameLocal = completedByNameLocal,
    completedByNameRemote = completedByNameRemote,
    detectedAt = detectedAt,
    isResolved = isResolved
)
