package com.crescentapps.turnly.data.repository

import com.crescentapps.turnly.core.model.*
import com.crescentapps.turnly.core.util.DateUtils
import com.crescentapps.turnly.data.local.TurnlyDatabase
import com.crescentapps.turnly.data.local.entity.*
import com.crescentapps.turnly.domain.engine.RotationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.time.LocalDate

class TurnlyRepository(
    private val db: TurnlyDatabase
) {
    private val participantDao = db.participantDao()
    private val scheduleDao = db.scheduleDao()
    private val occurrenceDao = db.occurrenceDao()

    // -------------------------------------------------------------
    // Participants
    // -------------------------------------------------------------
    val allActiveParticipants: Flow<List<Participant>> =
        participantDao.getAllActiveParticipantsFlow().map { entities ->
            entities.map { it.toDomain() }
        }

    val allParticipants: Flow<List<Participant>> =
        participantDao.getAllParticipantsFlow().map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun saveParticipant(participant: Participant): Long = withContext(Dispatchers.IO) {
        val entity = participant.toEntity()
        if (participant.id == 0L) {
            participantDao.insertParticipant(entity)
        } else {
            participantDao.updateParticipant(entity)
            participant.id
        }
    }

    suspend fun deactivateParticipant(id: Long) = withContext(Dispatchers.IO) {
        participantDao.deactivateParticipant(id)
    }

    suspend fun activateParticipant(id: Long) = withContext(Dispatchers.IO) {
        participantDao.activateParticipant(id)
    }

    // -------------------------------------------------------------
    // Schedules
    // -------------------------------------------------------------
    val activeSchedules: Flow<List<Schedule>> =
        scheduleDao.getActiveSchedulesFlow().map { entities ->
            entities.map { it.toDomain() }
        }

    val allSchedules: Flow<List<Schedule>> =
        scheduleDao.getAllSchedulesFlow().map { entities ->
            entities.map { it.toDomain() }
        }

    fun getScheduleFlow(id: Long): Flow<Schedule?> =
        scheduleDao.getScheduleFlow(id).map { it?.toDomain() }

    fun getParticipantsForScheduleFlow(scheduleId: Long): Flow<List<Participant>> =
        scheduleDao.getParticipantsForScheduleFlow(scheduleId).map { entities ->
            entities.map { it.toDomain() }
        }

    fun getRotationPatternFlow(scheduleId: Long): Flow<List<RotationPatternItem>> =
        scheduleDao.getRotationPatternFlow(scheduleId).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun saveScheduleWithDetails(
        schedule: Schedule,
        participantIds: List<Long>,
        patternParticipantIds: List<Long>
    ): Long = withContext(Dispatchers.IO) {
        val scheduleEntity = schedule.toEntity()
        val sId = if (schedule.id == 0L) {
            scheduleDao.insertSchedule(scheduleEntity)
        } else {
            scheduleDao.updateSchedule(scheduleEntity)
            schedule.id
        }

        // Update schedule participants
        scheduleDao.deleteScheduleParticipants(sId)
        val spList = participantIds.mapIndexed { index, pId ->
            ScheduleParticipantEntity(
                scheduleId = sId,
                participantId = pId,
                position = index,
                isActive = true
            )
        }
        scheduleDao.insertScheduleParticipants(spList)

        // Update rotation pattern
        scheduleDao.deleteRotationPatterns(sId)
        val patternList = patternParticipantIds.mapIndexed { index, pId ->
            RotationPatternEntity(
                scheduleId = sId,
                participantId = pId,
                patternPosition = index
            )
        }
        scheduleDao.insertRotationPatterns(patternList)

        sId
    }

    suspend fun archiveSchedule(scheduleId: Long) = withContext(Dispatchers.IO) {
        scheduleDao.archiveSchedule(scheduleId)
    }

    suspend fun unarchiveSchedule(scheduleId: Long) = withContext(Dispatchers.IO) {
        scheduleDao.unarchiveSchedule(scheduleId)
    }

    suspend fun setSchedulePauseState(scheduleId: Long, isPaused: Boolean, pauseUntil: String?) = withContext(Dispatchers.IO) {
        scheduleDao.setSchedulePauseState(scheduleId, isPaused, pauseUntil)
    }

    suspend fun deleteSchedulePermanently(scheduleId: Long) = withContext(Dispatchers.IO) {
        val schedule = scheduleDao.getScheduleById(scheduleId)
        if (schedule != null) {
            scheduleDao.deleteSchedule(schedule)
        }
    }

    // -------------------------------------------------------------
    // Skip Dates & Manual Overrides
    // -------------------------------------------------------------
    fun getSkipDatesFlow(scheduleId: Long): Flow<List<SkipDate>> =
        scheduleDao.getSkipDatesFlow(scheduleId).map { it.map { s -> s.toDomain() } }

    fun getManualAssignmentsFlow(scheduleId: Long): Flow<List<ManualAssignment>> =
        scheduleDao.getManualAssignmentsFlow(scheduleId).map { it.map { m -> m.toDomain() } }

    suspend fun addSkipDate(scheduleId: Long, date: String, reason: String = "") = withContext(Dispatchers.IO) {
        scheduleDao.insertSkipDate(SkipDateEntity(scheduleId = scheduleId, date = date, reason = reason))
    }

    suspend fun removeSkipDate(scheduleId: Long, date: String) = withContext(Dispatchers.IO) {
        scheduleDao.removeSkipDate(scheduleId, date)
    }

    suspend fun setManualOverride(scheduleId: Long, date: String, participantId: Long, reason: String = "") = withContext(Dispatchers.IO) {
        scheduleDao.insertManualAssignment(ManualAssignmentEntity(scheduleId = scheduleId, date = date, participantId = participantId, reason = reason))
    }

    suspend fun removeManualOverride(scheduleId: Long, date: String) = withContext(Dispatchers.IO) {
        scheduleDao.removeManualAssignment(scheduleId, date)
    }

    // -------------------------------------------------------------
    // Occurrences (Completion / History)
    // -------------------------------------------------------------
    val allOccurrencesFlow: Flow<List<TurnOccurrence>> =
        occurrenceDao.getAllOccurrencesFlow().map { it.map { o -> o.toDomain() } }

    fun getOccurrencesForScheduleFlow(scheduleId: Long): Flow<List<TurnOccurrence>> =
        occurrenceDao.getOccurrencesForScheduleFlow(scheduleId).map { it.map { o -> o.toDomain() } }

    suspend fun markTurnComplete(
        scheduleId: Long,
        date: String,
        participantId: Long,
        actualAmount: Double? = null,
        note: String = "",
        completedByMemberName: String? = null
    ): Long = withContext(Dispatchers.IO) {
        val schedule = scheduleDao.getScheduleById(scheduleId)?.toDomain()
        val existing = occurrenceDao.getOccurrence(scheduleId, date)

        val entity = OccurrenceEntity(
            id = existing?.id ?: 0L,
            scheduleId = scheduleId,
            date = date,
            participantId = participantId,
            status = OccurrenceStatus.COMPLETED,
            isManualOverride = existing?.isManualOverride ?: false,
            expectedAmount = existing?.expectedAmount ?: schedule?.defaultAmount,
            actualAmount = actualAmount ?: existing?.actualAmount ?: schedule?.defaultAmount,
            completedAt = System.currentTimeMillis(),
            completedByMemberName = completedByMemberName ?: existing?.completedByMemberName,
            note = note.ifBlank { existing?.note.orEmpty() },
            entityVersion = (existing?.entityVersion ?: 0L) + 1L
        )
        val id = occurrenceDao.insertOccurrence(entity)
        id
    }

    suspend fun undoTurnCompletion(scheduleId: Long, date: String) = withContext(Dispatchers.IO) {
        occurrenceDao.deleteOccurrenceByDate(scheduleId, date)
    }

    suspend fun markTurnSkipped(
        scheduleId: Long,
        date: String,
        participantId: Long,
        note: String = ""
    ) = withContext(Dispatchers.IO) {
        val existing = occurrenceDao.getOccurrence(scheduleId, date)
        val entity = OccurrenceEntity(
            id = existing?.id ?: 0L,
            scheduleId = scheduleId,
            date = date,
            participantId = participantId,
            status = OccurrenceStatus.SKIPPED,
            note = note,
            entityVersion = (existing?.entityVersion ?: 0L) + 1L
        )
        occurrenceDao.insertOccurrence(entity)
    }

    // -------------------------------------------------------------
    // Active Today's Turns Resolution
    // -------------------------------------------------------------
    // -------------------------------------------------------------
    // Active Today's Turns Resolution - High Performance Batch Flow
    // -------------------------------------------------------------
    fun getTodayTurnsFlow(targetDate: LocalDate = DateUtils.today()): Flow<List<ResolvedTurn>> {
        return combine(
            scheduleDao.getActiveSchedulesFlow(),
            occurrenceDao.getAllOccurrencesFlow(),
            db.roomDao().getAllSharedSchedulesFlow(),
            db.roomDao().getAllRoomsFlow(),
            participantDao.getAllParticipantsFlow()
        ) { schedules, occurrences, sharedSchedules, rooms, allParticipants ->
            val roomMap = rooms.associateBy { it.id }
            val sharedMap = sharedSchedules.associateBy { it.localScheduleId }
            val participantMap = allParticipants.associate { it.id to it.toDomain() }

            withContext(Dispatchers.Default) {
                // Batch-fetch all mapping tables once instead of per-schedule
                val allScheduleParticipants = scheduleDao.getAllScheduleParticipants()
                    .groupBy { it.scheduleId }
                val allPatterns = scheduleDao.getAllRotationPatterns()
                    .groupBy { it.scheduleId }
                val allSkips = scheduleDao.getAllSkipDates()
                    .groupBy { it.scheduleId }
                val allManuals = scheduleDao.getAllManualAssignments()
                    .groupBy { it.scheduleId }
                val occurrencesBySchedule = occurrences
                    .groupBy { it.scheduleId }

                schedules.mapNotNull { scheduleEntity ->
                    val schedule = scheduleEntity.toDomain()
                    val sId = schedule.id

                    val participants = (allScheduleParticipants[sId] ?: emptyList())
                        .sortedBy { it.position }
                        .mapNotNull { participantMap[it.participantId] }

                    val pattern = (allPatterns[sId] ?: emptyList())
                        .sortedBy { it.patternPosition }
                        .map { it.toDomain() }

                    val skips = (allSkips[sId] ?: emptyList())
                        .map { it.date }
                        .toSet()

                    val manual = (allManuals[sId] ?: emptyList())
                        .associate { it.date to it.participantId }

                    val scheduleOccs = (occurrencesBySchedule[sId] ?: emptyList())
                        .associate { it.date to it.toDomain() }

                    val sharedInfo = sharedMap[sId]
                    val room = sharedInfo?.let { roomMap[it.roomId] }

                    val resolved = RotationEngine.resolveTurnForDate(
                        schedule = schedule,
                        participants = participants,
                        pattern = pattern,
                        targetDate = targetDate,
                        manualAssignments = manual,
                        skipDates = skips,
                        persistedOccurrences = scheduleOccs
                    )

                    resolved?.let { r ->
                        val occ = scheduleOccs[r.date]
                        r.copy(
                            completedAt = occ?.completedAt,
                            completedByMemberName = occ?.completedByMemberName,
                            isShared = sharedInfo != null,
                            roomName = room?.name,
                            syncState = room?.syncState
                        )
                    }
                }.sortedWith(
                    compareBy<ResolvedTurn> {
                        if (it.status == OccurrenceStatus.PENDING) 0 else 1
                    }.thenBy { it.schedule.name }
                )
            }
        }
    }

    /**
     * Batch snapshot for high-performance Calendar resolution without N+1 queries.
     */
    data class MonthScheduleContext(
        val schedules: List<Schedule>,
        val participantsBySchedule: Map<Long, List<Participant>>,
        val patternBySchedule: Map<Long, List<RotationPatternItem>>,
        val skipsBySchedule: Map<Long, Set<String>>,
        val manualBySchedule: Map<Long, Map<String, Long>>,
        val occurrencesBySchedule: Map<Long, Map<String, TurnOccurrence>>
    )

    suspend fun getMonthScheduleContext(startDate: String, endDate: String): MonthScheduleContext = withContext(Dispatchers.IO) {
        val schedules = scheduleDao.getActiveSchedulesFlow().first().map { it.toDomain() }
        val allParticipants = participantDao.getAllParticipants().associate { it.id to it.toDomain() }
        val allScheduleParticipants = scheduleDao.getAllScheduleParticipants().groupBy { it.scheduleId }
        val allPatterns = scheduleDao.getAllRotationPatterns().groupBy { it.scheduleId }
        val allSkips = scheduleDao.getAllSkipDates().groupBy { it.scheduleId }
        val allManuals = scheduleDao.getAllManualAssignments().groupBy { it.scheduleId }
        val occurrences = occurrenceDao.getOccurrencesBetween(startDate, endDate).groupBy { it.scheduleId }

        MonthScheduleContext(
            schedules = schedules,
            participantsBySchedule = schedules.associate { s ->
                s.id to (allScheduleParticipants[s.id] ?: emptyList())
                    .sortedBy { it.position }
                    .mapNotNull { allParticipants[it.participantId] }
            },
            patternBySchedule = schedules.associate { s ->
                s.id to (allPatterns[s.id] ?: emptyList())
                    .sortedBy { it.patternPosition }
                    .map { it.toDomain() }
            },
            skipsBySchedule = schedules.associate { s ->
                s.id to (allSkips[s.id] ?: emptyList()).map { it.date }.toSet()
            },
            manualBySchedule = schedules.associate { s ->
                s.id to (allManuals[s.id] ?: emptyList()).associate { it.date to it.participantId }
            },
            occurrencesBySchedule = schedules.associate { s ->
                s.id to (occurrences[s.id] ?: emptyList()).associate { it.date to it.toDomain() }
            }
        )
    }

    // -------------------------------------------------------------
    // Automatic History Reconciliation on Launch
    // -------------------------------------------------------------
    suspend fun reconcileMissedHistory() = withContext(Dispatchers.IO) {
        val today = DateUtils.today()
        val schedules = scheduleDao.getAllSchedules()

        for (scheduleEntity in schedules) {
            val schedule = scheduleEntity.toDomain()
            val startDate = DateUtils.parse(schedule.startDate)
            if (today.isBefore(startDate)) continue

            val participants = scheduleDao.getParticipantsForSchedule(schedule.id).map { it.toDomain() }
            val pattern = scheduleDao.getRotationPattern(schedule.id).map { it.toDomain() }
            val skips = scheduleDao.getSkipDates(schedule.id).map { it.date }.toSet()
            val manual = scheduleDao.getManualAssignments(schedule.id).associate { it.date to it.participantId }
            val existingOccs = occurrenceDao.getOccurrencesForSchedule(schedule.id).associateBy { it.date }

            // Check up to past 30 days or start date, whichever is later
            val checkStart = startDate.coerceAtLeast(today.minusDays(30))
            var cursor = checkStart
            val newMissed = mutableListOf<OccurrenceEntity>()

            while (cursor.isBefore(today)) {
                val cursorStr = DateUtils.format(cursor)
                if (RotationEngine.isScheduledDate(schedule, cursor, skips) && !existingOccs.containsKey(cursorStr)) {
                    val p = RotationEngine.calculateParticipantForDate(
                        schedule, participants, pattern, cursor,
                        manual, skips, existingOccs.mapValues { it.value.toDomain() }
                    )
                    if (p != null) {
                        newMissed.add(
                            OccurrenceEntity(
                                scheduleId = schedule.id,
                                date = cursorStr,
                                participantId = p.id,
                                status = OccurrenceStatus.MISSED,
                                expectedAmount = schedule.defaultAmount
                            )
                        )
                    }
                }
                cursor = cursor.plusDays(1)
            }

            if (newMissed.isNotEmpty()) {
                occurrenceDao.insertOccurrences(newMissed)
            }
        }
    }

    // -------------------------------------------------------------
    // Full Data Export & Import
    // -------------------------------------------------------------
    suspend fun exportAllData(): TurnlyExportData = withContext(Dispatchers.IO) {
        TurnlyExportData(
            schemaVersion = 1,
            exportTimestamp = System.currentTimeMillis(),
            participants = participantDao.getAllParticipants().map { it.toDomain() },
            schedules = scheduleDao.getAllSchedules().map { it.toDomain() },
            occurrences = occurrenceDao.getAllOccurrences().map { it.toDomain() }
        )
    }

    suspend fun resetDatabase() = withContext(Dispatchers.IO) {
        db.clearAllTables()
    }
}

