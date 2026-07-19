package com.example.daypilot_test_desing.core.data.local

import com.example.daypilot_test_desing.data.supabase.supabase
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.broadcastFlow
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject

// One shared channel via addListener/removeListener, not one per ViewModel — Realtime
// delivers to only one subscriber per topic name, so duplicates would silently starve each other.
object FriendStatsBroadcast {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var channel: RealtimeChannel? = null
    private val listeners = mutableListOf<() -> Unit>()

    fun addListener(onChange: () -> Unit) {
        listeners.add(onChange)
        ensureSubscribed()
    }

    fun removeListener(onChange: () -> Unit) {
        listeners.remove(onChange)
    }

    private fun ensureSubscribed() {
        if (channel != null) return
        val uid = supabase.auth.currentUserOrNull()?.id ?: return

        scope.launch {
            supabase.realtime.setAuth(supabase.auth.currentAccessTokenOrNull())

            fun notifyAll() {
                listeners.toList().forEach { it() }
            }

            val ch = supabase.channel("friend-stats:$uid") {
                isPrivate = true
            }
            ch.broadcastFlow<JsonObject>("INSERT").onEach { notifyAll() }.launchIn(scope)
            ch.broadcastFlow<JsonObject>("UPDATE").onEach { notifyAll() }.launchIn(scope)
            ch.broadcastFlow<JsonObject>("DELETE").onEach { notifyAll() }.launchIn(scope)
            ch.subscribe()
            channel = ch
        }
    }
}
