package com.example.daypilot.main.mainZone.habits.tech

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.daypilot.R

object TechHealthNotifier {
    const val CHANNEL_LIMITS = "tech_health_limits"
    const val CHANNEL_ONGOING = "tech_health_ongoing"

    fun ensureChannels(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_LIMITS, "DayPilot - Límites de uso", NotificationManager.IMPORTANCE_HIGH)
        )
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ONGOING, "DayPilot - Monitor", NotificationManager.IMPORTANCE_DEFAULT)
        )
    }

    private fun canPostNotifications(context: Context): Boolean {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return false
        return if (Build.VERSION.SDK_INT >= 33) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED
        } else true
    }

    fun notifyLimit(context: Context, title: String, text: String, stableId: String) {
        if (!canPostNotifications(context)) return

        val id = stableId.hashCode()
        val n = NotificationCompat.Builder(context, CHANNEL_LIMITS)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(id, n)
        } catch (_: SecurityException) { }
    }

    fun ongoingMonitor(context: Context): android.app.Notification {
        return NotificationCompat.Builder(context, CHANNEL_ONGOING)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("DayPilot: monitor activo")
            .setContentText("Midiendo apps en primer plano…")
            .setOngoing(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
    }
}