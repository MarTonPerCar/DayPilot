package com.example.daypilot_test_desing.ui.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*

enum class TaskDifficulty(val label: String, val color: Color) {
    EASY(  "Fácil",   Color(0xFF4CAF50)),
    MEDIUM("Media",   Color(0xFFFF9800)),
    HARD(  "Difícil", Color(0xFFF44336))
}

enum class TaskCategory(val label: String, val icon: ImageVector, val color: Color) {
    WORK(    "Trabajo",  Icons.Default.Work,        Color(0xFF2196F3)),
    STUDY(   "Estudio",  Icons.Default.School,      Color(0xFF9C27B0)),
    SPORT(   "Deporte",  Icons.Default.FitnessCenter, Color(0xFF4CAF50)),
    HEALTH(  "Salud",    Icons.Default.Favorite,    Color(0xFFE91E63)),
    PERSONAL("Personal", Icons.Default.Person,      Color(0xFFFF9800)),
    HOME(    "Hogar",    Icons.Default.Home,        Color(0xFFFFB300)),
    OTHER(   "Otro",     Icons.Default.Star,        Color(0xFF607D8B))
}

data class CalendarTaskData(
    val id: String,
    val day: Int,
    val title: String,
    val category: TaskCategory,
    val difficulty: TaskDifficulty,
    val duration: Int,
    val isDone: Boolean
)

data class RankingData(
    val id: String,
    val name: String,
    val points: Int,
    val streak: Int,
    val avatarUrl: String? = null
)

data class ReminderData(
    val id: String,
    val title: String,
    val time: String,
    val description: String = "",
    val isEnabled: Boolean = true
)