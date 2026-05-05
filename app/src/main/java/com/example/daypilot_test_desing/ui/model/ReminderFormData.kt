package com.example.daypilot_test_desing.ui.model

import java.util.Calendar

data class ReminderFormData(
    val title: String,
    val frequencyType: FrequencyType,
    val earlyWarning: Boolean,
    val quickMinutes: Int?,
    val scheduledDateTime: Calendar?
)
