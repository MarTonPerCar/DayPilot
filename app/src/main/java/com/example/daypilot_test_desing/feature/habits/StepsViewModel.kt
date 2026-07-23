package com.example.daypilot_test_desing.feature.habits

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.core.connectivity.ConnectivityState
import com.example.daypilot_test_desing.core.data.local.StepsSignal
import com.example.daypilot_test_desing.core.data.repository.StepsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Purely a display + on-demand-sync layer now — StepsForegroundService owns the actual sensor
 * listener and keeps counting regardless of whether this ViewModel (or the app) is alive. This
 * just mirrors whatever the service (or a previous session) already persisted to local prefs,
 * and reacts to StepsSignal for live updates while the Habits/Steps screen is open.
 */
class StepsViewModel(
    application: Application,
    private val stepsRepo: StepsRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(StepsUiState())
    val uiState: StateFlow<StepsUiState> = _uiState.asStateFlow()

    private var lastSyncedSteps: Int = -1

    init {
        val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val hasSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null

        updateStepsDisplay()
        _uiState.update { it.copy(sensorAvailable = hasSensor) }

        viewModelScope.launch {
            if (ConnectivityState.ensureOnline()) {
                stepsRepo.hydrateGoalFromServer()
            }
            updateStepsDisplay()
            refreshPoints()
        }
        viewModelScope.launch { loadWeeklyStats() }

        StepsSignal.updated.onEach { updateStepsDisplay() }.launchIn(viewModelScope)

        // The service keeps counting/syncing in the background; this just refreshes what's
        // shown whenever the user comes back to the app, plus does an on-demand sync.
        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                updateStepsDisplay()
                triggerSync()
            }
        })
    }

    /** Sync trigger: entering the Steps/Habits screen mid-session. */
    fun refresh() {
        updateStepsDisplay()
        triggerSync()
        viewModelScope.launch { loadWeeklyStats() }
    }

    private fun triggerSync(steps: Int = stepsRepo.getCurrentSteps()) {
        if (steps == lastSyncedSteps) return
        lastSyncedSteps = steps
        val goal = stepsRepo.getGoalSteps()
        viewModelScope.launch {
            if (!ConnectivityState.ensureOnline()) return@launch
            stepsRepo.syncSteps(steps, goal)
            refreshPoints()
        }
    }

    fun configureGoal(newGoal: Int) {
        stepsRepo.configureGoal(newGoal)
        updateStepsDisplay()
    }

    /** Local-only — steps/goal fields the prefs already know synchronously. */
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
        if (!ConnectivityState.ensureOnline()) return
        val earned = stepsRepo.getPointsEarned()
        _uiState.update { current ->
            current.copy(
                pointsEarned    = earned,
                pointsRemaining = maxOf(0, 60 - earned)
            )
        }
    }

    private suspend fun loadWeeklyStats() {
        if (!ConnectivityState.ensureOnline()) return
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

    companion object {
        fun factory(application: Application, repo: StepsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    StepsViewModel(application, repo) as T
            }
    }
}
