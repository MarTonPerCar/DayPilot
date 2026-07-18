package com.example.daypilot_test_desing.feature.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.core.data.repository.StepsRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HabitsViewModel(private val stepsRepo: StepsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(localState())
    val uiState: StateFlow<HabitsUiState> = _uiState.asStateFlow()

    init { refresh() }

    /** Local-only fields the sensor/prefs already know synchronously — no network. Safe to
     *  call on every sensor tick (see DayPilotNavGraph's stepsState-keyed LaunchedEffect). */
    private fun localState() = HabitsUiState(
        currentSteps     = stepsRepo.getCurrentSteps(),
        goalSteps        = stepsRepo.getGoalSteps(),
        pendingGoal      = stepsRepo.getPendingGoal(),
        goalChangedToday = !stepsRepo.canChangeGoal()
    )

    /** Cheap, no network — keeps the step-count preview live as the sensor updates. */
    fun refreshLocal() {
        _uiState.update { current ->
            current.copy(
                currentSteps     = stepsRepo.getCurrentSteps(),
                goalSteps        = stepsRepo.getGoalSteps(),
                pendingGoal      = stepsRepo.getPendingGoal(),
                goalChangedToday = !stepsRepo.canChangeGoal()
            )
        }
    }

    /** Sync trigger #2: entering the Habits screen mid-session (also used at init, which
     *  doubles as the app-cold-start case for this widget's own points preview). */
    fun refresh(): Job = viewModelScope.launch {
        stepsRepo.syncSteps(stepsRepo.getCurrentSteps(), stepsRepo.getGoalSteps())
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

    fun configureGoal(newGoal: Int) {
        stepsRepo.configureGoal(newGoal)
        refreshLocal()
    }

    companion object {
        fun factory(repo: StepsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    HabitsViewModel(repo) as T
            }
    }
}
