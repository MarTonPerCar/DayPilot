package com.example.daypilot_test_desing.core.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.daypilot_test_desing.core.data.local.SharedPrefsTechHealthRepository
import com.example.daypilot_test_desing.data.supabase.dto.HabitsDailyTechDto
import com.example.daypilot_test_desing.data.supabase.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private const val TECH_HEALTH_WORK_NAME = "tech_health_usage_check"

/** Enqueues the periodic usage check; safe to call on every app start (KEEP avoids duplicate schedules). */
fun scheduleTechHealthWorker(context: Context) {
    val request = PeriodicWorkRequestBuilder<TechHealthWorker>(15, TimeUnit.MINUTES).build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        TECH_HEALTH_WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}

/**
 * Tarea de WorkManager que corre en background cada ~15 min (puede variar según la batería).
 * Actualiza el uso por app, detecta violaciones y escribe en habits_daily.
 * El bloqueo real lo hace DayPilotAccessibilityService; esto solo persiste el estado.
 *
 * FIXME: si el usuario tiene el modo de ahorro de batería agresivo puede que no corra
 */
class TechHealthWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val uid = supabase.auth.currentUserOrNull()?.id ?: return Result.success()
        if (!AppUsageTracker.hasPermission(applicationContext)) return Result.success()

        val repository = SharedPrefsTechHealthRepository(applicationContext)
        repository.applyPendingChangesIfNewDay()

        val usageMap     = AppUsageTracker.getTodayUsage(applicationContext)
        // pendingDelete apps are still tracked/enforced today — they're only removed tomorrow.
        val restrictions = repository.getAppRestrictions().filter { it.isEnabled }

        var anyViolated = false
        restrictions.forEach { r ->
            val used = usageMap[r.packageName] ?: 0
            if (used != r.usedMinutesToday) repository.updateUsage(r.id, used)
            if (used >= r.dailyLimitMinutes && r.dailyLimitMinutes > 0) anyViolated = true
        }

        if (anyViolated && !repository.isViolatedToday()) {
            repository.markViolatedToday()
            try {
                supabase.from("habits_daily").upsert(
                    HabitsDailyTechDto(
                        userId              = uid,
                        date                = today(),
                        techHealthPointEarned = false
                    )
                )
            } catch (_: Exception) { }
        }

        return Result.success()
    }

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
}
