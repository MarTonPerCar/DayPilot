package com.example.daypilot_test_desing.core.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.daypilot_test_desing.R
import java.util.Calendar
import java.util.TimeZone

const val DAILY_CHANNEL_ID    = "daypilot_daily"
const val EXTRA_ALARM_TYPE    = "alarm_type"
const val ALARM_TASK_REMINDER = "task_reminder"
const val ALARM_STREAK_DANGER = "streak_danger"

fun createDailyChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            DAILY_CHANNEL_ID,
            context.getString(R.string.daily_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.daily_channel_desc)
        }
        context.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }
}

object DailyNotificationScheduler {

    fun scheduleAll(context: Context, notificationsEnabled: Boolean, taskOn: Boolean, streakOn: Boolean) {
        scheduleTaskReminder(context, notificationsEnabled && taskOn)
        scheduleStreakAlert(context, notificationsEnabled && streakOn)
    }

    fun scheduleTaskReminder(context: Context, enabled: Boolean) {
        if (enabled) scheduleAlarm(context, ALARM_TASK_REMINDER, hour = 9)
        else cancelAlarm(context, ALARM_TASK_REMINDER)
    }

    fun scheduleStreakAlert(context: Context, enabled: Boolean) {
        if (enabled) scheduleAlarm(context, ALARM_STREAK_DANGER, hour = 22)
        else cancelAlarm(context, ALARM_STREAK_DANGER)
    }

    fun reschedule(context: Context, type: String) {
        val hour = if (type == ALARM_TASK_REMINDER) 9 else 22
        scheduleAlarm(context, type, hour)
    }

    fun cancelAll(context: Context) {
        cancelAlarm(context, ALARM_TASK_REMINDER)
        cancelAlarm(context, ALARM_STREAK_DANGER)
    }

    private fun scheduleAlarm(context: Context, type: String, hour: Int) {
        val triggerAt = nextAlarmMillis(hour)
        val pi = buildPendingIntent(context, type, PendingIntent.FLAG_UPDATE_CURRENT) ?: return
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setWindow(AlarmManager.RTC_WAKEUP, triggerAt, 10 * 60_000L, pi)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi)
        }
    }

    private fun cancelAlarm(context: Context, type: String) {
        val pi = buildPendingIntent(context, type, PendingIntent.FLAG_NO_CREATE) ?: return
        context.getSystemService(AlarmManager::class.java)?.cancel(pi)
    }

    private fun buildPendingIntent(context: Context, type: String, flags: Int): PendingIntent? {
        val intent = Intent(context, DailyNotificationsReceiver::class.java).apply {
            putExtra(EXTRA_ALARM_TYPE, type)
        }
        return PendingIntent.getBroadcast(
            context,
            type.hashCode(),
            intent,
            flags or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextAlarmMillis(hour: Int): Long {
        val cal = Calendar.getInstance(TimeZone.getDefault()).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (cal.timeInMillis <= System.currentTimeMillis()) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }
}
