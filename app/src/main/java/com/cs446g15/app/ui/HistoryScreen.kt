package com.cs446g15.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cs446g15.app.data.DrivesRepository
import com.cs446g15.app.util.exportDrivesWithAggregation
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration

@Composable
fun HistoryScreen(
    toDrive: (String?) -> Unit,
    viewModel: HistoryScreenViewModel = viewModel(factory = HistoryScreenViewModel.factory()),
    exit: () -> Unit
) {
    Surface {
        Scaffold(
            topBar = {
                MediumTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    title = {
                        Text("Past Rides")
                    },
                    actions = {
                        ElevatedButton(
                            onClick = { exportDrivesWithAggregation(viewModel.repository.drives.values.toList())},
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Text("Export Data")
                        }
                    }
                )
            }
        ) {
            Box(modifier = Modifier.padding(it)) {
                HistoryScreenBody(viewModel, onViewDetails = {id: String -> toDrive(id)}, onExit = { exit() } )
            }
        }
    }
}

@Composable
fun HistoryScreenBody(
    viewModel: HistoryScreenViewModel,
    onViewDetails: (String) -> Unit,
    onExit: () -> Unit
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(top = 32.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val allDrives = viewModel.repository.drives
        var i = 0
        for ((id, drive) in allDrives) {
            i += 1
            Text(text = "Drive $i:",
                style = TextStyle(
                    color = Color.Blue,
                    fontSize = 18.sp
                )
            )
            val startTime: LocalDateTime = drive.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
            val endTime: LocalDateTime = drive.endTime.toLocalDateTime(TimeZone.currentSystemDefault())


            Text(text = "Date: " + endTime.date,
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 14.sp
                )
            )
            Text(text = startTime.hour.toString().padStart(2, '0') + ":" + startTime.minute.toString().padStart(2, '0') + " to " + endTime.hour.toString().padStart(2, '0') + ":" + endTime.minute.toString().padStart(2, '0'),
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 14.sp
                )
            )
            Column(
                // IntrinsicSize allows the elements to have equal size
                modifier = Modifier
                    .width(IntrinsicSize.Max)
                    .padding(top = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                ElevatedButton(
                    onClick = { onViewDetails(id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Details")
                }
            }
        }

        Column(
            // IntrinsicSize allows the elements to have equal size
            modifier = Modifier
                .width(IntrinsicSize.Max)
                .padding(top = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            ElevatedButton(
                onClick = onExit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Home")
            }
        }
    }
}

class HistoryScreenViewModel(
    val repository: DrivesRepository = DrivesRepository.DEFAULT
): ViewModel() {
    companion object {
        fun factory() = viewModelFactory {
            initializer {
                HistoryScreenViewModel()
            }
        }
    }
}
