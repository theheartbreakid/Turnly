package com.crescentapps.turnly.data.local

import android.content.Context
import androidx.room.*
import com.crescentapps.turnly.core.model.FrequencyType
import com.crescentapps.turnly.core.model.OccurrenceStatus
import com.crescentapps.turnly.core.model.OverrideStrategy
import com.crescentapps.turnly.core.model.ScheduleType
import com.crescentapps.turnly.data.local.dao.*
import com.crescentapps.turnly.data.local.entity.*

class Converters {
    @TypeConverter
    fun fromScheduleType(value: ScheduleType): String = value.name

    @TypeConverter
    fun toScheduleType(value: String): ScheduleType = runCatching { ScheduleType.valueOf(value) }.getOrDefault(ScheduleType.GENERAL)

    @TypeConverter
    fun fromFrequencyType(value: FrequencyType): String = value.name

    @TypeConverter
    fun toFrequencyType(value: String): FrequencyType = runCatching { FrequencyType.valueOf(value) }.getOrDefault(FrequencyType.DAILY)

    @TypeConverter
    fun fromOccurrenceStatus(value: OccurrenceStatus): String = value.name

    @TypeConverter
    fun toOccurrenceStatus(value: String): OccurrenceStatus = runCatching { OccurrenceStatus.valueOf(value) }.getOrDefault(OccurrenceStatus.PENDING)

    @TypeConverter
    fun fromOverrideStrategy(value: OverrideStrategy): String = value.name

    @TypeConverter
    fun toOverrideStrategy(value: String): OverrideStrategy = runCatching { OverrideStrategy.valueOf(value) }.getOrDefault(OverrideStrategy.SINGLE_DATE_ONLY)

    @TypeConverter
    fun fromRoomRole(value: com.crescentapps.turnly.core.model.RoomRole): String = value.name

    @TypeConverter
    fun toRoomRole(value: String): com.crescentapps.turnly.core.model.RoomRole = runCatching { com.crescentapps.turnly.core.model.RoomRole.valueOf(value) }.getOrDefault(com.crescentapps.turnly.core.model.RoomRole.MEMBER)

    @TypeConverter
    fun fromMemberPresence(value: com.crescentapps.turnly.core.model.MemberPresence): String = value.name

    @TypeConverter
    fun toMemberPresence(value: String): com.crescentapps.turnly.core.model.MemberPresence = runCatching { com.crescentapps.turnly.core.model.MemberPresence.valueOf(value) }.getOrDefault(com.crescentapps.turnly.core.model.MemberPresence.ONLINE)

    @TypeConverter
    fun fromSyncState(value: com.crescentapps.turnly.core.model.SyncState): String = value.name

    @TypeConverter
    fun toSyncState(value: String): com.crescentapps.turnly.core.model.SyncState = runCatching { com.crescentapps.turnly.core.model.SyncState.valueOf(value) }.getOrDefault(com.crescentapps.turnly.core.model.SyncState.SYNCED)

    @TypeConverter
    fun fromSharingState(value: com.crescentapps.turnly.core.model.SharingState): String = value.name

    @TypeConverter
    fun toSharingState(value: String): com.crescentapps.turnly.core.model.SharingState = runCatching { com.crescentapps.turnly.core.model.SharingState.valueOf(value) }.getOrDefault(com.crescentapps.turnly.core.model.SharingState.SHARED)

    @TypeConverter
    fun fromRoomPermission(value: com.crescentapps.turnly.core.model.RoomPermission): String = value.name

    @TypeConverter
    fun toRoomPermission(value: String): com.crescentapps.turnly.core.model.RoomPermission = runCatching { com.crescentapps.turnly.core.model.RoomPermission.valueOf(value) }.getOrDefault(com.crescentapps.turnly.core.model.RoomPermission.CAN_COMPLETE)

    @TypeConverter
    fun fromOperationSyncState(value: com.crescentapps.turnly.core.model.OperationSyncState): String = value.name

    @TypeConverter
    fun toOperationSyncState(value: String): com.crescentapps.turnly.core.model.OperationSyncState = runCatching { com.crescentapps.turnly.core.model.OperationSyncState.valueOf(value) }.getOrDefault(com.crescentapps.turnly.core.model.OperationSyncState.PENDING)
}

@Database(
    entities = [
        ParticipantEntity::class,
        ScheduleEntity::class,
        ScheduleParticipantEntity::class,
        RotationPatternEntity::class,
        OccurrenceEntity::class,
        SkipDateEntity::class,
        ManualAssignmentEntity::class,
        RoomEntity::class,
        RoomMemberEntity::class,
        SharedScheduleEntity::class,
        SyncOperationEntity::class,
        SyncConflictEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class TurnlyDatabase : RoomDatabase() {
    abstract fun participantDao(): ParticipantDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun occurrenceDao(): OccurrenceDao
    abstract fun roomDao(): RoomDao

    companion object {
        @Volatile
        private var INSTANCE: TurnlyDatabase? = null

        fun getInstance(context: Context): TurnlyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TurnlyDatabase::class.java,
                    "turnly.db"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
