package com.example.daypilot_test_desing.core.data.local

import com.example.daypilot_test_desing.core.data.model.NotificationData
import com.example.daypilot_test_desing.core.data.model.NotificationType
import java.util.UUID

object NotificationHub {
    val repo = LocalNotificationRepository()

    fun add(title: String, message: String, type: NotificationType) {
        repo.add(
            NotificationData(
                id      = UUID.randomUUID().toString(),
                title   = title,
                message = message,
                timeAgo = "Ahora",
                type    = type,
                isRead  = false
            )
        )
    }

    fun clear() {
        repo.clear()
    }
}
