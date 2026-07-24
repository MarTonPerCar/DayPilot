package com.example.daypilot_test_desing.feature.calendar

import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.data.model.CalendarTaskData
import com.example.daypilot_test_desing.core.data.model.NewTaskData
import com.example.daypilot_test_desing.core.data.model.TaskCategory
import com.example.daypilot_test_desing.core.data.model.TaskDifficulty
import com.example.daypilot_test_desing.core.data.repository.ProgressRepository
import com.example.daypilot_test_desing.core.data.repository.TaskRepository
import com.example.daypilot_test_desing.support.MainDispatcherRule
import com.example.daypilot_test_desing.support.initSupabaseSettingsForTest
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

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CalendarViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var taskRepo: TaskRepository
    private lateinit var progressRepo: ProgressRepository

    private val existingTask = CalendarTaskData(
        id = "t1", day = 24, month = 7, year = 2026,
        title = "Existing task", category = TaskCategory.PERSONAL,
        difficulty = TaskDifficulty.EASY, duration = 30, isDone = false
    )

    private val newTaskData = NewTaskData(
        day = 24, month = 7, year = 2026,
        title = "New task", category = TaskCategory.PERSONAL,
        difficulty = TaskDifficulty.EASY, duration = 15
    )

    @Before
    fun setUp() {
        initSupabaseSettingsForTest()
        taskRepo = mockk()
        progressRepo = mockk()
        coEvery { taskRepo.getTasks() } returns emptyList()
    }

    private fun buildViewModel() = CalendarViewModel(taskRepo, progressRepo)

    @Test
    fun `init loads tasks from the repository`() = runTest {
        coEvery { taskRepo.getTasks() } returns listOf(existingTask)

        val viewModel = buildViewModel()
        realAdvanceUntilIdle()

        assertEquals(listOf(existingTask), viewModel.uiState.value.tasks)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `addTask failure removes the placeholder and sets the create error`() = runTest {
        val viewModel = buildViewModel()
        realAdvanceUntilIdle()

        coEvery { taskRepo.addTask(newTaskData) } throws RuntimeException("insert failed")

        viewModel.addTask(newTaskData)
        realAdvanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(emptyList<CalendarTaskData>(), state.tasks)
        assertEquals(R.string.error_task_create, state.userMessage)
    }

    @Test
    fun `toggleTask failure rolls back the optimistic done and earned flags`() = runTest {
        coEvery { taskRepo.getTasks() } returns listOf(existingTask)
        val viewModel = buildViewModel()
        realAdvanceUntilIdle()

        coEvery { taskRepo.toggleTask("t1", true) } throws RuntimeException("toggle failed")

        viewModel.toggleTask("t1", true)
        realAdvanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.tasks.single().isDone)
        assertFalse(state.tasks.single().isEarned)
        assertEquals(R.string.error_task_toggle, state.userMessage)
    }
}
