package com.cs446g15.app.ui

import android.Manifest.permission
import android.content.Context
import android.location.Location
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs446g15.app.data.Drive
import com.cs446g15.app.data.DrivesRepository
import com.cs446g15.app.data.Violation
import com.cs446g15.app.util.KtPriority
import com.cs446g15.app.util.getCurrentLocation
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

@Composable
fun DriveScreen(
    viewModel: DriveViewModel = viewModel(),
    exit: (String?) -> Unit
) {
    val uiState by viewModel.uiFlow.collectAsState()
    val context = LocalContext.current

    BackHandler { viewModel.backRequested() }
    SideEffect { viewModel.exit = exit }

    val permissions = rememberMultiplePermissionsState(listOf(
        permission.ACCESS_FINE_LOCATION,
        permission.ACCESS_COARSE_LOCATION,
    ))

    LaunchedEffect(permissions) {
        viewModel.handlePermissions(permissions, context)
    }

    if (uiState.showConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::alertDismissed,
            confirmButton = {
                Button(onClick = viewModel::alertConfirmed) {
                    Text("Discard")
                }
            },
            dismissButton = {
                Button(onClick = viewModel::alertDismissed) {
                    Text("Continue")
                }
            },
            title = { Text("Discard Drive?") },
            text = {
                Text(
                    """
                    You will lose all data from this drive. To save, ${""
                    } use the End Drive button instead.
                    """.trimIndent()
                )
            }
        )
    }

    Surface {
        Scaffold(
            topBar = {
                MediumTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    title = {
                        Text("Your Drive")
                    },
                    navigationIcon = {
                        IconButton(onClick = viewModel::backRequested) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) {
            Box(modifier = Modifier.padding(it)) {
                DriveBody(viewModel)
            }
        }
    }
}

@Composable
fun DriveBody(
    viewModel: DriveViewModel
) {
    val uiState by viewModel.uiFlow.collectAsState()

    val timeValue by produceState("", uiState.startTime) {
        while (true) {
            val end = uiState.endTime ?: Clock.System.now()
            val duration = end - uiState.startTime
            duration.toComponents { hours, minutes, seconds, nanoseconds ->
                value = "%02d:%02d:%02d".format(hours, minutes, seconds)
                if (uiState.endTime != null) return@produceState
                // sleep through the rest of the "current" second
                delay(1.seconds - nanoseconds.nanoseconds)
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(top = 32.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(timeValue, fontSize = MaterialTheme.typography.headlineMedium.fontSize)
        ElevatedButton(
            modifier = Modifier.padding(top = 16.dp),
            onClick = { viewModel.endDrive() }
        ) {
            Text("End Drive")
        }

        ElevatedButton(
            modifier = Modifier.padding(top = 16.dp),
            onClick = { viewModel.simulateViolation() }
        ) {
            Text("Simulate Violation")
        }
        uiState.violations.forEach { violation ->
            if (Clock.System.now().epochSeconds - violation.time.epochSeconds <= 5) { // Assuming this is your condition function
                Text(text = violation.description,
                    style = TextStyle(
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp
                    ),
                    modifier = Modifier.padding(top = 16.dp)
                )

            }
        }
    }
}

data class UiState(
    val startTime: Instant = Clock.System.now(),
    val startLocation: Location? = null,
    val endTime: Instant? = null,
    val shouldExit: Boolean = false,
    val violations: List<Violation> = emptyList(),
    val showConfirmation: Boolean = false,
)

class DriveViewModel(
    private val repository: DrivesRepository = DrivesRepository.DEFAULT
): ViewModel() {
    var exit: (String?) -> Unit = {}

    // seems better than using mutableStateOf here because this
    // is more general => portable and testable
    private val _uiFlow = MutableStateFlow(UiState())
    val uiFlow: StateFlow<UiState>
        get() = _uiFlow

    private var locationProvider: FusedLocationProviderClient? = null

    private fun updateState(update: UiState.() -> UiState) {
        _uiFlow.value = _uiFlow.value.update()
    }

    fun handlePermissions(
        permissions: MultiplePermissionsState,
        context: Context
    ) {
        if (!permissions.allPermissionsGranted) {
            permissions.launchMultiplePermissionRequest()
            return
        }

        val locationProvider = LocationServices.getFusedLocationProviderClient(context)
        this.locationProvider = locationProvider

        viewModelScope.launch {
            try {
                val location = locationProvider.getCurrentLocation {
                    priority = KtPriority.HIGH_ACCURACY
                }
                updateState { copy(startLocation = location) }
            } catch (e: SecurityException) {
                Log.w("DriveViewModel", "location fetch failed: $e")
            }
        }
    }

    fun simulateViolation() {
        val violation = Violation(
            time = Clock.System.now(),
            description = "Speeding!",
            location = null
        )
        updateState { copy(violations = violations + violation) }
    }

    fun endDrive() {
        // don't allow ending multiple times
        if (uiFlow.value.endTime != null) return

        val endTime = Clock.System.now()
        updateState { copy(endTime = endTime) }
        viewModelScope.launch {
            val endLocation = try {
                locationProvider?.getCurrentLocation {
                    priority = KtPriority.HIGH_ACCURACY
                }
            } catch (e: SecurityException) { null }
            val uiState = uiFlow.value
            val drive = Drive(
                startTime = uiState.startTime,
                startLocation = uiState.startLocation,
                endTime = endTime,
                endLocation = endLocation,
                violations = uiState.violations
            )
            repository.addDrive(drive)
            exit(drive.id)
        }
    }

    fun backRequested() {
        updateState { copy(showConfirmation = true) }
    }

    fun alertDismissed() {
        updateState { copy(showConfirmation = false) }
    }

    fun alertConfirmed() {
        updateState { copy(showConfirmation = false) }
        exit(null)
    }
}
