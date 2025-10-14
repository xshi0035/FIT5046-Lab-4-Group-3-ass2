package com.example.fit5046_lab4_group3_ass2.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

// App-wide routes
const val ROUTE_HOME       = "home_main"
const val ROUTE_APPLIANCES = "appliances"
const val ROUTE_ECOTRACK   = "ecotrack"
const val ROUTE_REWARDS    = "rewards"
const val ROUTE_PROFILE    = "profile"
private data class BottomBarItem(val route: String, val label: String, val icon: ImageVector)

private val items = listOf(
    BottomBarItem(ROUTE_HOME,       "Home",       Icons.Filled.Home),
    BottomBarItem(ROUTE_APPLIANCES, "Appliances", Icons.Filled.Add),
    BottomBarItem(ROUTE_ECOTRACK,   "EcoTrack",   Icons.Filled.Info),
    BottomBarItem(ROUTE_REWARDS,    "Rewards",    Icons.Filled.Star),
    BottomBarItem(ROUTE_PROFILE,    "Profile",    Icons.Filled.AccountCircle),
)


/** Use this on every bottom-tab screen */
@Composable
fun EcoBottomBar(
    currentRoute: String,                 // which tab is currently active
    onTabSelected: (route: String) -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick   = { onTabSelected(item.route) },
                icon      = { Icon(item.icon, contentDescription = item.label) },
                label     = { Text(item.label) }
            )
        }
    }
}
