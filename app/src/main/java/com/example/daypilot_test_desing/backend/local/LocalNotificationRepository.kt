package com.example.daypilot_test_desing.backend.local

import com.example.daypilot_test_desing.backend.model.NotificationData
import com.example.daypilot_test_desing.backend.repository.NotificationRepository

/**
 * In-memory notification list for the current session.
 * Notifications are ephemeral events (friend reactions, step milestones, etc.)
 * that do not have a dedicated server-side table. They start empty and are
 * populated as the user interacts with the app.
 */
class LocalNotificationRepository : NotificationRepository {

    private val notifications = mutableListOf<NotificationData>()

    override fun getNotifications(): List<NotificationData> = notifications.toList()

    override fun markAsRead(id: String) {
        val idx = notifications.indexOfFirst { it.id == id }
        if (idx >= 0) notifications[idx] = notifications[idx].copy(isRead = true)
    }

    fun add(notification: NotificationData) {
        notifications.add(0, notification)
    }
}
