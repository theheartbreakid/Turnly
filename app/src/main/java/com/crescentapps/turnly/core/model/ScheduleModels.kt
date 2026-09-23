package com.crescentapps.turnly.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Participant(
    val id: Long = 0,
    val name: String,
    val nickname: String? = null,
    val colorHex: String = "#3B82F6",
    val avatarEmoji: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Serializable
data class Schedule(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val type: ScheduleType = ScheduleType.GENERAL,
    val currencyCode: String = "INR",
    val defaultAmount: Double? = null,
    val frequencyType: FrequencyType = FrequencyType.DAILY,
    val frequencyInterval: Int = 1,
    val selectedWeekdays: List<Int> = emptyList(), // 1 = Monday ... 7 = Sunday (java.time.DayOfWeek)
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

@Serializable
data class RotationPatternItem(
    val id: Long = 0,
    val scheduleId: Long,
    val participantId: Long,
    val patternPosition: Int
)

@Serializable
data class TurnOccurrence(
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

@Serializable
data class SkipDate(
    val id: Long = 0,
    val scheduleId: Long,
    val date: String, // YYYY-MM-DD
    val reason: String = ""
)

@Serializable
data class ManualAssignment(
    val id: Long = 0,
    val scheduleId: Long,
    val date: String, // YYYY-MM-DD
    val participantId: Long,
    val reason: String = ""
)

data class ResolvedTurn(
    val schedule: Schedule,
    val participant: Participant,
    val date: String, // YYYY-MM-DD
    val status: OccurrenceStatus,
    val occurrenceId: Long? = null,
    val isManualOverride: Boolean = false,
    val expectedAmount: Double? = null,
    val actualAmount: Double? = null,
    val completedAt: Long? = null,
    val completedByMemberName: String? = null,
    val isShared: Boolean = false,
    val roomName: String? = null,
    val syncState: SyncState? = null,
    val note: String = "",
    val nextTurnParticipant: Participant? = null,
    val nextTurnDate: String? = null,
    val previousTurnParticipant: Participant? = null,
    val previousTurnDate: String? = null
)

data class ScheduleWithParticipants(
    val schedule: Schedule,
    val participants: List<Participant>,
    val pattern: List<RotationPatternItem>
)

data class ScheduleStats(
    val totalTurnsCount: Int = 0,
    val completedCount: Int = 0,
    val pendingCount: Int = 0,
    val skippedCount: Int = 0,
    val missedCount: Int = 0,
    val turnsPerParticipant: Map<Participant, Int> = emptyMap(),
    val totalExpectedAmount: Double = 0.0,
    val totalRecordedAmount: Double = 0.0,
    val currencyCode: String = "INR"
)
