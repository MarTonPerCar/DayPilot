package com.example.daypilot_test_desing.data.repository

import com.example.daypilot_test_desing.data.model.CalendarTaskData
import com.example.daypilot_test_desing.data.model.NewTaskData
import com.example.daypilot_test_desing.data.model.TaskCategory
import com.example.daypilot_test_desing.data.model.TaskDifficulty

interface TaskRepository {
    fun getTasks(): List<CalendarTaskData>
    fun addTask(data: NewTaskData)
    fun updateTask(id: String, title: String, category: TaskCategory, difficulty: TaskDifficulty, duration: Int)
    fun toggleTask(id: String, isDone: Boolean)
    fun deleteTask(id: String)
    fun editTask(id: String)
}
