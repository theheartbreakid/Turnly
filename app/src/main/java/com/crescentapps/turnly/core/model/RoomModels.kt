package com.crescentapps.turnly.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class RoomRole {
    OWNER,
    MEMBER
}

@Serializable
enum class MemberPresence {
    ONLINE,
    OFFLINE
}

@Serializable
enum class SyncState {
    SYNCED,
    SYNCING,
    OFFLINE,
    CONFLICT
}

@Serializable
enum class SharingState {
    LOCAL,
    SHARED,
    STOPPED
}

@Serializable
enum class RoomPermission {
    VIEW_ONLY,
    CAN_COMPLETE,
    CAN_EDIT
}

@Serializable
enum class OperationSyncState {
    PENDING,
    IN_PROGRESS,
    FAILED,
    COMPLETED
}

@Serializable
data class P2PRoom(
    val id: Long = 0,
    val remoteRoomId: String,
    val name: String,
    val description: String = "",
    val role: RoomRole = RoomRole.MEMBER,
    val ownerMemberId: String,
    val ownerDisplayName: String = "Owner",
    val roomCode: String,
    val syncState: SyncState = SyncState.SYNCED,
    val lastSyncedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val memberCount: Int = 1,
    val sharedScheduleCount: Int = 0
)

@Serializable
data class RoomMember(
    val id: Long = 0,
    val roomId: Long,
    val remoteMemberId: String,
    val displayName: String,
    val role: RoomRole = RoomRole.MEMBER,
    val presence: MemberPresence = MemberPresence.ONLINE,
    val joinedAt: Long = System.currentTimeMillis(),
    val lastSeenAt: Long = System.currentTimeMillis()
)

@Serializable
data class SharedScheduleInfo(
    val roomId: Long,
    val localScheduleId: Long,
    val remoteScheduleId: String,
    val sharingState: SharingState = SharingState.SHARED,
    val permissions: RoomPermission = RoomPermission.CAN_COMPLETE,
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class RoomInvitePayload(
    val version: Int = 1,
    val roomId: String,
    val roomCode: String,
    val roomName: String,
    val ownerName: String,
    val inviteToken: String,
    val server: String = "api.turnly.app"
)

@Serializable
data class SyncConflict(
    val id: Long = 0,
    val roomId: Long,
    val scheduleId: Long,
    val scheduleName: String,
    val date: String,
    val localVersionDescription: String,
    val remoteVersionDescription: String,
    val localStatus: OccurrenceStatus,
    val remoteStatus: OccurrenceStatus,
    val completedByNameLocal: String? = null,
    val completedByNameRemote: String? = null,
    val detectedAt: Long = System.currentTimeMillis(),
    val isResolved: Boolean = false
)
