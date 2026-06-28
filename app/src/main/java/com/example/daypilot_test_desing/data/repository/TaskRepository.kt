package com.example.daypilot_test_desing.data.repository

import com.example.daypilot_test_desing.data.model.CalendarTaskData
import com.example.daypilot_test_desing.data.model.NewTaskData
import com.example.daypilot_test_desing.data.model.TaskCategory
import com.example.daypilot_test_desing.data.model.TaskDifficulty

interface TaskRepository {
    suspend fun getTasks(): List<CalendarTaskData>
    suspend fun addTask(data: NewTaskData)
    suspend fun updateTask(id: String, title: String, category: TaskCategory, difficulty: TaskDifficulty, duration: Int)
    suspend fun toggleTask(id: String, isDone: Boolean)
    suspend fun deleteTask(id: String)
    suspend fun editTask(id: String)
}
