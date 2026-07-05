package com.example.daypilot_test_desing.data.supabase

import android.content.SharedPreferences
import android.util.Log
import com.example.daypilot_test_desing.core.data.local.NotificationHub
import com.example.daypilot_test_desing.core.data.model.NotificationType
import com.example.daypilot_test_desing.data.supabase.SupabaseNotificationRepository
import com.example.daypilot_test_desing.core.data.repository.StepsRepository
import com.example.daypilot_test_desing.core.data.repository.StepsWeeklyStats
import com.example.daypilot_test_desing.data.supabase.dto.DailyLogDto
import com.example.daypilot_test_desing.data.supabase.dto.HabitsDailyUpsertDto
import com.example.daypilot_test_desing.data.supabase.dto.InsertPointsLogDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Shares the "daypilot_steps" SharedPreferences file with StepsViewModel — this
// class owns steps_goal/pending_goal/goal_change_date, the ViewModel owns
// baseline_date/baseline_steps. Don't reuse a key across the two.
class SupabaseStepsRepository(private val prefs: SharedPreferences) : StepsRepository {

    companion object {
        private const val TAG = "SupabaseStepsRepository"
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var currentSteps = 0
    private var milestone1Awarded = false
    private var milestone2Awarded = false
    private var milestone3Awarded = false

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())

    private fun applyPendingGoalIfNewDay() {
        val pendingGoal = prefs.getInt("pending_goal", -1)
        val pendingDate = prefs.getString("goal_change_date", "") ?: ""
        if (pendingGoal > 0 && pendingDate.isNotEmpty() && pendingDate < today()) {
            prefs.edit()
                .putInt("steps_goal", pendingGoal)
                .putInt("pending_goal", -1)
                .putString("goal_change_date", "")
                .apply()
        }
    }

    override fun getCurrentSteps(): Int = currentSteps

    override fun getGoalSteps(): Int {
        applyPendingGoalIfNewDay()
        return prefs.getInt("steps_goal", 10_000)
    }

    override fun getPendingGoal(): Int? {
        val pg = prefs.getInt("pending_goal", -1)
        return if (pg > 0) pg else null
    }

    override fun getPointsEarned(): Int =
        (if (milestone1Awarded) 10 else 0) +
        (if (milestone2Awarded) 20 else 0) +
        (if (milestone3Awarded) 30 else 0)

    override fun canChangeGoal(): Boolean = true

    override fun configureGoal(newGoal: Int) {
        prefs.edit()
            .putInt("pending_goal", newGoal)
            .putString("goal_change_date", today())
            .apply()
    }

    override fun setSteps(steps: Int) {
        currentSteps = steps
        checkMilestones()
    }

    override fun resetMilestones() {
        milestone1Awarded = false
        milestone2Awarded = false
        milestone3Awarded = false
    }

    private fun checkMilestones() {
        val goal = getGoalSteps()
        if (!milestone1Awarded && currentSteps >= goal / 2) {
            milestone1Awarded = true
            scope.launch { logMilestone(10) }
        }
        if (!milestone2Awarded && currentSteps >= (goal * 3) / 4) {
            milestone2Awarded = true
            scope.launch { logMilestone(20) }
        }
        if (!milestone3Awarded && currentSteps >= goal) {
            milestone3Awarded = true
            scope.launch { logMilestone(30) }
        }
    }

    private suspend fun logMilestone(points: Int) {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        val todayStr = today()
        try {
            supabase.from("points_log").insert(
                InsertPointsLogDto(userId = uid, points = points, source = "STEPS", dayKey = todayStr)
            )
            supabase.from("habits_daily").upsert(
                HabitsDailyUpsertDto(
                    userId    = uid,
                    date      = todayStr,
                    steps     = currentSteps,
                    stepsGoal = getGoalSteps()
                )
            ) { onConflict = "user_id,date" }
            Log.d(TAG, "Persisted milestone ($points pts) at $currentSteps steps")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist milestone ($points pts)", e)
        }
        val (title, msg) = when (points) {
            10   -> "A mitad de camino 🏃" to "Has completado el 50% de tu objetivo de pasos (+10 pts)"
            20   -> "¡Ya casi! 💪" to "Has completado el 75% de tu objetivo de pasos (+20 pts)"
            else -> "¡Objetivo completado! 🎉" to "Has alcanzado tu objetivo de pasos (+30 pts)"
        }
        if (points == 30) {
            // Persisted to the DB — the always-on realtime subscription delivers it to
            // NotificationHub, so adding it locally too would double it up.
            SupabaseNotificationRepository.insertForCurrentUser(
                type  = "STEPS_GOAL",
                title = title,
                body  = msg
            )
        } else {
            NotificationHub.add(title, msg, NotificationType.STEPS)
        }
    }

    override suspend fun syncSteps(steps: Int, goal: Int) {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        try {
            supabase.from("habits_daily").upsert(
                HabitsDailyUpsertDto(userId = uid, date = today(), steps = steps, stepsGoal = goal)
            ) { onConflict = "user_id,date" }
            Log.d(TAG, "Synced steps ($steps/$goal)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to sync steps ($steps/$goal)", e)
        }
    }

    // Guarded so it only runs once per device — otherwise a fresh install would
    // default to 10_000 and syncSteps() would push that straight back to the DB,
    // clobbering the real goal.
    override suspend fun hydrateGoalFromServer() {
        if (prefs.contains("steps_goal")) return
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        try {
            val row = supabase.from("habits_daily").select {
                filter { eq("user_id", uid) }
                order("date", Order.DESCENDING)
                limit(1)
            }.decodeList<HabitsDailyUpsertDto>().firstOrNull()
            if (row != null && row.stepsGoal > 0) {
                prefs.edit().putInt("steps_goal", row.stepsGoal).apply()
                Log.d(TAG, "Hydrated steps goal from DB: ${row.stepsGoal}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to hydrate steps goal from server", e)
        }
    }

    override suspend fun getWeeklyStats(): StepsWeeklyStats {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return StepsWeeklyStats()
        return try {
            val logs = supabase.from("user_daily_log")
                .select {
                    filter { eq("user_id", uid) }
                    order("date", Order.DESCENDING)
                    limit(7)
                }
                .decodeList<DailyLogDto>()

            val totalSteps = logs.sumOf { it.steps }
            val bestDay    = logs.maxOfOrNull { it.steps } ?: 0
            val avgSteps   = if (logs.isNotEmpty()) totalSteps / logs.size else 0
            var streak     = 0
            for (log in logs) {
                if (log.stepsGoal > 0 && log.steps >= log.stepsGoal) streak++ else break
            }
            StepsWeeklyStats(
                totalSteps7Days = totalSteps,
                bestDaySteps    = bestDay,
                dailyAverage    = avgSteps,
                goalStreak      = streak
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load weekly steps stats", e)
            StepsWeeklyStats()
        }
    }
}
