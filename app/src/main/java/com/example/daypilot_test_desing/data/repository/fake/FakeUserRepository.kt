package com.example.daypilot_test_desing.data.repository.fake

import com.example.daypilot_test_desing.data.model.ReceivedReaction
import com.example.daypilot_test_desing.data.model.ReactionType
import com.example.daypilot_test_desing.data.model.TimeZoneRegion
import com.example.daypilot_test_desing.data.model.UserProfile
import com.example.daypilot_test_desing.data.model.WeeklySummaryData
import com.example.daypilot_test_desing.data.repository.UserRepository

object FakeUserRepository : UserRepository {
    private var profile = UserProfile(
        id            = "user_1",
        name          = "Mario",
        username      = "mario_dev",
        email         = "mario@example.com",
        region        = TimeZoneRegion.EUROPE_MADRID,
        memberSince   = "2025",
        level         = 12,
        totalPoints   = 3480,
        currentStreak = 7,
        longestStreak = 21
    )

    override fun getCurrentUser(): UserProfile = profile

    override fun getWeeklySummary(): WeeklySummaryData = WeeklySummaryData(
        totalPoints    = 156,
        tasksCompleted = 23,
        totalSteps     = 47320,
        bestStreak     = 7,
        reactions      = listOf(
            ReceivedReaction("Ana García",  ReactionType.FIRE),
            ReceivedReaction("Luis Pérez",  ReactionType.CLAP),
            ReceivedReaction("Sara López",  ReactionType.STRONG)
        )
    )

    override fun updateProfile(name: String, username: String, region: TimeZoneRegion) {
        profile = profile.copy(name = name, username = username, region = region)
    }
}
