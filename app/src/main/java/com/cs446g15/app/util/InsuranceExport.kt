package com.cs446g15.app.util

import android.content.Intent
import androidx.core.content.FileProvider
import com.cs446g15.app.MainActivity
import com.cs446g15.app.data.AggregatedDriveData
import com.cs446g15.app.data.Drive
import com.cs446g15.app.data.aggregateDriveData
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
@Serializable
data class ExportedDriveData(
    val drives: List<Drive>,
    val aggregatedData: AggregatedDriveData
)

fun exportDrivesWithAggregation(drives: List<Drive>) {
    val json = Json { prettyPrint = true }

    val aggregatedData = aggregateDriveData(drives)

    val exportedData = ExportedDriveData(drives, aggregatedData)

    val jsonExportedData = json.encodeToString(ExportedDriveData.serializer(), exportedData)

    val fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "_drive_export"
    val file = File(MainActivity.appContext.cacheDir, fileName)
    file.writeText(jsonExportedData)

    val uri = FileProvider.getUriForFile(MainActivity.appContext, "com.cs446g15.app.fileprovider", file)

    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "application/json"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    val chooserIntent = Intent.createChooser(shareIntent, "Export Drive Data")
    chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    MainActivity.appContext.startActivity(chooserIntent)
}