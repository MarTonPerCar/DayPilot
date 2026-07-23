package com.example.daypilot_test_desing.core.connectivity

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

/** Global "no internet" flag, mirrored across the whole app the same way a single Riverpod
 *  provider does on the Flutter side — any ViewModel can flip it, and a single Compose overlay
 *  at the nav-graph root reacts to it regardless of which screen is currently showing. */
object ConnectivityState {
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    fun setOffline(value: Boolean) {
        if (_isOffline.value != value) _isOffline.value = value
    }

    /** Actively probes for a real connection before a ViewModel attempts a network call, instead
     *  of only reacting after it fails. A dead/blackholed connection can otherwise leave a
     *  request hanging or let it silently succeed later once connectivity returns, rather than
     *  failing promptly — this check is what avoids that. */
    suspend fun ensureOnline(): Boolean {
        val hasInternet = ConnectivityService.hasInternetConnection()
        if (!hasInternet) setOffline(true)
        return hasInternet
    }
}

/** Kept as a secondary safety net for reactive catch blocks — the proactive [ConnectivityState.ensureOnline]
 *  check above is what actually prevents most offline attempts from being made at all. */
fun isConnectivityError(e: Throwable): Boolean = e is IOException
