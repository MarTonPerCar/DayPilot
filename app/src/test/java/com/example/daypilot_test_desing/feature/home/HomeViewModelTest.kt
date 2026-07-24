package com.example.daypilot_test_desing.feature.home

import com.example.daypilot_test_desing.core.data.model.CalendarTaskData
import com.example.daypilot_test_desing.core.data.model.FriendData
import com.example.daypilot_test_desing.core.data.model.TaskCategory
import com.example.daypilot_test_desing.core.data.model.TaskDifficulty
import com.example.daypilot_test_desing.core.data.model.UserProfile
import com.example.daypilot_test_desing.core.data.repository.FriendRepository
import com.example.daypilot_test_desing.core.data.repository.ProgressRepository
import com.example.daypilot_test_desing.core.data.repository.StepsRepository
import com.example.daypilot_test_desing.core.data.repository.TaskRepository
import com.example.daypilot_test_desing.core.data.repository.UserRepository
import com.example.daypilot_test_desing.data.supabase.dto.DailyProgressDto
import com.example.daypilot_test_desing.support.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.daypilot_test_desing.support.realAdvanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var stepsRepo: StepsRepository
    private lateinit var progressRepo: ProgressRepository
    private lateinit var userRepo: UserRepository
    private lateinit var friendRepo: FriendRepository
    private lateinit var taskRepo: TaskRepository

    private val user = UserProfile(id = "u1", name = "Ana", username = "ana", email = "ana@daypilot.test", currentStreak = 4)

    private fun task(id: String, done: Boolean) = CalendarTaskData(
        id = id, day = 24, month = 7, year = 2026, title = "T", category = TaskCategory.PERSONAL,
        difficulty = TaskDifficulty.EASY, duration = 10, isDone = done
    )

    @Before
    fun setUp() {
        stepsRepo = mockk()
        progressRepo = mockk()
        userRepo = mockk()
        friendRepo = mockk()
        taskRepo = mockk()

        every { stepsRepo.getCurrentSteps() } returns 500
        every { stepsRepo.getGoalSteps() } returns 10_000
        coEvery { userRepo.getCurrentUser() } returns user
        coEvery { taskRepo.getTasks() } returns listOf(task("t1", true), task("t2", false))
        coEvery { progressRepo.getTodayProgress() } returns DailyProgressDto(userId = "u1", date = "2026-07-24", totalPoints = 30)
        coEvery { progressRepo.getHistory(30) } returns emptyList()
        coEvery { progressRepo.getRankingPosition() } returns 1
        coEvery { friendRepo.getFriends() } returns listOf(
            FriendData(id = "u2", name = "Carlos", email = "carlos@daypilot.test", points = 20, streak = 1)
        )
    }

    private fun buildViewModel() = HomeViewModel(stepsRepo, progressRepo, userRepo, friendRepo, taskRepo)

    @Test
    fun `init combines every repository's data into one ui state`() = runTest {
        val viewModel = buildViewModel()
        realAdvanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("Ana", state.userName)
        assertEquals(4, state.streak)
        assertEquals(500, state.stepsToday)
        assertEquals(30, state.pointsToday)
        assertEquals(1, state.rankingPosition)
        assertEquals(1, state.friendCount)
        assertEquals(1, state.tasksCompleted)
        assertEquals(2, state.tasksTotal)
    }

    @Test
    fun `awaitLoad returns false when any repository call fails`() = runTest {
        coEvery { progressRepo.getTodayProgress() } throws RuntimeException("network down")

        val viewModel = buildViewModel()
        val result = viewModel.awaitLoad()

        assertFalse(result)
    }

    @Test
    fun `duplicate task ids from the joined query are deduplicated before counting`() = runTest {
        coEvery { taskRepo.getTasks() } returns listOf(task("t1", true), task("t1", true), task("t2", false))

        val viewModel = buildViewModel()
        realAdvanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(2, state.tasksTotal)
        assertEquals(1, state.tasksCompleted)
    }
}
