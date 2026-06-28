package com.example.daypilot_test_desing.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.backend.fake.FakeFriendRepository
import com.example.daypilot_test_desing.backend.fake.FakeProgressRepository
import com.example.daypilot_test_desing.backend.fake.FakeTaskRepository
import com.example.daypilot_test_desing.backend.fake.FakeUserRepository
import com.example.daypilot_test_desing.backend.repository.StepsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(private val stepsRepo: StepsRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh() {
        viewModelScope.launch {
            try {
                val user     = FakeUserRepository.getCurrentUser()
                val tasks    = FakeTaskRepository.getTasks()
                val progress = FakeProgressRepository
                _uiState.value = HomeUiState(
                    userName            = user.name,
                    streak              = user.currentStreak,
                    stepsToday          = stepsRepo.getCurrentSteps(),
                    stepsGoal           = stepsRepo.getGoalSteps(),
                    tasksCompleted      = tasks.count { it.isDone },
                    tasksTotal          = tasks.size,
                    progressData        = progress.getProgressData(),
                    pointsToday         = progress.getPointsToday(),
                    rankingPosition     = progress.getRankingPosition(),
                    friendCount         = FakeFriendRepository.getFriends().size,
                    timerCompletedToday = progress.isTimerCompletedToday()
                )
            } catch (_: Exception) {
                // retain previous state on error
            }
        }
    }

    companion object {
        fun factory(stepsRepo: StepsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    HomeViewModel(stepsRepo) as T
            }
    }
}
