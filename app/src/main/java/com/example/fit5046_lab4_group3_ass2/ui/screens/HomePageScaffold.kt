package com.example.fit5046_lab4_group3_ass2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fit5046_lab4_group3_ass2.data.ApplianceEntity
import com.example.fit5046_lab4_group3_ass2.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePageScaffold(
    navController: NavController,
    viewModel: HomeViewModel
) {
    val appliances by viewModel.allAppliances.collectAsState(initial = emptyList())
    val items = bottomNavItems()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Home") },
                navigationIcon = {
                    Surface(
                        shape = CircleShape,
                        tonalElevation = 1.dp,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                    ) { Box(contentAlignment = Alignment.Center) { Text("←") } }
                },
                actions = { /* notification icon already present in your design – omitted for brevity */ }
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute == item.route) return@NavigationBarItem
                            navController.navigate(item.route) {
                                popUpTo(NavRoutes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { inner ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            Home(
                appliances = appliances,
                onAddAppliance = { navController.navigate(NavRoutes.ADD_APPLIANCE) },
                onOpenEcoTrack = { navController.navigate(NavRoutes.ECOTRACK) },
                onViewRewards = { navController.navigate(NavRoutes.REWARDS) }
            )
        }
    }

    // Show snackbar when coming back from Add Appliance
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    val applianceAdded: Boolean? = savedStateHandle?.get<Boolean>("appliance_added")
    if (applianceAdded == true) {
        LaunchedEffect(Unit) {
            snackbarHostState.showSnackbar("Appliance saved")
            savedStateHandle?.remove<Boolean>("appliance_added")
        }
    }
}

/* ----------------------------- CONTENT BELOW ------------------------------ */

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier.padding(top = 12.dp, bottom = 8.dp)
    )
}

@Composable
private fun HomeScreenCard(
    modifier: Modifier = Modifier,
    title: String,
    mainText: String,
    smallText: String,
    progress: Float = 0f,
    rightText: String,
    tonal: Boolean = false,
    valueStyle: TextStyle = MaterialTheme.typography.titleMedium
) {
    val colors = if (tonal)
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    else CardDefaults.cardColors()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = colors,
        modifier = modifier.padding(bottom = 12.dp)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (title.isNotEmpty()) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (rightText.isNotEmpty()) {
                    Text(
                        rightText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (mainText.isNotEmpty()) {
                Text(
                    text = mainText,
                    style = valueStyle,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (smallText.isNotEmpty()) {
                Text(
                    smallText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (progress > 0f) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    color = ProgressIndicatorDefaults.linearColor,
                    trackColor = ProgressIndicatorDefaults.linearTrackColor,
                    strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
                )
            }
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    FilledTonalButton(
        onClick = onClick,
        modifier = modifier.height(68.dp),
        shape = RoundedCornerShape(20.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(20.dp))
            Spacer(Modifier.height(4.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun Home(
    appliances: List<ApplianceEntity>,
    onAddAppliance: () -> Unit,
    onOpenEcoTrack: () -> Unit,
    onViewRewards: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp)
    ) {
        item {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = "Good morning!",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Let's track your eco impact today",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 12.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HomeScreenCard(
                    title = "EcoPoints",
                    mainText = "2,450",
                    smallText = "🔥 7-day streak",
                    rightText = "\uD83C\uDFC6",
                    tonal = true,
                    valueStyle = MaterialTheme.typography.displaySmall
                )
                HomeScreenCard(
                    title = "⚡ Electricity",
                    mainText = "8.4 kWh",
                    smallText = "Today's usage",
                    progress = 0.8f,
                    rightText = "-12%",
                    tonal = true,
                    valueStyle = MaterialTheme.typography.titleMedium
                )

                SectionTitle("Quick Actions")

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionButton(
                        icon = Icons.Filled.Add,
                        label = "Add Appliance",
                        modifier = Modifier.weight(1f),
                        onClick = onAddAppliance
                    )
                    QuickActionButton(
                        icon = Icons.Filled.Info,
                        label = "Open EcoTrack",
                        modifier = Modifier.weight(1f),
                        onClick = onOpenEcoTrack
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionButton(
                        icon = Icons.Filled.Star,
                        label = "View Rewards",
                        modifier = Modifier.weight(1f),
                        onClick = onViewRewards
                    )
                }

                SectionTitle("Recent Activity")

                HomeScreenCard(
                    title = "⚡ Usage goal met",
                    mainText = "",
                    smallText = "Yesterday",
                    rightText = "+100 pts"
                )
                HomeScreenCard(
                    title = "🔌 New appliance added",
                    mainText = "",
                    smallText = "5 hours ago",
                    rightText = "+25 pts"
                )
                HomeScreenCard(
                    title = "ℹ️ Price tip viewed",
                    mainText = "",
                    smallText = "2 hours ago",
                    rightText = "+10 pts"
                )

                SectionTitle("Information")
                HomeScreenCard(
                    title = "\uD83D\uDCA1 Today's Eco Tip",
                    mainText = "",
                    smallText = "Unplug devices when not in use",
                    rightText = ""
                )
            }
        }

        if (appliances.isNotEmpty()) {
            item { SectionTitle("Your Appliances") }
            items(appliances) { appliance ->
                ApplianceCard(appliance)
            }
        }
    }
}

@Composable
private fun ApplianceCard(appliance: ApplianceEntity) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(appliance.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text("Wattage: ${appliance.watt} W")
            Text("Usage: ${appliance.hours} hrs/day")
            Text("Category: ${appliance.category}")
        }
    }
}
