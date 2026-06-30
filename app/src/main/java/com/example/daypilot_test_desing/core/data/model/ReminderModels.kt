package com.example.daypilot_test_desing.core.data.model

import androidx.annotation.StringRes
import com.example.daypilot_test_desing.R

@kotlinx.serialization.Serializable
data class ReminderData(
    val id: String,
    val title: String,
    val time: String,
    val triggerAtMillis: Long = 0L,
    val description: String = "",
    val isEnabled: Boolean = true,
    val frequencyType: FrequencyType = FrequencyType.ONCE
)

enum class FrequencyType(@StringRes val labelRes: Int) {
    ONCE(R.string.reminders_freq_once),
    DAILY(R.string.reminders_freq_daily),
    WEEKLY(R.string.reminders_freq_weekly)
}

enum class TimeZoneRegion(val value: String) {
    EUROPE_MADRID(      "Europe/Madrid"),
    ATLANTIC_CANARY(    "Atlantic/Canary"),
    AMERICA_NEW_YORK(   "America/New_York"),
    AMERICA_LOS_ANGELES("America/Los_Angeles"),
    AMERICA_MEXICO(     "America/Mexico_City"),
    AMERICA_SAO_PAULO(  "America/Sao_Paulo"),
    ASIA_TOKYO(         "Asia/Tokyo"),
    ASIA_SHANGHAI(      "Asia/Shanghai"),
    AUSTRALIA_SYDNEY(   "Australia/Sydney")
}
