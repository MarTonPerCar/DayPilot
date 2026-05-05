package com.example.daypilot_test_desing.ui.model

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.daypilot_test_desing.R

enum class TimerMode(
    @StringRes val labelRes: Int,
    val durationMinutes: Int
) {
    POMODORO(R.string.timer_pomodoro, 25),
    TRAINING(R.string.timer_training, 90),
    MEDITATION(R.string.timer_meditation, 60),
    COOKING(R.string.timer_cooking, 120),
    CUSTOM(R.string.timer_custom, 30)
}

data class TimerOption(
    val id: String,
    @StringRes val labelRes: Int,
    @StringRes val descriptionRes: Int,
    val icon: ImageVector,
    val accentColor: Color,
    val durationMinutes: Int = 0,
    val isCustom: Boolean = false,
    val isPomodoro: Boolean = false
)
