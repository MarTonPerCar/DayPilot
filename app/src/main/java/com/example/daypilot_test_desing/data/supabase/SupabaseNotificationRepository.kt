package com.example.daypilot_test_desing.data.supabase

import android.util.Log
import com.example.daypilot_test_desing.core.data.model.NotificationData
import com.example.daypilot_test_desing.core.data.model.NotificationType
import com.example.daypilot_test_desing.core.data.model.RawTodayNotification
import com.example.daypilot_test_desing.core.data.repository.NotificationRepository
import com.example.daypilot_test_desing.data.supabase.dto.InsertNotificationDto
import com.example.daypilot_test_desing.data.supabase.dto.NotificationDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.UUID

object SupabaseNotificationRepository : NotificationRepository {

    private const val TAG = "SupabaseNotificationRepo"
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
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch unread count for user $userId", e)
            0
        }
    }

    override suspend fun getAll(userId: String): List<NotificationData> {
        return try {
            supabase.from("notifications").select {
                filter { eq("user_id", userId) }
                order("created_at", Order.DESCENDING)
                limit(DISPLAY_LIMIT.toLong())
            }.decodeList<NotificationDto>().map { it.toModel() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch notifications for user $userId", e)
            emptyList()
        }
    }

    override suspend fun markAsRead(notificationId: String) {
        try {
            supabase.from("notifications").update({ set("is_read", true) }) {
                filter { eq("id", notificationId) }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark notification $notificationId as read", e)
        }
    }

    override suspend fun markAllAsRead(userId: String) {
        try {
            supabase.from("notifications").update({ set("is_read", true) }) {
                filter {
                    eq("user_id", userId)
                    eq("is_read", false)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to mark all notifications as read for user $userId", e)
        }
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
        } catch (e: Exception) {
            Log.e(TAG, "Failed to insert $type notification for user $userId", e)
        }
    }

    override suspend fun getLatestOfTypeToday(userId: String, type: String): RawTodayNotification? {
        return try {
            val startOfToday = LocalDate.now(ZoneId.systemDefault())
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toString()
            supabase.from("notifications").select {
                filter {
                    eq("user_id", userId)
                    eq("type", type)
                    gte("created_at", startOfToday)
                }
                order("created_at", Order.DESCENDING)
                limit(1)
            }.decodeList<NotificationDto>().firstOrNull()?.let {
                RawTodayNotification(type = it.type, rawTitle = it.title, rawBody = it.body)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch today's $type notification for user $userId", e)
            null
        }
    }

    suspend fun insertForCurrentUser(type: String, title: String, body: String) {
        // Auth may still be restoring the session when called from a BroadcastReceiver.
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
        } catch (e: Exception) {
            Log.w(TAG, "Failed to prune old notifications for user $userId", e)
        }
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
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse notification timestamp: $isoTimestamp", e)
            ""
        }
    }
}
