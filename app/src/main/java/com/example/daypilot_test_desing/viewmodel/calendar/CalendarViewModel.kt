package com.example.daypilot_test_desing.viewmodel.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.backend.local.NotificationHub
import com.example.daypilot_test_desing.backend.model.CalendarTaskData
import com.example.daypilot_test_desing.backend.model.NewTaskData
import com.example.daypilot_test_desing.backend.model.NotificationType
import com.example.daypilot_test_desing.backend.model.TaskCategory
import com.example.daypilot_test_desing.backend.model.TaskDifficulty
import com.example.daypilot_test_desing.backend.repository.ProgressRepository
import com.example.daypilot_test_desing.backend.repository.TaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalendarViewModel(
    private val taskRepo: TaskRepository,
    private val progressRepo: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState(isLoading = true))
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init { refresh() }

    private suspend fun load() {
        _uiState.update { it.copy(isLoading = true) }
        try {
            val tasks = taskRepo.getTasks()
            _uiState.update { it.copy(tasks = tasks, isLoading = false) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun refresh(): Job = viewModelScope.launch { load() }

    fun addTask(data: NewTaskData) {
        val fakeId = "pending_${System.currentTimeMillis()}"
        val placeholder = CalendarTaskData(
            id          = fakeId,
            day         = data.day,
            month       = data.month,
            year        = data.year,
            title       = data.title,
            category    = data.category,
            difficulty  = data.difficulty,
            duration    = data.duration,
            isDone      = false,
            description = data.description.ifBlank { null },
            isRecurring = data.isRecurring,
            hasReminder = data.hasReminder,
            isPending   = true
        )
        _uiState.update { it.copy(tasks = it.tasks + placeholder) }

        viewModelScope.launch {
            try {
                taskRepo.addTask(data)
                load() // replace placeholder with real server ID
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        tasks = state.tasks.filter { it.id != fakeId },
                        userMessage = R.string.error_task_create
                    )
                }
            }
        }
    }

    fun updateTask(id: String, title: String, category: TaskCategory, difficulty: TaskDifficulty, duration: Int, description: String = "") {
        val original = _uiState.value.tasks.find { it.id == id }
        _uiState.update { state ->
            state.copy(tasks = state.tasks.map { task ->
                if (task.id == id) task.copy(
                    title       = title,
                    category    = category,
                    difficulty  = difficulty,
                    duration    = duration,
                    description = description.ifBlank { null }
                ) else task
            })
        }
        viewModelScope.launch {
            try {
                taskRepo.updateTask(id, title, category, difficulty, duration, description)
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        tasks = if (original != null)
                            state.tasks.map { if (it.id == id) original else it }
                        else
                            state.tasks,
                        userMessage = R.string.error_task_update
                    )
                }
            }
        }
    }

    fun toggleTask(id: String, isDone: Boolean) {
        val taskTitle = _uiState.value.tasks.firstOrNull { it.id == id }?.title
        _uiState.update { state ->
            state.copy(tasks = state.tasks.map { if (it.id == id) it.copy(isDone = isDone) else it })
        }
        viewModelScope.launch {
            try {
                taskRepo.toggleTask(id, isDone)
                val points = if (isDone) 20 else -20
                progressRepo.logPoints(points, "TASKS")
                if (isDone && taskTitle != null) {
                    NotificationHub.add(
                        title   = "✓ $taskTitle",
                        message = "+20 puntos ganados",
                        type    = NotificationType.TASK
                    )
                }
                // No reload — the optimistic flip is the truth
            } catch (e: Exception) {
                // Revert the flip
                _uiState.update { state ->
                    state.copy(
                        tasks = state.tasks.map { if (it.id == id) it.copy(isDone = !isDone) else it },
                        userMessage = R.string.error_task_toggle
                    )
                }
            }
        }
    }

    fun deleteTask(id: String) {
        val snapshot = _uiState.value.tasks
        _uiState.update { state -> state.copy(tasks = state.tasks.filter { it.id != id }) }
        viewModelScope.launch {
            try {
                val task = snapshot.find { it.id == id }
                if (task?.isDone == true) progressRepo.logPoints(-20, "TASKS")
                taskRepo.deleteTask(id)
            } catch (e: Exception) {
                _uiState.update { it.copy(tasks = snapshot, userMessage = R.string.error_task_delete) }
            }
        }
    }

    fun editTask(id: String) {
        viewModelScope.launch { taskRepo.editTask(id) }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    companion object {
        fun factory(taskRepo: TaskRepository, progressRepo: ProgressRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CalendarViewModel(taskRepo, progressRepo) as T
            }
    }
}
