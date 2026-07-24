package com.example.daypilot_test_desing.feature.habits

import com.example.daypilot_test_desing.core.data.repository.StepsRepository
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
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HabitsViewModelTest {

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
        coEvery { repo.syncSteps(any(), any()) } returns Unit
        coEvery { repo.getPointsEarned() } returns 10
    }

    private fun buildViewModel() = HabitsViewModel(repo)

    @Test
    fun `init reflects the repository's local state synchronously`() {
        val viewModel = buildViewModel()

        val state = viewModel.uiState.value
        assertEquals(1000, state.currentSteps)
        assertEquals(10_000, state.goalSteps)
        assertEquals(null, state.pendingGoal)
        assertFalse(state.goalChangedToday)
    }

    @Test
    fun `refresh syncs steps then updates points earned and remaining`() = runTest {
        val viewModel = buildViewModel()
        realAdvanceUntilIdle()

        viewModel.refresh()
        realAdvanceUntilIdle()

        coVerify { repo.syncSteps(1000, 10_000) }
        val state = viewModel.uiState.value
        assertEquals(10, state.pointsEarned)
        assertEquals(50, state.pointsRemaining)
    }

    @Test
    fun `configureGoal saves the new goal and refreshes local state`() {
        val viewModel = buildViewModel()
        every { repo.configureGoal(12_000) } returns Unit
        every { repo.getGoalSteps() } returns 12_000
        every { repo.canChangeGoal() } returns false

        viewModel.configureGoal(12_000)

        verify { repo.configureGoal(12_000) }
        val state = viewModel.uiState.value
        assertEquals(12_000, state.goalSteps)
        assertEquals(true, state.goalChangedToday)
    }
}
