package com.example.daypilot_test_desing.viewmodel.habits

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import com.example.daypilot_test_desing.backend.fake.FakeStepsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StepsViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<StepsUiState> = _uiState.asStateFlow()

    private val prefs = application.getSharedPreferences("daypilot_steps", Context.MODE_PRIVATE)
    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepSensor    = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    // Steps-since-boot value recorded at start of the current calendar day
    private var baseline: Int = -1

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val totalSinceBoot = event.values[0].toInt()
            // Initialise baseline on the very first sensor event of this session
            if (baseline < 0) {
                val today = todayStr()
                val savedDate = prefs.getString("baseline_date", "")
                baseline = if (savedDate == today) {
                    prefs.getInt("baseline_steps", totalSinceBoot)
                } else {
                    // New calendar day — reset baseline and milestone flags
                    prefs.edit()
                        .putString("baseline_date", today)
                        .putInt("baseline_steps", totalSinceBoot)
                        .apply()
                    FakeStepsRepository.milestone1Awarded = false
                    FakeStepsRepository.milestone2Awarded = false
                    FakeStepsRepository.milestone3Awarded = false
                    totalSinceBoot
                }
            }
            val dailySteps = maxOf(0, totalSinceBoot - baseline)
            FakeStepsRepository.setSteps(dailySteps)
            _uiState.value = buildState()
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    init {
        stepSensor?.let {
            sensorManager?.registerListener(
                sensorListener, it, SensorManager.SENSOR_DELAY_NORMAL
            )
        }
    }

    override fun onCleared() {
        sensorManager?.unregisterListener(sensorListener)
    }

    fun refresh() { _uiState.value = buildState() }

    fun configureGoal(newGoal: Int) {
        FakeStepsRepository.configureGoal(newGoal)
        _uiState.value = buildState()
    }

    private fun buildState(): StepsUiState {
        val s      = FakeStepsRepository
        val earned = s.getPointsEarned()
        return StepsUiState(
            currentSteps    = s.getCurrentSteps(),
            goalSteps       = s.getGoalSteps(),
            pointsEarned    = earned,
            pointsRemaining = maxOf(0, 60 - earned),
            totalSteps7Days = s.getTotalSteps7Days(),
            bestDaySteps    = s.getBestDaySteps(),
            dailyAverage    = s.getDailyAverage(),
            goalStreak      = s.getGoalStreak(),
            pendingGoal     = s.getPendingGoal(),
            goalChangedToday = !s.canChangeGoal(),
            sensorAvailable  = stepSensor != null
        )
    }

    private fun todayStr() = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
}
