package com.example.daypilot_test_desing.backend.supabase

import com.example.daypilot_test_desing.backend.model.FriendData
import com.example.daypilot_test_desing.backend.model.ReactionType
import com.example.daypilot_test_desing.backend.model.SearchUserData
import com.example.daypilot_test_desing.backend.repository.FriendRepository
import com.example.daypilot_test_desing.backend.supabase.dto.FriendRequestDto
import com.example.daypilot_test_desing.backend.supabase.dto.FriendRowDto
import com.example.daypilot_test_desing.backend.supabase.dto.InsertFriendDto
import com.example.daypilot_test_desing.backend.supabase.dto.InsertFriendRequestDto
import com.example.daypilot_test_desing.backend.supabase.dto.InsertReactionDto
import com.example.daypilot_test_desing.backend.supabase.dto.UserDto
import com.example.daypilot_test_desing.backend.supabase.dto.UserStreakDto
import com.example.daypilot_test_desing.backend.supabase.dto.WeeklySummaryRowDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class SupabaseFriendRepository : FriendRepository {

    private fun userId() = supabase.auth.currentUserOrNull()?.id

    private fun ReactionType.toDbString() = name.lowercase()

    private suspend fun getUsersForIds(ids: List<String>): List<UserDto> {
        if (ids.isEmpty()) return emptyList()
        return try {
            supabase.from("users").select {
                filter { isIn("id", ids) }
            }.decodeList<UserDto>()
        } catch (_: Exception) { emptyList() }
    }

    private suspend fun getStreaksForIds(ids: List<String>): Map<String, Int> {
        if (ids.isEmpty()) return emptyMap()
        return try {
            supabase.from("user_streaks").select {
                filter { isIn("user_id", ids) }
            }.decodeList<UserStreakDto>()
                .associate { it.userId to it.currentStreak }
        } catch (_: Exception) { emptyMap() }
    }

    override suspend fun getFriends(): List<FriendData> {
        val uid = userId() ?: return emptyList()
        return try {
            val rows = supabase.from("friends").select {
                filter {
                    or {
                        eq("requester_id", uid)
                        eq("receiver_id", uid)
                    }
                }
            }.decodeList<FriendRowDto>()

            val friendIds = rows.map { if (it.requesterId == uid) it.receiverId else it.requesterId }
            if (friendIds.isEmpty()) return emptyList()

            val users   = getUsersForIds(friendIds)
            val streaks = getStreaksForIds(friendIds)

            users.map { user ->
                FriendData(
                    id        = user.id,
                    name      = user.name,
                    email     = user.email,
                    points    = user.totalPointsHistorical,
                    streak    = streaks[user.id] ?: 0,
                    avatarUrl = user.photoUrl
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    override suspend fun getFriendRequests(): List<FriendData> {
        val uid = userId() ?: return emptyList()
        return try {
            val requests = supabase.from("friend_requests").select {
                filter { eq("to_user_id", uid) }
            }.decodeList<FriendRequestDto>()

            val requesterIds = requests.map { it.fromUserId }
            if (requesterIds.isEmpty()) return emptyList()

            val users   = getUsersForIds(requesterIds)
            val streaks = getStreaksForIds(requesterIds)

            users.map { user ->
                FriendData(
                    id        = user.id,
                    name      = user.name,
                    email     = user.email,
                    points    = user.totalPointsHistorical,
                    streak    = streaks[user.id] ?: 0,
                    avatarUrl = user.photoUrl
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    override suspend fun acceptRequest(userId: String) {
        val uid = this.userId() ?: return
        try {
            supabase.from("friend_requests").delete {
                filter {
                    eq("from_user_id", userId)
                    eq("to_user_id", uid)
                }
            }
            supabase.from("friends").insert(
                InsertFriendDto(requesterId = userId, receiverId = uid)
            )
        } catch (_: Exception) { }
    }

    override suspend fun rejectRequest(userId: String) {
        val uid = this.userId() ?: return
        try {
            supabase.from("friend_requests").delete {
                filter {
                    eq("from_user_id", userId)
                    eq("to_user_id", uid)
                }
            }
        } catch (_: Exception) { }
    }

    override suspend fun reactToFriend(userId: String, reaction: ReactionType) {
        val uid = this.userId() ?: return
        try {
            val summaryId = supabase.from("user_weekly_summary").select {
                filter { eq("user_id", userId) }
                order("week_start", Order.DESCENDING)
                limit(1)
            }.decodeList<WeeklySummaryRowDto>().firstOrNull()?.id ?: return

            supabase.from("reactions").upsert(
                InsertReactionDto(
                    fromUserId      = uid,
                    toUserId        = userId,
                    weeklySummaryId = summaryId,
                    type            = reaction.toDbString()
                )
            )
        } catch (_: Exception) { }
    }

    override suspend fun searchUsers(query: String): List<SearchUserData> {
        if (query.isBlank()) return emptyList()
        val uid = userId()
        return try {
            supabase.from("users").select {
                filter { ilike("username_lower", "%${query.lowercase()}%") }
                limit(20)
            }.decodeList<UserDto>()
                .filter { it.id != uid }
                .map { user ->
                    SearchUserData(
                        id        = user.id,
                        name      = user.name,
                        email     = user.email,
                        points    = user.totalPointsHistorical,
                        streak    = 0,
                        avatarUrl = user.photoUrl
                    )
                }
        } catch (_: Exception) { emptyList() }
    }

    override suspend fun addFriend(userId: String) {
        val uid = this.userId() ?: return
        try {
            supabase.from("friend_requests").insert(
                InsertFriendRequestDto(fromUserId = uid, toUserId = userId)
            )
        } catch (_: Exception) { }
    }

    override suspend fun removeFriend(userId: String) {
        val uid = this.userId() ?: return
        try {
            supabase.from("friends").delete {
                filter { eq("requester_id", uid); eq("receiver_id", userId) }
            }
            supabase.from("friends").delete {
                filter { eq("requester_id", userId); eq("receiver_id", uid) }
            }
        } catch (_: Exception) { }
    }
}
