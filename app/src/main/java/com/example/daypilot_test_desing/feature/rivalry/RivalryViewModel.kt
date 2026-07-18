package com.example.daypilot_test_desing.feature.rivalry

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.core.cache.SessionCache
import com.example.daypilot_test_desing.core.data.repository.RankingRepository
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

    fun invalidate() {
        SessionCache.ranking.value    = null
        SessionCache.rankingFetchedAt = 0L
    }

    /** Suspends until this ViewModel's data has actually loaded (or failed) — used by the
     *  startup join in DayPilotNavGraph, which needs real success/failure, not just "finished". */
    suspend fun awaitLoad(): Boolean = load()

    private suspend fun load(): Boolean {
        return try {
            val ranking  = repo.getRanking()  // cache-first with 5min TTL
            val uid      = repo.getCurrentUserId()
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
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load rivalry data", e)
            false
        }
    }

    companion object {
        private const val TAG = "RivalryViewModel"

        fun factory(repo: RankingRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    RivalryViewModel(repo) as T
            }
    }
}
