package com.example.daypilot.main.mainZone.habits.steps

import android.content.Context
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.stepsStore by preferencesDataStore("steps_prefs")

class StepsLocalStore(private val context: Context) {

    private object Keys {
        val GOAL_STEPS = intPreferencesKey("goal_steps")
    }

    val goalFlow: Flow<Int> =
        context.stepsStore.data.map { prefs -> prefs[Keys.GOAL_STEPS] ?: 8000 }

    suspend fun setGoalSteps(goal: Int) {
        context.stepsStore.edit { p ->
            p[Keys.GOAL_STEPS] = goal.coerceIn(1000, 50000)
        }
    }
}