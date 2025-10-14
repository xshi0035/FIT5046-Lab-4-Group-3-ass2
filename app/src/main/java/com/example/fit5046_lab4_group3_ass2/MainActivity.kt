package com.example.fit5046_lab4_group3_ass2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fit5046_lab4_group3_ass2.data.ProfileRepo
import com.example.fit5046_lab4_group3_ass2.data.UserPrefs
import com.example.fit5046_lab4_group3_ass2.ui.screens.HomePageScaffold
import com.example.fit5046_lab4_group3_ass2.ui.screens.HomeScaffold
import com.example.fit5046_lab4_group3_ass2.ui.screens.LoginScaffold
import com.example.fit5046_lab4_group3_ass2.ui.screens.ProfileScaffold
import com.example.fit5046_lab4_group3_ass2.ui.screens.SignUpScaffold
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FIT5046Lab4Group3ass2Theme {
                AppNav()
            }
        }
    }
}

@Composable
private fun AppNav() {
    val nav = rememberNavController()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()

    NavHost(navController = nav, startDestination = "login") {

        composable("login") {
            LoginScaffold(
                onLoginSuccess = {
                    // Desired flow:
                    // 1) If NOT onboarded -> home_start (Starting page).
                    // 2) If onboarded -> check profile -> home_main or profile_setup.
                    scope.launch {
                        val onboarded = UserPrefs.isOnboarded(ctx)  // default false
                        if (!onboarded) {
                            nav.navigate("home_start") {
                                popUpTo("login") { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            ProfileRepo.exists { res ->
                                val hasProfile = res.getOrElse { false }
                                nav.navigate(if (hasProfile) "home_main" else "profile_setup") {
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

        // Starting page for first-time users
        composable("home_start") {
            HomeScaffold(
                onNotificationsClick = { /* optional */ },
                onGetStartedClick = {
                    // Mark onboarded, then go to Profile setup
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

        // Profile setup (two steps). On successful save -> Home
        composable("profile_setup") {
            ProfileScaffold(
                onSaved = {
                    nav.navigate("home_main") {
                        popUpTo("profile_setup") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // Home for returning users
        composable("home_main") {
            HomePageScaffold()
        }
    }
}
