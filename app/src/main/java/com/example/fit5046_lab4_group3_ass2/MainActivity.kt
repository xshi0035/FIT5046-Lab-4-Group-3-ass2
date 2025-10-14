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
import com.example.fit5046_lab4_group3_ass2.data.UserPrefs
import com.example.fit5046_lab4_group3_ass2.data.ProfileRepo
import com.example.fit5046_lab4_group3_ass2.ui.screens.HomeScaffold
import com.example.fit5046_lab4_group3_ass2.ui.screens.HomePageScaffold
import com.example.fit5046_lab4_group3_ass2.ui.screens.LoginScaffold
import com.example.fit5046_lab4_group3_ass2.ui.screens.SignUpScaffold
import com.example.fit5046_lab4_group3_ass2.ui.screens.ProfileScaffold
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
                    // Decide next destination:
                    // 1) If first run -> go to home_start (intro)
                    // 2) else check whether a profile doc exists -> profile_setup or home_main
                    scope.launch {
                        val first = UserPrefs.isFirstRun(ctx)
                        if (first) {
                            nav.navigate("home_start") {
                                popUpTo("login") { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            ProfileRepo.exists { res ->
                                val hasProfile = res.getOrElse { false }
                                val next = if (hasProfile) "home_main" else "profile_setup"
                                nav.navigate(next) {
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

        // First-time “starting page”
        composable("home_start") {
            HomeScaffold(
                onNotificationsClick = { /* optional */ },
                onGetStartedClick = {
                    // When user finishes the intro, mark onboarded and go to profile setup
                    scope.launch {
                        UserPrefs.setOnboarded(ctx)
                        nav.navigate("profile_setup") {
                            popUpTo("home_start") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // Profile setup flow (two steps). Navigates to home on successful save.
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

        // Regular home for returning users
        composable("home_main") {
            HomePageScaffold()
        }
    }
}
