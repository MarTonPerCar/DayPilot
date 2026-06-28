package com.example.daypilot_test_desing.viewmodel.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.backend.model.NewTaskData
import com.example.daypilot_test_desing.backend.model.TaskCategory
import com.example.daypilot_test_desing.backend.model.TaskDifficulty
import com.example.daypilot_test_desing.backend.repository.TaskRepository
import com.example.daypilot_test_desing.backend.fake.FakeProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CalendarViewModel(
    private val repository: TaskRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState(isLoading = true))
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val tasks = repository.getTasks()
                _uiState.update { it.copy(tasks = tasks, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun refresh() { load() }

    fun addTask(data: NewTaskData) {
        viewModelScope.launch {
            try {
                repository.addTask(data)
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun updateTask(id: String, title: String, category: TaskCategory, difficulty: TaskDifficulty, duration: Int) {
        viewModelScope.launch {
            try {
                repository.updateTask(id, title, category, difficulty, duration)
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun toggleTask(id: String, isDone: Boolean) {
        // Optimistic update — reflect the change immediately in the UI
        _uiState.update { state ->
            state.copy(tasks = state.tasks.map { if (it.id == id) it.copy(isDone = isDone) else it })
        }
        viewModelScope.launch {
            try {
                repository.toggleTask(id, isDone)
                if (isDone) FakeProgressRepository.addTaskPoints(20)
                else        FakeProgressRepository.removeTaskPoints(20)
                load()
            } catch (e: Exception) {
                // Revert the optimistic update and show the real state
                load()
            }
        }
    }

    fun deleteTask(id: String) {
        viewModelScope.launch {
            try {
                val task = _uiState.value.tasks.find { it.id == id }
                if (task?.isDone == true) FakeProgressRepository.removeTaskPoints(20)
                repository.deleteTask(id)
                load()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun editTask(id: String) {
        viewModelScope.launch { repository.editTask(id) }
    }

    companion object {
        fun factory(repository: TaskRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    CalendarViewModel(repository) as T
            }
    }
}
