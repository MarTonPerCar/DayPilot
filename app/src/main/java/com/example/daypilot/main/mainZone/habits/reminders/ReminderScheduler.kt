package com.example.daypilot.main.mainZone.habits.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ReminderScheduler {

    const val CHANNEL_ID = "reminders_channel"

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) != null) return
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                "Recordatorios",
                NotificationManager.IMPORTANCE_HIGH
            )
        )
    }

    fun canScheduleExact(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < 31) return true
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return am.canScheduleExactAlarms()
    }

    fun cancel(context: Context, reminderId: String) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        am.cancel(pendingIntent(context, reminderId, AlarmKind.MAIN))
        am.cancel(pendingIntent(context, reminderId, AlarmKind.PRE))
    }

    suspend fun scheduleSmart(context: Context, store: RemindersLocalStore, reminder: Reminder) {
        withContext(Dispatchers.IO) {
            ensureChannel(context)

            if (!reminder.enabled) {
                cancel(context, reminder.id)
                return@withContext
            }

            val now = System.currentTimeMillis()
            val mainAt = reminder.triggerAtMillis

            if (mainAt > now + 500) {
                scheduleAt(context, reminder.id, AlarmKind.MAIN, mainAt)
            }

            val preMin = reminder.preAlertMin.coerceAtLeast(0)
            if (preMin > 0 && mainAt > now + 500) {
                val preAt = mainAt - preMin * 60_000L

                if (preAt > now + 500) {
                    scheduleAt(context, reminder.id, AlarmKind.PRE, preAt)
                } else {
                    if (reminder.lastPreSentForTriggerAt != mainAt) {
                        ReminderNotifier.notifyPreNow(context, reminder)
                        store.update(reminder.copy(lastPreSentForTriggerAt = mainAt))
                    }
                }
            }
        }
    }

    private fun scheduleAt(context: Context, reminderId: String, kind: AlarmKind, atMillis: Long) {
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val pi = pendingIntent(context, reminderId, kind)

        if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
            val showIntent = PendingIntent.getActivity(
                context,
                reminderId.hashCode(),
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            am.setAlarmClock(AlarmManager.AlarmClockInfo(atMillis, showIntent), pi)
            return
        }

        if (Build.VERSION.SDK_INT >= 23) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, atMillis, pi)
        } else {
            am.setExact(AlarmManager.RTC_WAKEUP, atMillis, pi)
        }
    }

    private fun pendingIntent(context: Context, reminderId: String, kind: AlarmKind): PendingIntent {
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("id", reminderId)
            putExtra("kind", kind.name)
        }
        val reqCode = reminderId.hashCode() + if (kind == AlarmKind.PRE) 1 else 0

        val flags = PendingIntent.FLAG_UPDATE_CURRENT or
                (if (Build.VERSION.SDK_INT >= 23) PendingIntent.FLAG_IMMUTABLE else 0)

        return PendingIntent.getBroadcast(context, reqCode, intent, flags)
    }
}