package com.example.daypilot_test_desing.backend.fake

import com.example.daypilot_test_desing.backend.model.ReceivedReaction
import com.example.daypilot_test_desing.backend.model.ReactionType
import com.example.daypilot_test_desing.backend.model.TimeZoneRegion
import com.example.daypilot_test_desing.backend.model.UserProfile
import com.example.daypilot_test_desing.backend.model.WeeklySummaryData
import com.example.daypilot_test_desing.backend.repository.UserRepository

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

    // ── UserRepository interface ──────────────────────────────────────

    override suspend fun getCurrentUser(): UserProfile = profile

    override suspend fun getWeeklySummary(): WeeklySummaryData = WeeklySummaryData(
        totalPoints    = 156,
        tasksCompleted = 23,
        totalSteps     = 47320,
        bestStreak     = 7,
        reactions      = listOf(
            ReceivedReaction("Ana García", ReactionType.FIRE),
            ReceivedReaction("Luis Pérez", ReactionType.CLAP),
            ReceivedReaction("Sara López", ReactionType.STRONG)
        )
    )

    override suspend fun updateProfile(name: String, username: String, region: TimeZoneRegion) {
        profile = profile.copy(name = name, username = username, region = region)
    }

    // ── Non-interface sync methods (used by FakeRankingRepository until Piece 6) ─

    fun getCurrentUserSync(): UserProfile = profile
}
