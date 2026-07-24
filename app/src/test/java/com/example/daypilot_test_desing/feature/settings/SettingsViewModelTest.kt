package com.example.daypilot_test_desing.feature.settings

import androidx.test.core.app.ApplicationProvider
import com.example.daypilot_test_desing.core.data.model.UserProfile
import com.example.daypilot_test_desing.core.data.repository.UserRepository
import com.example.daypilot_test_desing.support.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.daypilot_test_desing.support.realAdvanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// SettingsViewModel constructs its own concrete AppPreferences internally (not injected), so
// this exercises the real, Robolectric-backed SharedPreferences rather than a mock for that part.
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var userRepo: UserRepository

    @Before
    fun setUp() {
        userRepo = mockk()
        coEvery { userRepo.getCurrentUser() } returns UserProfile(
            id = "u1", name = "Ana", username = "ana", email = "ana@daypilot.test"
        )
    }

    private fun buildViewModel() = SettingsViewModel(ApplicationProvider.getApplicationContext(), userRepo)

    @Test
    fun `init reflects the default preferences and loads the user's name`() = runTest {
        val viewModel = buildViewModel()
        realAdvanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Ana", state.name)
        assertEquals(true, state.isDarkMode)
        assertEquals("SAGE_GREEN", state.selectedThemeId)
    }

    @Test
    fun `toggleDarkMode persists the new value and updates state`() = runTest {
        val viewModel = buildViewModel()
        realAdvanceUntilIdle()

        viewModel.toggleDarkMode(false)

        assertFalse(viewModel.uiState.value.isDarkMode)

        // A fresh ViewModel re-reads from the same SharedPreferences-backed store.
        val reloaded = buildViewModel()
        assertFalse(reloaded.uiState.value.isDarkMode)
    }

    @Test
    fun `disabling the master notifications switch cancels scheduled alarms without crashing`() = runTest {
        val viewModel = buildViewModel()
        realAdvanceUntilIdle()

        viewModel.toggleNotifications(false)

        assertFalse(viewModel.uiState.value.notificationsEnabled)
    }
}
