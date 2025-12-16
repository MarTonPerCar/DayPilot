package com.example.daypilot.main.mainZone.habits.steps

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.daypilot.firebaseLogic.authLogic.PointSource
import com.example.daypilot.firebaseLogic.pointsLogic.PointsRepository
import com.example.daypilot.firebaseLogic.pointsLogic.PointsTime
import com.example.daypilot.firebaseLogic.stepsLogic.StepsFirebaseRepository
import com.example.daypilot.mainDatabase.StepsLocalStore
import kotlinx.coroutines.flow.first
import java.time.ZoneId

class StepsForegroundSync(
    private val appContext: Context,
    private val zoneId: ZoneId,
    private val pointsRepo: PointsRepository = PointsRepository(),
    private val stepsRepo: StepsFirebaseRepository = StepsFirebaseRepository()
) {

    private val local = StepsLocalStore(appContext)
    private val sensor = StepsSensor(appContext)
    private val cache = StepsCounterCache(appContext)

    suspend fun sync(uid: String) {
        if (!hasActivityPermission()) return

        val counterNow = sensor.readCounterOnce() ?: return
        val todayKey = PointsTime.todayKey(zoneId)

        val ls = local.flow.first()

        if (ls.dayKey.isNotBlank() && ls.dayKey != todayKey) {
            val lastCounterYesterday = cache.getLastCounter(ls.dayKey)
            if (lastCounterYesterday != null && lastCounterYesterday >= ls.baselineCounter) {
                val yesterdaySteps = (lastCounterYesterday - ls.baselineCounter).coerceAtLeast(0L).toInt()
                stepsRepo.upsertDaily(
                    uid = uid,
                    dayKey = ls.dayKey,
                    steps = yesterdaySteps,
                    goal = ls.goalToday,
                    finalized = true
                )
            }

            val goalToday = ls.goalNextDay.takeIf { it > 0 } ?: ls.goalToday
            local.setDay(
                dayKey = todayKey,
                baseline = counterNow,
                goalToday = goalToday,
                goalNext = goalToday
            )
        }

        val cur = local.flow.first()
        if (cur.dayKey.isBlank()) {
            val goalToday = cur.goalNextDay.takeIf { it > 0 } ?: cur.goalToday
            local.setDay(
                dayKey = todayKey,
                baseline = counterNow,
                goalToday = goalToday,
                goalNext = goalToday
            )
        }

        val st = local.flow.first()
        val stepsToday = (counterNow - st.baselineCounter).coerceAtLeast(0L).toInt()
        val goal = st.goalToday.coerceAtLeast(1)
        val pct = stepsToday.toFloat() / goal.toFloat()

        cache.putLastCounter(todayKey, counterNow)

        stepsRepo.upsertDaily(
            uid = uid,
            dayKey = todayKey,
            steps = stepsToday,
            goal = goal,
            finalized = false
        )

        val pts50 = 1L
        val pts75 = 1L
        val pts100 = 1L
        val bonusOn100 = 3L

        if (pct >= 0.5f && !st.m50Sent) {
            pointsRepo.addPoints(
                uid = uid,
                points = pts50,
                source = PointSource.STEPS,
                metadata = mapOf("milestone" to "50", "dayKey" to todayKey)
            )
            local.markMilestone50()
        }

        if (pct >= 0.75f && !st.m75Sent) {
            pointsRepo.addPoints(
                uid = uid,
                points = pts75,
                source = PointSource.STEPS,
                metadata = mapOf("milestone" to "75", "dayKey" to todayKey)
            )
            local.markMilestone75()
        }

        if (pct >= 1.0f && !st.m100Sent) {
            pointsRepo.addPoints(
                uid = uid,
                points = pts100 + bonusOn100,
                source = PointSource.STEPS,
                metadata = mapOf("milestone" to "100", "dayKey" to todayKey)
            )
            local.markMilestone100()
        }
    }

    private fun hasActivityPermission(): Boolean {
        if (Build.VERSION.SDK_INT < 29) return true
        return ContextCompat.checkSelfPermission(
            appContext,
            android.Manifest.permission.ACTIVITY_RECOGNITION
        ) == PackageManager.PERMISSION_GRANTED
    }
}

private class StepsCounterCache(ctx: Context) {
    private val sp = ctx.getSharedPreferences("steps_counter_cache", Context.MODE_PRIVATE)

    fun putLastCounter(dayKey: String, counter: Long) {
        sp.edit().putLong("counter_$dayKey", counter).apply()
    }

    fun getLastCounter(dayKey: String): Long? {
        if (!sp.contains("counter_$dayKey")) return null
        return sp.getLong("counter_$dayKey", 0L)
    }
}