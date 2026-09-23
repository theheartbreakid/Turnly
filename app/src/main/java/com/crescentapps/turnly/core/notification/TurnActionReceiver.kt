package com.crescentapps.turnly.core.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.crescentapps.turnly.TurnlyApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TurnActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == NotificationHelper.ACTION_MARK_DONE) {
            val scheduleId = intent.getLongExtra(NotificationHelper.EXTRA_SCHEDULE_ID, -1L)
            val date = intent.getStringExtra(NotificationHelper.EXTRA_DATE) ?: return
            val participantId = intent.getLongExtra(NotificationHelper.EXTRA_PARTICIPANT_ID, -1L)
            val amount = if (intent.hasExtra(NotificationHelper.EXTRA_AMOUNT)) {
                intent.getDoubleExtra(NotificationHelper.EXTRA_AMOUNT, 0.0)
            } else null

            if (scheduleId != -1L && participantId != -1L) {
                val app = context.applicationContext as TurnlyApplication
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        app.repository.markTurnComplete(
                            scheduleId = scheduleId,
                            date = date,
                            participantId = participantId,
                            actualAmount = amount
                        )
                        // Dismiss the notification
                        NotificationManagerCompat.from(context).cancel(scheduleId.toInt())
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
