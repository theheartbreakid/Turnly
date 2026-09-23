package com.crescentapps.turnly.core.update

import android.content.Context
import androidx.work.*
import com.crescentapps.turnly.core.model.UpdateFrequency
import com.crescentapps.turnly.core.notification.NotificationHelper
import com.crescentapps.turnly.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit

/**
 * Background WorkManager worker for periodic update checking.
 * Checks GitHub Releases API at the user's configured frequency (Daily, Weekly, Monthly).
 * If an update is detected, triggers a non-intrusive system notification.
 */
class UpdateCheckWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val userPreferencesRepository = UserPreferencesRepository(applicationContext)
        val prefs = userPreferencesRepository.userPreferencesFlow.first()

        // If automatic checks are disabled, succeed immediately
        if (prefs.updateCheckFrequency == UpdateFrequency.OFF) {
            return Result.success()
        }

        val updateManager = UpdateManager.getInstance(applicationContext)
        if (!updateManager.isNetworkAvailable()) {
            return Result.retry()
        }

        val checkResult = updateManager.checkForUpdates(isManual = false)
        val now = System.currentTimeMillis()
        userPreferencesRepository.setLastUpdateCheckTimestamp(now)

        checkResult.onSuccess { updateInfo ->
            if (updateInfo != null) {
                NotificationHelper.showUpdateAvailableNotification(
                    context = applicationContext,
                    versionName = updateInfo.versionName,
                    releaseNotes = updateInfo.releaseNotes
                )
            }
        }

        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "turnly_update_check"

        fun schedule(context: Context, frequency: UpdateFrequency) {
            val workManager = WorkManager.getInstance(context)

            if (frequency == UpdateFrequency.OFF) {
                workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
                return
            }

            val repeatIntervalHours = when (frequency) {
                UpdateFrequency.DAILY -> 24L
                UpdateFrequency.WEEKLY -> 7L * 24L
                UpdateFrequency.MONTHLY -> 30L * 24L
                UpdateFrequency.OFF -> return
            }

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<UpdateCheckWorker>(
                repeatIntervalHours, TimeUnit.HOURS
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 1, TimeUnit.HOURS)
                .build()

            workManager.enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                workRequest
            )
        }
    }
}
