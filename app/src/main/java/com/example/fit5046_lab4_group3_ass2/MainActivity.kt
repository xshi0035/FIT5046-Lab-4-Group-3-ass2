package com.example.fit5046_lab4_group3_ass2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
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
import com.google.firebase.auth.FirebaseAuth
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

    // ---- Global auth listener: if user becomes null (sign out / delete), jump to login + clear stack
    var isLoggedIn by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }

    DisposableEffect(Unit) {
        val auth = FirebaseAuth.getInstance()
        val listener = FirebaseAuth.AuthStateListener { fa ->
            isLoggedIn = fa.currentUser != null
        }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            nav.navigate("login") {
                popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    // ----------------------------------------------------------------------

    fun navigateTab(route: String) {
        nav.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(nav.graph.findStartDestination().id) { saveState = true }
        }
    }
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
                        val auth = FirebaseAuth.getInstance()
                        val meta = auth.currentUser?.metadata
                        val isFirstSignIn = meta != null &&
                                meta.creationTimestamp == meta.lastSignInTimestamp

                        if (isFirstSignIn) {
                            // First ever sign-in -> show Get Started
                            UserPrefs.setOnboarded(ctx, false)
                            nav.navigate("home_start") {
                                popUpTo("login") { inclusive = true }
                                launchSingleTop = true
                            }
                            return@launch
                        }

                        // Otherwise check local onboarding flag
                        val onboarded = UserPrefs.isOnboarded(ctx)
                        if (!onboarded) {
                            nav.navigate("home_start") {
                                popUpTo("login") { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            // Already onboarded -> ensure profile exists
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

        // First-time "Get Started" (no bottom bar)
        composable("home_start") {
            HomeScaffold(
                onNotificationsClick = { /* optional */ },
                onGetStartedClick = {
                    scope.launch {
                        // Mark onboarded, then proceed to profile setup
                        UserPrefs.setOnboarded(ctx, true)
                        nav.navigate("profile_setup") {
                            popUpTo("home_start") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // Profile setup (no bottom bar). Save -> Home; Back -> previous.
        composable("profile_setup") {
            ProfileScaffold(
                onSaved = {
                    nav.navigate(ROUTE_HOME) {
                        popUpTo("profile_setup") { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onBack = { nav.popBackStack() }
            )
        }

        /* --------------------------- Tab destinations ------------------- */

        composable(ROUTE_HOME) {
            HomePageScaffold(
                currentRoute = ROUTE_HOME,
                onTabSelected = ::onTab,
                onAddAppliance = { onTab(ROUTE_APPLIANCES) },
                onOpenEcoTrack = { onTab(ROUTE_ECOTRACK) },
                onViewTips = { nav.navigate("tips") },  // non-tab page
                onViewRewards = { onTab(ROUTE_REWARDS) },
                viewModel = ecoTrackScreenViewModel
            )
        }

        composable(ROUTE_APPLIANCES) {
            ElectricityScaffold(
                currentRoute = ROUTE_APPLIANCES,
                onTabSelected = ::onTab,
                onBack = { nav.popBackStack() },
                onAddAppliance = { nav.navigate("appliance_add") },
                onEditAppliance = { id -> nav.navigate("appliance_add?applianceId=$id") }
            )
        }

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
                onBack = { nav.popBackStack() },
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
                onBack = { nav.popBackStack() }
            )
        }

        composable(ROUTE_PROFILE) {
            ProfileRoute(
                onBack = { nav.popBackStack() },
                onNotifications = { /* optional */ },
                onEditProfile = { nav.navigate("profile_setup") },
                onLogout = {
                    // ProfileRoute should call FirebaseAuth.getInstance().signOut()
                    nav.navigate("login") {
                        popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onTabSelected = ::onTab
            )
        }

        /* ---------------------- Non-tab pages WITH bar ------------------- */
        composable("tips") {
            TipsScaffold(
                onBack = { nav.popBackStack() },
                onNotifications = { /* optional */ },
                onTabSelected = ::onTab
            )
        }

        // Privacy
        composable("privacy") {
            PrivacyScreen(
                onBack = { nav.popBackStack() },
                onNotifications = { /* optional */ },
                onAccountDeleted = {
                    // This will also be triggered by the global auth listener; keeping it is harmless.
                    nav.navigate("login") {
                        popUpTo(nav.graph.findStartDestination().id) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}
