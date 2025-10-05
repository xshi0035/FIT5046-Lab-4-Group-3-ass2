package com.example.fit5046_lab4_group3_ass2.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

/** App routes used by NavHost + bottom navigation. */
object NavRoutes {
    const val HOME = "home"
    const val APPLIANCES = "appliances"
    const val ECOTRACK = "ecotrack"
    const val REWARDS = "rewards"
    const val PROFILE = "profile"          // we’ll show a simple placeholder for now
    const val ADD_APPLIANCE = "addAppliance"
    const val EDIT_APPLIANCE = "editAppliance"
}

/** Single source of truth for bottom navigation items. */
data class NavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

/** 5 bottom tabs. (Profile is a simple placeholder screen unless you wire your own.) */
fun bottomNavItems(): List<NavItem> = listOf(
    NavItem("Home", Icons.Filled.Home, NavRoutes.HOME),
    NavItem("Appliances", Icons.Filled.Add, NavRoutes.APPLIANCES),
    NavItem("EcoTrack", Icons.Filled.Info, NavRoutes.ECOTRACK),
    NavItem("Rewards", Icons.Filled.Star, NavRoutes.REWARDS),
    NavItem("Profile", Icons.Filled.AccountCircle, NavRoutes.PROFILE)
)
