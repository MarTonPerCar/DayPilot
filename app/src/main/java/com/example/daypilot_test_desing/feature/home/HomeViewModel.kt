package com.example.daypilot_test_desing.feature.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.core.data.model.buildProgressWindow
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

    /** Suspends until this ViewModel's data has actually loaded (or failed) — used by the
     *  startup join in DayPilotNavGraph, which needs real success/failure, not just "finished". */
    suspend fun awaitLoad(): Boolean = load()

    private suspend fun load(): Boolean {
        return try {
            coroutineScope {
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
                val progressData = buildProgressWindow(history, today)
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
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load home data", e)
            false
        }
    }

    companion object {
        private const val TAG = "HomeViewModel"
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
