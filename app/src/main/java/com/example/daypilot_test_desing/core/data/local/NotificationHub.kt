package com.example.daypilot_test_desing.core.data.local

import android.content.Context
import com.example.daypilot_test_desing.core.data.model.NotificationData
import com.example.daypilot_test_desing.core.data.model.NotificationType
import java.util.UUID

object NotificationHub {
    private var _repo: LocalNotificationRepository? = null

    fun init(context: Context) {
        if (_repo == null) _repo = LocalNotificationRepository(context.applicationContext)
    }

    val repo: LocalNotificationRepository
        get() = _repo ?: error("NotificationHub.init(context) must be called before use")

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
