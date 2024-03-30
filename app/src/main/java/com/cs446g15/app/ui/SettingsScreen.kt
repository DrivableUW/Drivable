package com.cs446g15.app.ui

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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(
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
                        Text("Settings")
                    }
                )
            }
        ) {
            Box(modifier = Modifier.padding(it)) {
                SettingsScreenBody(viewModel, onExit = { exit() } )
            }
        }
    }
}

@Composable
fun SettingsScreenBody(
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
        Column(
            // IntrinsicSize allows the elements to have equal size
            modifier = Modifier
                .width(IntrinsicSize.Max),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            ElevatedButton(
                onClick = { viewModel.repository.removeAllDrives() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete All User Data")
            }
            Spacer(modifier = Modifier.height(5.dp))
            ElevatedButton(
                onClick = onExit,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Back to Home")
            }
        }
    }
}