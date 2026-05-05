package com.example.daypilot_test_desing.ui.model

import androidx.annotation.StringRes
import com.example.daypilot_test_desing.R

data class ReminderData(
    val id: String,
    val title: String,
    val time: String,
    val description: String = "",
    val isEnabled: Boolean = true
)

enum class FrequencyType(@StringRes val labelRes: Int) {
    ONCE(R.string.reminders_freq_once),
    DAILY(R.string.reminders_freq_daily),
    WEEKLY(R.string.reminders_freq_weekly)
}
