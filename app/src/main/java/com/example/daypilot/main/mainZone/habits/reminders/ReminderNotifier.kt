package com.example.daypilot.main.mainZone.habits.reminders

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.daypilot.R

object ReminderNotifier {

    fun notifyPreNow(context: android.content.Context, reminder: Reminder) {
        ReminderScheduler.ensureChannel(context)

        val notif = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("En 10 minutos")
            .setContentText(reminder.title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify((reminder.id + AlarmKind.PRE.name).hashCode(), notif)
    }

    fun notifyMainNow(context: android.content.Context, reminder: Reminder) {
        ReminderScheduler.ensureChannel(context)

        val notif = NotificationCompat.Builder(context, ReminderScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Recordatorio")
            .setContentText(reminder.title)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify((reminder.id + AlarmKind.MAIN.name).hashCode(), notif)
    }
}