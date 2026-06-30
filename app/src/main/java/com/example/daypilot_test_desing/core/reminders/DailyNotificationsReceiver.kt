package com.example.daypilot_test_desing.core.reminders

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.data.preferences.AppPreferences
import com.example.daypilot_test_desing.data.supabase.SupabaseNotificationRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DailyNotificationsReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = AppPreferences(context)
        val type  = intent.getStringExtra(EXTRA_ALARM_TYPE) ?: return

        when (type) {
            ALARM_TASK_REMINDER -> handleTaskReminder(context, prefs)
            ALARM_STREAK_DANGER -> handleStreakDanger(context, prefs)
        }

        // Re-schedule for the same time tomorrow
        DailyNotificationScheduler.reschedule(context, type)
    }

    private fun handleTaskReminder(context: Context, prefs: AppPreferences) {
        if (!prefs.notificationsEnabled || !prefs.taskRemindersEnabled) return

        val today = today()
        val count = if (prefs.pendingTaskCountDate == today) prefs.pendingTaskCount else -1

        val body = when {
            count < 0  -> context.getString(R.string.notif_task_reminder_generic)
            count == 0 -> context.getString(R.string.notif_task_reminder_none)
            else       -> context.getString(R.string.notif_task_reminder_count, count)
        }
        val title = context.getString(R.string.notif_task_reminder_title)

        notify(context = context, id = ALARM_TASK_REMINDER.hashCode(), title = title, body = body)

        scope.launch {
            SupabaseNotificationRepository.insertForCurrentUser(
                type  = "TASK_REMINDER",
                title = title,
                body  = body
            )
        }
    }

    private fun handleStreakDanger(context: Context, prefs: AppPreferences) {
        if (!prefs.notificationsEnabled || !prefs.streakAlertsEnabled) return
        if (prefs.lastOpenDate == today()) return  // User opened the app today

        val title = context.getString(R.string.notif_streak_danger_title)
        val body  = context.getString(R.string.notif_streak_danger_body)

        notify(context = context, id = ALARM_STREAK_DANGER.hashCode(), title = title, body = body)

        scope.launch {
            SupabaseNotificationRepository.insertForCurrentUser(
                type  = "STREAK_RISK",
                title = title,
                body  = body
            )
        }
    }

    private fun notify(context: Context, id: Int, title: String, body: String) {
        val notification = NotificationCompat.Builder(context, DAILY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java)?.notify(id, notification)
    }

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
}
