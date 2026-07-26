package com.example.daypilot_test_desing.feature.techhealth

import android.app.AppOpsManager
import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.daypilot_test_desing.core.data.model.AppRestriction
import com.example.daypilot_test_desing.support.FakeApplication
import com.example.daypilot_test_desing.support.MainDispatcherRule
import com.example.daypilot_test_desing.support.initSupabaseSettingsForTest
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

// TechHealthViewModel constructs its own concrete SharedPrefsTechHealthRepository internally
// (not injected) — this exercises the real, Robolectric-backed store. Its Supabase sync calls
// all gate on supabase.auth.currentUserOrNull(), which is null with no persisted session, so
// they short-circuit before ever reaching the network, the same as the other ViewModels here.
@OptIn(ExperimentalCoroutinesApi::class)
class TechHealthViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

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

    // AppUsageTracker.hasPermission() force-casts the APP_OPS_SERVICE result, so it needs a
    // real-shaped (if inert) AppOpsManager rather than the null every other service gets.
    private val appOpsManager = mockk<AppOpsManager> {
        every { unsafeCheckOpNoThrow(any(), any(), any()) } returns AppOpsManager.MODE_IGNORED
        every { checkOpNoThrow(any(), any(), any()) } returns AppOpsManager.MODE_IGNORED
    }

    private fun buildViewModel() =
        TechHealthViewModel(FakeApplication(systemServices = mapOf(Context.APP_OPS_SERVICE to appOpsManager)))

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
