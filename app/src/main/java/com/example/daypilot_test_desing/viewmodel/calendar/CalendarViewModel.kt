package com.example.daypilot_test_desing.viewmodel.calendar

import androidx.lifecycle.ViewModel
import com.example.daypilot_test_desing.data.model.NewTaskData
import com.example.daypilot_test_desing.data.model.TaskCategory
import com.example.daypilot_test_desing.data.model.TaskDifficulty
import com.example.daypilot_test_desing.data.repository.fake.FakeProgressRepository

import com.example.daypilot_test_desing.data.repository.fake.FakeTaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CalendarViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(CalendarUiState(tasks = FakeTaskRepository.getTasks()))
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    private fun refresh() {
        _uiState.value = CalendarUiState(tasks = FakeTaskRepository.getTasks())
    }

    fun addTask(data: NewTaskData) {
        FakeTaskRepository.addTask(data)
        refresh()
    }

    fun updateTask(id: String, title: String, category: TaskCategory, difficulty: TaskDifficulty, duration: Int) {
        FakeTaskRepository.updateTask(id, title, category, difficulty, duration)
        refresh()
    }

    fun toggleTask(id: String, isDone: Boolean) {
        val task = FakeTaskRepository.getTasks().find { it.id == id }
        FakeTaskRepository.toggleTask(id, isDone)
        if (task != null) {
            if (isDone) FakeProgressRepository.addTaskPoints(20)
            else        FakeProgressRepository.removeTaskPoints(20)
        }
        refresh()
    }

    fun deleteTask(id: String) {
        val task = FakeTaskRepository.getTasks().find { it.id == id }
        if (task?.isDone == true) {
            FakeProgressRepository.removeTaskPoints(20)
        }
        FakeTaskRepository.deleteTask(id)
        refresh()
    }

    fun editTask(id: String) {
        FakeTaskRepository.editTask(id)
        refresh()
    }
}
