package com.example.daypilot_test_desing.data.supabase

import com.example.daypilot_test_desing.BuildConfig
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.realtime
import io.github.jan.supabase.storage.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

val supabase = createSupabaseClient(
    supabaseUrl = BuildConfig.SUPABASE_URL,
    supabaseKey = BuildConfig.SUPABASE_KEY
) {
    install(Postgrest)
    install(Auth)
    install(Realtime)
    install(Storage)
}

/**
 * Realtime channels are cached by topic inside the client and outlive any single ViewModel
 * instance (e.g. across Activity/back-stack-entry recreation). supabase-kt throws
 * IllegalStateException if postgresChangeFlow is registered on a channel that a previous
 * subscriber already joined, so callers must use this instead of `supabase.channel(id)`
 * whenever they're about to register new listeners.
 */
suspend fun freshRealtimeChannel(channelId: String): RealtimeChannel {
    val channel = supabase.channel(channelId)
    if (channel.status.value == RealtimeChannel.Status.UNSUBSCRIBED) return channel
    supabase.realtime.removeChannel(channel)
    return supabase.channel(channelId)
}

/** Unsubscribes and forgets [channel] so its topic can be freshly rejoined later. */
suspend fun removeRealtimeChannel(channel: RealtimeChannel?) {
    if (channel != null) runCatching { supabase.realtime.removeChannel(channel) }
}

/**
 * ViewModel.onCleared() runs after viewModelScope's Job is already cancelled, so launching
 * cleanup work on viewModelScope from onCleared silently never runs. Realtime channel
 * teardown needs a scope that outlives the ViewModel it belongs to.
 */
val realtimeCleanupScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
