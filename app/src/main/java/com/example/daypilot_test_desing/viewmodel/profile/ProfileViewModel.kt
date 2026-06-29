package com.example.daypilot_test_desing.viewmodel.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.backend.model.TimeZoneRegion
import com.example.daypilot_test_desing.backend.repository.ProgressRepository
import com.example.daypilot_test_desing.backend.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val userRepo: UserRepository,
    private val progressRepo: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init { viewModelScope.launch { load() } }

    fun refresh(): Job = viewModelScope.launch { load() }

    private suspend fun load() {
        try {
            val user    = userRepo.getCurrentUser()
            val summary = userRepo.getWeeklySummary()
            val today   = progressRepo.getTodayProgress()
            val ranking = progressRepo.getRankingPosition()
            _uiState.value = ProfileUiState(
                name             = user.name,
                username         = user.username,
                email            = user.email,
                memberSince      = user.memberSince,
                level            = user.level,
                totalPoints      = user.totalPoints,
                currentStreak    = user.currentStreak,
                longestStreak    = user.longestStreak,
                rankingPosition  = ranking,
                pointsToday      = today.totalPoints,
                pointsFromTasks  = today.tasksPoints,
                pointsFromSteps  = today.stepsPoints,
                pointsFromHabits = today.techHealthPoints + today.wellnessPoints,
                pointsFromTimers = today.timerPoints,
                avatarUrl        = user.avatarUrl,
                weeklySummary    = summary
            )
        } catch (_: Exception) { }
    }

    fun updateProfile(name: String, username: String, region: TimeZoneRegion) {
        viewModelScope.launch {
            userRepo.updateProfile(name, username, region)
            load()
        }
    }

    companion object {
        fun factory(userRepo: UserRepository, progressRepo: ProgressRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ProfileViewModel(userRepo, progressRepo) as T
            }
    }
}
