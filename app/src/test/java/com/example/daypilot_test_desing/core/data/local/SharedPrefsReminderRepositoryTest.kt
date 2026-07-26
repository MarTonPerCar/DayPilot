package com.example.daypilot_test_desing.core.data.local

import com.example.daypilot_test_desing.core.data.model.FrequencyType
import com.example.daypilot_test_desing.core.data.model.ReminderFormDataInfo
import com.example.daypilot_test_desing.support.FakeContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

class SharedPrefsReminderRepositoryTest {

    private lateinit var context: FakeContext

    @Before
    fun setUp() {
        context = FakeContext()
    }

    private fun repo() = SharedPrefsReminderRepository(context)

    @Test
    fun `getReminders returns an empty list when nothing was stored`() {
        assertTrue(repo().getReminders().isEmpty())
    }

    @Test
    fun `addReminder with quick minutes stores a formatted time and persists across instances`() {
        val added = repo().addReminder(
            ReminderFormDataInfo(
                title = "Drink water", frequencyType = FrequencyType.ONCE,
                earlyWarning = false, quickMinutes = 30, scheduledDateTime = null,
                triggerAtMillis = 1000L
            )
        )

        assertEquals("30 min", added.time)
        assertTrue(added.isEnabled)

        val reloaded = repo().getReminders().single()
        assertEquals(added.id, reloaded.id)
        assertEquals("Drink water", reloaded.title)
    }

    @Test
    fun `addReminder with a scheduled time formats it as HH mm`() {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 5)
        }
        val added = repo().addReminder(
            ReminderFormDataInfo(
                title = "Meeting", frequencyType = FrequencyType.DAILY,
                earlyWarning = false, quickMinutes = null, scheduledDateTime = cal
            )
        )

        assertEquals("07:05", added.time)
    }

    @Test
    fun `addReminder falls back to 00 00 when neither quick minutes nor a schedule is given`() {
        val added = repo().addReminder(
            ReminderFormDataInfo(
                title = "Untimed", frequencyType = FrequencyType.ONCE,
                earlyWarning = false, quickMinutes = null, scheduledDateTime = null
            )
        )

        assertEquals("00:00", added.time)
    }

    @Test
    fun `deleteReminder removes only the matching reminder`() {
        val repository = repo()
        val kept = repository.addReminder(
            ReminderFormDataInfo(
                title = "Keep", frequencyType = FrequencyType.ONCE,
                earlyWarning = false, quickMinutes = 5, scheduledDateTime = null
            )
        )
        val removed = repository.addReminder(
            ReminderFormDataInfo(
                title = "Remove", frequencyType = FrequencyType.ONCE,
                earlyWarning = false, quickMinutes = 10, scheduledDateTime = null
            )
        )

        repository.deleteReminder(removed.id)

        assertEquals(listOf(kept.id), repository.getReminders().map { it.id })
    }

    @Test
    fun `toggleReminder flips isEnabled for the matching reminder only`() {
        val repository = repo()
        val a = repository.addReminder(
            ReminderFormDataInfo(
                title = "A", frequencyType = FrequencyType.ONCE,
                earlyWarning = false, quickMinutes = 5, scheduledDateTime = null
            )
        )
        val b = repository.addReminder(
            ReminderFormDataInfo(
                title = "B", frequencyType = FrequencyType.ONCE,
                earlyWarning = false, quickMinutes = 5, scheduledDateTime = null
            )
        )

        repository.toggleReminder(a.id, false)

        val reloaded = repository.getReminders().associateBy { it.id }
        assertEquals(false, reloaded.getValue(a.id).isEnabled)
        assertEquals(true, reloaded.getValue(b.id).isEnabled)
    }

    @Test
    fun `toggleReminder is a no-op for an unknown id`() {
        val repository = repo()
        repository.addReminder(
            ReminderFormDataInfo(
                title = "A", frequencyType = FrequencyType.ONCE,
                earlyWarning = false, quickMinutes = 5, scheduledDateTime = null
            )
        )

        repository.toggleReminder("does-not-exist", false)

        assertTrue(repository.getReminders().single().isEnabled)
    }

    @Test
    fun `updateTriggerTime overwrites the stored trigger time for the matching reminder`() {
        val repository = repo()
        val added = repository.addReminder(
            ReminderFormDataInfo(
                title = "A", frequencyType = FrequencyType.DAILY,
                earlyWarning = false, quickMinutes = 5, scheduledDateTime = null,
                triggerAtMillis = 1000L
            )
        )

        repository.updateTriggerTime(added.id, 9999L)

        assertEquals(9999L, repository.getReminders().single().triggerAtMillis)
    }

    @Test
    fun `updateTriggerTime is a no-op for an unknown id`() {
        val repository = repo()
        val added = repository.addReminder(
            ReminderFormDataInfo(
                title = "A", frequencyType = FrequencyType.ONCE,
                earlyWarning = false, quickMinutes = 5, scheduledDateTime = null,
                triggerAtMillis = 1000L
            )
        )

        repository.updateTriggerTime("does-not-exist", 9999L)

        assertEquals(1000L, repository.getReminders().single().triggerAtMillis)
    }

    @Test
    fun `getReminders discards a corrupted stored payload instead of throwing`() {
        context.getSharedPreferences("daypilot_reminders", 0).edit().putString("reminders", "not json").apply()

        assertTrue(repo().getReminders().isEmpty())
    }
}
