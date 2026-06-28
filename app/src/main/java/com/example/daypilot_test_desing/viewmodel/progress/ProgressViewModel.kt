package com.example.daypilot_test_desing.viewmodel.progress

import androidx.lifecycle.ViewModel
import com.example.daypilot_test_desing.backend.fake.FakeProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProgressViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    private fun buildState(): ProgressUiState {
        val p = FakeProgressRepository
        return ProgressUiState(
            progressData        = p.getProgressData(),
            rankingPosition     = p.getRankingPosition(),
            pointsToday         = p.getPointsToday(),
            pointsFromTasks     = p.getPointsFromTasks(),
            pointsFromSteps     = p.getPointsFromSteps(),
            pointsFromHabits    = p.getPointsFromHabits(),
            pointsFromTimers    = p.getPointsFromTimers(),
            timerCompletedToday = p.isTimerCompletedToday()
        )
    }

    fun refresh() { _uiState.value = buildState() }

    fun recordTimerComplete() {
        FakeProgressRepository.addTimerPoints(10)
        _uiState.value = buildState()
    }
}
