package com.example.daypilot_test_desing.viewmodel.habits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.daypilot_test_desing.backend.repository.StepsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HabitsViewModel(private val stepsRepo: StepsRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<HabitsUiState> = _uiState.asStateFlow()

    private fun buildState(): HabitsUiState {
        val earned = stepsRepo.getPointsEarned()
        return HabitsUiState(
            currentSteps     = stepsRepo.getCurrentSteps(),
            goalSteps        = stepsRepo.getGoalSteps(),
            pointsEarned     = earned,
            pointsRemaining  = maxOf(0, 60 - earned),
            goalChangedToday = !stepsRepo.canChangeGoal(),
            pendingGoal      = stepsRepo.getPendingGoal()
        )
    }

    fun refresh() { _uiState.value = buildState() }

    fun configureGoal(newGoal: Int) {
        stepsRepo.configureGoal(newGoal)
        _uiState.value = buildState()
    }

    companion object {
        fun factory(repo: StepsRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    HabitsViewModel(repo) as T
            }
    }
}
