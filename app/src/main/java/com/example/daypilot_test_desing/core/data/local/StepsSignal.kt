package com.example.daypilot_test_desing.core.data.local

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/** StepsForegroundService owns the sensor listener now; this is how it tells the live UI
 *  (StepsViewModel) a new reading landed, without holding a direct reference to it. */
object StepsSignal {
    private val _updated = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val updated: SharedFlow<Unit> = _updated.asSharedFlow()

    fun notifyUpdated() {
        _updated.tryEmit(Unit)
    }
}
