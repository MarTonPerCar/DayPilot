package com.example.daypilot_test_desing.feature.notifications

import android.app.Application
import android.app.NotificationManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.connectivity.ConnectivityState
import com.example.daypilot_test_desing.core.data.local.NotificationHub
import com.example.daypilot_test_desing.core.data.preferences.AppPreferences
import com.example.daypilot_test_desing.core.data.repository.NotificationRepository
import com.example.daypilot_test_desing.core.reminders.DAILY_CHANNEL_ID
import com.example.daypilot_test_desing.core.reminders.NotificationBodyCodec
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

class NotificationsViewModel(
    application: Application,
    private val repo: NotificationRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private var realtimeChannel: RealtimeChannel? = null

    // Set synchronously before the async subscribe work starts, so a second awaitLoad() call
    // (e.g. the startup retry-once) can't register on an already-joined channel — supabase-kt
    // throws an unhandled IllegalStateException if it does.
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
     *  startup join in DayPilotNavGraph, which needs real success/failure, not just "finished". */
    suspend fun awaitLoad(): Boolean {
        if (!ConnectivityState.ensureOnline()) return false
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
                    if (dto.type == "FRIEND_REQUEST" || dto.type == "FRIEND_ACCEPTED") {
                        // Drop the cache slot so the refresh this triggers fetches fresh data.
                        SessionCache.friends.value    = null
                        SessionCache.friendsFetchedAt = 0L
                        NotificationHub.notifyFriendsChanged()
                    }
                    maybeShowSystemNotification(dto)
                }
            }.launchIn(viewModelScope)

            // supabase-kt settles at UNSUBSCRIBED for good after enough failed rejoin
            // attempts (e.g. a stale JWT) — rebuild instead of leaving notifications dead.
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

    // fn_check_task_reminders / fn_check_streak_danger (Supabase cron) insert these rows at
    // 9am/22:00 UTC. There's no reliable client-side alarm to catch that moment while the app
    // is closed, so this only fires while the app is open and this Realtime channel is live —
    // the instant the row lands, not on a guessed schedule.
    private fun maybeShowSystemNotification(dto: NotificationDto) {
        if (dto.type != "TASK_REMINDER" && dto.type != "STREAK_RISK") return
        val context = getApplication<Application>()
        val prefs = AppPreferences(context)
        if (!prefs.notificationsEnabled) return
        if (dto.type == "TASK_REMINDER" && !prefs.taskRemindersEnabled) return
        if (dto.type == "STREAK_RISK" && !prefs.streakAlertsEnabled) return

        val titleRes = NotificationBodyCodec.titleForType(dto.type) ?: return
        val decodedBody = NotificationBodyCodec.decodeBody(dto.body)
        val body = if (decodedBody != null) {
            val (resId, arg) = decodedBody
            if (arg != null) context.getString(resId, arg) else context.getString(resId)
        } else {
            dto.body
        }

        val notification = NotificationCompat.Builder(context, DAILY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(titleRes))
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        context.getSystemService(NotificationManager::class.java)?.notify(dto.type.hashCode(), notification)
    }

    fun markAsRead(id: String) {
        NotificationHub.repo.markAsRead(id)
        viewModelScope.launch {
            if (!ConnectivityState.ensureOnline()) return@launch
            repo.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        NotificationHub.repo.markAllAsRead()
        viewModelScope.launch {
            if (!ConnectivityState.ensureOnline()) return@launch
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

        fun factory(application: Application, repo: NotificationRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    NotificationsViewModel(application, repo) as T
            }
    }
}
