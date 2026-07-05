package com.example.daypilot_test_desing.feature.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.data.local.NotificationHub
import com.example.daypilot_test_desing.core.data.model.CalendarTaskData
import com.example.daypilot_test_desing.core.data.model.NewTaskData
import com.example.daypilot_test_desing.core.data.model.NotificationType
import com.example.daypilot_test_desing.core.data.model.TaskCategory
import com.example.daypilot_test_desing.core.data.model.TaskDifficulty
import com.example.daypilot_test_desing.core.data.repository.ProgressRepository
import com.example.daypilot_test_desing.core.data.repository.TaskRepository
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
            val tasks = taskRepo.getTasks()  // cache-first
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
                taskRepo.addTask(data)          // invalidates SessionCache.tasks
                load()                           // re-fetches from Supabase, repopulates cache
                SessionCache.tasks.value = _uiState.value.tasks
            } catch (e: Exception) {
                Log.e(TAG, "Failed to create task '${data.title}' (recurring=${data.isRecurring})", e)
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
                SessionCache.tasks.value = _uiState.value.tasks
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update task $id", e)
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

    fun toggleTask(occurrenceId: String, isDone: Boolean) {
        val original = _uiState.value.tasks.firstOrNull { it.occurrenceId == occurrenceId } ?: return
        // Points are only ever paid once per occurrence, tracked by isEarned (sticky,
        // unlike isDone) — unchecking never takes points back, rechecking never re-pays.
        val shouldAwardPoints = isDone && !original.isEarned
        _uiState.update { state ->
            state.copy(tasks = state.tasks.map {
                if (it.occurrenceId == occurrenceId)
                    it.copy(isDone = isDone, isEarned = it.isEarned || shouldAwardPoints)
                else it
            })
        }
        viewModelScope.launch {
            try {
                taskRepo.toggleTask(occurrenceId, isDone)
                if (shouldAwardPoints) {
                    progressRepo.logPoints(20, "TASKS")
                    NotificationHub.add(
                        title   = "✓ ${original.title}",
                        message = "+20 puntos ganados",
                        type    = NotificationType.TASK
                    )
                }
                SessionCache.tasks.value = _uiState.value.tasks
            } catch (e: Exception) {
                Log.e(TAG, "Failed to toggle task occurrence $occurrenceId to isDone=$isDone", e)
                _uiState.update { state ->
                    state.copy(
                        tasks = state.tasks.map { if (it.occurrenceId == occurrenceId) original else it },
                        userMessage = R.string.error_task_toggle
                    )
                }
            }
        }
    }

    fun deleteTask(id: String) {
        val snapshot = _uiState.value.tasks
        // Points already earned for completed occurrences stay earned — deleting a
        // task never takes points back.
        _uiState.update { state -> state.copy(tasks = state.tasks.filter { it.id != id }) }
        viewModelScope.launch {
            try {
                taskRepo.deleteTask(id)
                SessionCache.tasks.value = _uiState.value.tasks
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete task $id", e)
                _uiState.update { it.copy(tasks = snapshot, userMessage = R.string.error_task_delete) }
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    companion object {
        private const val TAG = "CalendarViewModel"

        fun factory(taskRepo: TaskRepository, progressRepo: ProgressRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CalendarViewModel(taskRepo, progressRepo) as T
            }
    }
}
