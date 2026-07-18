package com.example.daypilot_test_desing.core.reminders

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.example.daypilot_test_desing.data.supabase.SupabaseStepsRepository
import com.example.daypilot_test_desing.data.supabase.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

private const val STEPS_WORK_NAME = "steps_background_sync"
private const val SENSOR_READ_TIMEOUT_MS = 10_000L

fun scheduleStepsWorker(context: Context) {
    val request = PeriodicWorkRequestBuilder<StepsWorker>(15, TimeUnit.MINUTES).build()
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
        STEPS_WORK_NAME,
        ExistingPeriodicWorkPolicy.KEEP,
        request
    )
}

/**
 * Background safety net for when the app isn't in active use: register the step-counter
 * sensor, capture the single next value it reports, unregister immediately — no continuous
 * listening, no foreground service, no persistent notification, same tech-health philosophy
 * that ruled those out for TechHealthWorker too. Milestone points/notifications are computed
 * server-side by the fn_award_steps_milestones trigger the moment this upload lands.
 */
class StepsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (supabase.auth.currentUserOrNull()?.id == null) return Result.success()
        if (!hasActivityRecognitionPermission()) return Result.success()

        val sensorManager = applicationContext.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (sensorManager == null || stepSensor == null) return Result.success()

        val totalSinceBoot = readOneShotSteps(sensorManager, stepSensor) ?: return Result.success()

        // Shares the "daypilot_steps" prefs file with StepsViewModel — same
        // baseline_date/baseline_steps keys, so a device that's had the app open today
        // already has a trustworthy baseline; this worker adopts it as-is.
        val prefs = applicationContext.getSharedPreferences("daypilot_steps", Context.MODE_PRIVATE)
        val today = today()
        val savedDate = prefs.getString("baseline_date", "")
        val baseline = if (savedDate == today) {
            prefs.getInt("baseline_steps", totalSinceBoot)
        } else {
            // New day and the app hasn't been opened yet to establish today's baseline —
            // start counting from whatever the sensor reports right now.
            prefs.edit()
                .putString("baseline_date", today)
                .putInt("baseline_steps", totalSinceBoot)
                .apply()
            totalSinceBoot
        }
        val dailySteps = maxOf(0, totalSinceBoot - baseline)

        val repo = SupabaseStepsRepository(prefs)
        repo.setSteps(dailySteps)
        repo.syncSteps(dailySteps, repo.getGoalSteps())

        return Result.success()
    }

    private fun hasActivityRecognitionPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return ContextCompat.checkSelfPermission(applicationContext, Manifest.permission.ACTIVITY_RECOGNITION) ==
            PackageManager.PERMISSION_GRANTED
    }

    private suspend fun readOneShotSteps(sensorManager: SensorManager, sensor: Sensor): Int? =
        withTimeoutOrNull(SENSOR_READ_TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        sensorManager.unregisterListener(this)
                        if (cont.isActive) cont.resume(event.values[0].toInt())
                    }
                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
                }
                cont.invokeOnCancellation { sensorManager.unregisterListener(listener) }
                sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
            }
        }

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
}
