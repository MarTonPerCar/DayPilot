package com.example.daypilot_test_desing.core.reminders

import com.example.daypilot_test_desing.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotificationBodyCodecTest {

    @Test
    fun `titleForType maps known db types and falls back to null`() {
        assertEquals(R.string.notif_task_reminder_title, NotificationBodyCodec.titleForType("TASK_REMINDER"))
        assertEquals(R.string.notif_streak_danger_title, NotificationBodyCodec.titleForType("STREAK_RISK"))
        assertNull(NotificationBodyCodec.titleForType("SOMETHING_ELSE"))
    }

    @Test
    fun `titleForPlaceholder maps known placeholders and falls back to null`() {
        assertEquals(R.string.notif_task_reminder_title, NotificationBodyCodec.titleForPlaceholder("TASK_REMINDER_TITLE"))
        assertEquals(R.string.notif_streak_danger_title, NotificationBodyCodec.titleForPlaceholder("STREAK_RISK_TITLE"))
        assertNull(NotificationBodyCodec.titleForPlaceholder("SOMETHING_ELSE"))
    }

    @Test
    fun `decodeBody maps the no-tasks placeholder to plain text`() {
        val body = NotificationBodyCodec.decodeBody("TASK_REMINDER_NONE")
        assertEquals(NotificationBody.PlainText(R.string.notif_task_reminder_none), body)
    }

    @Test
    fun `decodeBody maps the count placeholder to a plurals resource with its quantity`() {
        val body = NotificationBodyCodec.decodeBody("TASK_REMINDER_COUNT:3")
        assertEquals(NotificationBody.Count(R.plurals.notif_task_reminder_count, 3), body)
    }

    @Test
    fun `decodeBody defaults the count to 0 when the suffix isn't a number`() {
        val body = NotificationBodyCodec.decodeBody("TASK_REMINDER_COUNT:not-a-number")
        assertEquals(NotificationBody.Count(R.plurals.notif_task_reminder_count, 0), body)
    }

    @Test
    fun `decodeBody maps the streak risk body to plain text`() {
        val body = NotificationBodyCodec.decodeBody("STREAK_RISK_BODY")
        assertEquals(NotificationBody.PlainText(R.string.notif_streak_danger_body), body)
    }

    @Test
    fun `decodeBody returns null for an unrecognized body`() {
        assertNull(NotificationBodyCodec.decodeBody("SOMETHING_ELSE"))
    }
}
