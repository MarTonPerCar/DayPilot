package com.example.daypilot_test_desing.feature.reminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.daypilot_test_desing.core.data.model.FrequencyType
import com.example.daypilot_test_desing.core.data.model.ReminderFormDataInfo
import com.example.daypilot_test_desing.core.data.preferences.AppPreferences
import com.example.daypilot_test_desing.core.data.local.SharedPrefsReminderRepository
import com.example.daypilot_test_desing.core.reminders.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RemindersViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SharedPrefsReminderRepository(application)
    private val appPrefs   = AppPreferences(application)

    private val _uiState = MutableStateFlow(RemindersUiState(reminders = repository.getReminders()))
    val uiState: StateFlow<RemindersUiState> = _uiState.asStateFlow()

    private fun refresh() {
        _uiState.value = RemindersUiState(reminders = repository.getReminders())
    }

    fun addReminder(form: ReminderFormDataInfo) {
        val triggerAtMillis = when {
            form.quickMinutes != null      -> System.currentTimeMillis() + form.quickMinutes * 60_000L
            form.scheduledDateTime != null -> form.scheduledDateTime.timeInMillis
            else                           -> return
        }
        val enriched = form.copy(triggerAtMillis = triggerAtMillis)
        val reminder = repository.addReminder(enriched)
        val ctx = getApplication<Application>()
        val isOneTime = form.frequencyType == FrequencyType.ONCE
        if (appPrefs.notificationsEnabled) {
            ReminderScheduler.schedule(
                context         = ctx,
                reminderId      = reminder.id,
                title           = reminder.title,
                triggerAtMillis = triggerAtMillis,
                isOneTime       = isOneTime,
                frequencyType   = form.frequencyType.name
            )
            if (form.earlyWarning && triggerAtMillis - System.currentTimeMillis() > 10 * 60_000L) {
                ReminderScheduler.schedule(
                    context         = ctx,
                    reminderId      = reminder.id,
                    title           = reminder.title,
                    triggerAtMillis = triggerAtMillis - 10 * 60_000L,
                    isEarly         = true,
                    frequencyType   = form.frequencyType.name
                )
            }
        }
        refresh()
    }

    fun deleteReminder(id: String) {
        ReminderScheduler.cancel(getApplication(), id)
        repository.deleteReminder(id)
        refresh()
    }

    fun toggleReminder(id: String, enabled: Boolean) {
        if (!enabled) {
            ReminderScheduler.cancel(getApplication(), id)
        } else {
            val reminder = repository.getReminders().find { it.id == id } ?: return
            val nextFire = nextFireMillis(reminder.triggerAtMillis, reminder.frequencyType)
            if (nextFire > 0L && appPrefs.notificationsEnabled) {
                ReminderScheduler.schedule(
                    context         = getApplication(),
                    reminderId      = id,
                    title           = reminder.title,
                    triggerAtMillis = nextFire,
                    isOneTime       = reminder.frequencyType == FrequencyType.ONCE,
                    frequencyType   = reminder.frequencyType.name
                )
                repository.updateTriggerTime(id, nextFire)
            }
        }
        repository.toggleReminder(id, enabled)
        refresh()
    }

    // Returns the next future fire time for a reminder.
    // ONCE reminders that have already passed return 0 (can't reschedule).
    private fun nextFireMillis(triggerAtMillis: Long, frequencyType: FrequencyType): Long {
        if (triggerAtMillis <= 0L) return 0L
        val now = System.currentTimeMillis()
        if (triggerAtMillis > now) return triggerAtMillis
        return when (frequencyType) {
            FrequencyType.ONCE   -> 0L
            FrequencyType.DAILY  -> {
                var t = triggerAtMillis
                while (t <= now) t += 24 * 3600 * 1_000L
                t
            }
            FrequencyType.WEEKLY -> {
                var t = triggerAtMillis
                while (t <= now) t += 7 * 24 * 3600 * 1_000L
                t
            }
        }
    }
}
