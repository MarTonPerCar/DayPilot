package com.example.daypilot_test_desing.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.data.repository.fake.FakeFriendRepository
import com.example.daypilot_test_desing.data.repository.fake.FakeProgressRepository
import com.example.daypilot_test_desing.data.repository.fake.FakeStepsRepository
import com.example.daypilot_test_desing.data.repository.fake.FakeTaskRepository
import com.example.daypilot_test_desing.data.repository.fake.FakeUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            val user     = FakeUserRepository.getCurrentUser()
            val tasks    = FakeTaskRepository.getTasks()
            val steps    = FakeStepsRepository
            val progress = FakeProgressRepository
            _uiState.value = HomeUiState(
                userName            = user.name,
                streak              = user.currentStreak,
                stepsToday          = steps.getCurrentSteps(),
                stepsGoal           = steps.getGoalSteps(),
                tasksCompleted      = tasks.count { it.isDone },
                tasksTotal          = tasks.size,
                progressData        = progress.getProgressData(),
                pointsToday         = progress.getPointsToday(),
                rankingPosition     = progress.getRankingPosition(),
                friendCount         = FakeFriendRepository.getFriends().size,
                timerCompletedToday = progress.isTimerCompletedToday()
            )
        }
    }
}
