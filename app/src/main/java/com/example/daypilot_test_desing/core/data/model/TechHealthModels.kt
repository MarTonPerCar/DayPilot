package com.example.daypilot_test_desing.backend.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class AppRestriction(
    val id: String,
    val appName: String,
    val packageName: String,
    val dailyLimitMinutes: Int,
    val notificationIntervalSeconds: Int,
    val isEnabled: Boolean,
    val usedMinutesToday: Int = 0,
    @Transient val pendingDelete: Boolean = false
)

@Serializable
data class GroupRestriction(
    val id: String,
    val groupName: String,
    val apps: List<AppRestriction>,
    val dailyLimitMinutes: Int,
    val notificationIntervalSeconds: Int,
    val isEnabled: Boolean,
    val usedMinutesToday: Int = 0,
    @Transient val pendingDelete: Boolean = false
)
