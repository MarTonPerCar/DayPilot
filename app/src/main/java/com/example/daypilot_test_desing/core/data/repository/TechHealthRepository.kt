package com.example.daypilot_test_desing.core.data.repository

import com.example.daypilot_test_desing.core.data.model.AppRestriction
import com.example.daypilot_test_desing.core.data.model.GroupRestriction

interface TechHealthRepository {
    fun getAppRestrictions(): List<AppRestriction>
    fun getGroupRestrictions(): List<GroupRestriction>
    fun saveApp(restriction: AppRestriction)
    fun saveGroup(restriction: GroupRestriction)
    fun toggleRestriction(id: String, enabled: Boolean)
    fun toggleGroup(id: String, enabled: Boolean)
    fun deleteRestriction(id: String)
    fun deleteGroup(id: String)
    fun updateUsage(id: String, usedMinutes: Int)
    fun updateGroupUsage(id: String, usedMinutes: Int)
    fun updateGroupAppUsage(groupId: String, packageName: String, usedMinutes: Int)
    fun markViolated(id: String)
    fun markGroupViolated(id: String)
    // Swaps a client-generated placeholder group id for the real Supabase row id.
    fun replaceGroupId(oldId: String, newId: String)
}
