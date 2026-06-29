package com.example.daypilot_test_desing.backend.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.daypilot_test_desing.R

enum class TaskDifficulty(@StringRes val labelRes: Int, val color: Color) {
    EASY(R.string.difficulty_easy, Color(0xFF4CAF50)),
    MEDIUM(R.string.difficulty_medium, Color(0xFFFF9800)),
    HARD(R.string.difficulty_hard, Color(0xFFF44336))
}

enum class TaskCategory(@StringRes val labelRes: Int, val icon: ImageVector, val color: Color) {
    WORK(R.string.category_work, Icons.Default.Work, Color(0xFF2196F3)),
    STUDY(R.string.category_study, Icons.Default.School, Color(0xFF9C27B0)),
    SPORT(R.string.category_sport, Icons.Default.FitnessCenter, Color(0xFF4CAF50)),
    HEALTH(R.string.category_health, Icons.Default.Favorite, Color(0xFFE91E63)),
    PERSONAL(R.string.category_personal, Icons.Default.Person, Color(0xFFFF9800)),
    HOME(R.string.category_home, Icons.Default.Home, Color(0xFFFFB300)),
    OTHER(R.string.category_other, Icons.Default.Star, Color(0xFF607D8B))
}

data class CalendarTaskData(
    val id: String,
    val day: Int,
    val month: Int,
    val year: Int,
    val title: String,
    val category: TaskCategory,
    val difficulty: TaskDifficulty,
    val duration: Int,
    val isDone: Boolean,
    val description: String? = null,
    val isRecurring: Boolean = false,
    val hasReminder: Boolean = false
)

data class NewTaskData(
    val day: Int,
    val month: Int,
    val year: Int,
    val title: String,
    val category: TaskCategory,
    val difficulty: TaskDifficulty,
    val duration: Int,
    val description: String = "",
    val isRecurring: Boolean = false,
    val hasReminder: Boolean = false,
    val recurrenceDays: Int = 1
)

data class CalendarTaskDot(
    val day: Int,
    val month: Int,
    val year: Int,
    val color: androidx.compose.ui.graphics.Color
)

enum class Month(val nameRes: Int) {
    JANUARY(R.string.month_january),
    FEBRUARY(R.string.month_february),
    MARCH(R.string.month_march),
    APRIL(R.string.month_april),
    MAY(R.string.month_may),
    JUNE(R.string.month_june),
    JULY(R.string.month_july),
    AUGUST(R.string.month_august),
    SEPTEMBER(R.string.month_september),
    OCTOBER(R.string.month_october),
    NOVEMBER(R.string.month_november),
    DECEMBER(R.string.month_december)
}

enum class WeekDay(val headerRes: Int) {
    MONDAY(R.string.day_mon),
    TUESDAY(R.string.day_tue),
    WEDNESDAY(R.string.day_wed),
    THURSDAY(R.string.day_thu),
    FRIDAY(R.string.day_fri),
    SATURDAY(R.string.day_sat),
    SUNDAY(R.string.day_sun)
}
