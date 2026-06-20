package com.example.daypilot_test_desing.reminders

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.daypilot_test_desing.R

class TechHealthReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val appName      = intent.getStringExtra("app_name")     ?: return
        val usedMinutes  = intent.getIntExtra("used_minutes",  0)
        val limitMinutes = intent.getIntExtra("limit_minutes", 0)

        val notification = NotificationCompat.Builder(context, TECH_HEALTH_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.tech_health_notif_title, appName))
            .setContentText(context.getString(R.string.tech_health_notif_body, usedMinutes, limitMinutes))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(false)
            .build()

        context.getSystemService(NotificationManager::class.java)
            ?.notify(appName.hashCode(), notification)
    }
}
