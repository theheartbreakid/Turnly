package com.crescentapps.turnly.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.crescentapps.turnly.MainActivity
import com.crescentapps.turnly.R
import com.crescentapps.turnly.core.model.ResolvedTurn
import com.crescentapps.turnly.core.model.ScheduleType
import com.crescentapps.turnly.core.util.CurrencyUtils

object NotificationHelper {
    const val CHANNEL_DAILY_TURNS = "turnly_daily_turns"
    const val CHANNEL_UPDATES = "turnly_updates"
    const val ACTION_MARK_DONE = "com.crescentapps.turnly.ACTION_MARK_DONE"
    const val EXTRA_SCHEDULE_ID = "extra_schedule_id"
    const val EXTRA_DATE = "extra_date"
    const val EXTRA_PARTICIPANT_ID = "extra_participant_id"
    const val EXTRA_AMOUNT = "extra_amount"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val dailyName = "Daily Turns"
            val dailyDesc = "Reminders for scheduled daily rotations"
            val dailyChannel = NotificationChannel(CHANNEL_DAILY_TURNS, dailyName, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = dailyDesc
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(dailyChannel)

            val updatesName = "App Updates"
            val updatesDesc = "Notifications for new Turnly releases"
            val updatesChannel = NotificationChannel(CHANNEL_UPDATES, updatesName, NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = updatesDesc
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(updatesChannel)
        }
    }

    fun showTurnNotification(context: Context, turn: ResolvedTurn) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            turn.schedule.id.toInt(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Mark Done
        val markDoneIntent = Intent(context, TurnActionReceiver::class.java).apply {
            action = ACTION_MARK_DONE
            putExtra(EXTRA_SCHEDULE_ID, turn.schedule.id)
            putExtra(EXTRA_DATE, turn.date)
            putExtra(EXTRA_PARTICIPANT_ID, turn.participant.id)
            if (turn.expectedAmount != null) {
                putExtra(EXTRA_AMOUNT, turn.expectedAmount)
            }
        }
        val markDonePendingIntent = PendingIntent.getBroadcast(
            context,
            (turn.schedule.id * 1000 + 1).toInt(),
            markDoneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = when (turn.schedule.type) {
            ScheduleType.MONEY -> {
                val amountStr = turn.expectedAmount?.let { CurrencyUtils.formatAmount(it, turn.schedule.currencyCode) } ?: ""
                "${turn.schedule.name} · ${turn.participant.name}'s turn ($amountStr)"
            }
            else -> "${turn.schedule.name} · ${turn.participant.name}'s turn today"
        }

        val body = when (turn.schedule.type) {
            ScheduleType.MONEY -> "Today's rotation is scheduled for ${turn.participant.name}."
            ScheduleType.TASK -> "${turn.participant.name} has today's task assigned."
            ScheduleType.RESPONSIBILITY -> "Assigned responsibility for today: ${turn.participant.name}."
            else -> "It's ${turn.participant.name}'s turn today."
        }

        val markDoneLabel = when (turn.schedule.type) {
            ScheduleType.MONEY -> "Mark Paid"
            ScheduleType.TASK -> "Mark Complete"
            else -> "Mark Done"
        }

        val builder = NotificationCompat.Builder(context, CHANNEL_DAILY_TURNS)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(tapPendingIntent)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_menu_agenda, markDoneLabel, markDonePendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(turn.schedule.id.toInt(), builder.build())
        } catch (e: SecurityException) {
            // Notification permission might not be granted yet
        }
    }

    fun showUpdateAvailableNotification(context: Context, versionName: String, releaseNotes: String) {
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val tapPendingIntent = PendingIntent.getActivity(
            context,
            9999,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_UPDATES)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Turnly update available")
            .setContentText("Version $versionName is available. Tap to view update.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Version $versionName is ready to download.\n\n$releaseNotes"))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(tapPendingIntent)
            .setAutoCancel(true)

        try {
            NotificationManagerCompat.from(context).notify(9999, builder.build())
        } catch (e: SecurityException) {
            // Notification permission might not be granted yet
        }
    }
}
