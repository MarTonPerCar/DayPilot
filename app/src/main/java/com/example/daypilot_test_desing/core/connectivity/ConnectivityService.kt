package com.example.daypilot_test_desing.core.connectivity

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.net.InetAddress

object ConnectivityService {

    /** Real reachability probe (DNS resolution of a well-known host), not just "attached to a
     *  network" — a device can be connected to Wi-Fi with no actual upstream internet. */
    suspend fun hasInternetConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            withTimeoutOrNull(4_000L) {
                !InetAddress.getByName("one.one.one.one").hostAddress.isNullOrEmpty()
            } ?: false
        } catch (e: IOException) {
            false
        }
    }
}
