package com.cs446g15.app.util

import IntegrityProvider
import android.content.Intent
import androidx.core.content.FileProvider
import com.cs446g15.app.MainActivity
import com.cs446g15.app.data.AggregatedDriveData
import com.cs446g15.app.data.Drive
import com.cs446g15.app.data.AggregateDriveData
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

class InsuranceExport {
    companion object {
        suspend fun exportDrivesWithAggregation(drives: List<Drive>) {
            val json = Json { prettyPrint = true }

            val aggregatedData = AggregateDriveData.computeAggregateDriveData(drives)

            val exportedData = ExportedDriveData(drives, aggregatedData)

            val jsonExportedData = json.encodeToString(ExportedDriveData.serializer(), exportedData)

            val attested = IntegrityProvider.attest(jsonExportedData)

            val fileName = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "_drive_export.jwt"
            val file = File(MainActivity.appContext.cacheDir, fileName)
            file.writeText(attested)

            val uri = FileProvider.getUriForFile(
                MainActivity.appContext,
                "com.cs446g15.app.fileprovider",
                file
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "application/jwt"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Export Drive Data")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            MainActivity.appContext.startActivity(chooserIntent)
        }
    }
}