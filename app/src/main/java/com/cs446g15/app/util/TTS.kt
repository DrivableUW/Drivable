package com.cs446g15.app.util

import android.content.Context
import android.speech.tts.TextToSpeech
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

suspend fun Context.getTextToSpeech(): TextToSpeech {
    var tts: TextToSpeech? = null
    suspendCoroutine { cont ->
        tts = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                cont.resume(Unit)
            } else {
                cont.resumeWithException(RuntimeException("Failed to initialize TTS: $status"))
            }
        }
    }
    return tts ?: throw NullPointerException()
}
