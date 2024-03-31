package com.cs446g15.app.detectors

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.mlkit.vision.MlKitAnalyzer
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.cs446g15.app.MainActivity
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.scan
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class DistractionDetector : Detector<DistractionDetector.Request> {
    data class Request(val lifecycleOwner: LifecycleOwner)

    val camera = MutableStateFlow<CameraController?>(null)

    @OptIn(FlowPreview::class)
    override fun launch(request: Request): Flow<String> {
        // the eyeOpenProbability at which we consider the eye "closed"
        val eyeThreshold = 0.5
        // the amount of time for which the eye(s) must be closed to trigger a violation
        val durationThreshold = 50.milliseconds
        // debounce threshold
        val debounceTime = 1000.milliseconds

        val events = MutableSharedFlow<MlKitAnalyzer.Result>(
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

        val detector = FaceDetection.getClient(
            FaceDetectorOptions.Builder()
                .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                .build()
        )
        val cameraController = LifecycleCameraController(MainActivity.appContext)
        cameraController.cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
        cameraController.setImageAnalysisAnalyzer(
            ContextCompat.getMainExecutor(MainActivity.appContext),
            MlKitAnalyzer(
                listOf(detector),
                ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL,
                ContextCompat.getMainExecutor(MainActivity.appContext)
            ) {
                events.tryEmit(it)
            }
        )
        cameraController.bindToLifecycle(request.lifecycleOwner)
        this.camera.value = cameraController

        return events
            // extract the first face if present
            .mapNotNull {
                it.getValue(detector)?.firstOrNull()
            }
            // map to true iff an eye is closed
            .map {
                val rightOpen = it.rightEyeOpenProbability ?: 0f
                val leftOpen = it.leftEyeOpenProbability ?: 0f
                rightOpen < eyeThreshold || leftOpen < eyeThreshold
            }
            // convert to the current timestamp
            .map {
                if (it) Clock.System.now() else null
            }
            // obtain the duration for which the eye(s) have been closed
            .scan(null as Pair<Instant, Duration>?) { prev, now ->
                now?.let { it to (it - (prev?.first ?: it)) }
            }
            // map to just the duration
            .map { it?.second ?: 0.milliseconds }
            // map to true iff the duration is over the threshold.
            // at this point, the stream emits a sequence of `true`
            // iff the user is currently distracted.
            .map { it > durationThreshold }
            // as with setupAccelerometer, we only want to trigger
            // on the leading edge.
            .distinctUntilChanged()
            .filter { it }
            .map {}
            .debounce(debounceTime)
            // if we get here, it corresponds to a "user has gotten distracted"
            // event. register a violation.
            .map { "Distracted driving!" }
    }
}
