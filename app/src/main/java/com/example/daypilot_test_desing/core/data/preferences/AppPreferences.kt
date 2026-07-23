package com.example.daypilot_test_desing.core.data.preferences

import android.content.Context

class AppPreferences(context: Context) {
    private val prefs = context.getSharedPreferences("daypilot_prefs", Context.MODE_PRIVATE)

    var isDarkMode: Boolean
        get() = prefs.getBoolean("dark_mode", true)
        set(v) { prefs.edit().putBoolean("dark_mode", v).apply() }

    var themeId: String
        get() = prefs.getString("theme_id", "SAGE_GREEN") ?: "SAGE_GREEN"
        set(v) { prefs.edit().putString("theme_id", v).apply() }

    var language: String
        get() = prefs.getString("language", "es") ?: "es"
        set(v) { prefs.edit().putString("language", v).apply() }

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean("notifications_enabled", true)
        set(v) { prefs.edit().putBoolean("notifications_enabled", v).apply() }

    var lastOpenDate: String
        get() = prefs.getString("last_open_date", "") ?: ""
        set(v) { prefs.edit().putString("last_open_date", v).apply() }

    var taskRemindersEnabled: Boolean
        get() = prefs.getBoolean("task_reminders_enabled", true)
        set(v) { prefs.edit().putBoolean("task_reminders_enabled", v).apply() }

    var streakAlertsEnabled: Boolean
        get() = prefs.getBoolean("streak_alerts_enabled", true)
        set(v) { prefs.edit().putBoolean("streak_alerts_enabled", v).apply() }

    var hasRequestedReliabilityPermissions: Boolean
        get() = prefs.getBoolean("has_requested_reliability_permissions", false)
        set(v) { prefs.edit().putBoolean("has_requested_reliability_permissions", v).apply() }
}
