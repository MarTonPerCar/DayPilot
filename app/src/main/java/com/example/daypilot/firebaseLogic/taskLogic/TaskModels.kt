package com.example.daypilot.firebaseLogic.taskLogic

enum class TaskDifficulty {
    EASY,
    MEDIUM,
    HARD
}

data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val difficulty: TaskDifficulty = TaskDifficulty.MEDIUM,
    val estimatedMinutes: Int = 30,
    val days: List<String> = emptyList(),
    val category: String = "General",
    val reminderEnabled: Boolean = false,

    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)