package com.example.daypilot_test_desing.core.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.daypilot_test_desing.core.data.model.NotificationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

class LocalNotificationRepository(context: Context) {

    companion object {
        private const val TAG = "LocalNotificationRepo"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences("daypilot_notifications", Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true }

    private fun load(): List<NotificationData> {
        val raw = prefs.getString("notifications", null) ?: return emptyList()
        return try {
            json.decodeFromString(ListSerializer(NotificationData.serializer()), raw)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode stored notifications, discarding local cache", e)
            emptyList()
        }
    }

    private fun save(list: List<NotificationData>) {
        prefs.edit()
            .putString("notifications", json.encodeToString(ListSerializer(NotificationData.serializer()), list))
            .apply()
    }

    private val _notifications = MutableStateFlow(load())
    val notificationsFlow: StateFlow<List<NotificationData>> = _notifications.asStateFlow()

    fun markAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        save(_notifications.value)
    }

    fun markAllAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        save(_notifications.value)
    }

    fun add(notification: NotificationData) {
        _notifications.value = (listOf(notification) + _notifications.value).take(50)
        save(_notifications.value)
    }

    // Replacing outright (not merging with existing local entries) is what clears stale/deleted rows.
    fun mergeServerNotifications(fromServer: List<NotificationData>) {
        _notifications.value = fromServer.take(50)
        save(_notifications.value)
    }

    fun clear() {
        _notifications.value = emptyList()
        save(_notifications.value)
    }
}
