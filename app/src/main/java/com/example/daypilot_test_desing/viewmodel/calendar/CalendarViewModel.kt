package com.example.daypilot_test_desing.viewmodel.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.backend.model.NewTaskData
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
        _uiState.update { it.copy(isLoading = true, error = null) }
        try {
            val tasks = taskRepo.getTasks()
            _uiState.update { it.copy(tasks = tasks, isLoading = false) }
        } catch (e: Exception) {
            _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    fun refresh(): Job = viewModelScope.launch { load() }

    fun addTask(data: NewTaskData) {
        viewModelScope.launch {
            try {
                taskRepo.addTask(data)
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateTask(id: String, title: String, category: TaskCategory, difficulty: TaskDifficulty, duration: Int) {
        viewModelScope.launch {
            try {
                taskRepo.updateTask(id, title, category, difficulty, duration)
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun toggleTask(id: String, isDone: Boolean) {
        _uiState.update { state ->
            state.copy(tasks = state.tasks.map { if (it.id == id) it.copy(isDone = isDone) else it })
        }
        viewModelScope.launch {
            try {
                taskRepo.toggleTask(id, isDone)
                val points = if (isDone) 20 else -20
                progressRepo.logPoints(points, "TASKS")
                load()
            } catch (e: Exception) {
                load()
            }
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            try {
                val task = _uiState.value.tasks.find { it.id == id }
                if (task?.isDone == true) progressRepo.logPoints(-20, "TASKS")
                taskRepo.deleteTask(id)
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun editTask(id: String) {
        viewModelScope.launch { taskRepo.editTask(id) }
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
