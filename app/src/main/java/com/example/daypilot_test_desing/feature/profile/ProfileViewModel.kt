package com.example.daypilot_test_desing.feature.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.daypilot_test_desing.core.data.model.TimeZoneRegion
import com.example.daypilot_test_desing.core.data.repository.ProgressRepository
import com.example.daypilot_test_desing.core.data.repository.UserRepository
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

    init { viewModelScope.launch { load() } }

    fun refresh(): Job = viewModelScope.launch { load() }

    fun invalidate() { /* cache freshness is managed at the repo/SessionCache layer */ }

    private suspend fun load() {
        try {
            val user    = userRepo.getCurrentUser()    // cache-first
            val summary = userRepo.getWeeklySummary()
            val today   = progressRepo.getTodayProgress()  // cache-first
            val ranking = progressRepo.getRankingPosition()  // uses cached ranking if available
            _uiState.value = ProfileUiState(
                name                 = user.name,
                username             = user.username,
                email                = user.email,
                memberSince          = user.memberSince,
                level                = user.level,
                totalPoints          = user.totalPoints,
                pointsToNextLevel    = user.pointsToNextLevel,
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
        } catch (_: Exception) { }
    }

    fun updateProfile(name: String, username: String, region: TimeZoneRegion) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSavingProfile = true, profileSaveError = false)
            try {
                userRepo.updateProfile(name, username, region)  // updates SessionCache.userProfile
                load()  // re-reads from cache (instant), refreshes UiState
                _uiState.value = _uiState.value.copy(isSavingProfile = false, profileSaveSuccess = true)
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(isSavingProfile = false, profileSaveError = true)
            }
        }
    }

    fun clearProfileSaveError() {
        _uiState.value = _uiState.value.copy(profileSaveError = false)
    }

    fun clearProfileSaveSuccess() {
        _uiState.value = _uiState.value.copy(profileSaveSuccess = false)
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
            else userRepo.uploadAvatar(bytes, mimeType) != null  // updates SessionCache.userProfile.avatarUrl
        } catch (_: Exception) { false }

        if (success) {
            load()  // re-reads from cache, picks up new avatarUrl
            _uiState.value = _uiState.value.copy(isUploadingAvatar = false)
        } else {
            _uiState.value = _uiState.value.copy(isUploadingAvatar = false, avatarUploadError = true)
        }
    }

    fun clearAvatarError() {
        _uiState.value = _uiState.value.copy(avatarUploadError = false)
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
