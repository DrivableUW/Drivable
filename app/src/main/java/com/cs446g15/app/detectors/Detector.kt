package com.cs446g15.app.detectors

import kotlinx.coroutines.flow.Flow

interface Detector<in Request> {
    fun launch(request: Request): Flow<String>
}
