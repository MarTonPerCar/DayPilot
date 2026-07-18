package com.example.daypilot_test_desing.data.supabase

import android.util.Log
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
import com.example.daypilot_test_desing.data.supabase.dto.SentRequestDto
import com.example.daypilot_test_desing.data.supabase.dto.UserDto
import com.example.daypilot_test_desing.data.supabase.dto.UserStreakDto
import com.example.daypilot_test_desing.data.supabase.dto.WeeklySummaryRowDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order

class SupabaseFriendRepository : FriendRepository {

    companion object {
        private const val TAG = "SupabaseFriendRepo"
    }

    private fun userId() = supabase.auth.currentUserOrNull()?.id

    private fun ReactionType.toDbString() = name.lowercase()

    // Left to propagate (not swallowed) — this directly determines who shows up in the
    // friends list, so a transient failure here must not silently look like "no friends"
    // (see getFriends()'s caller, which already leaves the previous state untouched on error).
    private suspend fun getUsersForIds(ids: List<String>): List<UserDto> {
        if (ids.isEmpty()) return emptyList()
        return try {
            supabase.from("users").select {
                filter { isIn("id", ids) }
            }.decodeList<UserDto>()
        } catch (e: Exception) {
            Log.e(TAG, "getUsersForIds: failed fetching ${ids.size} user(s)", e)
            throw e
        }
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

    // Two separate queries to avoid OR-filter PostgREST issues. Left to propagate (not
    // swallowed) — same reasoning as getUsersForIds: this decides who's a friend at all,
    // so a transient failure here must not silently look like "no friends".
    private suspend fun getFriendIds(uid: String): List<String> {
        val asRequester = try {
            supabase.from("friends").select {
                filter { eq("requester_id", uid) }
            }.decodeList<FriendRowDto>().map { it.receiverId }
        } catch (e: Exception) {
            Log.e(TAG, "getFriendIds: failed fetching requester-side rows for $uid", e)
            throw e
        }

        val asReceiver = try {
            supabase.from("friends").select {
                filter { eq("receiver_id", uid) }
            }.decodeList<FriendRowDto>().map { it.requesterId }
        } catch (e: Exception) {
            Log.e(TAG, "getFriendIds: failed fetching receiver-side rows for $uid", e)
            throw e
        }

        return (asRequester + asReceiver).distinct()
    }

    override suspend fun getFriendIds(): List<String> {
        val uid = userId() ?: return emptyList()
        return getFriendIds(uid)
    }

    // No outer catch-to-empty here anymore — a genuine failure now propagates to the
    // caller (FriendsViewModel.load()) instead of masquerading as "this user has zero
    // friends", which used to silently blank out an already-correct, already-displayed
    // list. getStreaksForIds/weekly-summary/reactions below stay best-effort on purpose:
    // losing those only degrades a friend card's extra details, not the list itself.
    override suspend fun getFriends(): List<FriendData> {
        val now = System.currentTimeMillis()
        SessionCache.friends.value?.let { cached ->
            if (now - SessionCache.friendsFetchedAt < SessionCache.SOCIAL_TTL_MS) return cached
        }
        val uid = userId() ?: return emptyList()

        val friendIds = getFriendIds(uid)
        if (friendIds.isEmpty()) return emptyList()

        val users   = getUsersForIds(friendIds)
        val streaks = getStreaksForIds(friendIds)

        val allSummaries = try {
            supabase.from("user_weekly_summary").select {
                filter { isIn("user_id", friendIds) }
                order("week_start", Order.DESCENDING)
            }.decodeList<WeeklySummaryRowDto>()
        } catch (e: Exception) {
            Log.e(TAG, "getFriends: failed fetching weekly summaries, showing friends without them", e)
            emptyList()
        }
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
            } catch (e: Exception) {
                Log.e(TAG, "getFriends: failed fetching my reactions", e)
                emptyList()
            }
        } else emptyList()
        val reactionBySummaryId = myReactions.associate { it.weeklySummaryId to it.type }

        return users.map { user ->
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
            Log.d(TAG, "getFriends: fetched ${result.size} friend(s) for $uid")
        }
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
        // FRIEND_ACCEPTED notification is now inserted by a Supabase DB trigger.
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
        // REACTION notification (to the friend) is now inserted by a Supabase DB trigger.
        // The "reaction sent" self-confirmation is a local-only toast now — see FriendsViewModel.
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
        // FRIEND_REQUEST notification is now inserted by a Supabase DB trigger.
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
