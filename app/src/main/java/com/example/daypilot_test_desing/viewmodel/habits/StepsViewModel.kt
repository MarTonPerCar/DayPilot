package com.example.daypilot_test_desing.viewmodel.habits

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.backend.repository.StepsRepository
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

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val totalSinceBoot = event.values[0].toInt()
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
            stepsRepo.setSteps(dailySteps)
            updateLocalState()
            // Stage 1: sync to DB whenever 100+ steps accumulated since last write
            if (lastSyncedSteps < 0 || dailySteps - lastSyncedSteps >= 100) {
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
        // Stage 2: periodic sync every 10 minutes
        viewModelScope.launch {
            while (true) {
                delay(10 * 60_000L)
                val steps = stepsRepo.getCurrentSteps()
                if (steps != lastSyncedSteps) triggerSync(steps)
            }
        }
    }

    override fun onCleared() {
        sensorManager?.unregisterListener(sensorListener)
    }

    fun refresh() {
        // Stage 3: sync on app open / foreground return
        triggerSync()
        updateLocalState()
        viewModelScope.launch { loadWeeklyStats() }
    }

    // Writes current steps to habits_daily; DB trigger propagates to daily_progress.
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
        fun factory(application: Application, repo: StepsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    StepsViewModel(application, repo) as T
            }
    }
}
