package com.example.fit5046_lab4_group3_ass2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.fit5046_lab4_group3_ass2.ui.screens.HomeScaffold
import com.example.fit5046_lab4_group3_ass2.ui.screens.LoginScaffold
import com.example.fit5046_lab4_group3_ass2.ui.screens.SignUpScaffold
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme

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

    NavHost(
        navController = nav,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScaffold(
                onLoginSuccess = {
                    // Navigate to Home after successful login
                    nav.navigate("home") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
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

        composable("home") {
            HomeScaffold(
                onNotificationsClick = { /* TODO: open notifications */ },
                onGetStartedClick = {
                    // TODO: navigate to your first real feature/screen
                    // e.g. nav.navigate("profileSetup")
                }
            )
        }
    }
}
