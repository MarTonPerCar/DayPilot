package com.example.daypilot_test_desing.data.supabase

import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.data.model.FriendData
import com.example.daypilot_test_desing.core.data.model.FriendWeeklySummary
import com.example.daypilot_test_desing.core.data.model.ReactionType
import com.example.daypilot_test_desing.core.data.model.SearchUserData
import com.example.daypilot_test_desing.core.data.repository.FriendRepository
import com.example.daypilot_test_desing.data.supabase.dto.FriendRequestDto
import com.example.daypilot_test_desing.data.supabase.dto.FriendRowDto
import com.example.daypilot_test_desing.data.supabase.dto.InsertFriendDto
import com.example.daypilot_test_desing.data.supabase.dto.InsertFriendRequestDto
import com.example.daypilot_test_desing.data.supabase.dto.InsertReactionDto
import com.example.daypilot_test_desing.data.supabase.dto.ReactionDto
import com.example.daypilot_test_desing.data.supabase.SupabaseNotificationRepository
import com.example.daypilot_test_desing.data.supabase.dto.SentRequestDto
import com.example.daypilot_test_desing.data.supabase.dto.UserDto
import com.example.daypilot_test_desing.data.supabase.dto.UserStreakDto
import com.example.daypilot_test_desing.data.supabase.dto.WeeklySummaryRowDto
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

    // Two separate queries to avoid OR-filter PostgREST issues.
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

    override suspend fun getFriendIds(): List<String> {
        val uid = userId() ?: return emptyList()
        return getFriendIds(uid)
    }

    override suspend fun getFriends(): List<FriendData> {
        val now = System.currentTimeMillis()
        SessionCache.friends.value?.let { cached ->
            if (now - SessionCache.friendsFetchedAt < SessionCache.SOCIAL_TTL_MS) return cached
        }
        val uid = userId() ?: return emptyList()
        return try {
            val friendIds = getFriendIds(uid)
            if (friendIds.isEmpty()) return emptyList()

            val users   = getUsersForIds(friendIds)
            val streaks = getStreaksForIds(friendIds)

            val allSummaries = try {
                supabase.from("user_weekly_summary").select {
                    filter { isIn("user_id", friendIds) }
                    order("week_start", Order.DESCENDING)
                }.decodeList<WeeklySummaryRowDto>()
            } catch (_: Exception) { emptyList() }
            val summaryByUser = allSummaries.groupBy { it.userId }.mapValues { it.value.first() }

            val summaryIds = summaryByUser.values.map { it.id }
            val myReactions = if (summaryIds.isNotEmpty()) {
                try {
                    supabase.from("reactions").select {
                        filter {
                            eq("from_user_id", uid)
                            isIn("weekly_summary_id", summaryIds)
                        }
                    }.decodeList<ReactionDto>()
                } catch (_: Exception) { emptyList() }
            } else emptyList()
            val reactionBySummaryId = myReactions.associate { it.weeklySummaryId to it.type }

            users.map { user ->
                val summary = summaryByUser[user.id]
                val weeklySummary = summary?.let {
                    FriendWeeklySummary(
                        totalPoints    = it.totalPoints,
                        tasksCompleted = it.totalTasksCompleted,
                        totalSteps     = it.totalSteps,
                        bestStreak     = it.bestStreak,
                        myReaction     = reactionBySummaryId[it.id]?.let { typeName ->
                            ReactionType.entries.firstOrNull { rt -> rt.name.lowercase() == typeName }
                        }
                    )
                }
                FriendData(
                    id            = user.id,
                    name          = user.name,
                    email         = user.email,
                    points        = user.totalPointsHistorical,
                    streak        = streaks[user.id] ?: 0,
                    avatarUrl     = user.photoUrl,
                    weeklySummary = weeklySummary
                )
            }.also { result ->
                SessionCache.friends.value    = result
                SessionCache.friendsFetchedAt = now
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
        supabase.from("friend_requests").delete {
            filter {
                eq("from_user_id", userId)
                eq("to_user_id", uid)
            }
        }
        supabase.from("friends").insert(
            InsertFriendDto(requesterId = userId, receiverId = uid)
        )
        try {
            val myName = supabase.from("users").select {
                filter { eq("id", uid) }
                limit(1)
            }.decodeList<UserDto>().firstOrNull()?.name ?: ""
            SupabaseNotificationRepository.insert(
                userId = userId,
                type   = "FRIEND_ACCEPTED",
                title  = "Solicitud aceptada",
                body   = if (myName.isNotEmpty()) "$myName aceptó tu solicitud de amistad"
                         else "Tu solicitud de amistad fue aceptada"
            )
        } catch (_: Exception) { }
    }

    override suspend fun rejectRequest(userId: String) {
        val uid = this.userId() ?: return
        supabase.from("friend_requests").delete {
            filter {
                eq("from_user_id", userId)
                eq("to_user_id", uid)
            }
        }
    }

    override suspend fun reactToFriend(userId: String, reaction: ReactionType) {
        val uid = this.userId() ?: return
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
        ) { onConflict = "from_user_id,weekly_summary_id" }

        try {
            val users = getUsersForIds(listOf(uid, userId))
            val myName     = users.firstOrNull { it.id == uid }?.name     ?: ""
            val targetName = users.firstOrNull { it.id == userId }?.name  ?: ""

            val emoji = when (reaction) {
                ReactionType.FIRE   -> "🔥"
                ReactionType.CLAP   -> "👏"
                ReactionType.STRONG -> "💪"
                ReactionType.STAR   -> "⭐"
            }

            if (targetName.isNotEmpty()) {
                SupabaseNotificationRepository.insert(
                    userId = userId,
                    type   = "REACTION",
                    title  = "Nueva reacción $emoji",
                    body   = "${myName.ifEmpty { "Un amigo" }} reaccionó a tu semana con $emoji"
                )
            }

            if (targetName.isNotEmpty()) {
                SupabaseNotificationRepository.insertForCurrentUser(
                    type  = "REACTION",
                    title = "Reacción enviada $emoji",
                    body  = "Reaccionaste a la semana de $targetName con $emoji"
                )
            }
        } catch (_: Exception) { }
    }

    override suspend fun searchUsers(query: String): List<SearchUserData> {
        if (query.isBlank()) return emptyList()
        val uid = userId()
        val q = query.lowercase()
        return try {
            supabase.from("users").select {
                filter {
                    or {
                        ilike("username_lower", "%$q%")
                        ilike("email", "%$q%")
                    }
                }
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
        supabase.from("friend_requests").insert(
            InsertFriendRequestDto(fromUserId = uid, toUserId = userId)
        )
        try {
            val myName = supabase.from("users").select {
                filter { eq("id", uid) }
                limit(1)
            }.decodeList<UserDto>().firstOrNull()?.name ?: ""
            SupabaseNotificationRepository.insert(
                userId = userId,
                type   = "FRIEND_REQUEST",
                title  = "Nueva solicitud de amistad",
                body   = if (myName.isNotEmpty()) "$myName quiere ser tu amigo"
                         else "Tienes una nueva solicitud de amistad"
            )
        } catch (_: Exception) { }
    }

    override suspend fun removeFriend(userId: String) {
        val uid = this.userId() ?: return
        supabase.from("friends").delete {
            filter { eq("requester_id", uid); eq("receiver_id", userId) }
        }
        supabase.from("friends").delete {
            filter { eq("requester_id", userId); eq("receiver_id", uid) }
        }
    }

    override suspend fun getPendingSentRequestUserIds(): List<String> {
        val uid = userId() ?: return emptyList()
        return try {
            supabase.from("friend_requests").select {
                filter { eq("from_user_id", uid) }
            }.decodeList<SentRequestDto>()
                .map { it.toUserId }
        } catch (_: Exception) { emptyList() }
    }
}