// -------------------------------------------------------------
// Mapping Extensions
// -------------------------------------------------------------
fun ParticipantEntity.toDomain() = Participant(
    id = id,
    name = name,
    nickname = nickname,
    colorHex = colorHex,
    avatarEmoji = avatarEmoji,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Participant.toEntity() = ParticipantEntity(
    id = id,
    name = name,
    nickname = nickname,
    colorHex = colorHex,
    avatarEmoji = avatarEmoji,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ScheduleEntity.toDomain() = Schedule(
    id = id,
    name = name,
    description = description,
    type = type,
    currencyCode = currencyCode,
    defaultAmount = defaultAmount,
    frequencyType = frequencyType,
    frequencyInterval = frequencyInterval,
    selectedWeekdays = if (selectedWeekdays.isBlank()) emptyList() else selectedWeekdays.split(",").mapNotNull { it.toIntOrNull() },
    dayOfMonth = dayOfMonth,
    startDate = startDate,
    timezone = timezone,
    isPaused = isPaused,
    pauseUntil = pauseUntil,
    overrideStrategy = overrideStrategy,
    notifyTimeHour = notifyTimeHour,
    notifyTimeMinute = notifyTimeMinute,
    notificationsEnabled = notificationsEnabled,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Schedule.toEntity() = ScheduleEntity(
    id = id,
    name = name,
    description = description,
    type = type,
    currencyCode = currencyCode,
    defaultAmount = defaultAmount,
    frequencyType = frequencyType,
    frequencyInterval = frequencyInterval,
    selectedWeekdays = selectedWeekdays.joinToString(","),
    dayOfMonth = dayOfMonth,
    startDate = startDate,
    timezone = timezone,
    isPaused = isPaused,
    pauseUntil = pauseUntil,
    overrideStrategy = overrideStrategy,
    notifyTimeHour = notifyTimeHour,
    notifyTimeMinute = notifyTimeMinute,
    notificationsEnabled = notificationsEnabled,
    isArchived = isArchived,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun RotationPatternEntity.toDomain() = RotationPatternItem(
    id = id,
    scheduleId = scheduleId,
    participantId = participantId,
    patternPosition = patternPosition
)

fun OccurrenceEntity.toDomain() = TurnOccurrence(
    id = id,
    scheduleId = scheduleId,
    date = date,
    participantId = participantId,
    status = status,
    isManualOverride = isManualOverride,
    expectedAmount = expectedAmount,
    actualAmount = actualAmount,
    completedAt = completedAt,
    completedByMemberName = completedByMemberName,
    note = note,
    entityVersion = entityVersion,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun SkipDateEntity.toDomain() = SkipDate(
    id = id,
    scheduleId = scheduleId,
    date = date,
    reason = reason
)

fun ManualAssignmentEntity.toDomain() = ManualAssignment(
    id = id,
    scheduleId = scheduleId,
    date = date,
    participantId = participantId,
    reason = reason
)

@kotlinx.serialization.Serializable
data class TurnlyExportData(
    val schemaVersion: Int = 1,
    val exportTimestamp: Long,
    val participants: List<Participant>,
    val schedules: List<Schedule>,
    val occurrences: List<TurnOccurrence>
)
