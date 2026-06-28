package com.example.daypilot_test_desing.backend.fake

import com.example.daypilot_test_desing.backend.model.AppRestriction
import com.example.daypilot_test_desing.backend.model.GroupRestriction
import com.example.daypilot_test_desing.backend.repository.TechHealthRepository

object FakeTechHealthRepository : TechHealthRepository {
    private val apps   = mutableListOf<AppRestriction>()
    private val groups = mutableListOf<GroupRestriction>()

    override fun getAppRestrictions(): List<AppRestriction>   = apps.toList()
    override fun getGroupRestrictions(): List<GroupRestriction> = groups.toList()

    override fun saveApp(restriction: AppRestriction) {
        val idx = apps.indexOfFirst { it.id == restriction.id }
        if (idx >= 0) apps[idx] = restriction else apps.add(restriction)
    }

    override fun saveGroup(restriction: GroupRestriction) {
        val idx = groups.indexOfFirst { it.id == restriction.id }
        if (idx >= 0) groups[idx] = restriction else groups.add(restriction)
    }

    override fun toggleRestriction(id: String, enabled: Boolean) {
        val idx = apps.indexOfFirst { it.id == id }
        if (idx >= 0) apps[idx] = apps[idx].copy(isEnabled = enabled)
    }

    override fun deleteRestriction(id: String) { apps.removeAll { it.id == id } }

    override fun toggleGroup(id: String, enabled: Boolean) {
        val idx = groups.indexOfFirst { it.id == id }
        if (idx >= 0) groups[idx] = groups[idx].copy(isEnabled = enabled)
    }

    override fun deleteGroup(id: String) { groups.removeAll { it.id == id } }

    override fun updateUsage(id: String, usedMinutes: Int) {
        val idx = apps.indexOfFirst { it.id == id }
        if (idx >= 0) apps[idx] = apps[idx].copy(usedMinutesToday = usedMinutes)
    }
}
