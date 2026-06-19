package com.example.daypilot_test_desing.presentation.reminders

import androidx.lifecycle.ViewModel
import com.example.daypilot_test_desing.data.model.ReminderFormDataInfo
import com.example.daypilot_test_desing.data.repository.fake.FakeReminderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RemindersViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(RemindersUiState(
        reminders = FakeReminderRepository.getReminders()
    ))
    val uiState: StateFlow<RemindersUiState> = _uiState.asStateFlow()

    private fun refresh() {
        _uiState.value = RemindersUiState(reminders = FakeReminderRepository.getReminders())
    }

    fun addReminder(form: ReminderFormDataInfo) {
        FakeReminderRepository.addReminder(form)
        refresh()
    }

    fun deleteReminder(id: String) {
        FakeReminderRepository.deleteReminder(id)
        refresh()
    }

    fun toggleReminder(id: String, enabled: Boolean) {
        FakeReminderRepository.toggleReminder(id, enabled)
        refresh()
    }
}
