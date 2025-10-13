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
import com.example.fit5046_lab4_group3_ass2.ui.screens.HomeScaffold
import com.example.fit5046_lab4_group3_ass2.ui.screens.HomePageScaffold
import com.example.fit5046_lab4_group3_ass2.ui.screens.LoginScaffold
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
                    // decide: first time? go to start, else main
                    scope.launch {
                        val first = UserPrefs.isFirstRun(ctx)
                        nav.navigate(if (first) "home_start" else "home_main") {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
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
                    // when user finishes the intro, mark as onboarded and go to real home
                    scope.launch {
                        UserPrefs.setOnboarded(ctx)
                        nav.navigate("home_main") {
                            popUpTo("home_start") { inclusive = true }
                            launchSingleTop = true
                        }
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
