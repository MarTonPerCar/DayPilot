package com.example.daypilot_test_desing.reminders

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.daypilot_test_desing.R

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title   = intent.getStringExtra("title") ?: return
        val notifId = intent.getIntExtra("notif_id", 0)
        val isEarly = intent.getBooleanExtra("is_early", false)

        val contentTitle = if (isEarly) "En 10 minutos: $title" else title
        val contentText  = if (isEarly) context.getString(R.string.reminder_early_body)
                           else context.getString(R.string.reminder_body)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        context.getSystemService(NotificationManager::class.java)
            ?.notify(notifId, notification)
    }
}
