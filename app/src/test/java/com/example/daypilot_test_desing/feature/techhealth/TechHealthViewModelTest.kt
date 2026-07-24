package com.example.daypilot_test_desing.feature.techhealth

import androidx.test.core.app.ApplicationProvider
import com.example.daypilot_test_desing.core.data.model.AppRestriction
import com.example.daypilot_test_desing.support.MainDispatcherRule
import com.example.daypilot_test_desing.support.initSupabaseSettingsForTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// TechHealthViewModel constructs its own concrete SharedPrefsTechHealthRepository internally
// (not injected) — this exercises the real, Robolectric-backed store. Its Supabase sync calls
// all gate on supabase.auth.currentUserOrNull(), which is null with no persisted session, so
// they short-circuit before ever reaching the network, the same as the other ViewModels here.
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class TechHealthViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val restriction = AppRestriction(
        id = "com.instagram.android", appName = "Instagram", packageName = "com.instagram.android",
        dailyLimitMinutes = 30, isEnabled = true
    )

    @Before
    fun setUp() {
        initSupabaseSettingsForTest()
    }

    private fun buildViewModel() = TechHealthViewModel(ApplicationProvider.getApplicationContext())

    @Test
    fun `saveApp adds a new restriction to state`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        viewModel.saveApp(restriction)
        advanceUntilIdle()

        assertEquals(listOf(restriction), viewModel.uiState.value.appRestrictions)
    }

    @Test
    fun `toggleRestriction defers the change to pendingActive rather than flipping isEnabled now`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.saveApp(restriction)
        advanceUntilIdle()

        viewModel.toggleRestriction(restriction.id, false)
        advanceUntilIdle()

        val stored = viewModel.uiState.value.appRestrictions.single()
        assertEquals(false, stored.pendingActive)
        assertTrue(stored.isEnabled) // unchanged until the next day's rollover
    }

    @Test
    fun `deleteRestriction soft-deletes by flagging pendingDelete, not removing it`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()
        viewModel.saveApp(restriction)
        advanceUntilIdle()

        viewModel.deleteRestriction(restriction.id)
        advanceUntilIdle()

        val stored = viewModel.uiState.value.appRestrictions.single()
        assertTrue(stored.pendingDelete)
    }
}
