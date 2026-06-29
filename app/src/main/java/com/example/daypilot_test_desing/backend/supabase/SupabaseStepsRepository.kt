package com.example.daypilot_test_desing.backend.supabase

import android.content.SharedPreferences
import com.example.daypilot_test_desing.backend.repository.StepsRepository
import com.example.daypilot_test_desing.backend.repository.StepsWeeklyStats
import com.example.daypilot_test_desing.backend.supabase.dto.DailyLogDto
import com.example.daypilot_test_desing.backend.supabase.dto.HabitsDailyUpsertDto
import com.example.daypilot_test_desing.backend.supabase.dto.InsertPointsLogDto
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

/**
 * Real implementation of StepsRepository.
 *
 * Synchronous methods operate on in-memory state (fast sensor path).
 * Milestone hits fire background DB writes to points_log and habits_daily.
 * getWeeklyStats() reads from user_daily_log (the closed-day archive).
 *
 * All goal configuration is persisted in the shared "daypilot_steps" SharedPreferences
 * file, which StepsViewModel also uses for baseline management. Key separation:
 *   Repository keys: steps_goal, pending_goal, goal_change_date
 *   ViewModel keys:  baseline_date, baseline_steps
 */
class SupabaseStepsRepository(private val prefs: SharedPreferences) : StepsRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var currentSteps = 0
    private var milestone1Awarded = false
    private var milestone2Awarded = false
    private var milestone3Awarded = false

    // ── Helpers ──────────────────────────────────────────────────────

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

    // ── Synchronous interface ─────────────────────────────────────────

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

    // The active goal for today is always locked via the pending-goal mechanism.
    // Users can update the pending goal as many times as they want; the change
    // only takes effect the next day via applyPendingGoalIfNewDay().
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
        if (!milestone1Awarded && currentSteps >= goal / 3) {
            milestone1Awarded = true
            scope.launch { logMilestone(10) }
        }
        if (!milestone2Awarded && currentSteps >= (goal * 2) / 3) {
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
            )
        } catch (_: Exception) { }
    }

    // ── Suspend (DB-backed) ───────────────────────────────────────────

    override suspend fun syncSteps(steps: Int, goal: Int) {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        try {
            supabase.from("habits_daily").upsert(
                HabitsDailyUpsertDto(userId = uid, date = today(), steps = steps, stepsGoal = goal)
            )
        } catch (_: Exception) { }
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
        } catch (_: Exception) { StepsWeeklyStats() }
    }
}
