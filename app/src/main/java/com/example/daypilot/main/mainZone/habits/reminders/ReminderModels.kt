package com.example.daypilot.main.mainZone.habits.reminders

enum class RepeatType { ONCE, DAILY }
enum class AlarmKind { MAIN, PRE }

data class Reminder(
    val id: String,
    val title: String,
    val repeat: RepeatType,
    val enabled: Boolean = true,
    val triggerAtMillis: Long,

    val hour: Int? = null,
    val minute: Int? = null,

    val preAlertMin: Int = 0,
    val lastPreSentForTriggerAt: Long = 0L
)