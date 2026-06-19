package com.example.daypilot_test_desing.presentation.notifications

import com.example.daypilot_test_desing.data.model.NotificationData

data class NotificationsUiState(
    val notifications: List<NotificationData> = emptyList()
)
