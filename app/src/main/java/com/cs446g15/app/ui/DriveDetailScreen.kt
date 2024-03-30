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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.cs446g15.app.data.DrivesRepository
import com.cs446g15.app.util.latLng
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.ktx.model.cameraPosition
import kotlin.time.Duration

@Composable
fun DriveDetailScreen(
    driveId: String,
    viewModel: DriveDetailViewModel = viewModel(factory = DriveDetailViewModel.factory(driveId)),
    exit: (String?) -> Unit
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
                            text = "Drive Details",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontSize = 32.sp
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { exit("") }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) {
            Box(modifier = Modifier.padding(it)) {
                DriveDetailBody(viewModel, onLeaveDetails = { exit("home") } )
            }
        }
    }
}

@Composable
fun DriveDetailBody(
    viewModel: DriveDetailViewModel,
    onLeaveDetails: () -> Unit
) {
    Column {
        val drive = viewModel.drive

        if (drive?.startLocation != null && drive.endLocation != null) {
            val start = drive.startLocation.latLng()
            val end = drive.endLocation.latLng()

            val cameraPositionState = rememberCameraPositionState {
                this.position = cameraPosition {
                    target(start)
                    zoom(10f)
                }
            }

            GoogleMap(
                modifier = Modifier.height(200.dp),
                cameraPositionState = cameraPositionState,
                onMapLoaded = {
                    val bounds = LatLngBounds.Builder().apply {
                        include(start)
                        include(end)
                        drive.violations
                            .mapNotNull { it.location }
                            .forEach { include(it.latLng()) }
                    }.build()
                    val update = CameraUpdateFactory.newLatLngBounds(bounds, 200)
                    cameraPositionState.move(update)
                }
            ) {
                Marker(
                    state = MarkerState(position = start),
                    title = "Start",
                    snippet = "You started here",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                )
                Marker(
                    state = MarkerState(position = end),
                    title = "End",
                    snippet = "You ended here",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                )
                for (violation in drive.violations) {
                    val location = violation.location ?: continue
                    Marker(
                        state = MarkerState(position = location.latLng()),
                        title = "Violation",
                        snippet = violation.description
                    )
                }
            }
        }

        DriveDetailInner(viewModel, onLeaveDetails)
    }
}

@Composable
fun DriveDetailInner(
    viewModel: DriveDetailViewModel,
    onLeaveDetails: () -> Unit
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(top = 32.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val drive = viewModel.drive

        Text(
            text = "Duration:",
            style = TextStyle(
                color = Color(red = 68, green = 188, blue = 216),
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
            Text(
                text = time,
                style = TextStyle(
                    color = Color(red = 68, green = 188, blue = 216),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            )
        }
        Spacer(modifier = Modifier.height(25.dp))

        Text(
            text = "Violations:",
            style = TextStyle(
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            modifier = Modifier.padding(top = 16.dp)
        )
        drive?.violations?.forEach { violation ->
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = violation.description,
                style = TextStyle(
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            )
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
                onClick = onLeaveDetails,
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

class DriveDetailViewModel(
    val id: String,
    val repository: DrivesRepository = DrivesRepository.DEFAULT
): ViewModel() {
    val drive = repository.drives[id]

    companion object {
        fun factory(id: String) = viewModelFactory {
            initializer {
                DriveDetailViewModel(id)
            }
        }
    }
}
