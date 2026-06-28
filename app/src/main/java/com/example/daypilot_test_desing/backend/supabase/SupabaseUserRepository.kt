package com.example.daypilot_test_desing.backend.supabase

import com.example.daypilot_test_desing.backend.model.TimeZoneRegion
import com.example.daypilot_test_desing.backend.model.UserProfile
import com.example.daypilot_test_desing.backend.model.WeeklySummaryData
import com.example.daypilot_test_desing.backend.repository.UserRepository
import com.example.daypilot_test_desing.backend.supabase.dto.DailyLogDto
import com.example.daypilot_test_desing.backend.supabase.dto.UpdateUserDto
import com.example.daypilot_test_desing.backend.supabase.dto.UserDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

            val region = TimeZoneRegion.entries.firstOrNull { it.value == dto.region }
                ?: TimeZoneRegion.EUROPE_MADRID
            val memberSince = dto.createdAt.take(4)  // year from "2025-01-01T..."

            UserProfile(
                id            = dto.id,
                name          = dto.name,
                username      = dto.username,
                email         = dto.email,
                avatarUrl     = dto.avatarUrl,
                region        = region,
                memberSince   = memberSince,
                level         = dto.level,
                totalPoints   = dto.totalPointsHistorical,
                currentStreak = dto.currentStreak,
                longestStreak = dto.longestStreak
            )
        } catch (_: Exception) {
            UserProfile(id = uid, name = "", username = "", email = "")
        }
    }

    override suspend fun getWeeklySummary(): WeeklySummaryData {
        val uid = userId() ?: return WeeklySummaryData(0, 0, 0, 0)
        return try {
            val logs = supabase.from("user_daily_log").select {
                filter { eq("user_id", uid) }
                order("date", ascending = false)
                limit(7)
            }.decodeList<DailyLogDto>()

            WeeklySummaryData(
                totalPoints    = logs.sumOf { it.totalPoints },
                tasksCompleted = logs.sumOf { it.tasksCompleted },
                totalSteps     = logs.sumOf { it.steps },
                bestStreak     = 0,   // streak data lives on users row; not repeated in log
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
                UpdateUserDto(name = name, username = username, region = region.value)
            ) {
                filter { eq("id", uid) }
            }
        } catch (_: Exception) { }
    }
}
