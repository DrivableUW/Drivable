package com.cs446g15.app.util

import android.location.Location
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@RequiresPermission(anyOf = [
    "android.permission.ACCESS_COARSE_LOCATION",
    "android.permission.ACCESS_FINE_LOCATION"
])
suspend fun FusedLocationProviderClient.getCurrentLocation(
    builder: KtCurrentLocationRequest.() -> Unit = {},
): Location {
    val request = KtCurrentLocationRequest().apply(builder)
    val rawRequest = CurrentLocationRequest.Builder()
        .setDurationMillis(request.duration.inWholeMilliseconds)
        .setGranularity(request.granularity.raw)
        .setPriority(request.priority.raw)
        .setMaxUpdateAgeMillis(request.maxUpdateAge.inWholeMilliseconds)
        .build()
    val tokenSource = CancellationTokenSource()
    return getCurrentLocation(rawRequest, tokenSource.token).await(tokenSource)
}

data class KtCurrentLocationRequest(
    var maxUpdateAge: Duration = 10.seconds,
    var duration: Duration = Long.MAX_VALUE.milliseconds,
    var granularity: KtGranularity = KtGranularity.PERMISSION_LEVEL,
    var priority: KtPriority = KtPriority.BALANCED_POWER_ACCURACY
)

enum class KtGranularity(val raw: Int) {
    PERMISSION_LEVEL(Granularity.GRANULARITY_PERMISSION_LEVEL),
    COARSE(Granularity.GRANULARITY_COARSE),
    FINE(Granularity.GRANULARITY_FINE),
}

enum class KtPriority(val raw: Int) {
    HIGH_ACCURACY(Priority.PRIORITY_HIGH_ACCURACY),
    BALANCED_POWER_ACCURACY(Priority.PRIORITY_BALANCED_POWER_ACCURACY),
    LOW_POWER(Priority.PRIORITY_LOW_POWER),
    PASSIVE(Priority.PRIORITY_PASSIVE),
}
