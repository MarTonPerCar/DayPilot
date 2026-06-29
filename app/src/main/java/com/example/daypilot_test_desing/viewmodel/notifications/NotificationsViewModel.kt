package com.example.daypilot_test_desing.viewmodel.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.backend.local.NotificationHub
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotificationsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            NotificationHub.repo.notificationsFlow.collect { notifs ->
                _uiState.value = NotificationsUiState(
                    notifications = notifs,
                    unreadCount   = notifs.count { !it.isRead }
                )
            }
        }
    }

    fun markAsRead(id: String) {
        NotificationHub.repo.markAsRead(id)
    }
}
