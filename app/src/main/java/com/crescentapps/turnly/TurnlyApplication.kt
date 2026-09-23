package com.crescentapps.turnly

import android.app.Application
import com.crescentapps.turnly.core.notification.NotificationHelper
import com.crescentapps.turnly.data.local.TurnlyDatabase
import com.crescentapps.turnly.data.preferences.UserPreferencesRepository
import com.crescentapps.turnly.data.repository.TurnlyRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

import com.crescentapps.turnly.data.network.RealtimeSyncClient
import com.crescentapps.turnly.data.network.RoomService
import com.crescentapps.turnly.data.network.impl.MockRealtimeSyncClient
import com.crescentapps.turnly.data.network.impl.MockRoomService
import com.crescentapps.turnly.data.repository.RoomRepository
import com.crescentapps.turnly.data.sync.SyncWorker
import kotlinx.coroutines.flow.first

class TurnlyApplication : Application() {
    val database: TurnlyDatabase by lazy { TurnlyDatabase.getInstance(this) }
    val repository: TurnlyRepository by lazy { TurnlyRepository(database) }
    val userPreferencesRepository: UserPreferencesRepository by lazy { UserPreferencesRepository(this) }
    val hapticManager: com.crescentapps.turnly.core.util.HapticManager by lazy { com.crescentapps.turnly.core.util.HapticManager(this) }
    val soundManager: com.crescentapps.turnly.core.util.SoundManager by lazy { com.crescentapps.turnly.core.util.SoundManager(this) }
    val updateManager: com.crescentapps.turnly.core.update.UpdateManager by lazy { com.crescentapps.turnly.core.update.UpdateManager.getInstance(this) }

    val roomService: RoomService by lazy { MockRoomService() }
    val realtimeSyncClient: RealtimeSyncClient by lazy { MockRealtimeSyncClient() }
    val roomRepository: RoomRepository by lazy {
        RoomRepository(
            db = database,
            turnlyRepository = repository,
            roomService = roomService,
            realtimeClient = realtimeSyncClient,
            preferencesRepository = userPreferencesRepository
        )
    }

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        NotificationHelper.createNotificationChannels(this)

        // Run automatic history reconciliation on launch
        applicationScope.launch {
            repository.reconcileMissedHistory()
            val prefs = userPreferencesRepository.userPreferencesFlow.first()
            if (prefs.onlineSyncEnabled) {
                SyncWorker.schedulePeriodicSync(this@TurnlyApplication, prefs.syncOverMobileData)
            }
            com.crescentapps.turnly.core.update.UpdateCheckWorker.schedule(
                this@TurnlyApplication,
                prefs.updateCheckFrequency
            )
        }
    }
}
