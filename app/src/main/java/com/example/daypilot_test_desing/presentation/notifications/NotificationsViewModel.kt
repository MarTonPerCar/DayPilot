package com.example.daypilot_test_desing.presentation.notifications

import androidx.lifecycle.ViewModel
import com.example.daypilot_test_desing.data.repository.fake.FakeNotificationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(NotificationsUiState(
        notifications = FakeNotificationRepository.getNotifications()
    ))
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    fun markAsRead(id: String) {
        FakeNotificationRepository.markAsRead(id)
        _uiState.value = NotificationsUiState(notifications = FakeNotificationRepository.getNotifications())
    }
}
