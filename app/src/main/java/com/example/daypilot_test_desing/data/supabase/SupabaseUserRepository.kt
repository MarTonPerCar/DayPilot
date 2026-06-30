package com.example.daypilot_test_desing.data.supabase

import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.data.model.ReactionType
import com.example.daypilot_test_desing.core.data.model.ReceivedReaction
import com.example.daypilot_test_desing.core.data.model.TimeZoneRegion
import com.example.daypilot_test_desing.core.data.model.UserProfile
import com.example.daypilot_test_desing.core.data.model.WeeklySummaryData
import com.example.daypilot_test_desing.core.data.repository.UserRepository
import com.example.daypilot_test_desing.data.supabase.dto.ReactionDto
import com.example.daypilot_test_desing.data.supabase.dto.UpdateUserDto
import com.example.daypilot_test_desing.data.supabase.dto.UserDto
import com.example.daypilot_test_desing.data.supabase.dto.UserStreakDto
import com.example.daypilot_test_desing.data.supabase.dto.WeeklySummaryRowDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType

class SupabaseUserRepository : UserRepository {

    private fun userId() = supabase.auth.currentUserOrNull()?.id

    override suspend fun getCurrentUser(): UserProfile {
        SessionCache.userProfile.value?.let { return it }
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

            val profile = UserProfile(
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
            SessionCache.userProfile.value = profile
            profile
        } catch (_: Exception) {
            UserProfile(id = uid, name = "", username = "", email = "")
        }
    }

    override suspend fun getWeeklySummary(): WeeklySummaryData {
        val uid = userId() ?: return WeeklySummaryData(0, 0, 0, 0)
        return try {
            val row = supabase.from("user_weekly_summary").select {
                filter { eq("user_id", uid) }
                order("week_start", Order.DESCENDING)
                limit(1)
            }.decodeList<WeeklySummaryRowDto>().firstOrNull()
                ?: return WeeklySummaryData(0, 0, 0, 0)

            val reactionRows = try {
                supabase.from("reactions").select {
                    filter { eq("weekly_summary_id", row.id) }
                }.decodeList<ReactionDto>()
            } catch (_: Exception) { emptyList() }

            val reactions = if (reactionRows.isNotEmpty()) {
                val senderIds = reactionRows.map { it.fromUserId }
                val senders = try {
                    supabase.from("users").select {
                        filter { isIn("id", senderIds) }
                    }.decodeList<UserDto>()
                } catch (_: Exception) { emptyList() }
                val senderById = senders.associateBy { it.id }
                reactionRows.mapNotNull { r ->
                    val name = senderById[r.fromUserId]?.name ?: return@mapNotNull null
                    val type = ReactionType.entries.firstOrNull { it.name.lowercase() == r.type }
                        ?: return@mapNotNull null
                    ReceivedReaction(fromName = name, reaction = type, avatarUrl = senderById[r.fromUserId]?.photoUrl)
                }
            } else emptyList()

            WeeklySummaryData(
                totalPoints    = row.totalPoints,
                tasksCompleted = row.totalTasksCompleted,
                totalSteps     = row.totalSteps,
                bestStreak     = row.bestStreak,
                reactions      = reactions
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
            SessionCache.userProfile.value = SessionCache.userProfile.value?.copy(
                name     = name,
                username = username,
                region   = region
            )
        } catch (_: Exception) { }
    }

    override suspend fun uploadAvatar(bytes: ByteArray, mimeType: String): String? {
        val uid = userId() ?: return null
        val ext = mimeType.substringAfterLast("/").replace("jpeg", "jpg").take(4)
        // Path includes timestamp so re-uploads get a fresh URL (Coil doesn't re-fetch same-URL images)
        val path = "$uid/${System.currentTimeMillis()}.$ext"
        return try {
            supabase.storage.from("avatars").upload(path, bytes) {
                upsert = true
                contentType = ContentType.parse(mimeType)
            }
            val url = supabase.storage.from("avatars").publicUrl(path)
            supabase.from("users").update({ set("photo_url", url) }) {
                filter { eq("id", uid) }
            }
            SessionCache.userProfile.value = SessionCache.userProfile.value?.copy(avatarUrl = url)
            url
        } catch (_: Exception) {
            null
        }
    }
}
