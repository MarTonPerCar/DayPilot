package com.example.daypilot_test_desing.backend.supabase

import com.example.daypilot_test_desing.backend.repository.ProgressRepository
import com.example.daypilot_test_desing.backend.supabase.dto.DailyLogDto
import com.example.daypilot_test_desing.backend.supabase.dto.DailyProgressDto
import com.example.daypilot_test_desing.backend.supabase.dto.FriendRowDto
import com.example.daypilot_test_desing.backend.supabase.dto.FriendsRankingDto
import com.example.daypilot_test_desing.backend.supabase.dto.InsertPointsLogDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
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
                order("date", Order.DESCENDING)
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

    override suspend fun getRankingPosition(): Int {
        val uid = userId() ?: return 0
        return try {
            val asRequester = try {
                supabase.from("friends").select {
                    filter { eq("requester_id", uid) }
                }.decodeList<FriendRowDto>().map { it.receiverId }
            } catch (_: Exception) { emptyList() }
            val asReceiver = try {
                supabase.from("friends").select {
                    filter { eq("receiver_id", uid) }
                }.decodeList<FriendRowDto>().map { it.requesterId }
            } catch (_: Exception) { emptyList() }
            val friendIds = (asRequester + asReceiver).distinct()
            val allIds = (friendIds + uid).distinct()
            val ranking = supabase.from("friends_ranking").select {
                filter { isIn("id", allIds) }
            }.decodeList<FriendsRankingDto>()
                .sortedByDescending { it.pointsLast30Days }
            val idx = ranking.indexOfFirst { it.id == uid }
            if (idx >= 0) idx + 1 else ranking.size + 1
        } catch (_: Exception) { 0 }
    }
}
