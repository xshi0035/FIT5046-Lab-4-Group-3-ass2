package com.example.fit5046_lab4_group3_ass2

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

enum class Destination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    HOME("home", "Home", Icons.Default.Home),
    APPLIANCES("appliances", "Appliances", Icons.Default.Add),
    ECOTRACK("ecotrack", "EcoTrack", Icons.Default.Info),
    REWARDS("rewards", "Rewards", Icons.Default.Star),
    PROFILE("profile", "Profile", Icons.Default.Person)
}