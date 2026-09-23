package com.crescentapps.turnly.data.local.dao

import androidx.room.*
import com.crescentapps.turnly.core.model.OccurrenceStatus
import com.crescentapps.turnly.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ParticipantDao {
    @Query("SELECT * FROM participants WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveParticipantsFlow(): Flow<List<ParticipantEntity>>

    @Query("SELECT * FROM participants ORDER BY name ASC")
    fun getAllParticipantsFlow(): Flow<List<ParticipantEntity>>

    @Query("SELECT * FROM participants")
    suspend fun getAllParticipants(): List<ParticipantEntity>

    @Query("SELECT * FROM participants WHERE id = :id")
    suspend fun getParticipantById(id: Long): ParticipantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: ParticipantEntity): Long

    @Update
    suspend fun updateParticipant(participant: ParticipantEntity)

    @Query("UPDATE participants SET isActive = 0 WHERE id = :id")
    suspend fun deactivateParticipant(id: Long)

    @Query("UPDATE participants SET isActive = 1 WHERE id = :id")
    suspend fun activateParticipant(id: Long)
}

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getActiveSchedulesFlow(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules ORDER BY createdAt DESC")
    fun getAllSchedulesFlow(): Flow<List<ScheduleEntity>>

    @Query("SELECT * FROM schedules")
    suspend fun getAllSchedules(): List<ScheduleEntity>

    @Query("SELECT * FROM schedules WHERE id = :id")
    fun getScheduleFlow(id: Long): Flow<ScheduleEntity?>

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getScheduleById(id: Long): ScheduleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ScheduleEntity): Long

    @Update
    suspend fun updateSchedule(schedule: ScheduleEntity)

    @Query("UPDATE schedules SET isArchived = 1 WHERE id = :id")
    suspend fun archiveSchedule(id: Long)

    @Query("UPDATE schedules SET isArchived = 0 WHERE id = :id")
    suspend fun unarchiveSchedule(id: Long)

    @Query("UPDATE schedules SET isPaused = :isPaused, pauseUntil = :pauseUntil WHERE id = :id")
    suspend fun setSchedulePauseState(id: Long, isPaused: Boolean, pauseUntil: String?)

    @Delete
    suspend fun deleteSchedule(schedule: ScheduleEntity)

    // Schedule Participants
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScheduleParticipants(items: List<ScheduleParticipantEntity>)

    @Query("DELETE FROM schedule_participants WHERE scheduleId = :scheduleId")
    suspend fun deleteScheduleParticipants(scheduleId: Long)

    @Query("""
        SELECT p.* FROM participants p
        INNER JOIN schedule_participants sp ON p.id = sp.participantId
        WHERE sp.scheduleId = :scheduleId
        ORDER BY sp.position ASC
    """)
    fun getParticipantsForScheduleFlow(scheduleId: Long): Flow<List<ParticipantEntity>>

    @Query("""
        SELECT p.* FROM participants p
        INNER JOIN schedule_participants sp ON p.id = sp.participantId
        WHERE sp.scheduleId = :scheduleId
        ORDER BY sp.position ASC
    """)
    suspend fun getParticipantsForSchedule(scheduleId: Long): List<ParticipantEntity>

    // Rotation Pattern
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRotationPatterns(items: List<RotationPatternEntity>)

    @Query("DELETE FROM rotation_patterns WHERE scheduleId = :scheduleId")
    suspend fun deleteRotationPatterns(scheduleId: Long)

    @Query("SELECT * FROM rotation_patterns WHERE scheduleId = :scheduleId ORDER BY patternPosition ASC")
    fun getRotationPatternFlow(scheduleId: Long): Flow<List<RotationPatternEntity>>

    @Query("SELECT * FROM rotation_patterns WHERE scheduleId = :scheduleId ORDER BY patternPosition ASC")
    suspend fun getRotationPattern(scheduleId: Long): List<RotationPatternEntity>

    // Skip Dates
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSkipDate(skipDate: SkipDateEntity): Long

    @Query("DELETE FROM skip_dates WHERE scheduleId = :scheduleId AND date = :date")
    suspend fun removeSkipDate(scheduleId: Long, date: String)

    @Query("SELECT * FROM skip_dates WHERE scheduleId = :scheduleId")
    fun getSkipDatesFlow(scheduleId: Long): Flow<List<SkipDateEntity>>

    @Query("SELECT * FROM skip_dates WHERE scheduleId = :scheduleId")
    suspend fun getSkipDates(scheduleId: Long): List<SkipDateEntity>

    // Manual Assignments
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertManualAssignment(assignment: ManualAssignmentEntity): Long

    @Query("DELETE FROM manual_assignments WHERE scheduleId = :scheduleId AND date = :date")
    suspend fun removeManualAssignment(scheduleId: Long, date: String)

    @Query("SELECT * FROM manual_assignments WHERE scheduleId = :scheduleId")
    fun getManualAssignmentsFlow(scheduleId: Long): Flow<List<ManualAssignmentEntity>>

    @Query("SELECT * FROM manual_assignments WHERE scheduleId = :scheduleId")
    suspend fun getManualAssignments(scheduleId: Long): List<ManualAssignmentEntity>

    // Batch Queries for high-performance in-memory resolution
    @Query("SELECT * FROM schedule_participants")
    fun getAllScheduleParticipantsFlow(): Flow<List<ScheduleParticipantEntity>>

    @Query("SELECT * FROM schedule_participants")
    suspend fun getAllScheduleParticipants(): List<ScheduleParticipantEntity>

    @Query("SELECT * FROM rotation_patterns")
    fun getAllRotationPatternsFlow(): Flow<List<RotationPatternEntity>>

    @Query("SELECT * FROM rotation_patterns")
    suspend fun getAllRotationPatterns(): List<RotationPatternEntity>

    @Query("SELECT * FROM skip_dates")
    fun getAllSkipDatesFlow(): Flow<List<SkipDateEntity>>

    @Query("SELECT * FROM skip_dates")
    suspend fun getAllSkipDates(): List<SkipDateEntity>

    @Query("SELECT * FROM manual_assignments")
    fun getAllManualAssignmentsFlow(): Flow<List<ManualAssignmentEntity>>

    @Query("SELECT * FROM manual_assignments")
    suspend fun getAllManualAssignments(): List<ManualAssignmentEntity>
}

