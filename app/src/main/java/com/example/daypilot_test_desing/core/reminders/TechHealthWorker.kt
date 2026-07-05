package com.example.daypilot_test_desing.core.reminders

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.daypilot_test_desing.core.data.local.SharedPrefsTechHealthRepository
import com.example.daypilot_test_desing.data.supabase.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import java.util.concurrent.TimeUnit

private const val TECH_HEALTH_WORK_NAME = "tech_health_usage_check"

fun scheduleTechHealthWorker(context: Context) {
    val request = PeriodicWorkRequestBuilder<TechHealthWorker>(15, TimeUnit.MINUTES).build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        TECH_HEALTH_WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}

// FIXME: aggressive battery-saver modes on some devices may prevent this from running.
class TechHealthWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (supabase.auth.currentUserOrNull()?.id == null) return Result.success()

        val repository = SharedPrefsTechHealthRepository(applicationContext)
        repository.applyPendingChangesIfNewDay()

        if (!AppUsageTracker.hasPermission(applicationContext)) return Result.success()

        val usageMap = AppUsageTracker.getTodayUsage(applicationContext)

        repository.getAppRestrictions().filter { it.isEnabled }.forEach { r ->
            val used = usageMap[r.packageName] ?: 0
            if (used != r.usedMinutesToday) repository.updateUsage(r.id, used)
            if (used >= r.dailyLimitMinutes && r.dailyLimitMinutes > 0 && !r.isViolatedToday) {
                repository.markViolated(r.id)
                markAppViolatedInSupabase(r.packageName)
            }
        }

        repository.getGroupRestrictions().filter { it.isEnabled }.forEach { g ->
            val used = g.apps.sumOf { usageMap[it.packageName] ?: 0 }
            if (used != g.usedMinutesToday) repository.updateGroupUsage(g.id, used)
            if (used >= g.dailyLimitMinutes && g.dailyLimitMinutes > 0 && !g.isViolatedToday) {
                repository.markGroupViolated(g.id)
                markGroupViolatedInSupabase(g.groupName)
            }
        }

        return Result.success()
    }

    private suspend fun markAppViolatedInSupabase(packageName: String) {
        try {
            val uid = supabase.auth.currentUserOrNull()?.id ?: return
            supabase.from("tech_health_config").update({ set("is_violated_today", true) }) {
                filter { eq("user_id", uid); eq("app_package", packageName) }
            }
        } catch (_: Exception) { }
    }

    private suspend fun markGroupViolatedInSupabase(groupName: String) {
        try {
            val uid = supabase.auth.currentUserOrNull()?.id ?: return
            supabase.from("tech_health_group_config").update({ set("is_violated_today", true) }) {
                filter { eq("user_id", uid); eq("group_name", groupName) }
            }
        } catch (_: Exception) { }
    }
}
