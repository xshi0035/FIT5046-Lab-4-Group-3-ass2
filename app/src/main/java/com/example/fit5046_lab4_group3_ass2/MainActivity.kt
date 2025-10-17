package com.example.fit5046_lab4_group3_ass2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.fit5046_lab4_group3_ass2.data.ProfileRepo
import com.example.fit5046_lab4_group3_ass2.data.UserPrefs
import com.example.fit5046_lab4_group3_ass2.ui.screens.*
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val ecoTrackScreenViewModel: EcoTrackScreenViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadCsvData(context = this, fileName = "household_power_consumption.csv")
        enableEdgeToEdge()
        setContent {
            FIT5046Lab4Group3ass2Theme {
                AppNav(ecoTrackScreenViewModel)
            }
        }
    }
}

@Composable
private fun AppNav(ecoTrackScreenViewModel: EcoTrackScreenViewModel) {
    val nav = rememberNavController()
    val scope = rememberCoroutineScope()
    val ctx = LocalContext.current

    /** One true way to move between tabs (Home/Appliances/EcoTrack/Rewards/Profile). */
    fun navigateTab(route: String) {
        nav.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
        }
    }

    fun goHome() = navigateTab(ROUTE_HOME)
    fun onTab(route: String) = navigateTab(route)

    NavHost(
        navController = nav,
        startDestination = "login",
        modifier = Modifier.padding()
    ) {
        /* -------------------- Auth / Onboarding flow -------------------- */

        composable("login") {
            LoginScaffold(
                onLoginSuccess = {
                    scope.launch {
                        val onboarded = UserPrefs.isOnboarded(ctx)
                        if (!onboarded) {
                            nav.navigate("home_start") {
                                popUpTo("login") { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            ProfileRepo.exists { res ->
                                val hasProfile = res.getOrElse { false }
                                nav.navigate(if (hasProfile) ROUTE_HOME else "profile_setup") {
                                    popUpTo("login") { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        }
                    }
                },
                onGoToSignUp = { nav.navigate("signup") }
            )
        }

        composable("signup") {
            SignUpScaffold(
                onGoToLogin = {
                    nav.navigate("login") {
                        popUpTo("signup") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Starting page (no bottom bar)
        composable("home_start") {
            HomeScaffold(
                onNotificationsClick = { /* optional */ },
                onGetStartedClick = {
                    scope.launch {
                        UserPrefs.setOnboarded(ctx, true)
                        nav.navigate("profile_setup") {
                            popUpTo("home_start") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // Profile setup (no bottom bar). On save -> Home tab.
        composable("profile_setup") {
            ProfileScaffold(
                onSaved = {
                    nav.navigate(ROUTE_HOME) {
                        popUpTo("profile_setup") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        /* --------------------------- Tab destinations ------------------- */

        composable(ROUTE_HOME) {
            HomePageScaffold(
                currentRoute = ROUTE_HOME,
                onTabSelected = ::onTab,

                // ✅ QUICK ACTIONS now use the same tab navigation helper
                onAddAppliance = { onTab(ROUTE_APPLIANCES) },
                onOpenEcoTrack = { onTab(ROUTE_ECOTRACK) },
                onViewTips = { nav.navigate("tips") },        // non-tab page
                onViewRewards = { onTab(ROUTE_REWARDS) }
            )
        }

        // Appliances list
        composable(ROUTE_APPLIANCES) {
            ElectricityScaffold(
                currentRoute = ROUTE_APPLIANCES,
                onTabSelected = ::onTab,
                onBack = { nav.popBackStack() },              // ✅ back to Home
                onAddAppliance = { nav.navigate("appliance_add") },
                onEditAppliance = { id -> nav.navigate("appliance_add?applianceId=$id") }
            )
        }

        // Add OR Edit appliance (optional id param)
        composable(
            route = "appliance_add?applianceId={applianceId}",
            arguments = listOf(
                navArgument("applianceId") { nullable = true; defaultValue = null }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("applianceId")
            AddApplianceScaffold(
                applianceId = id,
                onBack = { nav.popBackStack() },
                currentRoute = ROUTE_APPLIANCES,
                onTabSelected = ::onTab
            )
        }

        composable(ROUTE_ECOTRACK) {
            EcoTrackScaffold(
                currentRoute = ROUTE_ECOTRACK,
                onTabSelected = ::onTab,
                onBack = { nav.popBackStack() },              // ✅
                viewModel = ecoTrackScreenViewModel,
                onGoToElectricity = { onTab(ROUTE_APPLIANCES) }
            )
        }

        composable(ROUTE_REWARDS) {
            AchievementsScaffold(
                totalPoints = 2_847,
                electricityPoints = 1_523,
                badges = listOf(
                    Badge("Peak Shaver", "Avoided peak-hour usage for 7 days", "Jan 15"),
                    Badge("100 kWh Saved", "Reduced electricity consumption", "Jan 10")
                ),
                leaderboard = listOf(
                    LeaderboardEntry(rank = 7, name = "Your Rank", points = 2_847, isYou = true),
                    LeaderboardEntry(rank = 1, name = "PowerSaverPro", points = 4_892),
                    LeaderboardEntry(rank = 2, name = "GreenThumb_42", points = 4_156),
                    LeaderboardEntry(rank = 3, name = "WattWatcher", points = 3_924)
                ),
                monthly = MonthlyProgress(
                    pointsThisMonth = 847,
                    badgesEarned = 3,
                    daysActive = 18,
                    daysInMonth = 31,
                    monthlyGoal = 1000
                ),
                currentRoute = ROUTE_REWARDS,
                onTabSelected = ::onTab,
                onBack = { nav.popBackStack() }               // ✅
            )
        }

        composable(ROUTE_PROFILE) {
            ProfileRoute(
                onBack = { nav.popBackStack() },              // ✅
                onNotifications = { /* optional */ },
                onEditProfile = { nav.navigate("profile_setup") },
                onTabSelected = ::onTab
            )
        }

        /* ---------------------- Non-tab page WITH bar ------------------- */
        composable("tips") {
            TipsScaffold(
                onBack = { nav.popBackStack() },              // ✅
                onNotifications = { /* optional */ },
                onTabSelected = ::onTab
            )
        }

        composable(ROUTE_PROFILE) {
            ProfileRoute(
                onBack = { nav.popBackStack() },
                onNotifications = { /* optional */ },
                onEditProfile = { nav.navigate("profile_setup") },
                onLogout = {
                    // After signOut (done in ProfileRoute), go to login and clear stack
                    nav.navigate("login") {
                        popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onTabSelected = ::onTab
            )
        }
    }
}
