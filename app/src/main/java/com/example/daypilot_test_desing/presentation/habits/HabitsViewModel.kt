package com.example.daypilot_test_desing.presentation.habits

import androidx.lifecycle.ViewModel
import com.example.daypilot_test_desing.data.repository.fake.FakeStepsRepository
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
        val maxPoints = goal / 1000 * 10
        return HabitsUiState(
            currentSteps   = current,
            goalSteps      = goal,
            pointsEarned   = earned,
            pointsRemaining= maxOf(0, maxPoints - earned)
        )
    }

    fun refresh() { _uiState.value = buildState() }
}
