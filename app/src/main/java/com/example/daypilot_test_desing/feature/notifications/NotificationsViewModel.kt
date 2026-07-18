package com.example.daypilot_test_desing.feature.notifications

import android.util.Log
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

    // Set synchronously the moment a subscribe is kicked off (subscribeToRealtime's own work
    // is async) — guards against awaitLoad() being called more than once on the same instance
    // (e.g. Block 4's startup retry-once) trying to register postgresChangeFlow on a channel
    // that's already joined, which supabase-kt rejects with an unhandled IllegalStateException.
    private var realtimeSubscriptionStarted = false

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

    fun load(): Job = viewModelScope.launch { awaitLoad() }

    /** Suspends until this ViewModel's data has actually loaded (or failed) — used by the
     *  startup join in DayPilotNavGraph, which needs real success/failure, not just "finished".
     *  Unlike the fire-and-forget load() above, this never lets an exception escape uncaught —
     *  it used to (no try/catch at all), which could crash the app on a cold-start network hiccup. */
    suspend fun awaitLoad(): Boolean {
        return try {
            val uid = repo.getCurrentUserId() ?: return false
            val fromDb = repo.getAll(uid)
            NotificationHub.repo.mergeServerNotifications(fromDb)
            if (!realtimeSubscriptionStarted) {
                realtimeSubscriptionStarted = true
                subscribeToRealtime(uid)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load notifications", e)
            false
        }
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
        private const val TAG = "NotificationsViewModel"

        fun factory(repo: NotificationRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    NotificationsViewModel(repo) as T
            }
    }
}
