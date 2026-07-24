package com.example.daypilot_test_desing.feature.reminders

import androidx.test.core.app.ApplicationProvider
import com.example.daypilot_test_desing.core.data.model.FrequencyType
import com.example.daypilot_test_desing.core.data.model.ReminderFormDataInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// Unlike the other feature ViewModels, RemindersViewModel constructs its own concrete
// SharedPrefsReminderRepository/AppPreferences internally instead of taking an injected
// interface — so this exercises the real, Robolectric-backed SharedPreferences rather than a
// mock. That's still a fair unit test: no network, no real device, deterministic.
@RunWith(RobolectricTestRunner::class)
class RemindersViewModelTest {

    private fun buildViewModel() = RemindersViewModel(ApplicationProvider.getApplicationContext())

    @Test
    fun `addReminder with a quick-timer duration adds it to state as enabled`() {
        val viewModel = buildViewModel()

        viewModel.addReminder(
            ReminderFormDataInfo(
                title = "Drink water",
                frequencyType = FrequencyType.ONCE,
                earlyWarning = false,
                quickMinutes = 30,
                scheduledDateTime = null
            )
        )

        val reminder = viewModel.uiState.value.reminders.single()
        assertEquals("Drink water", reminder.title)
        assertTrue(reminder.isEnabled)
    }

    @Test
    fun `deleteReminder removes it from state`() {
        val viewModel = buildViewModel()
        viewModel.addReminder(
            ReminderFormDataInfo(
                title = "Stretch", frequencyType = FrequencyType.ONCE,
                earlyWarning = false, quickMinutes = 15, scheduledDateTime = null
            )
        )
        val id = viewModel.uiState.value.reminders.single().id

        viewModel.deleteReminder(id)

        assertTrue(viewModel.uiState.value.reminders.isEmpty())
    }

    @Test
    fun `toggleReminder flips isEnabled and persists it`() {
        val viewModel = buildViewModel()
        viewModel.addReminder(
            ReminderFormDataInfo(
                title = "Walk", frequencyType = FrequencyType.ONCE,
                earlyWarning = false, quickMinutes = 20, scheduledDateTime = null
            )
        )
        val id = viewModel.uiState.value.reminders.single().id

        viewModel.toggleReminder(id, false)

        assertFalse(viewModel.uiState.value.reminders.single().isEnabled)

        // A fresh ViewModel re-reads from the same SharedPreferences-backed store.
        val reloaded = buildViewModel()
        assertFalse(reloaded.uiState.value.reminders.single().isEnabled)
    }
}
