package com.cs446g15.app.util

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion

/** Obtain a hot flow of sensor events of type `type` */
fun SensorManager.getEventFlow(
    type: Int,
    bufferSize: Int = 10,
    samplingPeriod: Int = SensorManager.SENSOR_DELAY_NORMAL
): Flow<SensorEvent> {
    val sensor = getDefaultSensor(type) ?: return flowOf()
    val sensorEvents = MutableSharedFlow<SensorEvent>(
        extraBufferCapacity = bufferSize,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            sensorEvents.tryEmit(event ?: return)
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
    registerListener(listener, sensor, samplingPeriod)
    return sensorEvents
        .filter { it.sensor.type == type }
        .onCompletion { unregisterListener(listener) }
}
