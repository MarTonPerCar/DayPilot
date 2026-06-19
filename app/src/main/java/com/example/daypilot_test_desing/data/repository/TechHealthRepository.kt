package com.example.daypilot_test_desing.data.repository

import com.example.daypilot_test_desing.data.model.AppRestriction
import com.example.daypilot_test_desing.data.model.GroupRestriction

interface TechHealthRepository {
    fun getAppRestrictions(): List<AppRestriction>
    fun getGroupRestrictions(): List<GroupRestriction>
    fun saveApp(restriction: AppRestriction)
    fun saveGroup(restriction: GroupRestriction)
    fun toggleRestriction(id: String, enabled: Boolean)
    fun deleteRestriction(id: String)
    fun toggleGroup(id: String, enabled: Boolean)
    fun deleteGroup(id: String)
}
