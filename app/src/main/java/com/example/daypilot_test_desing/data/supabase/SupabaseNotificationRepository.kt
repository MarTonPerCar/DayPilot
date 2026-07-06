package com.example.daypilot_test_desing.data.supabase

import com.example.daypilot_test_desing.core.data.model.NotificationData
import com.example.daypilot_test_desing.core.data.model.NotificationType
import com.example.daypilot_test_desing.core.data.repository.NotificationRepository
import com.example.daypilot_test_desing.data.supabase.dto.InsertNotificationDto
import com.example.daypilot_test_desing.data.supabase.dto.NotificationDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.util.UUID

object SupabaseNotificationRepository : NotificationRepository {

    private const val DISPLAY_LIMIT = 30
    private const val RETAIN_LIMIT  = 50

    override suspend fun getCurrentUserId(): String? = supabase.auth.currentUserOrNull()?.id

    override suspend fun getUnreadCount(userId: String): Int {
        return try {
            supabase.from("notifications").select {
                filter {
                    eq("user_id", userId)
                    eq("is_read", false)
                }
            }.decodeList<NotificationDto>().size
        } catch (_: Exception) { 0 }
    }

    override suspend fun getAll(userId: String): List<NotificationData> {
        return try {
            supabase.from("notifications").select {
                filter { eq("user_id", userId) }
                order("created_at", Order.DESCENDING)
                limit(DISPLAY_LIMIT.toLong())
            }.decodeList<NotificationDto>().map { it.toModel() }
        } catch (_: Exception) { emptyList() }
    }

    override suspend fun markAsRead(notificationId: String) {
        try {
            supabase.from("notifications").update({ set("is_read", true) }) {
                filter { eq("id", notificationId) }
            }
        } catch (_: Exception) { }
    }

    override suspend fun markAllAsRead(userId: String) {
        try {
            supabase.from("notifications").update({ set("is_read", true) }) {
                filter {
                    eq("user_id", userId)
                    eq("is_read", false)
                }
            }
        } catch (_: Exception) { }
    }

    override suspend fun insert(userId: String, type: String, title: String, body: String) {
        try {
            supabase.from("notifications").insert(
                InsertNotificationDto(
                    id     = UUID.randomUUID().toString(),
                    userId = userId,
                    type   = type,
                    title  = title,
                    body   = body
                )
            )
            pruneOldest(userId)
        } catch (_: Exception) { }
    }

    suspend fun insertForCurrentUser(type: String, title: String, body: String) {
        // Needed when called from a BroadcastReceiver, where Auth may still be
        // restoring the session from SharedPreferences; a no-op otherwise.
        supabase.auth.awaitInitialization()
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        insert(uid, type, title, body)
    }

    private suspend fun pruneOldest(userId: String) {
        try {
            val all = supabase.from("notifications").select {
                filter { eq("user_id", userId) }
                order("created_at", Order.DESCENDING)
                limit(1000)
            }.decodeList<NotificationDto>()
            if (all.size > RETAIN_LIMIT) {
                val ids = all.drop(RETAIN_LIMIT).map { it.id }
                supabase.from("notifications").delete {
                    filter { isIn("id", ids) }
                }
            }
        } catch (_: Exception) { }
    }

    fun NotificationDto.toModel(): NotificationData {
        return NotificationData(
            id      = this.id,
            title   = this.title,
            message = this.body,
            timeAgo = relativeTime(this.createdAt),
            type    = dbTypeToLocal(this.type),
            isRead  = this.isRead
        )
    }

    private fun dbTypeToLocal(dbType: String): NotificationType = when (dbType) {
        "FRIEND_REQUEST", "FRIEND_ACCEPTED", "REACTION" -> NotificationType.SOCIAL
        "LEVEL_UP"                                      -> NotificationType.ACHIEVEMENT
        "STREAK_RISK"                                   -> NotificationType.STREAK
        "STEPS_GOAL"                                    -> NotificationType.STEPS
        "TIMER_DONE"                                    -> NotificationType.ACHIEVEMENT
        "TASK_COMPLETED"                                -> NotificationType.TASK
        "TASK_REMINDER"                                 -> NotificationType.REMINDER
        "DAILY_SUMMARY"                                 -> NotificationType.REMINDER
        else                                            -> NotificationType.REMINDER
    }

    private fun relativeTime(isoTimestamp: String): String {
        return try {
            val instant = OffsetDateTime.parse(isoTimestamp).toInstant()
            val diff    = Duration.between(instant, Instant.now())
            when {
                diff.toMinutes() < 1  -> "Ahora"
                diff.toMinutes() < 60 -> "${diff.toMinutes()} min"
                diff.toHours()   < 24 -> "${diff.toHours()}h"
                diff.toDays()    < 7  -> "${diff.toDays()}d"
                else                  -> "${diff.toDays() / 7}sem"
            }
        } catch (_: Exception) { "" }
    }
}
