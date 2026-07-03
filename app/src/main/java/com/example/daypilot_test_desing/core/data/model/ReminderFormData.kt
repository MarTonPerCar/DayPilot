package com.example.daypilot_test_desing.core.data.model

import java.util.Calendar

data class ReminderFormDataInfo(
    val title: String,
    val frequencyType: FrequencyType,
    val earlyWarning: Boolean,
    val quickMinutes: Int?,
    val scheduledDateTime: Calendar?,
    val triggerAtMillis: Long = 0L
)