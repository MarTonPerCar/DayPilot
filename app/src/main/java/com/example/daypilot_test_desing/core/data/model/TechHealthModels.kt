package com.example.daypilot_test_desing.core.data.model

import kotlinx.serialization.Serializable

// pendingActive/pendingLimitMinutes apply next day only, so they can't dodge an in-progress violation.
@Serializable
data class AppRestriction(
    val id: String,
    val appName: String,
    val packageName: String,
    val dailyLimitMinutes: Int,
    val isEnabled: Boolean,
    val usedMinutesToday: Int = 0,
    val pendingActive: Boolean? = null,
    val pendingLimitMinutes: Int? = null,
    val isViolatedToday: Boolean = false,
    val pendingDelete: Boolean = false
)

@Serializable
data class GroupRestriction(
    val id: String,
    val groupName: String,
    val apps: List<AppRestriction>,
    val dailyLimitMinutes: Int,
    val isEnabled: Boolean,
    val usedMinutesToday: Int = 0,
    val pendingActive: Boolean? = null,
    val pendingLimitMinutes: Int? = null,
    val isViolatedToday: Boolean = false,
    val pendingDelete: Boolean = false
)
