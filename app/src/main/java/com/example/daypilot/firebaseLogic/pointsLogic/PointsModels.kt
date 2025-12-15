package com.example.daypilot.firebaseLogic.pointsLogic

import com.example.daypilot.firebaseLogic.authLogic.PointSource

data class PointsDaily(
    val date: String = "",
    val zoneId: String = "",
    val total: Long = 0L,
    val tasks: Long = 0L,
    val steps: Long = 0L,
    val wellness: Long = 0L
)

data class RollingTotals(
    val total: Long,
    val tasks: Long,
    val steps: Long,
    val wellness: Long
)

internal fun sourceField(source: PointSource): String =
    when (source) {
        PointSource.TASKS -> "tasks"
        PointSource.STEPS -> "steps"
        PointSource.WELLNESS -> "wellness"
    }