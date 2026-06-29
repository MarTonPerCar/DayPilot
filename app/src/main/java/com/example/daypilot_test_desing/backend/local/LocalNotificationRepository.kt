package com.example.daypilot_test_desing.backend.local

import com.example.daypilot_test_desing.backend.model.NotificationData
import com.example.daypilot_test_desing.backend.repository.NotificationRepository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class LocalNotificationRepository : NotificationRepository {

    private val _notifications = MutableStateFlow<List<NotificationData>>(emptyList())
    val notificationsFlow: StateFlow<List<NotificationData>> = _notifications.asStateFlow()

    override fun getNotifications(): List<NotificationData> = _notifications.value

    override fun markAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
    }

    fun add(notification: NotificationData) {
        _notifications.value = listOf(notification) + _notifications.value
    }
}
