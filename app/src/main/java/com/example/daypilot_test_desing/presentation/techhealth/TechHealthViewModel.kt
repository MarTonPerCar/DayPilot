package com.example.daypilot_test_desing.presentation.techhealth

import androidx.lifecycle.ViewModel
import com.example.daypilot_test_desing.data.model.AppRestriction
import com.example.daypilot_test_desing.data.model.GroupRestriction
import com.example.daypilot_test_desing.data.repository.fake.FakeTechHealthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TechHealthViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(buildState())
    val uiState: StateFlow<TechHealthUiState> = _uiState.asStateFlow()

    private fun buildState() = TechHealthUiState(
        appRestrictions  = FakeTechHealthRepository.getAppRestrictions(),
        groupRestrictions= FakeTechHealthRepository.getGroupRestrictions()
    )

    private fun refresh() { _uiState.value = buildState() }

    fun saveApp(restriction: AppRestriction) {
        FakeTechHealthRepository.saveApp(restriction)
        refresh()
    }

    fun saveGroup(restriction: GroupRestriction) {
        FakeTechHealthRepository.saveGroup(restriction)
        refresh()
    }

    fun toggleRestriction(id: String, enabled: Boolean) {
        FakeTechHealthRepository.toggleRestriction(id, enabled)
        refresh()
    }

    fun deleteRestriction(id: String) {
        FakeTechHealthRepository.deleteRestriction(id)
        refresh()
    }

    fun toggleGroup(id: String, enabled: Boolean) {
        FakeTechHealthRepository.toggleGroup(id, enabled)
        refresh()
    }

    fun deleteGroup(id: String) {
        FakeTechHealthRepository.deleteGroup(id)
        refresh()
    }
}
