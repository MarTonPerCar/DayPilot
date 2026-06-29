package com.example.daypilot_test_desing.backend.repository

import com.example.daypilot_test_desing.backend.model.NotificationData

interface NotificationRepository {
    suspend fun getUnreadCount(userId: String): Int
    suspend fun getAll(userId: String): List<NotificationData>
    suspend fun markAsRead(notificationId: String)
    suspend fun markAllAsRead(userId: String)
    suspend fun insert(userId: String, type: String, title: String, body: String)
}
