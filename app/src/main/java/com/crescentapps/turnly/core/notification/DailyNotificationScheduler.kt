package com.crescentapps.turnly.core.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.crescentapps.turnly.TurnlyApplication
import com.crescentapps.turnly.core.model.OccurrenceStatus
import com.crescentapps.turnly.core.util.DateUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

class DailyAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val app = context.applicationContext as TurnlyApplication
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = app.userPreferencesRepository.userPreferencesFlow.first()
                if (prefs.masterNotificationsEnabled) {
                    val todayTurns = app.repository.getTodayTurnsFlow(DateUtils.today()).first()
                    for (turn in todayTurns) {
                        // Only notify if pending and schedule notifications are enabled
                        if (turn.status == OccurrenceStatus.PENDING && turn.schedule.notificationsEnabled) {
                            NotificationHelper.showTurnNotification(context, turn)
                        }
                    }
                }
                // Schedule next day's alarm
                DailyNotificationScheduler.scheduleDailyAlarm(context, prefs.defaultNotificationHour, prefs.defaultNotificationMinute)
            } finally {
                pendingResult.finish()
            }
        }
    }
}

object DailyNotificationScheduler {
    fun scheduleDailyAlarm(context: Context, hour: Int = 9, minute: Int = 0) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, DailyAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            999,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Exact alarm permission not allowed or restricted
        }
    }
}
