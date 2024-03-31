package com.cs446g15.app.detectors

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlin.math.log10
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.seconds

object NoiseDetector : Detector<Unit> {
    @SuppressLint("MissingPermission")
    @OptIn(FlowPreview::class)
    override fun launch(request: Unit): Flow<String> {
        val sampleRate = 44100
        val audioSource = MediaRecorder.AudioSource.MIC
        val channelConfig = AudioFormat.CHANNEL_IN_MONO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

        val audioThreshold = 70.0
        val debounceThreshold = 1.seconds

        val events = MutableSharedFlow<Double>(
            extraBufferCapacity = 10,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
        var complete = false

        CoroutineScope(Dispatchers.Default).launch {
            val audioRecord = AudioRecord(audioSource, sampleRate, channelConfig, audioFormat, bufferSizeInBytes)
            val audioData = ShortArray(bufferSizeInBytes)

            try {
                audioRecord.startRecording()
                while (!complete) {
                    val readSize = audioRecord.read(audioData, 0, bufferSizeInBytes)
                    if (readSize > 0) {
                        val loudness = calculateLoudness(audioData)
                        events.tryEmit(loudness)
                    }
                }
            } finally {
                audioRecord.stop()
                audioRecord.release()
            }
        }

        return events
            .map { it > audioThreshold }
            .distinctUntilChanged()
            .filter { it }
            .map {}
            .debounce(debounceThreshold)
            .onCompletion { complete = true }
            .map { "Excessive Noise!" }
    }

    private fun calculateLoudness(audioData: ShortArray): Double {
        var sum = 0.0
        for (sample in audioData) {
            sum += sample * sample
        }
        val rms = sqrt(sum / audioData.size)
        return 20 * log10(rms)
    }
}
