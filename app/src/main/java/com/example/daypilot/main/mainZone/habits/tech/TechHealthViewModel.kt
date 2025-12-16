package com.example.daypilot.main.mainZone.habits.tech

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class TechHealthViewModel(private val appContext: Context) : ViewModel() {

    private val store = TechHealthLocalStore(appContext)

    private val _hasAccess = MutableStateFlow<Boolean?>(null)
    private val _apps = MutableStateFlow<List<AppEntry>>(emptyList())

    private val _usage: StateFlow<Map<String, Long>> =
        store.usageTodayFlow.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    private var lastDayKeySeen: String? = null

    private val storeFlow = store.flow

    val state: StateFlow<TechHealthState> = combine(
        _hasAccess, _apps, _usage, storeFlow
    ) { access, apps, usage, pair ->
        val (groups, restrictions) = pair
        TechHealthState(
            hasUsageAccess = access,
            appsCatalog = apps,
            groups = groups,
            restrictions = restrictions,
            usageToday = usage
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, TechHealthState())

    fun bootstrap() {
        if (lastDayKeySeen == null) lastDayKeySeen = todayKey()

        viewModelScope.launch {
            if (_apps.value.isEmpty()) _apps.value = AppCatalog.loadLaunchableApps(appContext)
            refreshAccess()
            rolloverIfNeeded()
        }
    }

    fun refreshAccess() {
        _hasAccess.value = UsageReader.hasUsageAccess(appContext)
    }

    private fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(System.currentTimeMillis())

    fun rolloverIfNeeded() {
        viewModelScope.launch {
            val key = todayKey()
            lastDayKeySeen = key
            store.rolloverIfNewDay(key)
        }
    }

    // ---------- Groups ----------
    fun createGroup(name: String): String {
        val id = UUID.randomUUID().toString()
        viewModelScope.launch { store.createGroup(id, name) }
        return id
    }

    fun updateGroupApps(id: String, pkgs: Set<String>) =
        viewModelScope.launch { store.setGroupApps(id, pkgs) }

    // ---------- Restrictions ----------
    fun createRestriction(r: Restriction) = viewModelScope.launch { store.addRestriction(r) }

    fun setRestrictionPending(
        base: Restriction,
        newEnabled: Boolean? = null,
        newLimitMin: Int? = null,
        newDisplayName: String? = null,
        newNotifyEnabled: Boolean? = null,
        newNotifyEverySec: Int? = null
    ) {
        val key = todayKey()

        val updated = base.copy(
            displayName = newDisplayName ?: base.displayName,

            pendingEnabled = newEnabled ?: base.pendingEnabled,
            pendingLimitMin = newLimitMin ?: base.pendingLimitMin,

            pendingNotifyEnabled = newNotifyEnabled ?: base.pendingNotifyEnabled,
            pendingNotifyEverySec = newNotifyEverySec ?: base.pendingNotifyEverySec,

            pendingSinceDayKey = key
        )

        viewModelScope.launch { store.updateRestriction(updated) }
    }

    fun scheduleDisableTomorrow(base: Restriction) {
        setRestrictionPending(base, newEnabled = false)
    }

    // helpers
    fun appLabel(pkg: String): String =
        state.value.appsCatalog.firstOrNull { it.packageName == pkg }?.label ?: pkg

    fun appIcon(pkg: String): Any? =
        state.value.appsCatalog.firstOrNull { it.packageName == pkg }?.icon

    fun groupName(id: String): String =
        state.value.groups.firstOrNull { it.id == id }?.name ?: "Grupo"

    fun groupApps(id: String): Set<String> =
        state.value.groups.firstOrNull { it.id == id }?.appPkgs ?: emptySet()
}