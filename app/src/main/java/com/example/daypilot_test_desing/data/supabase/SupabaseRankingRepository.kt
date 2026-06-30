package com.example.daypilot_test_desing.data.supabase

import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.data.model.RankingData
import com.example.daypilot_test_desing.core.data.repository.RankingRepository
import com.example.daypilot_test_desing.data.supabase.dto.DailyProgressDto
import com.example.daypilot_test_desing.data.supabase.dto.FriendRowDto
import com.example.daypilot_test_desing.data.supabase.dto.FriendsRankingDto
import com.example.daypilot_test_desing.data.supabase.dto.UserDto
import com.example.daypilot_test_desing.data.supabase.dto.UserStreakDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from

class SupabaseRankingRepository : RankingRepository {

    private fun userId() = supabase.auth.currentUserOrNull()?.id ?: ""

    private suspend fun getFriendIds(uid: String): List<String> {
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
        return (asRequester + asReceiver).distinct()
    }

    // Returns me + friends sorted by 30-day points descending.
    private suspend fun buildRanking(uid: String): List<FriendsRankingDto> {
        val friendIds = getFriendIds(uid)
        val allIds = (friendIds + uid).distinct()
        return try {
            supabase.from("friends_ranking").select {
                filter { isIn("id", allIds) }
            }.decodeList<FriendsRankingDto>()
                .sortedByDescending { it.pointsLast30Days }
        } catch (_: Exception) { emptyList() }
    }

    override suspend fun getRanking(): List<RankingData> {
        val now = System.currentTimeMillis()
        SessionCache.ranking.value?.let { cached ->
            if (now - SessionCache.rankingFetchedAt < SessionCache.SOCIAL_TTL_MS) return cached
        }
        val uid = userId()
        val result = buildRanking(uid).map { dto ->
            RankingData(
                id        = dto.id,
                name      = dto.name.ifBlank { dto.username },
                points    = dto.pointsLast30Days,
                streak    = dto.currentStreak ?: 0,
                level     = dto.level,
                avatarUrl = dto.photoUrl
            )
        }
        SessionCache.ranking.value    = result
        SessionCache.rankingFetchedAt = now
        return result
    }

    override suspend fun getCurrentUserId(): String = userId()

    // Queries source tables directly — bypasses friends_ranking VIEW so RLS on
    // user_streaks (streaks_own FOR ALL) doesn't block the current user's streak.
    override suspend fun getCurrentUserData(): RankingData? {
        val uid = userId()
        if (uid.isEmpty()) return null
        return try {
            val user = supabase.from("users").select {
                filter { eq("id", uid) }
                limit(1)
            }.decodeList<UserDto>().firstOrNull() ?: return null

            val streak = try {
                supabase.from("user_streaks").select {
                    filter { eq("user_id", uid) }
                    limit(1)
                }.decodeList<UserStreakDto>().firstOrNull()?.currentStreak ?: 0
            } catch (_: Exception) { 0 }

            val points = try {
                supabase.from("daily_progress").select {
                    filter { eq("user_id", uid) }
                    limit(1)
                }.decodeList<DailyProgressDto>().firstOrNull()?.totalPoints ?: 0
            } catch (_: Exception) { 0 }

            RankingData(
                id        = uid,
                name      = user.name,
                points    = points,
                streak    = streak,
                level     = user.level,
                avatarUrl = user.photoUrl
            )
        } catch (_: Exception) { null }
    }

}
