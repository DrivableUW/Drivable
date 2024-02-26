package com.cs446g15.app.ui

import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cs446g15.app.data.DrivesRepository

@Composable
fun DriveDetailScreen(
    driveId: String,
    viewModel: DriveDetailViewModel = viewModel(factory = DriveDetailViewModel.factory(driveId))
) {
    Surface {
        Text("Drive: $driveId")
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
