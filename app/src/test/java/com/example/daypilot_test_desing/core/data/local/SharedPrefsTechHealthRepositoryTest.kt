package com.example.daypilot_test_desing.core.data.local

import com.example.daypilot_test_desing.core.data.model.AppRestriction
import com.example.daypilot_test_desing.core.data.model.GroupRestriction
import com.example.daypilot_test_desing.support.FakeContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SharedPrefsTechHealthRepositoryTest {

    private lateinit var context: FakeContext

    @Before
    fun setUp() {
        context = FakeContext()
    }

    private fun repo() = SharedPrefsTechHealthRepository(context)

    private fun app(
        id: String = "com.instagram.android",
        limit: Int = 30,
        enabled: Boolean = true,
        pendingActive: Boolean? = null,
        pendingLimit: Int? = null,
        violated: Boolean = false,
        used: Int = 0,
        pendingDelete: Boolean = false
    ) = AppRestriction(
        id = id, appName = "Instagram", packageName = id, dailyLimitMinutes = limit, isEnabled = enabled,
        usedMinutesToday = used, pendingActive = pendingActive, pendingLimitMinutes = pendingLimit,
        isViolatedToday = violated, pendingDelete = pendingDelete
    )

    private fun group(
        id: String = "g1",
        apps: List<AppRestriction> = emptyList(),
        enabled: Boolean = true,
        pendingActive: Boolean? = null,
        pendingLimit: Int? = null,
        violated: Boolean = false,
        used: Int = 0,
        pendingDelete: Boolean = false
    ) = GroupRestriction(
        id = id, groupName = "Social", apps = apps, dailyLimitMinutes = 60, isEnabled = enabled,
        usedMinutesToday = used, pendingActive = pendingActive, pendingLimitMinutes = pendingLimit,
        isViolatedToday = violated, pendingDelete = pendingDelete
    )

    @Test
    fun `getAppRestrictions and getGroupRestrictions start empty`() {
        val repository = repo()
        assertTrue(repository.getAppRestrictions().isEmpty())
        assertTrue(repository.getGroupRestrictions().isEmpty())
    }

    @Test
    fun `saveApp inserts a new restriction and persists across instances`() {
        repo().saveApp(app())
        assertEquals(listOf("com.instagram.android"), repo().getAppRestrictions().map { it.id })
    }

    @Test
    fun `saveApp overwrites an existing restriction with the same id`() {
        val repository = repo()
        repository.saveApp(app(limit = 30))
        repository.saveApp(app(limit = 45))

        val stored = repository.getAppRestrictions().single()
        assertEquals(45, stored.dailyLimitMinutes)
    }

    @Test
    fun `saveGroup inserts and overwrites the same way as saveApp`() {
        val repository = repo()
        repository.saveGroup(group())
        repository.saveGroup(group().copy(groupName = "Renamed"))

        val stored = repository.getGroupRestrictions().single()
        assertEquals("Renamed", stored.groupName)
    }

    @Test
    fun `toggleRestriction sets pendingActive only when it differs from the current state`() {
        val repository = repo()
        repository.saveApp(app(enabled = true))

        repository.toggleRestriction("com.instagram.android", false)
        assertEquals(false, repository.getAppRestrictions().single().pendingActive)

        repository.toggleRestriction("com.instagram.android", true)
        assertNull(repository.getAppRestrictions().single().pendingActive)
    }

    @Test
    fun `toggleRestriction is a no-op for an unknown id`() {
        val repository = repo()
        repository.saveApp(app())

        repository.toggleRestriction("does-not-exist", false)

        assertNull(repository.getAppRestrictions().single().pendingActive)
    }

    @Test
    fun `toggleGroup behaves the same as toggleRestriction`() {
        val repository = repo()
        repository.saveGroup(group(enabled = true))

        repository.toggleGroup("g1", false)

        assertEquals(false, repository.getGroupRestrictions().single().pendingActive)
    }

    @Test
    fun `deleteRestriction and deleteGroup soft-delete by flagging pendingDelete`() {
        val repository = repo()
        repository.saveApp(app())
        repository.saveGroup(group())

        repository.deleteRestriction("com.instagram.android")
        repository.deleteGroup("g1")

        assertTrue(repository.getAppRestrictions().single().pendingDelete)
        assertTrue(repository.getGroupRestrictions().single().pendingDelete)
    }

    @Test
    fun `updateUsage and updateGroupUsage overwrite usedMinutesToday for the matching id`() {
        val repository = repo()
        repository.saveApp(app())
        repository.saveGroup(group())

        repository.updateUsage("com.instagram.android", 12)
        repository.updateGroupUsage("g1", 34)

        assertEquals(12, repository.getAppRestrictions().single().usedMinutesToday)
        assertEquals(34, repository.getGroupRestrictions().single().usedMinutesToday)
    }

    @Test
    fun `updateGroupAppUsage updates only the matching app within the matching group`() {
        val repository = repo()
        repository.saveGroup(group(apps = listOf(app(id = "a1"), app(id = "a2"))))

        repository.updateGroupAppUsage("g1", "a1", 7)

        val apps = repository.getGroupRestrictions().single().apps.associateBy { it.packageName }
        assertEquals(7, apps.getValue("a1").usedMinutesToday)
        assertEquals(0, apps.getValue("a2").usedMinutesToday)
    }

    @Test
    fun `updateGroupAppUsage is a no-op for an unknown group or app`() {
        val repository = repo()
        repository.saveGroup(group(apps = listOf(app(id = "a1"))))

        repository.updateGroupAppUsage("does-not-exist", "a1", 7)
        repository.updateGroupAppUsage("g1", "does-not-exist", 7)

        assertEquals(0, repository.getGroupRestrictions().single().apps.single().usedMinutesToday)
    }

    @Test
    fun `markViolated and markGroupViolated set the flag only once`() {
        val repository = repo()
        repository.saveApp(app())
        repository.saveGroup(group())

        repository.markViolated("com.instagram.android")
        repository.markGroupViolated("g1")

        assertTrue(repository.getAppRestrictions().single().isViolatedToday)
        assertTrue(repository.getGroupRestrictions().single().isViolatedToday)
    }

    @Test
    fun `markViolated is a no-op when already violated`() {
        val repository = repo()
        repository.saveApp(app(violated = true))

        repository.markViolated("com.instagram.android")

        assertTrue(repository.getAppRestrictions().single().isViolatedToday)
    }

    @Test
    fun `replaceGroupId renames the matching group's id`() {
        val repository = repo()
        repository.saveGroup(group(id = "temp-id"))

        repository.replaceGroupId("temp-id", "real-id")

        assertEquals(listOf("real-id"), repository.getGroupRestrictions().map { it.id })
    }

    @Test
    fun `replaceGroupId is a no-op when old and new ids match`() {
        val repository = repo()
        repository.saveGroup(group(id = "g1"))

        repository.replaceGroupId("g1", "g1")

        assertEquals(listOf("g1"), repository.getGroupRestrictions().map { it.id })
    }

    @Test
    fun `clearAll removes both stored lists`() {
        val repository = repo()
        repository.saveApp(app())
        repository.saveGroup(group())

        repository.clearAll()

        assertTrue(repo().getAppRestrictions().isEmpty())
        assertTrue(repo().getGroupRestrictions().isEmpty())
    }

    @Test
    fun `applyPendingChangesIfNewDay is a no-op the same day it last ran`() {
        val repository = repo()
        repository.saveApp(app(enabled = true, pendingActive = false))
        // First call ever (no last_reset_date yet) always resolves — this is the baseline.
        repository.applyPendingChangesIfNewDay()
        assertEquals(false, repository.getAppRestrictions().single().isEnabled)

        // A fresh pending change made afterward, same day: must survive a second call untouched.
        repository.toggleRestriction("com.instagram.android", true)
        repository.applyPendingChangesIfNewDay()

        val stored = repository.getAppRestrictions().single()
        assertEquals(false, stored.isEnabled)
        assertEquals(true, stored.pendingActive)
    }

    @Test
    fun `applyPendingChangesIfNewDay resolves pending state on a new day`() {
        val repository = repo()
        repository.saveApp(
            app(enabled = true, pendingActive = false, pendingLimit = 45, violated = true, used = 20)
        )
        repository.saveGroup(
            group(
                apps = listOf(app(id = "a1", used = 5)),
                enabled = true, pendingActive = false, pendingLimit = 90, violated = true, used = 15
            )
        )
        // Force the "new day" branch: no last_reset_date has ever been recorded.
        repository.applyPendingChangesIfNewDay()

        val storedApp = repository.getAppRestrictions().single()
        assertEquals(false, storedApp.isEnabled)
        assertNull(storedApp.pendingActive)
        assertEquals(45, storedApp.dailyLimitMinutes)
        assertNull(storedApp.pendingLimitMinutes)
        assertEquals(false, storedApp.isViolatedToday)
        assertEquals(0, storedApp.usedMinutesToday)

        val storedGroup = repository.getGroupRestrictions().single()
        assertEquals(false, storedGroup.isEnabled)
        assertEquals(90, storedGroup.dailyLimitMinutes)
        assertEquals(false, storedGroup.isViolatedToday)
        assertEquals(0, storedGroup.usedMinutesToday)
        assertEquals(0, storedGroup.apps.single().usedMinutesToday)
    }

    @Test
    fun `applyPendingChangesIfNewDay drops restrictions flagged pendingDelete`() {
        val repository = repo()
        repository.saveApp(app(pendingDelete = true))
        repository.saveGroup(group(pendingDelete = true))

        repository.applyPendingChangesIfNewDay()

        assertTrue(repository.getAppRestrictions().isEmpty())
        assertTrue(repository.getGroupRestrictions().isEmpty())
    }

    @Test
    fun `a corrupted stored payload is discarded instead of thrown`() {
        context.getSharedPreferences("daypilot_tech_health", 0).edit()
            .putString("apps", "not json")
            .putString("groups", "not json")
            .apply()

        val repository = repo()
        assertTrue(repository.getAppRestrictions().isEmpty())
        assertTrue(repository.getGroupRestrictions().isEmpty())
    }
}
