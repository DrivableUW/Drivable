package com.cs446g15.app.data
import com.cs446g15.app.util.DurationSerializer
import kotlinx.serialization.Serializable
import java.time.Duration
@Serializable
data class AggregatedDriveData(
    @Serializable(with = DurationSerializer::class) val averageDuration: Duration,
    val topViolations: List<Pair<String, Int>>
)

class AggregateDriveData {
    companion object {
        // This function computes some aggregate statistics based on Drive data, used for data insight and export
        fun computeViolationCounts(drives: List<Drive>): Map<String, Int> {
            val violationCounts = mutableMapOf<String, Int>()
            drives.flatMap { it.violations }
                .map { it.description }
                .forEach { violation ->
                    violationCounts[violation] = violationCounts.getOrDefault(violation, 0) + 1
                }
            return violationCounts
        }

        fun computeAggregateDriveData(drives: List<Drive>): AggregatedDriveData {
            val totalDurationMillis =
                drives.sumOf { it.endTime.toEpochMilliseconds() - it.startTime.toEpochMilliseconds() }
            val averageDurationMillis =
                if (drives.isNotEmpty()) totalDurationMillis / drives.size else 0
            val averageDuration = Duration.ofMillis(averageDurationMillis)

            val violationCounts = computeViolationCounts(drives)
            val topViolations = violationCounts.entries.sortedByDescending { it.value }.take(3)
                .map { it.key to it.value }
            return AggregatedDriveData(averageDuration, topViolations)
        }
    }
}
