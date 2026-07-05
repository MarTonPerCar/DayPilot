package com.example.daypilot_test_desing.core.data.model

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
    val pointsToNextLevel: Int = 20,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0
)

data class AppSettings(
    val isDarkMode: Boolean = true,
    val selectedThemeId: String = "SAGE_GREEN",
    val selectedLanguage: String = "es",
    val notificationsEnabled: Boolean = true
)

// Both mirror fn_update_level() in the DB so the app can predict a level bump
// locally right after awarding points, without waiting for a re-fetch.
fun calculateLevel(totalPointsHistorical: Int): Int {
    val level = kotlin.math.floor(
        (-1.0 + kotlin.math.sqrt(1.0 + 4.0 * (2.0 + totalPointsHistorical / 5.0))) / 2.0
    ).toInt()
    return maxOf(1, level)
}

fun pointsToNextLevel(level: Int): Int = 5 * level * (level + 3)
