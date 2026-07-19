package com.example.daypilot_test_desing.feature.habits

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.core.data.repository.StepsRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepsViewModel(
    application: Application,
    private val stepsRepo: StepsRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(StepsUiState())
    val uiState: StateFlow<StepsUiState> = _uiState.asStateFlow()

    private val prefs = application.getSharedPreferences("daypilot_steps", Context.MODE_PRIVATE)
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepSensor    = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private var baseline: Int = -1
    private var lastSyncedSteps: Int = -1
    private var prevTotalSinceBoot: Int = -1
    private var prevEventNs: Long = 0L

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val totalSinceBoot = event.values[0].toInt()

            // >10 steps/sec within a 30s gap is sensor noise (emulator artifact or
            // over-eager step detection), not real steps; larger gaps mean backgrounded.
            if (prevTotalSinceBoot >= 0 && prevEventNs > 0) {
                val stepDelta  = totalSinceBoot - prevTotalSinceBoot
                val timeDeltaS = (event.timestamp - prevEventNs) / 1_000_000_000.0
                if (stepDelta > 0 && timeDeltaS in 0.001..30.0 && stepDelta / timeDeltaS > 10.0) {
                    Log.d(TAG, "Rejected step spike: $stepDelta steps in ${timeDeltaS}s")
                    prevEventNs = event.timestamp
                    return
                }
            }
            prevTotalSinceBoot = totalSinceBoot
            prevEventNs        = event.timestamp

            val today = todayStr()
            val savedDate = prefs.getString("baseline_date", "")
            if (baseline < 0 || savedDate != today) {
                baseline = if (savedDate == today) {
                    prefs.getInt("baseline_steps", totalSinceBoot)
                } else {
                    Log.d(TAG, "New day detected, resetting steps baseline to $totalSinceBoot")
                    prefs.edit()
                        .putString("baseline_date", today)
                        .putInt("baseline_steps", totalSinceBoot)
                        .apply()
                    // Milestone level/points recompute server-side on this device's next sync.
                    totalSinceBoot
                }
            }
            val dailySteps = maxOf(0, totalSinceBoot - baseline)
            val prevSteps  = stepsRepo.getCurrentSteps()
            stepsRepo.setSteps(dailySteps)
            // Local-only — keeps the visible counter live every tick without a network call.
            if (dailySteps != prevSteps) updateStepsDisplay()
            if (lastSyncedSteps < 0 || dailySteps - lastSyncedSteps >= STEP_SYNC_THRESHOLD) {
                triggerSync(dailySteps)
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    init {
        registerSensorIfPermitted()
        viewModelScope.launch {
            stepsRepo.hydrateGoalFromServer()
            updateStepsDisplay()
            refreshPoints()
        }
        viewModelScope.launch { loadWeeklyStats() }
        updateStepsDisplay()
        _uiState.update { it.copy(sensorAvailable = stepSensor != null) }

        viewModelScope.launch {
            while (true) {
                delay(PERIODIC_SYNC_MS)
                val steps = stepsRepo.getCurrentSteps()
                if (steps != lastSyncedSteps) triggerSync(steps)
            }
        }

        // Sync trigger #1: app comes to the foreground (cold start or resume, same handling).
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                triggerSync(stepsRepo.getCurrentSteps())
            }
            override fun onStart(owner: LifecycleOwner) {
                registerSensorIfPermitted()
                // baseline < 0 means the sensor hasn't reported yet this session, so currentSteps
                // is still 0 — syncing that would clobber today's real server-side count.
                if (baseline >= 0) triggerSync() else viewModelScope.launch { refreshPoints() }
            }
        })
    }

    private var sensorRegistered = false

    private fun registerSensorIfPermitted() {
        if (sensorRegistered) return
        val ctx = getApplication<Application>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED
        ) return
        stepSensor?.let {
            sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
            sensorRegistered = true
            Log.d(TAG, "Step sensor registered")
        }
    }

    override fun onCleared() {
        sensorManager?.unregisterListener(sensorListener)
    }

    /** Sync trigger #2: entering the Steps screen mid-session. */
    fun refresh() {
        triggerSync()
        updateStepsDisplay()
        viewModelScope.launch { loadWeeklyStats() }
    }

    private fun triggerSync(steps: Int = stepsRepo.getCurrentSteps()) {
        lastSyncedSteps = steps
        val goal = stepsRepo.getGoalSteps()
        Log.d(TAG, "Triggering steps sync: $steps/$goal")
        viewModelScope.launch {
            stepsRepo.syncSteps(steps, goal)
            // Re-fetch so displayed points reflect the server's recomputed milestone level.
            refreshPoints()
        }
    }

    fun configureGoal(newGoal: Int) {
        stepsRepo.configureGoal(newGoal)
        updateStepsDisplay()
    }

    /** Local-only — steps/goal fields the sensor and prefs already know synchronously. */
    private fun updateStepsDisplay() {
        _uiState.update { current ->
            current.copy(
                currentSteps     = stepsRepo.getCurrentSteps(),
                goalSteps        = stepsRepo.getGoalSteps(),
                pendingGoal      = stepsRepo.getPendingGoal(),
                goalChangedToday = !stepsRepo.canChangeGoal()
            )
        }
    }

    /** Server round-trip — only called after a sync, never on every sensor tick. */
    private suspend fun refreshPoints() {
        val earned = stepsRepo.getPointsEarned()
        _uiState.update { current ->
            current.copy(
                pointsEarned    = earned,
                pointsRemaining = maxOf(0, 60 - earned)
            )
        }
    }

    private suspend fun loadWeeklyStats() {
        val stats = stepsRepo.getWeeklyStats()
        _uiState.update { current ->
            current.copy(
                totalSteps7Days = stats.totalSteps7Days,
                bestDaySteps    = stats.bestDaySteps,
                dailyAverage    = stats.dailyAverage,
                goalStreak      = stats.goalStreak
            )
        }
    }

    private fun todayStr() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())

    companion object {
        private const val TAG = "StepsViewModel"
        private const val STEP_SYNC_THRESHOLD = 50
        private const val PERIODIC_SYNC_MS    = 5 * 60_000L

        fun factory(application: Application, repo: StepsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    StepsViewModel(application, repo) as T
            }
    }
}
