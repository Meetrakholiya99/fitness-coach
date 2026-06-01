package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProgressScreen
import com.example.ui.screens.WorkoutScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FitnessViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: FitnessViewModel by viewModels {
        FitnessViewModel.Companion.Factory(application)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var selectedTab by remember { mutableStateOf(0) }

                val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
                val latestPlan by viewModel.latestPlan.collectAsStateWithLifecycle()
                val progressLogs by viewModel.progressLogs.collectAsStateWithLifecycle()
                val prs by viewModel.prs.collectAsStateWithLifecycle()
                val streakStats by viewModel.streakStats.collectAsStateWithLifecycle()
                val isGenerating by viewModel.isGenerating.collectAsStateWithLifecycle()
                val generationError by viewModel.generationError.collectAsStateWithLifecycle()
                val isReviewing by viewModel.isReviewing.collectAsStateWithLifecycle()
                val coachReview by viewModel.coachReview.collectAsStateWithLifecycle()

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        val title = when (selectedTab) {
                            0 -> "Coach Custom Splits"
                            1 -> "Workout Logger & Analytics"
                            else -> "Physiological Characteristics"
                        }
                        CenterAlignedTopAppBar(
                            title = {
                                Text(
                                    text = title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.Black
                                )
                            },
                            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                titleContentColor = MaterialTheme.colorScheme.onBackground
                            )
                        )
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = NavigationBarDefaults.Elevation
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.AutoAwesome, contentDescription = "Workout plans") },
                                label = { Text("Coach Plan") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.TrendingUp, contentDescription = "Weight & BMI changes") },
                                label = { Text("Analytics") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary
                                )
                            )
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Injuries, gear, and weight variables") },
                                label = { Text("Profile") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (selectedTab) {
                            0 -> WorkoutScreen(
                                latestPlan = latestPlan,
                                isGenerating = isGenerating,
                                generationError = generationError,
                                onGenerate = { viewModel.generatePlan() },
                                onCompleteWorkout = { calories -> viewModel.completeWorkout(calories) },
                                onClearError = { viewModel.clearGenerationError() }
                            )
                            1 -> ProgressScreen(
                                logs = progressLogs,
                                prs = prs,
                                streakStats = streakStats,
                                isReviewing = isReviewing,
                                coachReview = coachReview,
                                onRequestReview = { viewModel.getCoachReview() },
                                onAddLog = { weight, bodyFat, caloriesBurned, note ->
                                    viewModel.addProgressLog(weight, bodyFat, caloriesBurned, note)
                                },
                                onDeleteLog = { id -> viewModel.deleteLog(id) },
                                onAddPR = { prName, weight, reps ->
                                    viewModel.addPR(prName, weight, reps)
                                },
                                onDeletePR = { id -> viewModel.deletePR(id) }
                            )
                            2 -> ProfileScreen(
                                userProfile = userProfile,
                                onSaveProfile = { profile ->
                                    viewModel.saveProfile(profile)
                                    selectedTab = 0 // Transition beautifully back to generate new customized program
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
