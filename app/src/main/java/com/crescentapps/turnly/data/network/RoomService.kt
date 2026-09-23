package com.crescentapps.turnly.data.network

import com.crescentapps.turnly.core.model.*

data class CreateRoomRequest(
    val name: String,
    val description: String,
    val ownerMemberId: String,
    val ownerDisplayName: String,
    val initialSchedules: List<ScheduleWithParticipants> = emptyList()
)

data class CreateRoomResponse(
    val roomId: String,
    val roomCode: String,
    val inviteToken: String,
    val createdAt: Long
)

data class RoomMetadataResponse(
    val roomId: String,
    val roomCode: String,
    val name: String,
    val description: String,
    val ownerMemberId: String,
    val ownerDisplayName: String,
    val members: List<RoomMember>,
    val sharedSchedules: List<ScheduleWithParticipants>,
    val recentOccurrences: List<TurnOccurrence>
)

data class JoinRoomRequest(
    val roomCode: String,
    val inviteToken: String? = null,
    val memberId: String,
    val memberDisplayName: String
)

interface RoomService {
    suspend fun createRoom(request: CreateRoomRequest): Result<CreateRoomResponse>
    suspend fun previewRoom(roomCode: String): Result<RoomMetadataResponse>
    suspend fun joinRoom(request: JoinRoomRequest): Result<RoomMetadataResponse>
    suspend fun leaveRoom(roomId: String, memberId: String): Result<Unit>
    suspend fun deleteRoom(roomId: String, ownerMemberId: String): Result<Unit>
    suspend fun uploadSchedule(roomId: String, schedule: ScheduleWithParticipants, shareHistory: Boolean): Result<String>
    suspend fun stopSharingSchedule(roomId: String, remoteScheduleId: String): Result<Unit>
    suspend fun pushSyncOperations(roomId: String, operations: List<SyncEvent>): Result<List<String>>
    suspend fun fetchLatestEvents(roomId: String, sinceTimestamp: Long): Result<List<SyncEvent>>
}
