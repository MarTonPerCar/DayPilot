package com.example.daypilot_test_desing.viewmodel.rivalry

import androidx.lifecycle.ViewModel
import com.example.daypilot_test_desing.data.repository.fake.FakeRankingRepository
import com.example.daypilot_test_desing.data.repository.fake.FakeUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RivalryViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<RivalryUiState> = _uiState.asStateFlow()

    private fun buildState(): RivalryUiState {
        val r = FakeRankingRepository
        return RivalryUiState(
            currentUserName    = FakeUserRepository.getCurrentUser().name,
            currentUserId      = r.getCurrentUserId(),
            currentUserPosition= r.getCurrentUserPosition(),
            currentUserPoints  = r.getCurrentUserPoints(),
            currentUserStreak  = r.getCurrentUserStreak(),
            ranking            = r.getRanking()
        )
    }

    fun refresh() { _uiState.value = buildState() }
}
