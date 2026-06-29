package com.example.daypilot_test_desing.viewmodel.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.backend.local.NotificationHub
import com.example.daypilot_test_desing.backend.supabase.SupabaseNotificationRepository
import com.example.daypilot_test_desing.backend.supabase.SupabaseNotificationRepository.toModel
import com.example.daypilot_test_desing.backend.supabase.dto.NotificationDto
import com.example.daypilot_test_desing.backend.supabase.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

class NotificationsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private var realtimeChannel: RealtimeChannel? = null

    private val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }

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

    fun load(): Job = viewModelScope.launch {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return@launch
        val fromDb = SupabaseNotificationRepository.getAll(uid)
        NotificationHub.repo.setAll(fromDb)
        subscribeToRealtime(uid)
    }

    private fun subscribeToRealtime(userId: String) {
        realtimeChannel?.let { old ->
            viewModelScope.launch { runCatching { old.unsubscribe() } }
        }
        viewModelScope.launch {
            val channel = supabase.channel("notifications-$userId")
            channel.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                table = "notifications"
            }.onEach { change ->
                runCatching {
                    val dto = json.decodeFromJsonElement<NotificationDto>(change.record)
                    NotificationHub.repo.add(dto.toModel())
                }
            }.launchIn(viewModelScope)
            channel.subscribe()
            realtimeChannel = channel
        }
    }

    fun markAsRead(id: String) {
        NotificationHub.repo.markAsRead(id)
        viewModelScope.launch { SupabaseNotificationRepository.markAsRead(id) }
    }

    fun markAllAsRead() {
        NotificationHub.repo.markAllAsRead()
        viewModelScope.launch {
            val uid = supabase.auth.currentUserOrNull()?.id ?: return@launch
            SupabaseNotificationRepository.markAllAsRead(uid)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { runCatching { realtimeChannel?.unsubscribe() } }
    }
}
