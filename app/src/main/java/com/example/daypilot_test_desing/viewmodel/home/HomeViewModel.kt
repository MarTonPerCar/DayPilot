package com.example.daypilot_test_desing.viewmodel.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.backend.model.DayProgress
import com.example.daypilot_test_desing.backend.repository.FriendRepository
import java.util.Calendar
import com.example.daypilot_test_desing.backend.repository.ProgressRepository
import com.example.daypilot_test_desing.backend.repository.StepsRepository
import com.example.daypilot_test_desing.backend.repository.TaskRepository
import com.example.daypilot_test_desing.backend.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val stepsRepo: StepsRepository,
    private val progressRepo: ProgressRepository,
    private val userRepo: UserRepository,
    private val friendRepo: FriendRepository,
    private val taskRepo: TaskRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { refresh() }

    fun refresh(): Job = viewModelScope.launch {
            try {
                val cal       = Calendar.getInstance()
                val todayDay  = cal.get(Calendar.DAY_OF_MONTH)
                val todayMon  = cal.get(Calendar.MONTH) + 1
                val todayYear = cal.get(Calendar.YEAR)

                val user     = userRepo.getCurrentUser()
                val allTasks = taskRepo.getTasks()
                val todayTasks = allTasks.filter {
                    it.day == todayDay && it.month == todayMon && it.year == todayYear
                }
                val today    = progressRepo.getTodayProgress()
                val history  = progressRepo.getHistory(7)
                val ranking  = progressRepo.getRankingPosition()
                val friends  = friendRepo.getFriends()
                val progressData = history.map { log ->
                    val day = log.date.substringAfterLast("-").toIntOrNull() ?: 0
                    DayProgress(day = day, points = log.totalPoints, steps = log.steps, tasksCompleted = log.tasksCompleted)
                }
                _uiState.value = HomeUiState(
                    userName            = user.name,
                    streak              = user.currentStreak,
                    stepsToday          = stepsRepo.getCurrentSteps(),
                    stepsGoal           = stepsRepo.getGoalSteps(),
                    tasksCompleted      = todayTasks.count { it.isDone },
                    tasksTotal          = todayTasks.size,
                    progressData        = progressData,
                    pointsToday         = today.totalPoints,
                    rankingPosition     = ranking,
                    friendCount         = friends.size,
                    timerCompletedToday = today.timerPoints > 0
                )
            } catch (_: Exception) { }
    }

    companion object {
        fun factory(
            stepsRepo: StepsRepository,
            progressRepo: ProgressRepository,
            userRepo: UserRepository,
            friendRepo: FriendRepository,
            taskRepo: TaskRepository
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    HomeViewModel(stepsRepo, progressRepo, userRepo, friendRepo, taskRepo) as T
            }
    }
}
