package com.example.daypilot_test_desing.backend.local

import com.example.daypilot_test_desing.backend.model.NotificationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalNotificationRepository {

    private val _notifications = MutableStateFlow<List<NotificationData>>(emptyList())
    val notificationsFlow: StateFlow<List<NotificationData>> = _notifications.asStateFlow()

    fun markAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun markAllAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun add(notification: NotificationData) {
        _notifications.value = (listOf(notification) + _notifications.value).take(50)
    }

    fun setAll(notifications: List<NotificationData>) {
        _notifications.value = notifications
    }

    fun clear() {
        _notifications.value = emptyList()
    }
}
