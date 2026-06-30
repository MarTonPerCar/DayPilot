package com.example.daypilot_test_desing.viewmodel.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.backend.model.TimeZoneRegion
import com.example.daypilot_test_desing.backend.repository.ProgressRepository
import com.example.daypilot_test_desing.backend.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileViewModel(
    private val userRepo: UserRepository,
    private val progressRepo: ProgressRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var loadedAt = 0L

    init { viewModelScope.launch { load() } }

    fun refresh(): Job = viewModelScope.launch {
        if (System.currentTimeMillis() - loadedAt < CACHE_TTL_MS) return@launch
        load()
    }

    fun invalidate() { loadedAt = 0L }

    private suspend fun load() {
        try {
            val user    = userRepo.getCurrentUser()
            val summary = userRepo.getWeeklySummary()
            val today   = progressRepo.getTodayProgress()
            val ranking = progressRepo.getRankingPosition()
            _uiState.value = ProfileUiState(
                name                 = user.name,
                username             = user.username,
                email                = user.email,
                memberSince          = user.memberSince,
                level                = user.level,
                totalPoints          = user.totalPoints,
                currentStreak        = user.currentStreak,
                longestStreak        = user.longestStreak,
                rankingPosition      = ranking,
                pointsToday          = today.totalPoints,
                pointsFromTasks      = today.tasksPoints,
                pointsFromSteps      = today.stepsPoints,
                pointsFromHabits     = today.techHealthPoints + today.wellnessPoints,
                pointsFromTimers     = today.timerPoints,
                stepsToday           = today.steps,
                tasksCompletedToday  = today.tasksCompleted,
                avatarUrl            = user.avatarUrl,
                weeklySummary        = summary
            )
            loadedAt = System.currentTimeMillis()
        } catch (_: Exception) { }
    }

    fun updateProfile(name: String, username: String, region: TimeZoneRegion) {
        viewModelScope.launch {
            userRepo.updateProfile(name, username, region)
            invalidate()
            load()
        }
    }

    fun uploadAvatar(uri: Uri, context: Context): Job = viewModelScope.launch {
        _uiState.value = _uiState.value.copy(isUploadingAvatar = true, avatarUploadError = false)
        val success = try {
            val (bytes, mimeType) = withContext(Dispatchers.IO) {
                val b = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                val m = context.contentResolver.getType(uri) ?: "image/jpeg"
                Pair(b, m)
            }
            if (bytes == null) false
            else userRepo.uploadAvatar(bytes, mimeType) != null
        } catch (_: Exception) { false }

        if (success) {
            invalidate()
            load()
        } else {
            _uiState.value = _uiState.value.copy(isUploadingAvatar = false, avatarUploadError = true)
        }
    }

    fun clearAvatarError() {
        _uiState.value = _uiState.value.copy(avatarUploadError = false)
    }

    companion object {
        private const val CACHE_TTL_MS = 2 * 60_000L

        fun factory(userRepo: UserRepository, progressRepo: ProgressRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ProfileViewModel(userRepo, progressRepo) as T
            }
    }
}
