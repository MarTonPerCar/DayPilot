package com.example.daypilot_test_desing.core.reminders

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.daypilot_test_desing.core.data.local.SharedPrefsReminderRepository
import com.example.daypilot_test_desing.core.data.preferences.AppPreferences

// AlarmManager wipes all pending alarms on reboot — this re-arms them.
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val appPrefs = AppPreferences(context)

        ReminderScheduler.rescheduleAll(
            context              = context,
            repository           = SharedPrefsReminderRepository(context),
            notificationsEnabled = appPrefs.notificationsEnabled
        )

        DailyNotificationScheduler.scheduleAll(
            context              = context,
            notificationsEnabled = appPrefs.notificationsEnabled,
            taskOn               = appPrefs.taskRemindersEnabled,
            streakOn             = appPrefs.streakAlertsEnabled
        )
    }
}
