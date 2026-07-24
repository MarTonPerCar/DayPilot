package com.example.daypilot_test_desing.support

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle

/** ConnectivityState.ensureOnline() (added throughout this branch's ViewModels) does a real
 *  DNS lookup on the real Dispatchers.IO thread pool — a background dispatcher a virtual test
 *  dispatcher has no influence over. advanceUntilIdle() alone can return before that real
 *  lookup finishes, leaving assertions racing a still-in-flight coroutine. This gives the real
 *  background work a short, bounded slice of actual wall-clock time between virtual advances,
 *  which is enough since the lookup resolves in well under a second on a reachable network. */
@OptIn(ExperimentalCoroutinesApi::class)
fun TestScope.realAdvanceUntilIdle(maxRealWaitMs: Long = 1_500) {
    advanceUntilIdle()
    val deadline = System.currentTimeMillis() + maxRealWaitMs
    while (System.currentTimeMillis() < deadline) {
        Thread.sleep(20)
        advanceUntilIdle()
    }
}
