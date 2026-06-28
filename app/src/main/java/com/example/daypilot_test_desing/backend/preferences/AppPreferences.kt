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
}
