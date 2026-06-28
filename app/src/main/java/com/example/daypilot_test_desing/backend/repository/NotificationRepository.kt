package com.example.daypilot_test_desing.backend.repository

import com.example.daypilot_test_desing.backend.model.NotificationData

interface NotificationRepository {
    fun getNotifications(): List<NotificationData>
    fun markAsRead(id: String)
}
