package com.example.daypilot_test_desing.core.data.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.daypilot_test_desing.R

// ── Podium ────────────────────────────────────────────────────────

data class PodiumEntry(
    val name: String,
    val points: Int,
    val streak: Int,
    val avatarUrl: String? = null,
    val isCurrentUser: Boolean = false
)

// ── Weekly reactions ──────────────────────────────────────────────

data class ReceivedReaction(
    val fromName: String,
    val reaction: ReactionType,
    val avatarUrl: String? = null
)

data class WeeklySummaryData(
    val totalPoints: Int,
    val tasksCompleted: Int,
    val totalSteps: Int,
    val bestStreak: Int,
    val reactions: List<ReceivedReaction> = emptyList()
)

// ── Home sections ─────────────────────────────────────────────────

enum class HomeSection(
    @StringRes val titleRes: Int,
    val icon: ImageVector,
    val accentColor: Color
) {
    CALENDAR(
        titleRes = R.string.section_calendar,
        icon = Icons.Default.CalendarMonth,
        accentColor = Color(0xFF4A7C59)
    ),
    PROGRESS(
        titleRes = R.string.section_progress,
        icon = Icons.Default.BarChart,
        accentColor = Color(0xFF1A6B8A)
    ),
    HABITS(
        titleRes = R.string.section_habits,
        icon = Icons.Default.FitnessCenter,
        accentColor = Color(0xFF6B4FA8)
    ),
    RIVALRY(
        titleRes = R.string.section_rivalry,
        icon = Icons.Default.EmojiEvents,
        accentColor = Color(0xFFB85C00)
    )
}

sealed class HomeSectionData {
    data class Calendar(
        val pendingTasks: Int,
        val completedTasks: Int
    ) : HomeSectionData()

    data class Progress(
        val data: List<DayProgress>,
        val currentFilter: ProgressFilter = ProgressFilter.POINTS
    ) : HomeSectionData()

    data class Habits(
        val stepsProgress: Float,
        val timerDone: Boolean
    ) : HomeSectionData()

    data class Rivalry(
        val position: Int,
        val totalFriends: Int
    ) : HomeSectionData()
}
