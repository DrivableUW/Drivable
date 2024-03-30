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
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
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
                onSafetyTips = { navController.navigate("safety_tips") },
                onSettings = { navController.navigate("settings") }
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
            DriveScreen { id ->
                navController.popBackStack()
                if (id != null) { navController.navigate("detail/$id") }
            }
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
            HistoryScreen(toDrive = {id ->
            if (id != null) { navController.navigate("detail/$id") }}) {
                navController.navigate("home")
            }
        }

        composable(
            "safety_tips",
            enterTransition = {
                slideIntoContainer(towards = SlideDirection.Start)
            },
            exitTransition = {
                slideOutOfContainer(towards = SlideDirection.End)
            },
        ) {
            SafetyTipsScreen() {
                navController.navigate("home")
            }
        }

        composable(
            "settings",
            enterTransition = {
                slideIntoContainer(towards = SlideDirection.Start)
            },
            exitTransition = {
                slideOutOfContainer(towards = SlideDirection.End)
            },
        ) {
            SettingsScreen() {
                navController.navigate("home")
            }
        }

        composable("detail/{driveId}",
            enterTransition = {
                slideIntoContainer(towards = SlideDirection.Start) },
            exitTransition = {
                slideOutOfContainer(towards = SlideDirection.End)
            },
            )
        { backStackEntry ->
            val driveId = backStackEntry.arguments?.getString("driveId")
            DriveDetailScreen(driveId = driveId!!) { dest: String? ->
                navController.popBackStack()
                if (dest == "home") navController.navigate("home")
            }
        }
    }
}

@Composable
fun HomeScreen(
    onStartDrive: () -> Unit,
    onViewHistory: () -> Unit,
    onSafetyTips: () -> Unit,
    onSettings: () -> Unit
) {
    Scaffold(
        containerColor = Color(red = 255, green = 230, blue = 208),
        topBar = {
            MediumTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(red = 68, green = 188, blue = 216)
                ),
                title = {
                    Text(
                        text = "Drivable",
                        style = TextStyle(
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontSize = 32.sp
                        )
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onStartDrive,
                containerColor = Color(red = 68, green = 188, blue = 216), // Background color of the button
                contentColor = Color.Black // Text color
            ) {
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
                            Icons.Default.PlayArrow,
                            contentDescription = "Start",
                        )
                        Text(
                            text = "Start Drive",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                ElevatedButton(
                    onClick = onViewHistory,
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
                            Icons.Default.List,
                            contentDescription = "List",
                        )
                        Text(
                            text = "View History",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                ElevatedButton(
                    onClick = onSafetyTips,
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
                            Icons.Default.Warning,
                            contentDescription = "Safety",
                        )
                        Text(
                            text = "Safety Tips",
                            style = TextStyle(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                ElevatedButton(
                    onClick = onSettings,
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
                            Icons.Default.Settings,
                            contentDescription = "Settings",
                        )
                        Text(
                            text = "Settings",
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
}

@Preview
@Composable
fun AppRootPreview() {
    AppRoot()
}
