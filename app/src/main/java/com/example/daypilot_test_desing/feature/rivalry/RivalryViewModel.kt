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

    private var loadedAt = 0L

    init { viewModelScope.launch { load() } }

    fun refresh(): Job = viewModelScope.launch {
        if (System.currentTimeMillis() - loadedAt < CACHE_TTL_MS) return@launch
        load()
    }

    fun invalidate() { loadedAt = 0L }

    private suspend fun load() {
        try {
            val ranking  = repo.getRanking()
            val uid      = repo.getCurrentUserId()
            // friends_ranking VIEW may not return the current user due to
            // security_invoker + streaks_own RLS interaction; fall back to
            // direct table queries which always work for the own user.
            val me       = ranking.firstOrNull { it.id == uid }
                           ?: repo.getCurrentUserData()
            val position = ranking.indexOfFirst { it.id == uid }
                           .let { if (it >= 0) it + 1 else ranking.size + 1 }
            _uiState.value = RivalryUiState(
                currentUserName     = me?.name ?: "",
                currentUserId       = uid,
                currentUserPosition = position,
                currentUserPoints   = me?.points ?: 0,
                currentUserStreak   = me?.streak ?: 0,
                currentUserLevel    = me?.level ?: 1,
                ranking             = ranking
            )
            loadedAt = System.currentTimeMillis()
        } catch (_: Exception) { }
    }

    companion object {
        private const val CACHE_TTL_MS = 2 * 60_000L

        fun factory(repo: RankingRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    RivalryViewModel(repo) as T
            }
    }
}
