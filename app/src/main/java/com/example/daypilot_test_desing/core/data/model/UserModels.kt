package com.example.daypilot_test_desing.backend.model

data class UserProfile(
    val id: String,
    val name: String,
    val username: String,
    val email: String,
    val avatarUrl: String? = null,
    val region: TimeZoneRegion = TimeZoneRegion.EUROPE_MADRID,
    val memberSince: String = "2025",
    val level: Int = 1,
    val totalPoints: Int = 0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0
)

data class AppSettings(
    val isDarkMode: Boolean = true,
    val selectedThemeId: String = "SAGE_GREEN",
    val selectedLanguage: String = "es",
    val notificationsEnabled: Boolean = true
)
