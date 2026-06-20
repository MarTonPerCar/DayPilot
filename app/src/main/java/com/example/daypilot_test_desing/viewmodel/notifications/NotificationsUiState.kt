package com.example.daypilot_test_desing.viewmodel.notifications

import com.example.daypilot_test_desing.data.model.NotificationData

data class NotificationsUiState(
    val notifications: List<NotificationData> = emptyList()
)
