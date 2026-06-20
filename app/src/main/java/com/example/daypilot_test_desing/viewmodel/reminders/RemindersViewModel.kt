package com.example.daypilot_test_desing.viewmodel.reminders

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.daypilot_test_desing.data.model.ReminderFormDataInfo
import com.example.daypilot_test_desing.data.repository.fake.FakeReminderRepository
import com.example.daypilot_test_desing.data.repository.fake.FakeSettingsRepository
import com.example.daypilot_test_desing.reminders.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RemindersViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(RemindersUiState(
        reminders = FakeReminderRepository.getReminders()
    ))
    val uiState: StateFlow<RemindersUiState> = _uiState.asStateFlow()

    private fun refresh() {
        _uiState.value = RemindersUiState(reminders = FakeReminderRepository.getReminders())
    }

    fun addReminder(form: ReminderFormDataInfo) {
        val triggerAtMillis = when {
            form.quickMinutes != null      -> System.currentTimeMillis() + form.quickMinutes * 60_000L
            form.scheduledDateTime != null -> form.scheduledDateTime.timeInMillis
            else                           -> return
        }
        val enriched = form.copy(triggerAtMillis = triggerAtMillis)
        val reminder = FakeReminderRepository.addReminder(enriched)
        val ctx = getApplication<Application>()
        if (FakeSettingsRepository.getSettings().notificationsEnabled) {
            ReminderScheduler.schedule(ctx, reminder.id, reminder.title, triggerAtMillis)
            if (form.earlyWarning && triggerAtMillis - System.currentTimeMillis() > 10 * 60_000L) {
                ReminderScheduler.schedule(ctx, reminder.id, reminder.title, triggerAtMillis - 10 * 60_000L, isEarly = true)
            }
        }
        refresh()
    }

    fun deleteReminder(id: String) {
        ReminderScheduler.cancel(getApplication(), id)
        FakeReminderRepository.deleteReminder(id)
        refresh()
    }

    fun toggleReminder(id: String, enabled: Boolean) {
        if (!enabled) ReminderScheduler.cancel(getApplication(), id)
        FakeReminderRepository.toggleReminder(id, enabled)
        refresh()
    }
}
