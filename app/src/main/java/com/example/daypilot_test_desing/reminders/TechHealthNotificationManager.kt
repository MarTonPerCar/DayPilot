package com.example.daypilot_test_desing.reminders

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.example.daypilot_test_desing.R

const val TECH_HEALTH_CHANNEL_ID = "daypilot_tech_health"

fun createTechHealthChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            TECH_HEALTH_CHANNEL_ID,
            context.getString(R.string.tech_health_channel_name),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.tech_health_channel_desc)
        }
        context.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }
}

object TechHealthNotificationManager {

    fun scheduleRepeating(
        context: Context,
        restrictionId: String,
        appName: String,
        usedMinutes: Int,
        limitMinutes: Int,
        intervalSeconds: Int
    ) {
        val intent = buildIntent(context, appName, usedMinutes, limitMinutes)
        val pi = PendingIntent.getBroadcast(
            context,
            restrictionId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        am.setRepeating(
            AlarmManager.RTC_WAKEUP,
            System.currentTimeMillis(),
            intervalSeconds * 1_000L,
            pi
        )
    }

    fun cancel(context: Context, restrictionId: String) {
        val am = context.getSystemService(AlarmManager::class.java) ?: return
        val pi = PendingIntent.getBroadcast(
            context,
            restrictionId.hashCode(),
            Intent(context, TechHealthReceiver::class.java),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pi?.let { am.cancel(it) }
    }

    private fun buildIntent(
        context: Context,
        appName: String,
        usedMinutes: Int,
        limitMinutes: Int
    ) = Intent(context, TechHealthReceiver::class.java).apply {
        putExtra("app_name",      appName)
        putExtra("used_minutes",  usedMinutes)
        putExtra("limit_minutes", limitMinutes)
    }
}
