package com.example.daypilot_test_desing.ui.model

data class DayProgress(
    val day: Int,
    val points: Int,
    val steps: Int,
    val tasksCompleted: Int
)

enum class ProgressFilter(val label: String) {
    POINTS("Puntos"),
    STEPS("Pasos"),
    TASKS("Tareas")
}