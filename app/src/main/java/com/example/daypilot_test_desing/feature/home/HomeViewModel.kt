package com.example.daypilot_test_desing.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.core.data.model.DayProgress
import java.util.Calendar
import com.example.daypilot_test_desing.core.data.repository.FriendRepository
import com.example.daypilot_test_desing.core.data.repository.ProgressRepository
import com.example.daypilot_test_desing.core.data.repository.StepsRepository
import com.example.daypilot_test_desing.core.data.repository.TaskRepository
import com.example.daypilot_test_desing.core.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

    fun refresh(): Job = viewModelScope.launch { load() }

    fun invalidate() { /* cache freshness is managed at the repo/SessionCache layer */ }

    private suspend fun load() {
        try {
            coroutineScope {
                // All repo calls are cache-first; only the first caller fetches from Supabase
                val userD    = async { userRepo.getCurrentUser() }
                val tasksD   = async { taskRepo.getTasks() }
                val todayD   = async { progressRepo.getTodayProgress() }
                val historyD = async { progressRepo.getHistory(30) }
                val rankingD = async { progressRepo.getRankingPosition() }
                val friendsD = async { friendRepo.getFriends() }

                val user        = userD.await()
                val allTasks    = tasksD.await()
                val today       = todayD.await()
                val history     = historyD.await()
                val ranking     = rankingD.await()
                val friends     = friendsD.await()

                val uniqueTasks = allTasks.distinctBy { it.id }
                val closedData  = history.reversed().map { log ->
                    val day = log.date.substringAfterLast("-").toIntOrNull() ?: 0
                    DayProgress(day = day, points = log.totalPoints, steps = log.steps, tasksCompleted = log.tasksCompleted)
                }
                val todayDayNum = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                val progressData = closedData + DayProgress(
                    day            = todayDayNum,
                    points         = today.totalPoints,
                    steps          = today.steps,
                    tasksCompleted = today.tasksCompleted,
                    isToday        = true
                )
                _uiState.value = HomeUiState(
                    userName            = user.name,
                    streak              = user.currentStreak,
                    stepsToday          = stepsRepo.getCurrentSteps(),
                    stepsGoal           = stepsRepo.getGoalSteps(),
                    tasksCompleted      = uniqueTasks.count { it.isDone },
                    tasksTotal          = uniqueTasks.size,
                    progressData        = progressData,
                    pointsToday         = today.totalPoints,
                    rankingPosition     = ranking,
                    friendCount         = friends.size,
                    timerCompletedToday = today.timerPoints > 0
                )
            }
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
