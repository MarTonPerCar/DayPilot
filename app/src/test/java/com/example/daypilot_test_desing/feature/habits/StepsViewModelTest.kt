package com.example.daypilot_test_desing.feature.habits

import androidx.test.core.app.ApplicationProvider
import com.example.daypilot_test_desing.core.data.repository.StepsRepository
import com.example.daypilot_test_desing.core.data.repository.StepsWeeklyStats
import com.example.daypilot_test_desing.support.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.daypilot_test_desing.support.realAdvanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// StepsForegroundService now owns the actual sensor listener; StepsViewModel is purely a
// display + on-demand-sync layer reading persisted repo state (no more sensor registration or
// infinite periodic-sync loop in init{}), so unlike its earlier shape this needs no sensor
// simulation and no manual scheduler workaround at all.
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class StepsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repo: StepsRepository

    @Before
    fun setUp() {
        repo = mockk()
        every { repo.getCurrentSteps() } returns 1000
        every { repo.getGoalSteps() } returns 10_000
        every { repo.getPendingGoal() } returns null
        every { repo.canChangeGoal() } returns true
        coEvery { repo.hydrateGoalFromServer() } returns Unit
        coEvery { repo.getPointsEarned() } returns 20
        coEvery { repo.syncSteps(any(), any()) } returns Unit
        coEvery { repo.getWeeklyStats() } returns StepsWeeklyStats(
            totalSteps7Days = 7000, bestDaySteps = 2000, dailyAverage = 1000, goalStreak = 3
        )
    }

    private fun buildViewModel() = StepsViewModel(ApplicationProvider.getApplicationContext(), repo)

    @Test
    fun `init hydrates the goal from the server and loads points and weekly stats`() = runTest {
        val viewModel = buildViewModel()
        realAdvanceUntilIdle()

        coVerify { repo.hydrateGoalFromServer() }
        val state = viewModel.uiState.value
        assertEquals(1000, state.currentSteps)
        assertEquals(10_000, state.goalSteps)
        assertEquals(20, state.pointsEarned)
        assertEquals(7000, state.totalSteps7Days)
        assertEquals(2000, state.bestDaySteps)
    }

    @Test
    fun `refresh triggers a sync with the current steps and goal`() = runTest {
        val viewModel = buildViewModel()
        realAdvanceUntilIdle()

        viewModel.refresh()
        realAdvanceUntilIdle()

        coVerify { repo.syncSteps(1000, 10_000) }
    }

    @Test
    fun `configureGoal saves the new goal and updates the displayed state`() = runTest {
        val viewModel = buildViewModel()
        realAdvanceUntilIdle()

        every { repo.configureGoal(12_000) } returns Unit
        every { repo.getGoalSteps() } returns 12_000

        viewModel.configureGoal(12_000)

        verify { repo.configureGoal(12_000) }
        assertEquals(12_000, viewModel.uiState.value.goalSteps)
    }
}
