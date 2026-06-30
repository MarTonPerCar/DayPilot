package com.example.daypilot_test_desing.feature.habits

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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

            // Reject physiologically impossible spikes: if more than 10 steps/sec
            // arrived since the last event and the gap is under 30 s, it's sensor
            // noise (emulator artifact or aggressive software step detection).
            // Gaps > 30 s are ignored — the app may have been backgrounded.
            if (prevTotalSinceBoot >= 0 && prevEventNs > 0) {
                val stepDelta  = totalSinceBoot - prevTotalSinceBoot
                val timeDeltaS = (event.timestamp - prevEventNs) / 1_000_000_000.0
                if (stepDelta > 0 && timeDeltaS in 0.001..30.0 && stepDelta / timeDeltaS > 10.0) {
                    prevEventNs = event.timestamp
                    return
                }
            }
            prevTotalSinceBoot = totalSinceBoot
            prevEventNs        = event.timestamp

            if (baseline < 0) {
                val today = todayStr()
                val savedDate = prefs.getString("baseline_date", "")
                baseline = if (savedDate == today) {
                    prefs.getInt("baseline_steps", totalSinceBoot)
                } else {
                    prefs.edit()
                        .putString("baseline_date", today)
                        .putInt("baseline_steps", totalSinceBoot)
                        .apply()
                    stepsRepo.resetMilestones()
                    totalSinceBoot
                }
            }
            val dailySteps = maxOf(0, totalSinceBoot - baseline)
            val prevSteps  = stepsRepo.getCurrentSteps()
            stepsRepo.setSteps(dailySteps)
            if (dailySteps != prevSteps) updateLocalState()
            if (lastSyncedSteps < 0 || dailySteps - lastSyncedSteps >= STEP_SYNC_THRESHOLD) {
                triggerSync(dailySteps)
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    init {
        stepSensor?.let {
            sensorManager?.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
        viewModelScope.launch { loadWeeklyStats() }
        updateLocalState()
        _uiState.update { it.copy(sensorAvailable = stepSensor != null) }

        // Periodic sync every 5 minutes
        viewModelScope.launch {
            while (true) {
                delay(PERIODIC_SYNC_MS)
                val steps = stepsRepo.getCurrentSteps()
                if (steps != lastSyncedSteps) triggerSync(steps)
            }
        }

        // Sync when the app goes to background so the latest step count is persisted
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                triggerSync(stepsRepo.getCurrentSteps())
            }
        })
    }

    override fun onCleared() {
        sensorManager?.unregisterListener(sensorListener)
    }

    fun refresh() {
        triggerSync()
        updateLocalState()
        viewModelScope.launch { loadWeeklyStats() }
    }

    private fun triggerSync(steps: Int = stepsRepo.getCurrentSteps()) {
        lastSyncedSteps = steps
        val goal = stepsRepo.getGoalSteps()
        viewModelScope.launch { stepsRepo.syncSteps(steps, goal) }
    }

    fun configureGoal(newGoal: Int) {
        stepsRepo.configureGoal(newGoal)
        updateLocalState()
    }

    private fun updateLocalState() {
        val earned = stepsRepo.getPointsEarned()
        _uiState.update { current ->
            current.copy(
                currentSteps     = stepsRepo.getCurrentSteps(),
                goalSteps        = stepsRepo.getGoalSteps(),
                pointsEarned     = earned,
                pointsRemaining  = maxOf(0, 60 - earned),
                pendingGoal      = stepsRepo.getPendingGoal(),
                goalChangedToday = !stepsRepo.canChangeGoal()
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
