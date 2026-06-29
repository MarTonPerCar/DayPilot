package com.example.daypilot_test_desing.backend.supabase

import com.example.daypilot_test_desing.backend.model.RankingData
import com.example.daypilot_test_desing.backend.repository.RankingRepository
import com.example.daypilot_test_desing.backend.supabase.dto.FriendRowDto
import com.example.daypilot_test_desing.backend.supabase.dto.FriendsRankingDto
import com.example.daypilot_test_desing.backend.supabase.dto.UserStreakDto
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
        val uid = userId()
        return buildRanking(uid).map { dto ->
            RankingData(
                id        = dto.id,
                name      = dto.username,
                points    = dto.pointsLast30Days,
                streak    = dto.currentStreak,
                avatarUrl = dto.photoUrl
            )
        }
    }

    override suspend fun getCurrentUserId(): String = userId()

    override suspend fun getCurrentUserPosition(): Int {
        val uid = userId()
        val ranking = buildRanking(uid)
        val idx = ranking.indexOfFirst { it.id == uid }
        return if (idx >= 0) idx + 1 else ranking.size + 1
    }

    override suspend fun getCurrentUserPoints(): Int {
        val uid = userId()
        return try {
            buildRanking(uid).firstOrNull { it.id == uid }?.pointsLast30Days ?: 0
        } catch (_: Exception) { 0 }
    }

    override suspend fun getCurrentUserStreak(): Int {
        val uid = userId()
        return try {
            supabase.from("user_streaks").select {
                filter { eq("user_id", uid) }
                limit(1)
            }.decodeList<UserStreakDto>().firstOrNull()?.currentStreak ?: 0
        } catch (_: Exception) { 0 }
    }
}
