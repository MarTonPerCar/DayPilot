package com.example.daypilot_test_desing.viewmodel.rivalry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.backend.repository.RankingRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RivalryViewModel(private val repo: RankingRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RivalryUiState())
    val uiState: StateFlow<RivalryUiState> = _uiState.asStateFlow()

    init { viewModelScope.launch { load() } }

    fun refresh(): Job = viewModelScope.launch { load() }

    private suspend fun load() {
        try {
            val ranking  = repo.getRanking()
            val uid      = repo.getCurrentUserId()
            val position = repo.getCurrentUserPosition()
            val points   = repo.getCurrentUserPoints()
            val streak   = repo.getCurrentUserStreak()
            val name     = ranking.firstOrNull { it.id == uid }?.name ?: ""
            _uiState.value = RivalryUiState(
                currentUserName     = name,
                currentUserId       = uid,
                currentUserPosition = position,
                currentUserPoints   = points,
                currentUserStreak   = streak,
                ranking             = ranking
            )
        } catch (_: Exception) { }
    }

    companion object {
        fun factory(repo: RankingRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    RivalryViewModel(repo) as T
            }
    }
}
