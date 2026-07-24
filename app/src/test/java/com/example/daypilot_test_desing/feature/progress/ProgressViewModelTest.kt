package com.example.daypilot_test_desing.feature.progress

import androidx.test.core.app.ApplicationProvider
import com.example.daypilot_test_desing.core.data.repository.ProgressRepository
import com.example.daypilot_test_desing.data.supabase.dto.DailyProgressDto
import com.example.daypilot_test_desing.support.MainDispatcherRule
import com.example.daypilot_test_desing.support.initSupabaseSettingsForTest
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ProgressViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: ProgressRepository

    private val todayA = DailyProgressDto(userId = "u1", date = "2026-07-24", totalPoints = 40, tasksPoints = 20, stepsPoints = 20)
    private val todayB = DailyProgressDto(userId = "u1", date = "2026-07-24", totalPoints = 70, timerPoints = 30)

    @Before
    fun setUp() {
        initSupabaseSettingsForTest()
        repo = mockk()
        coEvery { repo.getTodayProgress() } returns todayA
        coEvery { repo.getHistory(30) } returns emptyList()
        coEvery { repo.getRankingPosition() } returns 3
    }

    private fun buildViewModel() = ProgressViewModel(ApplicationProvider.getApplicationContext(), repo)

    @Test
    fun `init loads today's progress, history and ranking`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(40, state.pointsToday)
        assertEquals(20, state.pointsFromTasks)
        assertEquals(20, state.pointsFromSteps)
        assertEquals(3, state.rankingPosition)
    }

    @Test
    fun `recordTimerComplete reloads when the server awarded points`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        coEvery { repo.completeTimerSession() } returns true
        coEvery { repo.getTodayProgress() } returns todayB

        viewModel.recordTimerComplete()
        advanceUntilIdle()

        assertEquals(70, viewModel.uiState.value.pointsToday)
        assertEquals(30, viewModel.uiState.value.pointsFromTimers)
    }

    @Test
    fun `recordTimerComplete does not reload when nothing was awarded`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        coEvery { repo.completeTimerSession() } returns false
        coEvery { repo.getTodayProgress() } returns todayB

        viewModel.recordTimerComplete()
        advanceUntilIdle()

        // Stale todayA value, proving the second getTodayProgress() stub was never reached.
        assertEquals(40, viewModel.uiState.value.pointsToday)
    }
}
