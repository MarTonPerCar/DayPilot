package com.example.daypilot_test_desing.viewmodel.profile

import androidx.lifecycle.ViewModel
import com.example.daypilot_test_desing.backend.model.TimeZoneRegion
import com.example.daypilot_test_desing.backend.fake.FakeProgressRepository
import com.example.daypilot_test_desing.backend.fake.FakeUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private fun buildState(): ProfileUiState {
        val user     = FakeUserRepository.getCurrentUser()
        val summary  = FakeUserRepository.getWeeklySummary()
        val progress = FakeProgressRepository
        return ProfileUiState(
            name            = user.name,
            username        = user.username,
            email           = user.email,
            memberSince     = user.memberSince,
            level           = user.level,
            totalPoints     = FakeProgressRepository.getMonthlyPoints(),
            currentStreak   = user.currentStreak,
            longestStreak   = user.longestStreak,
            rankingPosition = FakeProgressRepository.getRankingPositionSync(),
            pointsToday     = progress.getPointsToday(),
            pointsFromTasks = progress.getPointsFromTasks(),
            pointsFromSteps = progress.getPointsFromSteps(),
            pointsFromHabits= progress.getPointsFromHabits(),
            pointsFromTimers= progress.getPointsFromTimers(),
            avatarUrl       = user.avatarUrl,
            weeklySummary   = summary
        )
    }

    fun refresh() { _uiState.value = buildState() }

    fun updateProfile(name: String, username: String, region: TimeZoneRegion) {
        FakeUserRepository.updateProfile(name, username, region)
        _uiState.value = buildState()
    }
}
