package com.example.daypilot_test_desing.feature.notifications

import com.example.daypilot_test_desing.core.data.model.NotificationData

data class NotificationsUiState(
    val notifications: List<NotificationData> = emptyList(),
    val unreadCount: Int = 0
)
