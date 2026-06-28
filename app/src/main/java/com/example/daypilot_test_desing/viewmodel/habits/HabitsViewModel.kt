package com.example.daypilot_test_desing.viewmodel.habits

import androidx.lifecycle.ViewModel
import com.example.daypilot_test_desing.backend.fake.FakeStepsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HabitsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<HabitsUiState> = _uiState.asStateFlow()

    private fun buildState(): HabitsUiState {
        val current  = FakeStepsRepository.getCurrentSteps()
        val goal     = FakeStepsRepository.getGoalSteps()
        val earned   = FakeStepsRepository.getPointsEarned()
        return HabitsUiState(
            currentSteps      = current,
            goalSteps         = goal,
            pointsEarned      = earned,
            pointsRemaining   = maxOf(0, 60 - earned),
            goalChangedToday  = !FakeStepsRepository.canChangeGoal(),
            pendingGoal       = FakeStepsRepository.getPendingGoal()
        )
    }

    fun refresh() { _uiState.value = buildState() }

    fun configureGoal(newGoal: Int) {
        FakeStepsRepository.configureGoal(newGoal)
        _uiState.value = buildState()
    }
}
