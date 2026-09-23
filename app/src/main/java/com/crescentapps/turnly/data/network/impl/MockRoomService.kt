package com.crescentapps.turnly.data.network.impl

import com.crescentapps.turnly.core.model.*
import com.crescentapps.turnly.core.util.RoomCodeGenerator
import com.crescentapps.turnly.data.network.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Production-ready In-Memory / Local-Network Mock Room Service and Realtime Client.
 * Enables zero-dependency live synchronization, testing, and development fallback,
 * while matching the exact network specifications and typed payloads.
 */
object MockNetworkServer {
    val rooms = ConcurrentHashMap<String, RoomMetadataResponse>()
    val codeToRoomId = ConcurrentHashMap<String, String>()
    val eventsByRoom = ConcurrentHashMap<String, MutableList<SyncEvent>>()
    val globalEventBus = MutableSharedFlow<SyncEvent>(extraBufferCapacity = 100)
}

class MockRoomService : RoomService {

    override suspend fun createRoom(request: CreateRoomRequest): Result<CreateRoomResponse> {
        delay(300) // realistic network latency
        val roomId = UUID.randomUUID().toString()
        val roomCode = RoomCodeGenerator.generateCode()
        val inviteToken = UUID.randomUUID().toString().replace("-", "").take(16)

        val ownerMember = RoomMember(
            id = 1,
            roomId = 1,
            remoteMemberId = request.ownerMemberId,
            displayName = request.ownerDisplayName,
            role = RoomRole.OWNER,
            presence = MemberPresence.ONLINE,
            joinedAt = System.currentTimeMillis()
        )

        val metadata = RoomMetadataResponse(
            roomId = roomId,
            roomCode = roomCode,
            name = request.name,
            description = request.description,
            ownerMemberId = request.ownerMemberId,
            ownerDisplayName = request.ownerDisplayName,
            members = listOf(ownerMember),
            sharedSchedules = request.initialSchedules,
            recentOccurrences = emptyList()
        )

        MockNetworkServer.rooms[roomId] = metadata
        MockNetworkServer.codeToRoomId[roomCode] = roomId
        MockNetworkServer.eventsByRoom[roomId] = mutableListOf()

        return Result.success(
            CreateRoomResponse(
                roomId = roomId,
                roomCode = roomCode,
                inviteToken = inviteToken,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun previewRoom(roomCode: String): Result<RoomMetadataResponse> {
        delay(200)
        val normalized = RoomCodeGenerator.normalizeCode(roomCode)
        val roomId = MockNetworkServer.codeToRoomId[normalized]
            ?: return Result.failure(IllegalArgumentException("Room code '$normalized' not found."))

        val room = MockNetworkServer.rooms[roomId]
            ?: return Result.failure(IllegalArgumentException("Room expired or no longer available."))

        return Result.success(room)
    }

    override suspend fun joinRoom(request: JoinRoomRequest): Result<RoomMetadataResponse> {
        delay(300)
        val normalized = RoomCodeGenerator.normalizeCode(request.roomCode)
        val roomId = MockNetworkServer.codeToRoomId[normalized]
            ?: return Result.failure(IllegalArgumentException("Room code '$normalized' not found."))

        val room = MockNetworkServer.rooms[roomId]
            ?: return Result.failure(IllegalArgumentException("Room not found."))

        // Add member if not already joined
        val existingMember = room.members.find { it.remoteMemberId == request.memberId }
        val updatedMembers = if (existingMember == null) {
            room.members + RoomMember(
                id = (room.members.size + 1).toLong(),
                roomId = 0,
                remoteMemberId = request.memberId,
                displayName = request.memberDisplayName,
                role = RoomRole.MEMBER,
                presence = MemberPresence.ONLINE,
                joinedAt = System.currentTimeMillis()
            )
        } else {
            room.members
        }

        val updatedRoom = room.copy(members = updatedMembers)
        MockNetworkServer.rooms[roomId] = updatedRoom

        // Broadcast member joined
        MockNetworkServer.globalEventBus.tryEmit(
            SyncEvent(
                eventId = UUID.randomUUID().toString(),
                roomId = roomId,
                type = SyncEventType.MEMBER_JOINED,
                senderDeviceId = "device_${request.memberId}",
                senderMemberId = request.memberId,
                senderDisplayName = request.memberDisplayName,
                entityId = request.memberId
            )
        )

        return Result.success(updatedRoom)
    }

    override suspend fun leaveRoom(roomId: String, memberId: String): Result<Unit> {
        delay(200)
        val room = MockNetworkServer.rooms[roomId] ?: return Result.success(Unit)
        val updated = room.copy(members = room.members.filterNot { it.remoteMemberId == memberId })
        MockNetworkServer.rooms[roomId] = updated

        MockNetworkServer.globalEventBus.tryEmit(
            SyncEvent(
                eventId = UUID.randomUUID().toString(),
                roomId = roomId,
                type = SyncEventType.MEMBER_LEFT,
                senderDeviceId = "device_$memberId",
                senderMemberId = memberId,
                senderDisplayName = "Member",
                entityId = memberId
            )
        )
        return Result.success(Unit)
    }

    override suspend fun deleteRoom(roomId: String, ownerMemberId: String): Result<Unit> {
        delay(200)
        val room = MockNetworkServer.rooms[roomId] ?: return Result.success(Unit)
        if (room.ownerMemberId != ownerMemberId) {
            return Result.failure(IllegalStateException("Only the room owner can delete this room."))
        }
        MockNetworkServer.codeToRoomId.remove(room.roomCode)
        MockNetworkServer.rooms.remove(roomId)

        MockNetworkServer.globalEventBus.tryEmit(
            SyncEvent(
                eventId = UUID.randomUUID().toString(),
                roomId = roomId,
                type = SyncEventType.ROOM_DELETED,
                senderDeviceId = "device_$ownerMemberId",
                senderMemberId = ownerMemberId,
                senderDisplayName = room.ownerDisplayName,
                entityId = roomId
            )
        )
        return Result.success(Unit)
    }

    override suspend fun uploadSchedule(roomId: String, schedule: ScheduleWithParticipants, shareHistory: Boolean): Result<String> {
        delay(250)
        val room = MockNetworkServer.rooms[roomId] ?: return Result.failure(IllegalArgumentException("Room not found"))
        val remoteScheduleId = "rem_sched_${schedule.schedule.id}"
        val updatedSchedules = room.sharedSchedules + schedule
        MockNetworkServer.rooms[roomId] = room.copy(sharedSchedules = updatedSchedules)

        MockNetworkServer.globalEventBus.tryEmit(
            SyncEvent(
                eventId = UUID.randomUUID().toString(),
                roomId = roomId,
                type = SyncEventType.SCHEDULE_CREATED,
                senderDeviceId = "device_${room.ownerMemberId}",
                senderMemberId = room.ownerMemberId,
                senderDisplayName = room.ownerDisplayName,
                entityId = remoteScheduleId
            )
        )
        return Result.success(remoteScheduleId)
    }

    override suspend fun stopSharingSchedule(roomId: String, remoteScheduleId: String): Result<Unit> {
        delay(200)
        val room = MockNetworkServer.rooms[roomId] ?: return Result.success(Unit)
        val updated = room.copy(sharedSchedules = room.sharedSchedules.filterNot { "rem_sched_${it.schedule.id}" == remoteScheduleId })
        MockNetworkServer.rooms[roomId] = updated
        return Result.success(Unit)
    }

    override suspend fun pushSyncOperations(roomId: String, operations: List<SyncEvent>): Result<List<String>> {
        delay(250)
        val eventList = MockNetworkServer.eventsByRoom.getOrPut(roomId) { mutableListOf() }
        val processedIds = mutableListOf<String>()

        operations.forEach { op ->
            eventList.add(op)
            MockNetworkServer.globalEventBus.tryEmit(op)
            processedIds.add(op.eventId)
        }
        return Result.success(processedIds)
    }

    override suspend fun fetchLatestEvents(roomId: String, sinceTimestamp: Long): Result<List<SyncEvent>> {
        delay(150)
        val list = MockNetworkServer.eventsByRoom[roomId]?.filter { it.timestamp > sinceTimestamp } ?: emptyList()
        return Result.success(list)
    }
}

class MockRealtimeSyncClient : RealtimeSyncClient {
    private val _connectionState = MutableStateFlow(false)
    override val connectionState: Flow<Boolean> = _connectionState.asStateFlow()

    private val _incomingEvents = MutableSharedFlow<SyncEvent>(extraBufferCapacity = 64)
    override val incomingEvents: Flow<SyncEvent> = _incomingEvents.asSharedFlow()

    private var activeRoomId: String? = null
    private var currentMemberId: String? = null

    override suspend fun connect(roomId: String, memberId: String, displayName: String) {
        activeRoomId = roomId
        currentMemberId = memberId
        _connectionState.value = true
    }

    override suspend fun disconnect() {
        activeRoomId = null
        currentMemberId = null
        _connectionState.value = false
    }

    override suspend fun sendEvent(event: SyncEvent): Boolean {
        MockNetworkServer.globalEventBus.tryEmit(event)
        return true
    }

    override suspend fun updatePresence(presence: MemberPresence) {
        // Heartbeat or state change
    }
}
