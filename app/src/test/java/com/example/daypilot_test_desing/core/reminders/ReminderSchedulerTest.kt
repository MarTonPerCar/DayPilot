package com.example.daypilot_test_desing.core.reminders

import com.example.daypilot_test_desing.core.data.model.FrequencyType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ReminderSchedulerTest {

    @Test
    fun `nextFireMillis returns 0 for an unset trigger time`() {
        assertEquals(0L, ReminderScheduler.nextFireMillis(0L, FrequencyType.DAILY))
        assertEquals(0L, ReminderScheduler.nextFireMillis(-1L, FrequencyType.DAILY))
    }

    @Test
    fun `nextFireMillis returns the trigger time unchanged when it's still in the future`() {
        val future = System.currentTimeMillis() + 3_600_000L
        assertEquals(future, ReminderScheduler.nextFireMillis(future, FrequencyType.ONCE))
        assertEquals(future, ReminderScheduler.nextFireMillis(future, FrequencyType.DAILY))
        assertEquals(future, ReminderScheduler.nextFireMillis(future, FrequencyType.WEEKLY))
    }

    @Test
    fun `nextFireMillis returns 0 for a passed ONCE reminder`() {
        val past = System.currentTimeMillis() - 3_600_000L
        assertEquals(0L, ReminderScheduler.nextFireMillis(past, FrequencyType.ONCE))
    }

    @Test
    fun `nextFireMillis rolls a passed DAILY reminder forward by whole days`() {
        val dayMs = 24 * 3600 * 1_000L
        val past = System.currentTimeMillis() - (dayMs + 3_600_000L) // one day and one hour ago
        val next = ReminderScheduler.nextFireMillis(past, FrequencyType.DAILY)

        assertTrue(next > System.currentTimeMillis())
        assertEquals(0L, (next - past) % dayMs)
    }

    @Test
    fun `nextFireMillis rolls a passed WEEKLY reminder forward by whole weeks`() {
        val weekMs = 7 * 24 * 3600 * 1_000L
        val past = System.currentTimeMillis() - (weekMs + 3_600_000L) // one week and one hour ago
        val next = ReminderScheduler.nextFireMillis(past, FrequencyType.WEEKLY)

        assertTrue(next > System.currentTimeMillis())
        assertEquals(0L, (next - past) % weekMs)
    }
}
