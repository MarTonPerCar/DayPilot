package com.example.daypilot_test_desing.data.supabase

import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.data.model.RankingData
import com.example.daypilot_test_desing.core.data.model.calculateLevel
import com.example.daypilot_test_desing.core.data.model.pointsToNextLevel
import com.example.daypilot_test_desing.core.data.repository.ProgressRepository
import com.example.daypilot_test_desing.data.supabase.dto.DailyLogDto
import com.example.daypilot_test_desing.data.supabase.dto.DailyProgressDto
import com.example.daypilot_test_desing.data.supabase.dto.FriendRowDto
import com.example.daypilot_test_desing.data.supabase.dto.FriendsRankingDto
import com.example.daypilot_test_desing.data.supabase.dto.HabitsDailyReadTimerDto
import com.example.daypilot_test_desing.data.supabase.dto.HabitsDailyTimerDto
import com.example.daypilot_test_desing.data.supabase.dto.InsertPointsLogDto
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
        SessionCache.todayProgress.value?.let { cached ->
            if (cached.date == today()) return cached
        }
        val uid = userId() ?: return DailyProgressDto(userId = "", date = today())
        return try {
            val result = supabase.from("daily_progress").select {
                filter {
                    eq("user_id", uid)
                    eq("date", today())
                }
                limit(1)
            }.decodeList<DailyProgressDto>().firstOrNull()
                ?: DailyProgressDto(userId = uid, date = today())
            SessionCache.todayProgress.value = result
            result
        } catch (_: Exception) {
            DailyProgressDto(userId = "", date = today())
        }
    }

    override suspend fun getHistory(days: Int): List<DailyLogDto> {
        val now = System.currentTimeMillis()
        SessionCache.weeklyHistory.value?.let { cached ->
            if (now - SessionCache.weeklyHistoryFetchedAt < SessionCache.HISTORY_TTL_MS) return cached
        }
        val uid = userId() ?: return emptyList()
        return try {
            val result = supabase.from("user_daily_log").select {
                filter { eq("user_id", uid) }
                order("date", Order.DESCENDING)
                limit(days.toLong())
            }.decodeList<DailyLogDto>()
            SessionCache.weeklyHistory.value    = result
            SessionCache.weeklyHistoryFetchedAt = now
            result
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun logPoints(points: Int, source: String) {
        val uid = userId() ?: return
        supabase.from("points_log").insert(
            InsertPointsLogDto(userId = uid, points = points, source = source, dayKey = today())
        )
        SessionCache.todayProgress.value = SessionCache.todayProgress.value?.let { current ->
            current.copy(
                tasksPoints      = current.tasksPoints      + if (source == "TASKS")       points else 0,
                stepsPoints      = current.stepsPoints      + if (source == "STEPS")       points else 0,
                wellnessPoints   = current.wellnessPoints   + if (source == "WELLNESS")    points else 0,
                timerPoints      = current.timerPoints      + if (source == "TIMER")       points else 0,
                techHealthPoints = current.techHealthPoints + if (source == "TECH_HEALTH") points else 0,
                totalPoints      = current.totalPoints      + points
            )
        }
        val profile = SessionCache.userProfile.value
        if (profile != null) {
            val newTotal = profile.totalPoints + points
            val newLevel = calculateLevel(newTotal)
            SessionCache.userProfile.value = profile.copy(
                totalPoints       = newTotal,
                level             = newLevel,
                pointsToNextLevel = pointsToNextLevel(newLevel)
            )
            // LEVEL_UP notification is now inserted by a Supabase DB trigger.
        }
    }

    override suspend fun getRankingPosition(): Int {
        SessionCache.ranking.value?.let { cached ->
            val uid = userId() ?: return 0
            val idx = cached.indexOfFirst { it.id == uid }
            return if (idx >= 0) idx + 1 else cached.size + 1
        }
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
            val allIds    = (friendIds + uid).distinct()
            val ranking = supabase.from("friends_ranking").select {
                filter { isIn("id", allIds) }
            }.decodeList<FriendsRankingDto>()
                .sortedByDescending { it.pointsLast30Days }
            SessionCache.ranking.value       = ranking.map { dto ->
                RankingData(
                    id        = dto.id,
                    name      = dto.name.ifBlank { dto.username },
                    points    = dto.pointsLast30Days,
                    streak    = dto.currentStreak ?: 0,
                    level     = dto.level,
                    avatarUrl = dto.photoUrl
                )
            }
            SessionCache.rankingFetchedAt    = System.currentTimeMillis()
            val idx = ranking.indexOfFirst { it.id == uid }
            if (idx >= 0) idx + 1 else ranking.size + 1
        } catch (_: Exception) { 0 }
    }

    override suspend fun completeTimerSession(): Boolean {
        val uid = userId() ?: return false
        val alreadyEarned = try {
            supabase.from("habits_daily").select {
                filter { eq("user_id", uid); eq("date", today()) }
                limit(1)
            }.decodeList<HabitsDailyReadTimerDto>()
                .firstOrNull()?.timerPointEarned ?: false
        } catch (_: Exception) { false }
        if (alreadyEarned) return false

        logPoints(10, "TIMER")
        supabase.from("habits_daily").upsert(
            HabitsDailyTimerDto(userId = uid, date = today(), timerPointEarned = true)
        ) { onConflict = "user_id,date" }
        return true
    }
}
