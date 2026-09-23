package com.crescentapps.turnly.data.local.entity

import androidx.room.*
import com.crescentapps.turnly.core.model.FrequencyType
import com.crescentapps.turnly.core.model.OccurrenceStatus
import com.crescentapps.turnly.core.model.OverrideStrategy
import com.crescentapps.turnly.core.model.ScheduleType

@Entity(tableName = "participants")
data class ParticipantEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val nickname: String? = null,
    val colorHex: String,
    val avatarEmoji: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val type: ScheduleType = ScheduleType.GENERAL,
    val currencyCode: String = "INR",
    val defaultAmount: Double? = null,
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val frequencyInterval: Int = 1,
    val selectedWeekdays: String = "", // comma-separated e.g. "1,3,5"
    val dayOfMonth: Int? = 1,
    val startDate: String, // YYYY-MM-DD
    val timezone: String = "UTC",
    val isPaused: Boolean = false,
    val pauseUntil: String? = null, // YYYY-MM-DD
    val overrideStrategy: OverrideStrategy = OverrideStrategy.SINGLE_DATE_ONLY,
    val notifyTimeHour: Int = 9,
    val notifyTimeMinute: Int = 0,
    val notificationsEnabled: Boolean = true,
    val isArchived: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "schedule_participants",
    primaryKeys = ["scheduleId", "participantId"],
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ParticipantEntity::class,
            parentColumns = ["id"],
            childColumns = ["participantId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("scheduleId"), Index("participantId")]
)
data class ScheduleParticipantEntity(
    val scheduleId: Long,
    val participantId: Long,
    val position: Int,
    val isActive: Boolean = true
)

@Entity(
    tableName = "rotation_patterns",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ParticipantEntity::class,
            parentColumns = ["id"],
            childColumns = ["participantId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [Index("scheduleId"), Index("participantId")]
)
data class RotationPatternEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scheduleId: Long,
    val participantId: Long,
    val patternPosition: Int
)

@Entity(
    tableName = "occurrences",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ParticipantEntity::class,
            parentColumns = ["id"],
            childColumns = ["participantId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["scheduleId", "date"], unique = true),
        Index(value = ["scheduleId", "status"]),
        Index(value = ["date", "status"]),
        Index("participantId"),
        Index("date")
    ]
)
data class OccurrenceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scheduleId: Long,
    val date: String, // YYYY-MM-DD
    val participantId: Long,
    val status: OccurrenceStatus = OccurrenceStatus.PENDING,
    val isManualOverride: Boolean = false,
    val expectedAmount: Double? = null,
    val actualAmount: Double? = null,
    val completedAt: Long? = null,
    val completedByMemberName: String? = null,
    val note: String = "",
    val entityVersion: Long = 1L,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "skip_dates",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["scheduleId", "date"], unique = true)]
)
data class SkipDateEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scheduleId: Long,
    val date: String, // YYYY-MM-DD
    val reason: String = ""
)

@Entity(
    tableName = "manual_assignments",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ParticipantEntity::class,
            parentColumns = ["id"],
            childColumns = ["participantId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["scheduleId", "date"], unique = true),
        Index("participantId")
    ]
)
data class ManualAssignmentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val scheduleId: Long,
    val date: String, // YYYY-MM-DD
    val participantId: Long,
    val reason: String = ""
)

// =========================================================================
// P2P Room Entities
// =========================================================================

@Entity(
    tableName = "rooms",
    indices = [
        Index(value = ["remoteRoomId"], unique = true),
        Index(value = ["roomCode"])
    ]
)
data class RoomEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val remoteRoomId: String,
    val name: String,
    val description: String = "",
    val role: com.crescentapps.turnly.core.model.RoomRole = com.crescentapps.turnly.core.model.RoomRole.MEMBER,
    val ownerMemberId: String,
    val ownerDisplayName: String = "Owner",
    val roomCode: String,
    val syncState: com.crescentapps.turnly.core.model.SyncState = com.crescentapps.turnly.core.model.SyncState.SYNCED,
    val lastSyncedAt: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "room_members",
    foreignKeys = [
        ForeignKey(
            entity = RoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("roomId"),
        Index(value = ["roomId", "remoteMemberId"], unique = true)
    ]
)
data class RoomMemberEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roomId: Long,
    val remoteMemberId: String,
    val displayName: String,
    val role: com.crescentapps.turnly.core.model.RoomRole = com.crescentapps.turnly.core.model.RoomRole.MEMBER,
    val presence: com.crescentapps.turnly.core.model.MemberPresence = com.crescentapps.turnly.core.model.MemberPresence.ONLINE,
    val joinedAt: Long = System.currentTimeMillis(),
    val lastSeenAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "shared_schedules",
    foreignKeys = [
        ForeignKey(
            entity = RoomEntity::class,
            parentColumns = ["id"],
            childColumns = ["roomId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ScheduleEntity::class,
            parentColumns = ["id"],
            childColumns = ["localScheduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("roomId"),
        Index(value = ["localScheduleId"], unique = true),
        Index(value = ["roomId", "remoteScheduleId"], unique = true)
    ]
)
data class SharedScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val roomId: Long,
    val localScheduleId: Long,
    val remoteScheduleId: String,
    val sharingState: com.crescentapps.turnly.core.model.SharingState = com.crescentapps.turnly.core.model.SharingState.SHARED,
    val permissions: com.crescentapps.turnly.core.model.RoomPermission = com.crescentapps.turnly.core.model.RoomPermission.CAN_COMPLETE,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "sync_operations",
    indices = [
        Index("roomId"),
        Index("syncState"),
        Index("createdAt")
    ]
)
data class SyncOperationEntity(
    @PrimaryKey
    val id: String, // UUID
    val roomId: Long,
    val entityType: String,
    val entityId: String,
    val operationType: String,
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val retryCount: Int = 0,
    val syncState: com.crescentapps.turnly.core.model.OperationSyncState = com.crescentapps.turnly.core.model.OperationSyncState.PENDING,
    val lastError: String? = null
)

@Entity(
    tableName = "sync_conflicts",
    indices = [
        Index("roomId"),
        Index("scheduleId"),
        Index("isResolved")
    ]
)
data class SyncConflictEntity(
    @PrimaryKey(autoGenerate = true)
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
