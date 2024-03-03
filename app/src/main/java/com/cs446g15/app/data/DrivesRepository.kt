package com.cs446g15.app.data

import LocationSerializer
import android.location.Location
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import java.io.File

@Serializable
data class Drive(
    val id: String = UUID.randomUUID().toString(),
    val startTime: Instant,
    val endTime: Instant,
    @Serializable(with = LocationSerializer::class) val startLocation: Location?,
    @Serializable(with = LocationSerializer::class) val endLocation: Location?,
    val violations: List<Violation>,
)

@Serializable
data class Violation(
    val time: Instant,
    @Serializable(with = LocationSerializer::class) val location: Location?,
    val description: String,
)

class DrivesRepository {
    private val _drives = mutableMapOf<String, Drive>()
    private val json = Json { encodeDefaults = true}

    val drives: Map<String, Drive>
        get() = _drives

    fun addDrive(drive: Drive) {
        _drives[drive.id] = drive
        saveDrives()
    }

    fun removeDrive(id: String) {
        _drives.remove(id)
        saveDrives()
    }

    private fun saveDrives() {
        val jsonEncoding = json.encodeToString(_drives)
        File("driveHistory.json").writeText(jsonEncoding)
    }

    private fun loadDrives() {
        val jsonEncoding = File("driveHistory.json").readText()
        val driveData = json.decodeFromString<List<Drive>>(jsonEncoding)
        _drives.clear()
        driveData.forEach { drive -> _drives[drive.id] = drive }
    }

    companion object {
        val DEFAULT by lazy { DrivesRepository() }
    }
}
