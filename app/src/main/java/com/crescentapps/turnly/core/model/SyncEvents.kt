package com.crescentapps.turnly.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class SyncEventType {
    ROOM_UPDATED,
    MEMBER_JOINED,
    MEMBER_LEFT,
    SCHEDULE_CREATED,
    SCHEDULE_UPDATED,
    SCHEDULE_ARCHIVED,
    OCCURRENCE_UPDATED,
    OCCURRENCE_COMPLETED,
    OCCURRENCE_SKIPPED,
    MANUAL_OVERRIDE_CREATED,
    MANUAL_OVERRIDE_REMOVED,
    SYNC_CONFLICT,
    ROOM_DELETED,
    INVITE_REVOKED
}

@Serializable
data class SyncEvent(
    val eventId: String,
    val roomId: String,
    val type: SyncEventType,
    val senderDeviceId: String,
    val senderMemberId: String,
    val senderDisplayName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val entityId: String,
    val entityVersion: Long = 1L,
    val payloadJson: String = ""
)

@Serializable
data class OccurrenceCompletedPayload(
    val scheduleIdRemote: String,
    val date: String,
    val participantIdRemote: String,
    val completedByMemberId: String,
    val completedByName: String,
    val actualAmount: Double? = null,
    val note: String = "",
    val completedAt: Long = System.currentTimeMillis()
)

@Serializable
data class OccurrenceSkippedPayload(
    val scheduleIdRemote: String,
    val date: String,
    val skippedByMemberId: String,
    val note: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Serializable
data class ManualOverridePayload(
    val scheduleIdRemote: String,
    val date: String,
    val participantIdRemote: String,
    val reason: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
