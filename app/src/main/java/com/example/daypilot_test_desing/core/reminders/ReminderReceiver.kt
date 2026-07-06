package com.example.daypilot_test_desing.core.reminders

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.data.local.SharedPrefsReminderRepository
import com.example.daypilot_test_desing.data.supabase.SupabaseNotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ReminderReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val title         = intent.getStringExtra("title") ?: return
        val notifId       = intent.getIntExtra("notif_id", 0)
        val isEarly       = intent.getBooleanExtra("is_early", false)
        val reminderId    = intent.getStringExtra("reminder_id")
        val isOneTime     = intent.getBooleanExtra("is_one_time", false)
        val triggerAt     = intent.getLongExtra("trigger_at", 0L)
        val frequencyType = intent.getStringExtra("frequency_type") ?: "ONCE"

        val contentTitle = if (isEarly) "En 10 minutos: $title" else title
        val contentText  = if (isEarly) context.getString(R.string.reminder_early_body)
                           else context.getString(R.string.reminder_body)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            ?.notify(notifId, notification)

        if (!isEarly && reminderId != null) {
            // Delete one-time reminders after firing.
            if (isOneTime) {
                SharedPrefsReminderRepository(context).deleteReminder(reminderId)
            }

            // Reschedule repeating reminders for the next occurrence.
            if (triggerAt > 0L) {
                val nextMillis: Long = when (frequencyType) {
                    "DAILY"  -> triggerAt + 24 * 3600 * 1_000L
                    "WEEKLY" -> triggerAt + 7 * 24 * 3600 * 1_000L
                    else     -> 0L
                }
                if (nextMillis > 0L) {
                    ReminderScheduler.schedule(
                        context       = context,
                        reminderId    = reminderId,
                        title         = title,
                        triggerAtMillis = nextMillis,
                        frequencyType = frequencyType
                    )
                    SharedPrefsReminderRepository(context).updateTriggerTime(reminderId, nextMillis)
                }
            }

            // Insert into the in-app notification center (skip the early warning
            // to avoid duplicate entries).
            scope.launch {
                SupabaseNotificationRepository.insertForCurrentUser(
                    type  = "TASK_REMINDER",
                    title = contentTitle,
                    body  = contentText
                )
            }
        }
    }
}
