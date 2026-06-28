package com.example.daypilot_test_desing.backend.repository

import com.example.daypilot_test_desing.backend.model.CalendarTaskData
import com.example.daypilot_test_desing.backend.model.NewTaskData
import com.example.daypilot_test_desing.backend.model.TaskCategory
import com.example.daypilot_test_desing.backend.model.TaskDifficulty

interface TaskRepository {
    suspend fun getTasks(): List<CalendarTaskData>
    suspend fun addTask(data: NewTaskData)
    suspend fun updateTask(id: String, title: String, category: TaskCategory, difficulty: TaskDifficulty, duration: Int)
    suspend fun toggleTask(id: String, isDone: Boolean)
    suspend fun deleteTask(id: String)
    suspend fun editTask(id: String)
}
