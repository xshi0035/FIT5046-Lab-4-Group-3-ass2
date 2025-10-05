package com.example.fit5046_lab4_group3_ass2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fit5046_lab4_group3_ass2.data.AppDatabase
import com.example.fit5046_lab4_group3_ass2.ui.screens.*
import com.example.fit5046_lab4_group3_ass2.ui.screens.NavRoutes.ADD_APPLIANCE
import com.example.fit5046_lab4_group3_ass2.ui.screens.NavRoutes.APPLIANCES
import com.example.fit5046_lab4_group3_ass2.ui.screens.NavRoutes.ECOTRACK
import com.example.fit5046_lab4_group3_ass2.ui.screens.NavRoutes.HOME
import com.example.fit5046_lab4_group3_ass2.ui.screens.NavRoutes.PROFILE
import com.example.fit5046_lab4_group3_ass2.ui.screens.NavRoutes.REWARDS
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme
import com.example.fit5046_lab4_group3_ass2.viewmodel.AddApplianceViewModel
import com.example.fit5046_lab4_group3_ass2.viewmodel.HomeViewModel
import com.example.fit5046_lab4_group3_ass2.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)

        setContent {
            FIT5046Lab4Group3ass2Theme {
                EcoTrackApp(db)
            }
        }
    }
}

@Composable
fun EcoTrackApp(db: AppDatabase) {
    val navController = rememberNavController()

    // Shared VMs (kept alive across destinations)
    val homeVM: HomeViewModel = viewModel(factory = ViewModelFactory(db))
    val addVM: AddApplianceViewModel = viewModel(factory = ViewModelFactory(db))

    Scaffold { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = HOME,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Home (bottom tab #1)
            composable(HOME) {
                HomePageScaffold(
                    navController = navController,
                    viewModel = homeVM
                )
            }

            // Appliances (bottom tab #2) – your existing screen
            composable(APPLIANCES) {
                ElectricityScaffold(navController)
            }

            // EcoTrack (bottom tab #3) – your existing screen
            composable(ECOTRACK) {
                EcoTrackScaffold(navController)
            }

            // Rewards (bottom tab #4) – using your Achievements screen
            composable(REWARDS) {
                AchievementsScaffold(
                    totalPoints = 2847,
                    electricityPoints = 1523,
                    badges = emptyList(),
                    leaderboard = emptyList(),
                    monthly = MonthlyProgress(
                        pointsThisMonth = 0,
                        badgesEarned = 0,
                        daysActive = 0,
                        daysInMonth = 30,
                        monthlyGoal = 1000
                    )
                )
            }

            // Profile (bottom tab #5) – safe placeholder so we don’t hit param errors
            composable(PROFILE) {
                // If you want your own ProfileScreen with parameters, replace this call.
                Text("Profile (placeholder screen)")
            }

            // Add Appliance (FAB/Quick Action from Home)
            composable(ADD_APPLIANCE) {
                AddApplianceScaffold(
                    navController = navController,
                    viewModel = addVM,
                    onBack = { navController.popBackStack() },
                    onSave = { name, watt, hours, category ->
                        addVM.addAppliance(name, watt, hours, category)
                        // notify Home that an appliance was added
                        navController.previousBackStackEntry?.savedStateHandle?.set("appliance_added", true)
                        navController.navigate(HOME) {
                            popUpTo(HOME) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onCancel = { navController.popBackStack() }
                )
            }
        }
    }
}
