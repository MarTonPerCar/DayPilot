package com.example.daypilot_test_desing.ui.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import com.example.daypilot_test_desing.R

enum class TaskDifficulty(@StringRes val labelRes: Int, val color: Color) {
    EASY(R.string.difficulty_easy,     Color(0xFF4CAF50)),
    MEDIUM(R.string.difficulty_medium, Color(0xFFFF9800)),
    HARD(R.string.difficulty_hard,     Color(0xFFF44336))
}

enum class TaskCategory(@StringRes val labelRes: Int, val icon: ImageVector, val color: Color) {
    WORK(R.string.category_work,         Icons.Default.Work,          Color(0xFF2196F3)),
    STUDY(R.string.category_study,       Icons.Default.School,        Color(0xFF9C27B0)),
    SPORT(R.string.category_sport,       Icons.Default.FitnessCenter, Color(0xFF4CAF50)),
    HEALTH(R.string.category_health,     Icons.Default.Favorite,      Color(0xFFE91E63)),
    PERSONAL(R.string.category_personal, Icons.Default.Person,        Color(0xFFFF9800)),
    HOME(R.string.category_home,         Icons.Default.Home,          Color(0xFFFFB300)),
    OTHER(R.string.category_other,       Icons.Default.Star,          Color(0xFF607D8B))
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
