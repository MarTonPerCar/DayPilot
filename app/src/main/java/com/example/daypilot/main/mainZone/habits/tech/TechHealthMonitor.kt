package com.example.daypilot.main.mainZone.habits.tech

import android.content.Context
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Locale

class TechHealthMonitor(private val context: Context) {

    private val store = TechHealthLocalStore(context)

    private var lastTickAt: Long = 0L
    private var lastTopPkg: String? = null

    private val lastNotifyAt = mutableMapOf<String, Long>()
    private val lastOverState = mutableMapOf<String, Boolean>()
    private var lastDayKeySeen: String? = null

    private fun todayKey(): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(System.currentTimeMillis())

    suspend fun tick(): Boolean {
        if (!UsageReader.hasUsageAccess(context)) return false

        val now = System.currentTimeMillis()
        val key = todayKey()

        if (lastDayKeySeen != null && lastDayKeySeen != key) {
            lastNotifyAt.clear()
            lastOverState.clear()
            store.resetUsageForDay(key)
        }
        lastDayKeySeen = key

        store.rolloverIfNewDay(key)
        val (groups, restrictions) = store.flow.first()
        if (restrictions.none { it.activeEnabled }) return false

        // 1) Suma SIEMPRE el delta al último paquete conocido
        if (lastTickAt != 0L && lastTopPkg != null) {
            val delta = (now - lastTickAt).coerceIn(0L, 60_000L)
            store.addUsageDelta(key, lastTopPkg!!, delta)
        }
        lastTickAt = now

        // 2) Intenta leer top actual (si es null, mantenemos el anterior)
        val topNow = UsageReader.currentTopPackage(context)
        if (topNow != null) lastTopPkg = topNow

        val currentPkg = lastTopPkg ?: return true  // si aún no sabemos nada, no hacemos notifs
        android.util.Log.d("TechHealth", "top=$currentPkg")

        val usageMs = store.usageTodayFlow.first()

        fun usedMsForGroup(groupId: String): Long {
            val g = groups.firstOrNull { it.id == groupId } ?: return 0L
            return g.appPkgs.sumOf { pkg -> usageMs[pkg] ?: 0L }
        }

        val now2 = System.currentTimeMillis()

        // usa currentPkg en vez de "top"
        restrictions.filter { it.activeEnabled }.forEach { r ->
            val isCurrentlyUsingTarget = when (r.type) {
                RestrictionType.APP -> (currentPkg == r.targetId)
                RestrictionType.GROUP -> groups.firstOrNull { it.id == r.targetId }?.appPkgs?.contains(currentPkg) == true
            }

            if (!isCurrentlyUsingTarget) {
                lastOverState[r.id] = false
                return@forEach
            }

            val used = when (r.type) {
                RestrictionType.APP -> usageMs[r.targetId] ?: 0L
                RestrictionType.GROUP -> usedMsForGroup(r.targetId)
            }

            val limitMs = r.activeLimitMin * 60_000L
            val over = used >= limitMs

            if (!r.activeNotifyEnabled) {
                lastOverState[r.id] = over
                return@forEach
            }

            val repeatEveryMs = (r.activeNotifyEverySec.coerceIn(5, 60) * 1000L)
            val wasOver = lastOverState[r.id] ?: false
            val last = lastNotifyAt[r.id] ?: 0L
            val canRepeat = (now2 - last) >= repeatEveryMs

            if (over && (!wasOver || canRepeat)) {
                val usedMin = (used / 60_000L).toInt()
                TechHealthNotifier.notifyLimit(
                    context = context,
                    title = "Límite alcanzado",
                    text = "${r.displayName}: $usedMin / ${r.activeLimitMin} min",
                    stableId = "tech_limit_${r.id}"
                )
                lastNotifyAt[r.id] = now2
            }

            lastOverState[r.id] = over
        }

        return true
    }
}