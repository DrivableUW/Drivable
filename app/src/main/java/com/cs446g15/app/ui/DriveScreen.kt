package com.cs446g15.app.ui

import android.Manifest.permission
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import android.location.Location
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cs446g15.app.data.Drive
import com.cs446g15.app.data.DrivesRepository
import com.cs446g15.app.data.Violation
import com.cs446g15.app.util.KtPriority
import com.cs446g15.app.util.getCurrentLocation
import com.cs446g15.app.util.getEventFlow
import com.cs446g15.app.util.getTextToSpeech
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.milliseconds
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
                Button(
                    onClick = viewModel::alertConfirmed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(red = 68, green = 188, blue = 216), // Background color of the button
                        contentColor = Color.Black // Text color
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                    )
                    Text("Discard")
                }
            },
            dismissButton = {
                Button(
                    onClick = viewModel::alertDismissed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(red = 68, green = 188, blue = 216), // Background color of the button
                        contentColor = Color.Black // Text color
                    )
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "Continue",
                    )
                    Text("Continue")
                }
            },
            title = {
                Text(
                    text = "Discard Drive?",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                )
            },
            text = {
                Text(
                    text = """
                    You will lose all data from this drive. To save, ${""
                    } use the End Drive button instead.
                    """.trimIndent(),
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                )
            }
        )
    }

    Surface {
        Scaffold(
            topBar = {
                MediumTopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(red = 68, green = 188, blue = 216)
                    ),
                    title = {
                        Text(
                            text = "Your Drive",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontSize = 32.sp
                            )
                        )
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
        Spacer(modifier = Modifier.height(40.dp))
        ElevatedButton(
            onClick = { viewModel.endDrive() },
            modifier = Modifier
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(red = 68, green = 188, blue = 216), // Background color of the button
                contentColor = Color.Black // Text color
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            )
            {
                Icon(
                    Icons.Default.ExitToApp,
                    contentDescription = "Exit",
                )
                Text(
                    text = "End Drive",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        ElevatedButton(
            onClick = { viewModel.simulateViolation() },
            modifier = Modifier
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(red = 68, green = 188, blue = 216), // Background color of the button
                contentColor = Color.Black // Text color
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            )
            {
                Icon(
                    Icons.Default.Create,
                    contentDescription = "Create",
                )
                Text(
                    text = "Simulate Violation",
                    style = TextStyle(
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                )
            }
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

    private var tts: TextToSpeech? = null

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

        viewModelScope.launch {
            awaitAll(
                async { setupTts(context) },
                async { setupLocation(context) },
                async { setupAccelerometer(context) },
            )
        }
    }

    private suspend fun setupTts(context: Context) {
        try {
            val tts = context.getTextToSpeech()
            addCloseable { tts.shutdown() }
            this.tts = tts
        } catch (e: Exception) {
            Log.w("DriveViewModel", "tts init failed: $e")
        }
    }

    private suspend fun setupLocation(context: Context) {
        try {
            val locationProvider = LocationServices.getFusedLocationProviderClient(context)
            this.locationProvider = locationProvider
            val location = getLocation()
            updateState { copy(startLocation = location) }
        } catch (e: SecurityException) {
            Log.w("DriveViewModel", "location fetch failed: $e")
        }
    }

    @OptIn(FlowPreview::class)
    private suspend fun setupAccelerometer(context: Context) {
        val threshold = 3 // G force threshold
        val debounceTime = 1000.milliseconds

        val sensorManager = context.getSystemService(SensorManager::class.java) ?: return
        sensorManager.getEventFlow(Sensor.TYPE_ACCELEROMETER)
            // low-pass filter to discount gravity
            .scan(Pair(listOf(0f, 0f, 0f), listOf(0f, 0f, 0f))) { (prevGravity, prevAccel), event ->
                // adapted for Kotlin Flows (reactive), based on
                // https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-accel
                val alpha = 0.8f

                val gravity = prevGravity.toMutableList()
                val accel = prevAccel.toMutableList()

                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

                accel[0] = event.values[0] - gravity[0]
                accel[1] = event.values[1] - gravity[1]
                accel[2] = event.values[2] - gravity[2]

                Pair(gravity, accel)
            }
            // accel
            .map { it.second }
            // accel magnitude
            .map { it[0] * it[0] + it[1] * it[1] + it[2] * it[2] }
            // iff over threshold
            .map { it > (threshold * threshold) }
            // remove duplicate events (only trigger on leading/trailing edge)
            .distinctUntilChanged()
            // remove false (only trigger on leading edge)
            .filter { it }
            // we only have true now so map to Unit
            .map {}
            // debounce to remove noise
            .debounce(debounceTime)
            // we'll always receive an event on startup, discard it
            .drop(1)
            // register as a violation
            .collect { registerViolation("Reckless maneuvering!") }
    }

    private suspend fun getLocation(): Location? {
        return try {
            locationProvider?.getCurrentLocation {
                priority = KtPriority.HIGH_ACCURACY
            }
        } catch (e: SecurityException) {
            null
        }
    }

    private suspend fun registerViolation(message: String) {
        tts?.speak(message, TextToSpeech.QUEUE_FLUSH, null, null)
        val violation = Violation(
            time = Clock.System.now(),
            description = message,
            location = getLocation()
        )
        updateState { copy(violations = violations + violation) }
    }

    fun simulateViolation() {
        val lengthModulo = uiFlow.value.violations.size % 3
        val message = when (lengthModulo) {
            0 -> "Speeding!"
            1 -> "Red light!"
            else -> "Stop Sign!"
        }
        viewModelScope.launch { registerViolation(message) }
    }

    fun endDrive() {
        // don't allow ending multiple times
        if (uiFlow.value.endTime != null) return

        val endTime = Clock.System.now()
        updateState { copy(endTime = endTime) }
        viewModelScope.launch {
            val uiState = uiFlow.value
            val drive = Drive(
                startTime = uiState.startTime,
                startLocation = uiState.startLocation,
                endTime = endTime,
                endLocation = getLocation(),
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
