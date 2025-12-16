package com.example.daypilot.main.mainZone.habits.steps

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

class StepsSensor(private val context: Context) {

    fun counterFlow(): Flow<Long> = callbackFlow {
        val sm = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if (sensor == null) {
            close(IllegalStateException("Este dispositivo no soporta TYPE_STEP_COUNTER"))
            return@callbackFlow
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val v = event.values.firstOrNull()?.toLong() ?: return
                trySend(v).isSuccess
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_UI)
        awaitClose { sm.unregisterListener(listener) }
    }


    suspend fun readCounterOnce(timeoutMs: Long = 2500L): Long? {
        return withTimeoutOrNull(timeoutMs) {
            counterFlow().first()
        }
    }
}