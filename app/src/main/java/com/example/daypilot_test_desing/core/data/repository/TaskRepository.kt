package com.example.daypilot_test_desing.core.data.repository

import com.example.daypilot_test_desing.core.data.model.CalendarTaskData
import com.example.daypilot_test_desing.core.data.model.NewTaskData
import com.example.daypilot_test_desing.core.data.model.TaskCategory
import com.example.daypilot_test_desing.core.data.model.TaskDifficulty

interface TaskRepository {
    suspend fun getTasks(): List<CalendarTaskData>
    suspend fun addTask(data: NewTaskData)
    suspend fun updateTask(id: String, title: String, category: TaskCategory, difficulty: TaskDifficulty, duration: Int, description: String = "")
    suspend fun toggleTask(occurrenceId: String, isDone: Boolean)
    suspend fun deleteTask(id: String)
}
