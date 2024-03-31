package com.cs446g15.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cs446g15.app.data.DrivesRepository
import com.cs446g15.app.util.exportDrivesWithAggregation
import kotlinx.coroutines.launch
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
            containerColor = Color(red = 255, green = 230, blue = 208),
            topBar = {
                MediumTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(red = 68, green = 188, blue = 216)
                    ),
                    title = {
                        Text(
                            text = "Past Rides",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontSize = 32.sp
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { exit() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        ElevatedButton(
                            onClick = { viewModel.export() },
                            modifier = Modifier
                                .padding(vertical = 20.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                contentColor = Color.White // Text color
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                            )
                            {
                                Icon(
                                    Icons.Default.Send,
                                    contentDescription = "Send",
                                )
                                Text(
                                    text = "Export Data",
                                    style = TextStyle(
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    ),
                                )
                            }
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
            .values
            .sortedBy { it.startTime }
            .withIndex()
            .reversed()
        for ((i, drive) in allDrives) {
            val id = drive.id
            ElevatedCard(
                modifier = Modifier
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(red = 211, green = 211, blue = 211))
            ) {
                Column(
                    modifier = Modifier
                        .padding(vertical = 16.dp, horizontal = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Drive $i:",
                        style = TextStyle(
                            color = Color(red = 50, green = 160, blue = 200),
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    )
                    val startTime: LocalDateTime =
                        drive.startTime.toLocalDateTime(TimeZone.currentSystemDefault())
                    val endTime: LocalDateTime =
                        drive.endTime.toLocalDateTime(TimeZone.currentSystemDefault())


                    Text(
                        text = "Date: " + endTime.date,
                        style = TextStyle(
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    )
                    Text(
                        text = startTime.hour.toString()
                            .padStart(2, '0') + ":" + startTime.minute.toString()
                            .padStart(2, '0') + " to " + endTime.hour.toString()
                            .padStart(2, '0') + ":" + endTime.minute.toString().padStart(2, '0'),
                        style = TextStyle(
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(
                                    red = 68,
                                    green = 188,
                                    blue = 216
                                ), // Background color of the button
                                contentColor = Color.Black // Text color
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            )
                            {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = "Info",
                                )
                                Text(
                                    text = "View Details",
                                    style = TextStyle(
                                        fontFamily = FontFamily.SansSerif,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
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
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(red = 68, green = 188, blue = 216), // Background color of the button
                    contentColor = Color.Black // Text color
                )
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                )
                {
                    Icon(
                        Icons.Default.Home,
                        contentDescription = "Home",
                    )
                    Text(
                        text = "Back to Home",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
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

    fun export() {
        viewModelScope.launch {
            exportDrivesWithAggregation(repository.drives.values.toList())
        }
    }
}
