package com.cs446g15.app.ui

import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun SafetyTipsScreen(
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
                SafetyTipsBody(viewModel, onExit = { exit() } )
            }
        }
    }
}

@Composable
fun SafetyTipsBody(
    viewModel: HistoryScreenViewModel,
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
        val violationsMap = mutableMapOf<String, Int>()
        for ((_, drive) in allDrives) {
            drive.violations.forEach{ violation ->
                if (violationsMap.containsKey(violation.description)) {
                    violationsMap[violation.description] = (violationsMap[violation.description] as Int) + 1
                } else {
                    violationsMap[violation.description] = 1
                }
            }
        }

        var worstViolation = ""
        var worstViolationCount = 0
        for ((violation, count) in violationsMap) {
            if (count > worstViolationCount) {
                worstViolationCount = count
                worstViolation = violation
            }
        }

        if (worstViolation == "") {
            Text(text = "Congrats! You have not committed any driving violations yet, keep it up!",
                style = TextStyle(
                    color = Color.Blue,
                    fontSize = 18.sp
                )
            )
        } else {
            Text(text = "Your Most Frequent Violation:",
                style = TextStyle(
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = worstViolation,
                style = TextStyle(
                    color = Color.Red,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "You have committed this infraction $worstViolationCount times.",
                style = TextStyle(
                    color = Color.Red,
                    fontSize = 18.sp
                )
            )
            val safetySuggestion = mapOf(
                ("Speeding!" to "Speed down and obey the speed limit!"),
                ("Reckless maneuvering!" to "Keep the car stable and avoid jerky movements!"),
                ("Red light!" to "Make sure to always stop for red lights!"),
                ("Stop Sign!" to "Always come to a full stop at each stop sign!")
            )
            Text(text = safetySuggestion[worstViolation].orEmpty(),
                style = TextStyle(
                    color = Color.Red,
                    fontSize = 18.sp
                )
            )

            Spacer(modifier = Modifier.height(25.dp))
            Text(text = "Counts of All Your Violations:",
                style = TextStyle(
                    color = Color.Blue,
                    fontSize = 20.sp
                )
            )
            for ((violation, count) in violationsMap) {
                val violationText = violation.dropLast(1)
                Text(text = "$violationText : $count",
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = 18.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(text = "Please use this information to improve your driving behaviour in the future!",
                style = TextStyle(
                    color = Color.Blue,
                    textAlign = TextAlign.Center,
                    fontSize = 18.sp
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
                onClick = onExit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Home")
            }
        }
    }
}

