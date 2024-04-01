package com.cs446g15.app.detectors

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlin.time.Duration.Companion.seconds

object SpeedingDetector : Detector<SpeedingDetector.Request> {
    data class Request(val locationProvider: FusedLocationProviderClient)

    @OptIn(FlowPreview::class)
    @SuppressLint("MissingPermission")
    override fun launch(request: Request): Flow<String> {
        val locationCadence = 1.seconds
        val debounceThreshold = 5.seconds
        val speedLimit = 60.0 // km/h

        val locationRequest = LocationRequest.Builder(locationCadence.inWholeMilliseconds).build()

        val events = MutableSharedFlow<Location>(
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

        val listener = LocationListener { events.tryEmit(it) }

        request.locationProvider.requestLocationUpdates(
            locationRequest,
            listener,
            Looper.getMainLooper()
        )

        return events
            .onCompletion {
                request.locationProvider.removeLocationUpdates(listener)
            }
            // Speed converted to km/h
            .map { it.speed * 3.6 }
            .map { it > speedLimit }
            .distinctUntilChanged()
            .filter { it }
            .map {}
            .debounce(debounceThreshold)
            .map { "Speeding!" }
    }
}
