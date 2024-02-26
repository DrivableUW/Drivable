package com.cs446g15.app.data

import android.location.Location
import kotlinx.datetime.Instant
import java.util.UUID

data class Drive(
    val id: String = UUID.randomUUID().toString(),
    val startTime: Instant,
    val endTime: Instant,
    val startLocation: Location?,
    val endLocation: Location?,
    val violations: List<Violation>,
)

data class Violation(
    val time: Instant,
    val location: Location?,
    val description: String,
)

// TODO: persistence

class DrivesRepository {
    private val _drives = mutableMapOf<String, Drive>()

    val drives: Map<String, Drive>
        get() = _drives

    fun addDrive(drive: Drive) {
        _drives[drive.id] = drive
    }

    fun removeDrive(id: String) {
        _drives.remove(id)
    }

    companion object {
        val DEFAULT by lazy { DrivesRepository() }
    }
}
