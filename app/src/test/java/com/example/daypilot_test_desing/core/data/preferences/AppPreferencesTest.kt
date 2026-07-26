package com.example.daypilot_test_desing.core.data.preferences

import com.example.daypilot_test_desing.support.FakeContext
import org.junit.Assert.assertEquals
import org.junit.Test

class AppPreferencesTest {

    @Test
    fun `defaults match a fresh install`() {
        val prefs = AppPreferences(FakeContext())

        assertEquals(true, prefs.isDarkMode)
        assertEquals("SAGE_GREEN", prefs.themeId)
        assertEquals("es", prefs.language)
        assertEquals(true, prefs.notificationsEnabled)
        assertEquals("", prefs.lastOpenDate)
        assertEquals(true, prefs.taskRemindersEnabled)
        assertEquals(true, prefs.streakAlertsEnabled)
    }

    @Test
    fun `each property persists across instances backed by the same context`() {
        val context = FakeContext()
        AppPreferences(context).apply {
            isDarkMode = false
            themeId = "OCEAN_BLUE"
            language = "en"
            notificationsEnabled = false
            lastOpenDate = "2026-07-26"
            taskRemindersEnabled = false
            streakAlertsEnabled = false
        }

        val reloaded = AppPreferences(context)
        assertEquals(false, reloaded.isDarkMode)
        assertEquals("OCEAN_BLUE", reloaded.themeId)
        assertEquals("en", reloaded.language)
        assertEquals(false, reloaded.notificationsEnabled)
        assertEquals("2026-07-26", reloaded.lastOpenDate)
        assertEquals(false, reloaded.taskRemindersEnabled)
        assertEquals(false, reloaded.streakAlertsEnabled)
    }
}
