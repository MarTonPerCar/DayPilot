package com.example.daypilot_test_desing.core.reminders

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.annotation.StringRes
import androidx.core.app.NotificationCompat
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.data.preferences.AppPreferences
import com.example.daypilot_test_desing.data.supabase.SupabaseNotificationRepository
import com.example.daypilot_test_desing.data.supabase.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * These two alarms only wake the app and ask Supabase what (if anything) it already
 * decided for today — fn_check_task_reminders / fn_check_streak_danger (cron jobs) own
 * the actual decision now. No row for today means nothing to show; nothing is written
 * back to the notifications table here, the cron job already inserted it.
 */
class DailyNotificationsReceiver : BroadcastReceiver() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val prefs = AppPreferences(context)
        val type  = intent.getStringExtra(EXTRA_ALARM_TYPE) ?: return

        // The Supabase fetch below is async I/O — goAsync() keeps the receiver (and process)
        // alive long enough for it to complete instead of being torn down right after return.
        val pending = goAsync()
        scope.launch {
            try {
                when (type) {
                    ALARM_TASK_REMINDER -> handleTaskReminder(context, prefs)
                    ALARM_STREAK_DANGER -> handleStreakDanger(context, prefs)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to handle daily notification alarm type=$type", e)
            } finally {
                pending.finish()
            }
        }

        // Re-schedule for the same time tomorrow regardless of fetch outcome.
        DailyNotificationScheduler.reschedule(context, type)
    }

    private suspend fun handleTaskReminder(context: Context, prefs: AppPreferences) {
        if (!prefs.notificationsEnabled || !prefs.taskRemindersEnabled) return
        val uid = awaitCurrentUserId() ?: return
        val row = SupabaseNotificationRepository.getLatestOfTypeToday(uid, "TASK_REMINDER") ?: return

        val title = context.getString(
            NotificationBodyCodec.titleForType(row.type) ?: R.string.notif_task_reminder_title
        )
        val body = decodeBody(context, row.rawBody, fallback = R.string.notif_task_reminder_generic)

        notify(context = context, id = ALARM_TASK_REMINDER.hashCode(), title = title, body = body)
    }

    private suspend fun handleStreakDanger(context: Context, prefs: AppPreferences) {
        if (!prefs.notificationsEnabled || !prefs.streakAlertsEnabled) return

        val uid = awaitCurrentUserId() ?: return
        val row = SupabaseNotificationRepository.getLatestOfTypeToday(uid, "STREAK_RISK") ?: return

        val title = context.getString(
            NotificationBodyCodec.titleForType(row.type) ?: R.string.notif_streak_danger_title
        )
        val body = decodeBody(context, row.rawBody, fallback = R.string.notif_streak_danger_body)

        notify(context = context, id = ALARM_STREAK_DANGER.hashCode(), title = title, body = body)
    }

    /** Auth may still be restoring the session from SharedPreferences at this point. */
    private suspend fun awaitCurrentUserId(): String? {
        supabase.auth.awaitInitialization()
        return supabase.auth.currentUserOrNull()?.id
    }

    private fun decodeBody(context: Context, rawBody: String, @StringRes fallback: Int): String {
        val decoded = NotificationBodyCodec.decodeBody(rawBody) ?: return context.getString(fallback)
        val (resId, arg) = decoded
        return if (arg != null) context.getString(resId, arg) else context.getString(resId)
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

    companion object {
        private const val TAG = "DailyNotifReceiver"
    }
}
