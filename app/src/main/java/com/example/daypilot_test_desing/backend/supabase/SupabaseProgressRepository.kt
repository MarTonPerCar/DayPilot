package com.example.daypilot_test_desing.backend.supabase

import com.example.daypilot_test_desing.backend.repository.ProgressRepository
import com.example.daypilot_test_desing.backend.supabase.dto.DailyLogDto
import com.example.daypilot_test_desing.backend.supabase.dto.DailyProgressDto
import com.example.daypilot_test_desing.backend.supabase.dto.InsertPointsLogDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SupabaseProgressRepository : ProgressRepository {

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
    private fun userId() = supabase.auth.currentUserOrNull()?.id

    override suspend fun getTodayProgress(): DailyProgressDto {
        val uid = userId() ?: return DailyProgressDto(userId = "", date = today())
        return try {
            supabase.from("daily_progress").select {
                filter {
                    eq("user_id", uid)
                    eq("date", today())
                }
                limit(1)
            }.decodeList<DailyProgressDto>().firstOrNull()
                ?: DailyProgressDto(userId = uid, date = today())
        } catch (_: Exception) {
            DailyProgressDto(userId = "", date = today())
        }
    }

    override suspend fun getHistory(days: Int): List<DailyLogDto> {
        val uid = userId() ?: return emptyList()
        return try {
            supabase.from("user_daily_log").select {
                filter { eq("user_id", uid) }
                order("date", ascending = false)
                limit(days.toLong())
            }.decodeList<DailyLogDto>()
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun logPoints(points: Int, source: String) {
        val uid = userId() ?: return
        try {
            supabase.from("points_log").insert(
                InsertPointsLogDto(userId = uid, points = points, source = source, dayKey = today())
            )
        } catch (_: Exception) { }
    }

    override suspend fun getRankingPosition(): Int = 1  // stub — Piece 6 (Rivalry) will replace
}
