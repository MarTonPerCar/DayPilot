// com.example.daypilot.main.mainZone.habits.steps.StepsLocalStore.kt
package com.example.daypilot.mainDatabase

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.stepsDataStore by preferencesDataStore(name = "steps_prefs")

data class StepsLocalState(
    val dayKey: String = "",
    val baselineCounter: Long = 0L,
    val goalToday: Int = 2000,
    val goalNextDay: Int = 2000,
    val m50Sent: Boolean = false,
    val m75Sent: Boolean = false,
    val m100Sent: Boolean = false
)

class StepsLocalStore(private val context: Context) {

    private object Keys {
        val DAY_KEY = stringPreferencesKey("day_key")
        val BASELINE = longPreferencesKey("baseline_counter")
        val GOAL_TODAY = intPreferencesKey("goal_today")
        val GOAL_NEXT = intPreferencesKey("goal_next_day")
        val M50 = booleanPreferencesKey("m50_sent")
        val M75 = booleanPreferencesKey("m75_sent")
        val M100 = booleanPreferencesKey("m100_sent")
    }

    val flow: Flow<StepsLocalState> = context.stepsDataStore.data.map { p ->
        StepsLocalState(
            dayKey = p[Keys.DAY_KEY] ?: "",
            baselineCounter = p[Keys.BASELINE] ?: 0L,
            goalToday = p[Keys.GOAL_TODAY] ?: 2000,
            goalNextDay = p[Keys.GOAL_NEXT] ?: (p[Keys.GOAL_TODAY] ?: 2000),
            m50Sent = p[Keys.M50] ?: false,
            m75Sent = p[Keys.M75] ?: false,
            m100Sent = p[Keys.M100] ?: false
        )
    }

    suspend fun setDay(dayKey: String, baseline: Long, goalToday: Int, goalNext: Int) {
        context.stepsDataStore.edit { p ->
            p[Keys.DAY_KEY] = dayKey
            p[Keys.BASELINE] = baseline
            p[Keys.GOAL_TODAY] = goalToday
            p[Keys.GOAL_NEXT] = goalNext
            p[Keys.M50] = false
            p[Keys.M75] = false
            p[Keys.M100] = false
        }
    }

    suspend fun setGoalNextDay(value: Int) {
        context.stepsDataStore.edit { p -> p[Keys.GOAL_NEXT] = value }
    }

    suspend fun markMilestone50() = context.stepsDataStore.edit { it[Keys.M50] = true }
    suspend fun markMilestone75() = context.stepsDataStore.edit { it[Keys.M75] = true }
    suspend fun markMilestone100() = context.stepsDataStore.edit { it[Keys.M100] = true }
}