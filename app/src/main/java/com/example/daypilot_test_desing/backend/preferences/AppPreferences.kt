package com.example.daypilot_test_desing.backend.preferences

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

    // Tracks the calendar date (yyyy-MM-dd) when each daily bonus was last awarded.
    // Used to prevent double-awarding within the same day without a DB round-trip.
    var techHealthBonusDate: String
        get() = prefs.getString("tech_bonus_date", "") ?: ""
        set(v) { prefs.edit().putString("tech_bonus_date", v).apply() }

    var timerPointsDate: String
        get() = prefs.getString("timer_points_date", "") ?: ""
        set(v) { prefs.edit().putString("timer_points_date", v).apply() }

    var lastOpenDate: String
        get() = prefs.getString("last_open_date", "") ?: ""
        set(v) { prefs.edit().putString("last_open_date", v).apply() }

    var pendingTaskCount: Int
        get() = prefs.getInt("pending_task_count", 0)
        set(v) { prefs.edit().putInt("pending_task_count", v).apply() }

    var pendingTaskCountDate: String
        get() = prefs.getString("pending_task_count_date", "") ?: ""
        set(v) { prefs.edit().putString("pending_task_count_date", v).apply() }

    var lastKnownLevel: Int
        get() = prefs.getInt("last_known_level", 0)
        set(v) { prefs.edit().putInt("last_known_level", v).apply() }

    var taskRemindersEnabled: Boolean
        get() = prefs.getBoolean("task_reminders_enabled", true)
        set(v) { prefs.edit().putBoolean("task_reminders_enabled", v).apply() }

    var streakAlertsEnabled: Boolean
        get() = prefs.getBoolean("streak_alerts_enabled", true)
        set(v) { prefs.edit().putBoolean("streak_alerts_enabled", v).apply() }
}
