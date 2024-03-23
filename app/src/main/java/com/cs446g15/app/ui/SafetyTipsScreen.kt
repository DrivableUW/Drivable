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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun SafetyTipsScreen(
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
                        Text("Safety Tips")
                    }
                )
            }
        ) {
            Box(modifier = Modifier.padding(it)) {
                SafetyTipsBody(viewModel, onViewDetails = {id: String -> toDrive(id)}, onExit = { exit() } )
            }
        }
    }
}

@Composable
fun SafetyTipsBody(
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
            Text(text = startTime.hour.toString() + ":" + startTime.minute.toString() + " to " + endTime.hour.toString() + ":" + endTime.minute.toString(),
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
