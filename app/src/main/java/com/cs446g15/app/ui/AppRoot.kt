package com.cs446g15.app.ui

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.EnterTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.cs446g15.app.ui.theme.CS446Theme

@Composable
fun AppRoot() {
    CS446Theme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            NavRoot()
        }
    }
}

@Composable
fun NavRoot() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "home") {
        composable(
            "home",
            popEnterTransition = {
                EnterTransition.None
            }
        ) {
            HomeScreen(
                onStartDrive = { navController.navigate("drive") },
                onViewHistory = { navController.navigate("history") },
            )
        }

        composable(
            "drive",
            enterTransition = {
                slideIntoContainer(towards = SlideDirection.Start)
            },
            exitTransition = {
                slideOutOfContainer(towards = SlideDirection.End)
            },
        ) {
            DriveScreen { navController.popBackStack() }
        }

        composable(
            "history",
            enterTransition = {
                slideIntoContainer(towards = SlideDirection.Start)
            },
            exitTransition = {
                slideOutOfContainer(towards = SlideDirection.End)
            },
        ) {
            HistoryScreen()
        }
    }
}

@Composable
fun HomeScreen(
    onStartDrive: () -> Unit,
    onViewHistory: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                title = {
                    Text("Drivable")
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onStartDrive) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                // IntrinsicSize allows the elements to have equal size
                modifier = Modifier.width(IntrinsicSize.Max),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                ElevatedButton(
                    onClick = onStartDrive,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Start Drive")
                }

                ElevatedButton(
                    onClick = onViewHistory,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View History")
                }
            }
        }
    }
}

@Preview
@Composable
fun AppRootPreview() {
    AppRoot()
}
