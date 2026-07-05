package com.example.daypilot_test_desing.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.core.data.local.NotificationHub
import com.example.daypilot_test_desing.core.data.repository.NotificationRepository
import com.example.daypilot_test_desing.data.supabase.SupabaseNotificationRepository.toModel
import com.example.daypilot_test_desing.data.supabase.dto.NotificationDto
import com.example.daypilot_test_desing.data.supabase.supabase
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

class NotificationsViewModel(private val repo: NotificationRepository) : ViewModel() {

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
        val uid = repo.getCurrentUserId() ?: return@launch
        val fromDb = repo.getAll(uid)
        NotificationHub.repo.mergeServerNotifications(fromDb)
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

            // supabase-kt gives up and settles at UNSUBSCRIBED for good after enough
            // failed rejoin attempts (e.g. a stale JWT) — rebuild instead of leaving
            // notifications dead for the rest of the session.
            channel.status.onEach { status ->
                if (status == RealtimeChannel.Status.UNSUBSCRIBED && realtimeChannel === channel) {
                    delay(5_000)
                    if (realtimeChannel === channel) subscribeToRealtime(userId)
                }
            }.launchIn(viewModelScope)

            channel.subscribe()
            realtimeChannel = channel
        }
    }

    fun markAsRead(id: String) {
        NotificationHub.repo.markAsRead(id)
        viewModelScope.launch { repo.markAsRead(id) }
    }

    fun markAllAsRead() {
        NotificationHub.repo.markAllAsRead()
        viewModelScope.launch {
            val uid = repo.getCurrentUserId() ?: return@launch
            repo.markAllAsRead(uid)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch { runCatching { realtimeChannel?.unsubscribe() } }
    }

    companion object {
        fun factory(repo: NotificationRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    NotificationsViewModel(repo) as T
            }
    }
}
