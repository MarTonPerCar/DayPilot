package com.example.daypilot_test_desing.backend.supabase

import com.example.daypilot_test_desing.backend.model.TimeZoneRegion
import com.example.daypilot_test_desing.backend.model.UserProfile
import com.example.daypilot_test_desing.backend.model.WeeklySummaryData
import com.example.daypilot_test_desing.backend.repository.UserRepository
import com.example.daypilot_test_desing.backend.supabase.dto.UpdateUserDto
import com.example.daypilot_test_desing.backend.supabase.dto.UserDto
import com.example.daypilot_test_desing.backend.supabase.dto.UserStreakDto
import com.example.daypilot_test_desing.backend.supabase.dto.WeeklySummaryRowDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

class SupabaseUserRepository : UserRepository {

    private fun userId() = supabase.auth.currentUserOrNull()?.id

    override suspend fun getCurrentUser(): UserProfile {
        val uid = userId() ?: return UserProfile(id = "", name = "", username = "", email = "")
        return try {
            val dto = supabase.from("users").select {
                filter { eq("id", uid) }
                limit(1)
            }.decodeList<UserDto>().firstOrNull()
                ?: return UserProfile(id = uid, name = "", username = "", email = "")

            val streak = supabase.from("user_streaks").select {
                filter { eq("user_id", uid) }
                limit(1)
            }.decodeList<UserStreakDto>().firstOrNull()

            val region = TimeZoneRegion.entries.firstOrNull { it.value == dto.region }
                ?: TimeZoneRegion.EUROPE_MADRID
            val memberSince = dto.createdAt.take(4)

            UserProfile(
                id            = dto.id,
                name          = dto.name,
                username      = dto.username,
                email         = dto.email,
                avatarUrl     = dto.photoUrl,
                region        = region,
                memberSince   = memberSince,
                level         = dto.level,
                totalPoints   = dto.totalPointsHistorical,
                currentStreak = streak?.currentStreak ?: 0,
                longestStreak = streak?.longestStreak ?: 0
            )
        } catch (_: Exception) {
            UserProfile(id = uid, name = "", username = "", email = "")
        }
    }

    override suspend fun getWeeklySummary(): WeeklySummaryData {
        val uid = userId() ?: return WeeklySummaryData(0, 0, 0, 0)
        return try {
            val row = supabase.from("user_weekly_summary").select {
                filter { eq("user_id", uid) }
                order("week_start", ascending = false)
                limit(1)
            }.decodeList<WeeklySummaryRowDto>().firstOrNull()
                ?: return WeeklySummaryData(0, 0, 0, 0)

            WeeklySummaryData(
                totalPoints    = row.totalPoints,
                tasksCompleted = row.totalTasksCompleted,
                totalSteps     = row.totalSteps,
                bestStreak     = row.bestStreak,
                reactions      = emptyList()  // reactions added in Piece 5
            )
        } catch (_: Exception) {
            WeeklySummaryData(0, 0, 0, 0)
        }
    }

    override suspend fun updateProfile(name: String, username: String, region: TimeZoneRegion) {
        val uid = userId() ?: return
        try {
            supabase.from("users").update(
                UpdateUserDto(
                    name          = name,
                    username      = username,
                    usernameLower = username.lowercase(),
                    region        = region.value
                )
            ) {
                filter { eq("id", uid) }
            }
        } catch (_: Exception) { }
    }
}
