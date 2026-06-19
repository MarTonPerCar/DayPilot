package com.example.daypilot_test_desing.presentation.calendar

import androidx.lifecycle.ViewModel
import com.example.daypilot_test_desing.data.model.NewTaskData
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

    fun toggleTask(id: String, isDone: Boolean) {
        FakeTaskRepository.toggleTask(id, isDone)
        refresh()
    }

    fun deleteTask(id: String) {
        FakeTaskRepository.deleteTask(id)
        refresh()
    }

    fun editTask(id: String) {
        FakeTaskRepository.editTask(id)
        refresh()
    }
}
