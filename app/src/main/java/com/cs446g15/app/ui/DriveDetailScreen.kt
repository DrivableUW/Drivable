package com.cs446g15.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cs446g15.app.data.DrivesRepository
import kotlin.time.Duration

@Composable
fun DriveDetailScreen(
    driveId: String,
    viewModel: DriveDetailViewModel = viewModel(factory = DriveDetailViewModel.factory(driveId))
) {
    Surface {
        Scaffold(
            topBar = {
                MediumTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    title = {
                        Text("Drive Details")
                    }
                )
            }
        )
        {
            Box(modifier = Modifier.padding(it)) {
                DriveDetailBody(viewModel)
            }
        }
    }
}

@Composable
fun DriveDetailBody(
    viewModel: DriveDetailViewModel
) {
    Column(
        modifier = Modifier
            .padding(top = 32.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val drive = viewModel.repository.drives[viewModel.id]
        Text(text = "Duration:",
            style = TextStyle(
                color = Color.Blue,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        )
        val duration: Duration
        if (drive != null) {
            duration = drive.endTime - drive.startTime
            val hours = duration.inWholeHours
            val minutes = (duration.inWholeMinutes % 60)
            val seconds = (duration.inWholeSeconds % 60)

            val time = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            Text(text = time,
                style = TextStyle(
                    color = Color.Blue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )
        }
        Text(text = "Violations:",
            style = TextStyle(
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            modifier = Modifier.padding(top = 16.dp)
        )
        drive?.violations?.forEach{ violation ->
            Text(text = violation.description,
                style = TextStyle(
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )
        }
    }
}

class DriveDetailViewModel(
    val id: String,
    val repository: DrivesRepository = DrivesRepository.DEFAULT
): ViewModel() {
    companion object {
        fun factory(id: String) = viewModelFactory {
            initializer {
                DriveDetailViewModel(id)
            }
        }
    }
}
