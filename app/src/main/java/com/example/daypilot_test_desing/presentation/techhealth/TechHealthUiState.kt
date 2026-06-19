package com.example.daypilot_test_desing.presentation.techhealth

import com.example.daypilot_test_desing.data.model.AppRestriction
import com.example.daypilot_test_desing.data.model.GroupRestriction

data class TechHealthUiState(
    val appRestrictions: List<AppRestriction> = emptyList(),
    val groupRestrictions: List<GroupRestriction> = emptyList()
)
