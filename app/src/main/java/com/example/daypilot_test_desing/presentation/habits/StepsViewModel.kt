package com.example.daypilot_test_desing.presentation.habits

import androidx.lifecycle.ViewModel
import com.example.daypilot_test_desing.data.repository.fake.FakeStepsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StepsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<StepsUiState> = _uiState.asStateFlow()

    private fun buildState(): StepsUiState {
        val s       = FakeStepsRepository
        val earned  = s.getPointsEarned()
        val maxPts  = s.getGoalSteps() / 1000 * 10
        return StepsUiState(
            currentSteps   = s.getCurrentSteps(),
            goalSteps      = s.getGoalSteps(),
            pointsEarned   = earned,
            pointsRemaining= maxOf(0, maxPts - earned),
            totalSteps7Days= s.getTotalSteps7Days(),
            bestDaySteps   = s.getBestDaySteps(),
            dailyAverage   = s.getDailyAverage(),
            goalStreak     = s.getGoalStreak()
        )
    }

    fun configureGoal(newGoal: Int) {
        FakeStepsRepository.configureGoal(newGoal)
        _uiState.value = buildState()
    }
}
