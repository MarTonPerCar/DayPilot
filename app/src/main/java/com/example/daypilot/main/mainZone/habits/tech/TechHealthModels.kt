package com.example.daypilot.main.mainZone.habits.tech

enum class RestrictionType { APP, GROUP }

data class GroupDef(
    val id: String,
    val name: String,
    val appPkgs: Set<String> = emptySet()
)

data class Restriction(
    val id: String,
    val type: RestrictionType,
    val targetId: String,
    val displayName: String,

    val activeEnabled: Boolean,
    val activeLimitMin: Int,

    val activeNotifyEnabled: Boolean = true,
    val activeNotifyEverySec: Int = 60,

    val pendingEnabled: Boolean? = null,
    val pendingLimitMin: Int? = null,

    val pendingNotifyEnabled: Boolean? = null,
    val pendingNotifyEverySec: Int? = null,

    val pendingSinceDayKey: String? = null,

    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class AppEntry(
    val packageName: String,
    val label: String,
    val icon: Any? = null
)

data class TechHealthState(
    val hasUsageAccess: Boolean? = null,
    val appsCatalog: List<AppEntry> = emptyList(),
    val groups: List<GroupDef> = emptyList(),
    val restrictions: List<Restriction> = emptyList(),
    val usageToday: Map<String, Long> = emptyMap()
)