@Dao
interface OccurrenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOccurrence(occurrence: OccurrenceEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOccurrences(occurrences: List<OccurrenceEntity>)

    @Update
    suspend fun updateOccurrence(occurrence: OccurrenceEntity)

    @Query("SELECT * FROM occurrences WHERE scheduleId = :scheduleId AND date = :date LIMIT 1")
    suspend fun getOccurrence(scheduleId: Long, date: String): OccurrenceEntity?

    @Query("SELECT * FROM occurrences WHERE scheduleId = :scheduleId AND date = :date LIMIT 1")
    fun getOccurrenceFlow(scheduleId: Long, date: String): Flow<OccurrenceEntity?>

    @Query("SELECT * FROM occurrences WHERE scheduleId = :scheduleId ORDER BY date DESC")
    fun getOccurrencesForScheduleFlow(scheduleId: Long): Flow<List<OccurrenceEntity>>

    @Query("SELECT * FROM occurrences WHERE scheduleId = :scheduleId ORDER BY date DESC")
    suspend fun getOccurrencesForSchedule(scheduleId: Long): List<OccurrenceEntity>

    @Query("SELECT * FROM occurrences ORDER BY date DESC")
    fun getAllOccurrencesFlow(): Flow<List<OccurrenceEntity>>

    @Query("SELECT * FROM occurrences")
    suspend fun getAllOccurrences(): List<OccurrenceEntity>

    @Query("SELECT * FROM occurrences WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getOccurrencesBetweenFlow(startDate: String, endDate: String): Flow<List<OccurrenceEntity>>

    @Query("SELECT * FROM occurrences WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    suspend fun getOccurrencesBetween(startDate: String, endDate: String): List<OccurrenceEntity>

    @Query("DELETE FROM occurrences WHERE id = :id")
    suspend fun deleteOccurrence(id: Long)

    @Query("DELETE FROM occurrences WHERE scheduleId = :scheduleId AND date = :date")
    suspend fun deleteOccurrenceByDate(scheduleId: Long, date: String)
}

@Dao
interface RoomDao {
    @Query("SELECT * FROM rooms ORDER BY updatedAt DESC")
    fun getAllRoomsFlow(): Flow<List<RoomEntity>>

    @Query("SELECT * FROM rooms ORDER BY updatedAt DESC")
    suspend fun getAllRooms(): List<RoomEntity>

    @Query("SELECT * FROM rooms WHERE id = :id")
    fun getRoomFlow(id: Long): Flow<RoomEntity?>

    @Query("SELECT * FROM rooms WHERE id = :id")
    suspend fun getRoomById(id: Long): RoomEntity?

    @Query("SELECT * FROM rooms WHERE remoteRoomId = :remoteRoomId")
    suspend fun getRoomByRemoteId(remoteRoomId: String): RoomEntity?

    @Query("SELECT * FROM rooms WHERE roomCode = :roomCode")
    suspend fun getRoomByCode(roomCode: String): RoomEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoom(room: RoomEntity): Long

    @Update
    suspend fun updateRoom(room: RoomEntity)

    @Delete
    suspend fun deleteRoom(room: RoomEntity)

    @Query("DELETE FROM rooms WHERE id = :id")
    suspend fun deleteRoomById(id: Long)

    @Query("UPDATE rooms SET syncState = :syncState, lastSyncedAt = :lastSyncedAt WHERE id = :id")
    suspend fun updateRoomSyncState(id: Long, syncState: com.crescentapps.turnly.core.model.SyncState, lastSyncedAt: Long = System.currentTimeMillis())

    // Room Members
    @Query("SELECT * FROM room_members WHERE roomId = :roomId ORDER BY role ASC, displayName ASC")
    fun getMembersForRoomFlow(roomId: Long): Flow<List<RoomMemberEntity>>

    @Query("SELECT * FROM room_members WHERE roomId = :roomId")
    suspend fun getMembersForRoom(roomId: Long): List<RoomMemberEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembers(members: List<RoomMemberEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: RoomMemberEntity): Long

    @Query("DELETE FROM room_members WHERE roomId = :roomId AND remoteMemberId = :remoteMemberId")
    suspend fun deleteMember(roomId: Long, remoteMemberId: String)

    @Query("DELETE FROM room_members WHERE roomId = :roomId")
    suspend fun deleteAllMembersForRoom(roomId: Long)

    @Query("UPDATE room_members SET presence = :presence, lastSeenAt = :lastSeenAt WHERE roomId = :roomId AND remoteMemberId = :remoteMemberId")
    suspend fun updateMemberPresence(roomId: Long, remoteMemberId: String, presence: com.crescentapps.turnly.core.model.MemberPresence, lastSeenAt: Long = System.currentTimeMillis())

    // Shared Schedules
    @Query("SELECT * FROM shared_schedules WHERE roomId = :roomId")
    fun getSharedSchedulesForRoomFlow(roomId: Long): Flow<List<SharedScheduleEntity>>

    @Query("SELECT * FROM shared_schedules WHERE roomId = :roomId")
    suspend fun getSharedSchedulesForRoom(roomId: Long): List<SharedScheduleEntity>

    @Query("SELECT * FROM shared_schedules WHERE localScheduleId = :scheduleId LIMIT 1")
    fun getSharedScheduleForLocalIdFlow(scheduleId: Long): Flow<SharedScheduleEntity?>

    @Query("SELECT * FROM shared_schedules WHERE localScheduleId = :scheduleId LIMIT 1")
    suspend fun getSharedScheduleForLocalId(scheduleId: Long): SharedScheduleEntity?

    @Query("SELECT * FROM shared_schedules")
    fun getAllSharedSchedulesFlow(): Flow<List<SharedScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedSchedule(sharedSchedule: SharedScheduleEntity): Long

    @Query("DELETE FROM shared_schedules WHERE roomId = :roomId AND localScheduleId = :scheduleId")
    suspend fun deleteSharedSchedule(roomId: Long, scheduleId: Long)

    @Query("DELETE FROM shared_schedules WHERE localScheduleId = :scheduleId")
    suspend fun deleteSharedScheduleByScheduleId(scheduleId: Long)

    // Sync Operations Queue
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun enqueueOperation(operation: SyncOperationEntity)

    @Query("SELECT * FROM sync_operations WHERE syncState = 'PENDING' ORDER BY createdAt ASC")
    suspend fun getPendingOperations(): List<SyncOperationEntity>

    @Query("SELECT COUNT(*) FROM sync_operations WHERE syncState = 'PENDING'")
    fun getPendingOperationsCountFlow(): Flow<Int>

    @Update
    suspend fun updateOperation(operation: SyncOperationEntity)

    @Query("DELETE FROM sync_operations WHERE id = :id")
    suspend fun deleteOperation(id: String)

    // Conflicts
    @Query("SELECT * FROM sync_conflicts WHERE isResolved = 0 ORDER BY detectedAt DESC")
    fun getActiveConflictsFlow(): Flow<List<SyncConflictEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConflict(conflict: SyncConflictEntity): Long

    @Query("UPDATE sync_conflicts SET isResolved = 1 WHERE id = :id")
    suspend fun markConflictResolved(id: Long)
}
