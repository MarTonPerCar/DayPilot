package com.example.daypilot.main.mainZone.habits.reminders

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.UUID

data class RemindersUiState(val reminders: List<Reminder> = emptyList())

class RemindersViewModel(private val appContext: Context) : ViewModel() {

    private val store = RemindersLocalStore(appContext)

    val ui: StateFlow<RemindersUiState> = store.flow
        .map { RemindersUiState(it) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, RemindersUiState())

    fun buildOnce(title: String, triggerAtMillis: Long, preAlertMin: Int): Reminder {
        val id = UUID.randomUUID().toString()
        return Reminder(
            id = id,
            title = title.trim().ifBlank { "Recordatorio" },
            repeat = RepeatType.ONCE,
            enabled = true,
            triggerAtMillis = triggerAtMillis,
            preAlertMin = preAlertMin,
            lastPreSentForTriggerAt = 0L
        )
    }

    fun buildDaily(title: String, hour: Int, minute: Int, preAlertMin: Int): Reminder {
        val id = UUID.randomUUID().toString()
        val next = nextDailyTriggerMillis(hour, minute)
        return Reminder(
            id = id,
            title = title.trim().ifBlank { "Recordatorio" },
            repeat = RepeatType.DAILY,
            enabled = true,
            triggerAtMillis = next,
            hour = hour,
            minute = minute,
            preAlertMin = preAlertMin,
            lastPreSentForTriggerAt = 0L
        )
    }

    fun saveNew(reminder: Reminder) {
        viewModelScope.launch {
            store.add(reminder)
            ReminderScheduler.cancel(appContext, reminder.id)
            ReminderScheduler.scheduleSmart(appContext, store, reminder)
        }
    }

    fun updateExisting(updated: Reminder) {
        viewModelScope.launch {
            ReminderScheduler.cancel(appContext, updated.id)

            val normalized = updated.copy(
                lastPreSentForTriggerAt = 0L
            )

            store.update(normalized)
            ReminderScheduler.scheduleSmart(appContext, store, normalized)
        }
    }

    fun delete(reminder: Reminder) {
        viewModelScope.launch {
            ReminderScheduler.cancel(appContext, reminder.id)
            store.delete(reminder.id)
        }
    }

    fun setEnabled(reminder: Reminder, enabled: Boolean) {
        viewModelScope.launch {
            val updated = reminder.copy(enabled = enabled)
            store.update(updated)

            if (enabled) {
                ReminderScheduler.cancel(appContext, updated.id)
                ReminderScheduler.scheduleSmart(appContext, store, updated)
            } else {
                ReminderScheduler.cancel(appContext, updated.id)
            }
        }
    }

    fun computeNextDaily(hour: Int, minute: Int): Long = nextDailyTriggerMillis(hour, minute)

    private fun nextDailyTriggerMillis(hour: Int, minute: Int): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        if (cal.timeInMillis <= System.currentTimeMillis() + 1000) {
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        return cal.timeInMillis
    }
}