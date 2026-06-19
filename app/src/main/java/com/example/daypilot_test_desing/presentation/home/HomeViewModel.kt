package com.example.daypilot_test_desing.presentation.home

import androidx.lifecycle.ViewModel
import com.example.daypilot_test_desing.data.repository.fake.FakeProgressRepository
import com.example.daypilot_test_desing.data.repository.fake.FakeStepsRepository
import com.example.daypilot_test_desing.data.repository.fake.FakeTaskRepository
import com.example.daypilot_test_desing.data.repository.fake.FakeUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private fun buildState(): HomeUiState {
        val user     = FakeUserRepository.getCurrentUser()
        val tasks    = FakeTaskRepository.getTasks()
        val steps    = FakeStepsRepository
        val progress = FakeProgressRepository
        return HomeUiState(
            userName       = user.name,
            streak         = user.currentStreak,
            stepsToday     = steps.getCurrentSteps(),
            stepsGoal      = steps.getGoalSteps(),
            tasksCompleted = tasks.count { it.isDone },
            tasksTotal     = tasks.size,
            progressData   = progress.getProgressData(),
            pointsToday    = progress.getPointsToday(),
            rankingPosition= progress.getRankingPosition()
        )
    }

    fun refresh() { _uiState.value = buildState() }
}
