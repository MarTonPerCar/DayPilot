package com.example.daypilot_test_desing.data.repository

import com.example.daypilot_test_desing.data.model.NotificationData

interface NotificationRepository {
    fun getNotifications(): List<NotificationData>
    fun markAsRead(id: String)
}
