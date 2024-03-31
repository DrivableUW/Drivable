package com.cs446g15.app.detectors

import kotlinx.coroutines.flow.Flow

interface Detector {
    fun launch(): Flow<String>
}
