package com.cs446g15.app.detectors

import android.hardware.Sensor
import android.hardware.SensorManager
import com.cs446g15.app.MainActivity
import com.cs446g15.app.util.getEventFlow
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlin.time.Duration.Companion.milliseconds

object AccelerationDetector : Detector {
    @OptIn(FlowPreview::class)
    override fun launch(): Flow<String> {
        val threshold = 3 // G force threshold
        val debounceTime = 1000.milliseconds

        val sensorManager = MainActivity.appContext.getSystemService(SensorManager::class.java) ?: return emptyFlow()
        return sensorManager.getEventFlow(Sensor.TYPE_ACCELEROMETER)
            // low-pass filter to discount gravity
            .scan(Pair(listOf(0f, 0f, 0f), listOf(0f, 0f, 0f))) { (prevGravity, prevAccel), event ->
                // adapted for Kotlin Flows (reactive), based on
                // https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-accel
                val alpha = 0.8f

                val gravity = prevGravity.toMutableList()
                val accel = prevAccel.toMutableList()

                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

                accel[0] = event.values[0] - gravity[0]
                accel[1] = event.values[1] - gravity[1]
                accel[2] = event.values[2] - gravity[2]

                Pair(gravity, accel)
            }
            // accel
            .map { it.second }
            // accel magnitude
            .map { it[0] * it[0] + it[1] * it[1] + it[2] * it[2] }
            // iff over threshold
            .map { it > (threshold * threshold) }
            // remove duplicate events (only trigger on leading/trailing edge)
            .distinctUntilChanged()
            // remove false (only trigger on leading edge)
            .filter { it }
            // we only have true now so map to Unit
            .map {}
            // debounce to remove noise
            .debounce(debounceTime)
            // we'll always receive an event on startup, discard it
            .drop(1)
            // register as a violation
            .map { "Reckless maneuvering!" }
    }
}
