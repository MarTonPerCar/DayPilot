package com.example.daypilot_test_desing.feature.techhealth

import com.example.daypilot_test_desing.core.data.model.AppRestriction
import com.example.daypilot_test_desing.core.data.model.GroupRestriction

data class TechHealthUiState(
    val appRestrictions: List<AppRestriction> = emptyList(),
    val groupRestrictions: List<GroupRestriction> = emptyList(),
    val hasUsagePermission: Boolean = true,
    val hasAccessibilityPermission: Boolean = false,
    val techHealthPointEarned: Boolean = false,
    val activeRestrictionCount: Int = 0
)
