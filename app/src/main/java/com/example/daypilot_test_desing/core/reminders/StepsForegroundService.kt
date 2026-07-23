package com.example.daypilot_test_desing.core.reminders

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.daypilot_test_desing.R
import com.example.daypilot_test_desing.core.connectivity.ConnectivityService
import com.example.daypilot_test_desing.core.data.local.StepsSignal
import com.example.daypilot_test_desing.data.supabase.SupabaseStepsRepository
import com.example.daypilot_test_desing.data.supabase.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

const val STEPS_CHANNEL_ID = "daypilot_steps_tracking"

fun createStepsChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
            STEPS_CHANNEL_ID,
            context.getString(R.string.steps_channel_name),
            NotificationManager.IMPORTANCE_MIN
        ).apply {
            description = context.getString(R.string.steps_channel_desc)
            setShowBadge(false)
        }
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
    }
}

fun startStepsService(context: Context) {
    ContextCompat.startForegroundService(context, Intent(context, StepsForegroundService::class.java))
}

/**
 * Keeps a single step-counter sensor listener alive independent of the app's UI lifecycle.
 * Android's own guide for reading step data recommends exactly this — a foreground service is
 * the supported way to stay registered while not in the foreground; WorkManager periodic jobs
 * (what this replaces) are only their suggested fallback for when you don't have one.
 */
class StepsForegroundService : Service(), SensorEventListener {

    companion object {
        private const val TAG = "StepsForegroundService"
        private const val NOTIFICATION_ID = 4201
        private const val STEP_SYNC_THRESHOLD = 100
        private const val PERIODIC_SYNC_MS = 5 * 60_000L
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var periodicJob: Job? = null

    private lateinit var repo: SupabaseStepsRepository
    private var sensorManager: SensorManager? = null
    private var sensorRegistered = false

    private var prevTotalSinceBoot = -1
    private var prevEventNs = 0L
    private var lastSyncedSteps = -1

    override fun onCreate() {
        super.onCreate()
        repo = SupabaseStepsRepository(getSharedPreferences("daypilot_steps", MODE_PRIVATE))
        sensorManager = getSystemService(SensorManager::class.java)

        if (!startAsForeground()) {
            // The "health" FGS type requires ACTIVITY_RECOGNITION/BODY_SENSORS to already be
            // granted, or startForeground() throws — an uncaught exception here would crash the
            // whole process, not just this service. If we can't legally run as foreground, stop
            // cleanly; MainActivity/onStartCommand will retry once the permission is granted.
            stopSelf()
            return
        }
        registerSensorIfPermitted()

        periodicJob = scope.launch {
            while (true) {
                delay(PERIODIC_SYNC_MS)
                val steps = repo.getCurrentSteps()
                if (steps != lastSyncedSteps) sync(steps)
            }
        }
    }

    // The runtime permission may not be granted yet the instant this service is first started
    // (MainActivity requests it asynchronously) — retrying here on every subsequent start (i.e.
    // every app open) catches the case where it gets granted afterward.
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        registerSensorIfPermitted()
        return START_STICKY
    }

    private fun registerSensorIfPermitted() {
        if (sensorRegistered) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED
        ) return
        val sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        if (sensor == null) {
            Log.w(TAG, "No step counter sensor on this device")
            return
        }
        sensorManager?.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL)
        sensorRegistered = true
        Log.d(TAG, "Step sensor registered")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent) {
        val totalSinceBoot = event.values[0].toInt()

        // >10 steps/sec within a 30s gap is sensor noise, not real steps; larger gaps just mean
        // the OS batched several readings together, which is normal and shouldn't be rejected.
        if (prevTotalSinceBoot >= 0 && prevEventNs > 0) {
            val stepDelta = totalSinceBoot - prevTotalSinceBoot
            val timeDeltaS = (event.timestamp - prevEventNs) / 1_000_000_000.0
            if (stepDelta > 0 && timeDeltaS in 0.001..30.0 && stepDelta / timeDeltaS > 10.0) {
                Log.d(TAG, "Rejected step spike: $stepDelta steps in ${timeDeltaS}s")
                prevEventNs = event.timestamp
                return
            }
        }
        prevTotalSinceBoot = totalSinceBoot
        prevEventNs = event.timestamp

        val dailySteps = repo.recordRawSteps(totalSinceBoot)
        StepsSignal.notifyUpdated()

        if (lastSyncedSteps < 0 || dailySteps - lastSyncedSteps >= STEP_SYNC_THRESHOLD) {
            scope.launch { sync(dailySteps) }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // Uses the raw connectivity check rather than ConnectivityState.ensureOnline() on purpose —
    // this runs with the app possibly closed, and flipping the global "no internet" overlay from
    // a background service would greet the user with a stale banner next time they open the app.
    private suspend fun sync(steps: Int) {
        lastSyncedSteps = steps
        if (supabase.auth.currentUserOrNull() == null) return
        if (!ConnectivityService.hasInternetConnection()) return
        repo.syncSteps(steps, repo.getGoalSteps())
        Log.d(TAG, "Background sync: $steps steps")
    }

    private fun hasSensorPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return true
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) ==
            PackageManager.PERMISSION_GRANTED
    }

    /** Returns false (instead of throwing) if this device/API level won't let us start as a
     *  "health"-typed foreground service right now — see the onCreate() comment for why that
     *  matters here specifically. */
    private fun startAsForeground(): Boolean {
        val notification: Notification = NotificationCompat.Builder(this, STEPS_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(getString(R.string.steps_channel_name))
            .setContentText(getString(R.string.steps_notification_text))
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                if (!hasSensorPermission()) {
                    Log.w(TAG, "Skipping foreground start: ACTIVITY_RECOGNITION not granted")
                    return false
                }
                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
            } else {
                startForeground(NOTIFICATION_ID, notification)
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "startForeground failed", e)
            false
        }
    }

    override fun onDestroy() {
        sensorManager?.unregisterListener(this)
        periodicJob?.cancel()
        scope.cancel()
        super.onDestroy()
    }
}
