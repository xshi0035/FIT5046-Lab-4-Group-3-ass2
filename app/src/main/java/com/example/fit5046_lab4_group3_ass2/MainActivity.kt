package com.example.fit5046_lab4_group3_ass2

import android.net.http.SslCertificate.saveState
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.fit5046_lab4_group3_ass2.ui.screens.EcoTrackScaffold
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme
import com.example.retrofittesting.Items
import com.example.retrofittesting.RetrofitViewModel
import kotlin.getValue

class MainActivity : ComponentActivity() {
    private val priceViewModel: RetrofitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FIT5046Lab4Group3ass2Theme {
                BottomNavigationBar(priceViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomNavigationBar(priceViewModel: RetrofitViewModel) {
    LaunchedEffect(Unit) {
        priceViewModel.customSearch()
    }
    val itemsReturned by priceViewModel.retrofitResponse
    val price = if (itemsReturned.data.isNotEmpty()) {
        itemsReturned.data[0].results[0].data
            .last { it.size > 1 && it[1] is Number }[1].toString().toFloat()
    } else {
        0f
    }
    /*if (itemsReturned.data.isNotEmpty()) {
        price = itemsReturned.data[0].results[0].data[0][1].toString().toFloat()
    } else {
        price = 0f
    }*/
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.padding(bottom = 20.dp),
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                Destination.entries.forEach { destination ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                destination.icon,
                                contentDescription = destination.label
                            )
                        },
                        label = { Text(destination.label) },
                        selected = currentDestination?.route == destination.route,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Destination.ECOTRACK.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            /*composable(Destination.HOME.route) { HomeScreen() }
            composable(Destination.HISTORY.route) { AppliancesScreen() }*/
            composable(Destination.ECOTRACK.route) { com.example.fit5046_lab4_group3_ass2.EcoTrackScaffold(rrpAudPerMwh = price) }/*
            composable(Destination.PROFILE.route) { RewardsScreen() }
            composable(Destination.PROFILE.route) { ProfileScreen() }*/

        }
    }
}