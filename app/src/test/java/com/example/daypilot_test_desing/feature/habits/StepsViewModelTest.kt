package com.example.daypilot_test_desing.feature.habits

import androidx.test.core.app.ApplicationProvider
import com.example.daypilot_test_desing.core.data.repository.StepsRepository
import com.example.daypilot_test_desing.core.data.repository.StepsWeeklyStats
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

// StepsViewModel starts an infinite `while (true) { delay(5 min); ... }` periodic-sync loop in
// init{}. runTest {}'s auto-drain-on-completion tries to advanceUntilIdle() the dispatcher even
// past the test body, which spins forever (OOM) against a coroutine that reschedules itself
// forever — confirmed empirically. Driving the scheduler directly, outside runTest {}, avoids
// that auto-drain entirely: only the time span this test explicitly asks for gets run.
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class StepsViewModelTest {

    private val scheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(scheduler)

    private lateinit var repo: StepsRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = mockk()
        every { repo.getCurrentSteps() } returns 1000
        every { repo.getGoalSteps() } returns 10_000
        every { repo.getPendingGoal() } returns null
        every { repo.canChangeGoal() } returns true
        every { repo.setSteps(any()) } returns Unit
        coEvery { repo.hydrateGoalFromServer() } returns Unit
        coEvery { repo.getPointsEarned() } returns 20
        coEvery { repo.syncSteps(any(), any()) } returns Unit
        coEvery { repo.getWeeklyStats() } returns StepsWeeklyStats(
            totalSteps7Days = 7000, bestDaySteps = 2000, dailyAverage = 1000, goalStreak = 3
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun buildViewModel() = StepsViewModel(ApplicationProvider.getApplicationContext(), repo)

    @Test
    fun `init hydrates the goal from the server and loads points and weekly stats`() {
        val viewModel = buildViewModel()
        scheduler.advanceTimeBy(100)
        scheduler.runCurrent()

        coVerify { repo.hydrateGoalFromServer() }
        val state = viewModel.uiState.value
        assertEquals(1000, state.currentSteps)
        assertEquals(10_000, state.goalSteps)
        assertEquals(20, state.pointsEarned)
        assertEquals(7000, state.totalSteps7Days)
        assertEquals(2000, state.bestDaySteps)
    }

    @Test
    fun `refresh triggers a sync with the current steps and goal`() {
        val viewModel = buildViewModel()
        scheduler.advanceTimeBy(100)
        scheduler.runCurrent()

        viewModel.refresh()
        scheduler.advanceTimeBy(100)
        scheduler.runCurrent()

        coVerify { repo.syncSteps(1000, 10_000) }
    }

    @Test
    fun `configureGoal saves the new goal and updates the displayed state`() {
        val viewModel = buildViewModel()
        scheduler.advanceTimeBy(100)
        scheduler.runCurrent()

        every { repo.configureGoal(12_000) } returns Unit
        every { repo.getGoalSteps() } returns 12_000

        viewModel.configureGoal(12_000)

        verify { repo.configureGoal(12_000) }
        assertEquals(12_000, viewModel.uiState.value.goalSteps)
    }
}
