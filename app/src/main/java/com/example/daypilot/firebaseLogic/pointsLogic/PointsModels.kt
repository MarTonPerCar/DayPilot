package com.example.daypilot.firebaseLogic.pointsLogic

import com.example.daypilot.firebaseLogic.authLogic.PointSource

internal fun sourceField(source: PointSource): String =
    when (source) {
        PointSource.TASKS -> "tasks"
        PointSource.STEPS -> "steps"
        PointSource.WELLNESS -> "wellness"
    }