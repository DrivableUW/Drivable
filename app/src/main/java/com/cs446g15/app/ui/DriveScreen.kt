package com.cs446g15.app.ui

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds

data class UiState(val start: Instant) {
    constructor() : this(Clock.System.now())
}

class DriveViewModel: ViewModel() {
    // seems better than using mutableStateOf here because this
    // is more general => portable and testable
    private val _uiFlow = MutableStateFlow(UiState())
    val uiFlow: StateFlow<UiState>
        get() = _uiFlow
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun DriveScreen(
    viewModel: DriveViewModel = viewModel(),
    cancel: () -> Unit
) {
    var showConfirmation by remember { mutableStateOf(false) }

    BackHandler {
        showConfirmation = true
    }

    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = {
                showConfirmation = false
            },
            confirmButton = {
                Button(onClick = {
                    showConfirmation = false
                    cancel()
                }) {
                    Text("Discard")
                }
            },
            dismissButton = {
                Button(onClick = {
                    showConfirmation = false
                }) {
                    Text("Continue")
                }
            },
            title = { Text("Discard Drive?") },
            text = {
                Text(
                    """
                    You will lose all data from this drive. To save, ${""
                    } use the Save Drive button instead.
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
                        IconButton(onClick = { showConfirmation = true }) {
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

    val timeValue by produceState("", uiState.start) {
        while (true) {
            val duration = Clock.System.now() - uiState.start
            duration.toComponents { hours, minutes, seconds, nanoseconds ->
                value = "%02d:%02d:%02d".format(hours, minutes, seconds)
                // sleep through the rest of the "current" second
                delay(1.seconds - nanoseconds.nanoseconds)
            }
        }
    }

    Column {
        Text(timeValue)
    }
}
