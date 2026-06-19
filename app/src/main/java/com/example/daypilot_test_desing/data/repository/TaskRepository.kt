package com.example.daypilot_test_desing.data.repository

import com.example.daypilot_test_desing.data.model.CalendarTaskData
import com.example.daypilot_test_desing.data.model.NewTaskData

interface TaskRepository {
    fun getTasks(): List<CalendarTaskData>
    fun addTask(data: NewTaskData)
    fun toggleTask(id: String, isDone: Boolean)
    fun deleteTask(id: String)
    fun editTask(id: String)
}
