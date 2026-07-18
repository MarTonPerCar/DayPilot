package com.example.daypilot_test_desing.data.supabase

import android.content.SharedPreferences
import android.util.Log
import com.example.daypilot_test_desing.core.data.repository.StepsRepository
import com.example.daypilot_test_desing.core.data.repository.StepsWeeklyStats
import com.example.daypilot_test_desing.data.supabase.dto.DailyLogDto
import com.example.daypilot_test_desing.data.supabase.dto.HabitsDailyMilestoneDto
import com.example.daypilot_test_desing.data.supabase.dto.HabitsDailyUpsertDto
import com.example.daypilot_test_desing.data.supabase.dto.UserPendingGoalDto
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
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

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())

    private fun tomorrow(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, 1)
        return SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(cal.time)
    }

    // "goal_change_date" stores the date the pending goal *takes effect* (not the
    // date it was set) — same semantics as users.pending_steps_goal_date, so a
    // value pulled from the server can be copied in as-is, no translation needed.
    private fun applyPendingGoalIfNewDay() {
        val pendingGoal = prefs.getInt("pending_goal", -1)
        val pendingDate = prefs.getString("goal_change_date", "") ?: ""
        if (pendingGoal > 0 && pendingDate.isNotEmpty() && pendingDate <= today()) {
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

    override suspend fun getPointsEarned(): Int {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return 0
        return try {
            val level = supabase.from("habits_daily").select {
                filter { eq("user_id", uid); eq("date", today()) }
                limit(1)
            }.decodeList<HabitsDailyMilestoneDto>().firstOrNull()?.stepsMilestoneLevel ?: 0
            pointsForMilestoneLevel(level)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read steps milestone level", e)
            0
        }
    }

    // Cumulative — matches what fn_award_steps_milestones awards server-side per level.
    private fun pointsForMilestoneLevel(level: Int): Int = when (level) {
        1    -> 10
        2    -> 30
        3    -> 60
        else -> 0
    }

    override fun canChangeGoal(): Boolean = true

    override fun configureGoal(newGoal: Int) {
        val applyDate = tomorrow()
        prefs.edit()
            .putInt("pending_goal", newGoal)
            .putString("goal_change_date", applyDate)
            .apply()
        // Mirrors it to users.pending_steps_goal/_date so any other device this
        // account is signed into sees the same pending change, instead of the
        // change silently only taking effect on the device it was set from.
        scope.launch { pushPendingGoalToServer(newGoal, applyDate) }
    }

    private suspend fun pushPendingGoalToServer(newGoal: Int, applyDate: String) {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        try {
            supabase.from("users").update({
                set("pending_steps_goal", newGoal)
                set("pending_steps_goal_date", applyDate)
            }) {
                filter { eq("id", uid) }
            }
            Log.d(TAG, "Pushed pending goal to server: $newGoal effective $applyDate")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to push pending goal to server", e)
        }
    }

    // Adopts a pending goal queued from another device into the local prefs
    // mirror, so this device's own applyPendingGoalIfNewDay() picks it up too.
    private suspend fun pullPendingGoalFromServer() {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return
        try {
            val dto = supabase.from("users").select {
                filter { eq("id", uid) }
                limit(1)
            }.decodeList<UserPendingGoalDto>().firstOrNull() ?: return
            val serverGoal = dto.pendingStepsGoal
            val serverDate = dto.pendingStepsGoalDate
            if (serverGoal != null && serverGoal > 0 && !serverDate.isNullOrEmpty()) {
                val localGoal = prefs.getInt("pending_goal", -1)
                val localDate = prefs.getString("goal_change_date", "")
                if (serverGoal != localGoal || serverDate != localDate) {
                    prefs.edit()
                        .putInt("pending_goal", serverGoal)
                        .putString("goal_change_date", serverDate)
                        .apply()
                    Log.d(TAG, "Adopted pending goal from server: $serverGoal effective $serverDate")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to pull pending goal from server", e)
        }
    }

    // Milestone points and the STEPS_GOAL notification are computed and inserted
    // server-side by the fn_award_steps_milestones trigger on habits_daily writes —
    // this only ever stores the raw sensor-derived count for display/upload.
    override fun setSteps(steps: Int) {
        currentSteps = steps
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

    override suspend fun hydrateGoalFromServer() {
        hydrateActiveGoalIfFirstRun()
        pullPendingGoalFromServer()
    }

    // Guarded so it only runs once per device — otherwise a fresh install would
    // default to 10_000 and syncSteps() would push that straight back to the DB,
    // clobbering the real goal.
    private suspend fun hydrateActiveGoalIfFirstRun() {
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
