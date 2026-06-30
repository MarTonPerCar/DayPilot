package com.example.daypilot_test_desing.core.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.daypilot_test_desing.R

const val CHANNEL_ID = "daypilot_reminders"

fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.reminder_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.reminder_channel_desc)
        }
        context.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }
}

object ReminderScheduler {

    fun schedule(
        context: Context,
        reminderId: String,
        title: String,
        triggerAtMillis: Long,
        isEarly: Boolean = false,
        isOneTime: Boolean = false,
        frequencyType: String = "ONCE"
    ) {
        if (triggerAtMillis <= System.currentTimeMillis()) return

        val notifId     = notificationId(reminderId, isEarly)
        val intent      = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("title",          title)
            putExtra("notif_id",       notifId)
            putExtra("is_early",       isEarly)
            putExtra("reminder_id",    reminderId)
            putExtra("is_one_time",    isOneTime && !isEarly)
            putExtra("trigger_at",     triggerAtMillis)
            putExtra("frequency_type", frequencyType)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, notifId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = context.getSystemService(AlarmManager::class.java) ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !am.canScheduleExactAlarms()) {
            am.setWindow(AlarmManager.RTC_WAKEUP, triggerAtMillis, 5 * 60_000L, pendingIntent)
        } else {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancel(context: Context, reminderId: String) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        listOf(false, true).forEach { isEarly ->
            val notifId = notificationId(reminderId, isEarly)
            val intent  = Intent(context, ReminderReceiver::class.java)
            val pi = PendingIntent.getBroadcast(
                context, notifId, intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pi?.let { am.cancel(it) }
        }
    }

    private fun notificationId(reminderId: String, isEarly: Boolean): Int {
        val base = reminderId.hashCode()
        return if (isEarly) base xor 0x8000_0000.toInt() else base
    }
}
