package com.example.daypilot_test_desing.viewmodel.techhealth

import com.example.daypilot_test_desing.backend.model.AppRestriction
import com.example.daypilot_test_desing.backend.model.GroupRestriction

data class TechHealthUiState(
    val appRestrictions: List<AppRestriction> = emptyList(),
    val groupRestrictions: List<GroupRestriction> = emptyList(),
    val hasUsagePermission: Boolean = true,
    val techHealthPointEarned: Boolean = false
)
