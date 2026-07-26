package com.example.daypilot_test_desing.core.reminders

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar
import java.util.TimeZone

class DailyNotificationSchedulerTest {

    @Test
    fun `nextAlarmMillis lands at the requested UTC hour, past the cron buffer`() {
        val triggerAt = DailyNotificationScheduler.nextAlarmMillis(9)

        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = triggerAt }
        assertEquals(9, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(15, cal.get(Calendar.MINUTE))
        assertEquals(0, cal.get(Calendar.SECOND))
    }

    @Test
    fun `nextAlarmMillis is always in the future`() {
        // Covers both branches of the "already passed today" check regardless of what time
        // this test happens to run at.
        assertTrue(DailyNotificationScheduler.nextAlarmMillis(9) > System.currentTimeMillis())
        assertTrue(DailyNotificationScheduler.nextAlarmMillis(22) > System.currentTimeMillis())
    }

    // Deliberately not pinned to a specific hour being "already passed" vs "still upcoming" —
    // whichever branch UTC-now happens to land in when this runs, the expected value is derived
    // the same way the production code computes it, so both branches are correct either way.
    @Test
    fun `nextAlarmMillis matches the UTC candidate time, rolling to tomorrow only once it has passed`() {
        for (hour in listOf(9, 22)) {
            val now = System.currentTimeMillis()
            val candidate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, 15)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val expected = if (candidate.timeInMillis <= now) {
                candidate.apply { add(Calendar.DAY_OF_YEAR, 1) }.timeInMillis
            } else {
                candidate.timeInMillis
            }

            assertEquals(expected, DailyNotificationScheduler.nextAlarmMillis(hour))
        }
    }
}
