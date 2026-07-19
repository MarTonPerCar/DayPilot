package com.example.daypilot_test_desing.core.data.local

import android.content.Context
import com.example.daypilot_test_desing.core.data.model.NotificationData
import com.example.daypilot_test_desing.core.data.model.NotificationType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    // Friends has no realtime channel of its own; signaled here from the notifications channel instead.
    private val _friendsShouldRefresh = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val friendsShouldRefresh: SharedFlow<Unit> = _friendsShouldRefresh.asSharedFlow()

    fun notifyFriendsChanged() {
        _friendsShouldRefresh.tryEmit(Unit)
    }
}
