package com.example.fit5046_lab4_group3_ass2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme
import com.example.fit5046_lab4_group3_ass2.viewmodel.AddApplianceViewModel
import com.example.fit5046_lab4_group3_ass2.viewmodel.HomeViewModel
import com.example.fit5046_lab4_group3_ass2.data.ApplianceEntity
import androidx.compose.runtime.collectAsState

/* ------------------------------- DATA (UI only) ------------------------------- */

data class Appliance(
    val iconEmoji: String,
    val name: String,
    val spec: String,       // e.g., "150W • 6h daily"
    val costPerDay: String, // e.g., "$0.27/day"
    val kwh: String         // e.g., "0.9 kWh"
)

data class Suggestion(
    val title: String,
    val body: String,
    val leadingEmoji: String = "💡"
)

private val demoAppliances = listOf(
    Appliance("📺", "Living Room TV", "150W • 6h daily", "$0.27/day", "0.9 kWh"),
    Appliance("❄️", "Refrigerator", "200W • 24h daily", "$1.44/day", "4.8 kWh"),
    Appliance("👕", "Washing Machine", "500W • 2h daily", "$0.30/day", "1.0 kWh"),
    Appliance("💡", "LED Lights (8)", "80W • 8h daily", "$0.19/day", "0.64 kWh")
)

private val demoSuggestions = listOf(
    Suggestion(
        title = "Peak Hour Alert",
        body = "High load expected at 7pm today. Consider running your washing machine earlier to save $0.15."
    ),
    Suggestion(
        title = "Eco Tip",
        body = "Your TV has been on for 8+ hours. Consider using sleep mode to save 20W per hour.",
        leadingEmoji = "🪶"
    )
)

/* ------------------------------- MAIN SCAFFOLD ------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectricityScaffold(
    navController: NavController,
    homeViewModel: HomeViewModel,
    addViewModel: AddApplianceViewModel
) {
    // Public types only (keep consistent with other screens)
    val navItems: List<Pair<String, ImageVector>> = listOf(
        "Home" to Icons.Filled.Home,
        "Appliances" to Icons.Filled.Add,
        "EcoTrack" to Icons.Filled.Info,
        "Rewards" to Icons.Filled.Star,
        "Profile" to Icons.Filled.AccountCircle,
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Appliances") },
                navigationIcon = {
                    IconButton(onClick = { /* UI-only back */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Bell + tiny unread dot (same pattern as Rewards)
                    Box {
                        IconButton(onClick = { /* UI only */ }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8A2BE2))
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                val backStackEntry = navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry.value?.destination?.route
                val items = bottomNavItems()
                items.forEach { item ->
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
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(NavRoutes.ADD_APPLIANCE) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { inner ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            val appliances by homeViewModel.allAppliances.collectAsState(initial = emptyList())
            ElectricityScreen(
                usageKwh = "8.4 kWh",
                costEstimate = "$2.52 estimated cost",
                co2 = "CO₂: 4.2kg equivalent",
                changePercent = "-12%",
                appliances = appliances,
                suggestions = demoSuggestions,
                onEdit = { id -> navController.navigate("${NavRoutes.EDIT_APPLIANCE}/$id") },
                onDelete = { entity -> addViewModel.deleteAppliance(entity) }
            )
        }
    }
}

/* --------------------------------- SCREEN ---------------------------------- */

@Composable
fun ElectricityScreen(
    usageKwh: String,
    costEstimate: String,
    co2: String,
    changePercent: String,
    appliances: List<ApplianceEntity>,
    suggestions: List<Suggestion>,
    onEdit: (Int) -> Unit,
    onDelete: (ApplianceEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // "Today's Usage" — card title & supporting follow app typography rules
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Today's Usage",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            usageKwh,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f)
                        )
                        ChangePill(changePercent)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        costEstimate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                    Text(
                        co2,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // My Appliances header (titleMedium like other section headers)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("My Appliances", style = MaterialTheme.typography.titleMedium)
                Text(
                    "View Usage",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Appliance list
        items(appliances, key = { it.id }) { entity ->
            ApplianceCard(
                appliance = Appliance(
                    iconEmoji = "🔌",
                    name = entity.name,
                    spec = "${entity.watt}W • ${entity.hours}h daily",
                    costPerDay = String.format("$%.2f/day", (entity.watt * entity.hours / 1000f) * 0.30f),
                    kwh = String.format("%.1f kWh", (entity.watt * entity.hours / 1000f))
                ),
                onEdit = { onEdit(entity.id) },
                onDelete = { onDelete(entity) }
            )
            Spacer(Modifier.height(12.dp))
        }

        // Smart Suggestions header
        item {
            Text(
                "Smart Suggestions",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }

        // Suggestions list
        items(suggestions) { tip ->
            SuggestionCard(tip)
            Spacer(Modifier.height(12.dp))
        }
    }
}

/* ------------------------------- COMPONENTS -------------------------------- */

@Composable
private fun ChangePill(text: String) {
    val isDown = text.trim().startsWith("-")
    val bg = if (isDown) MaterialTheme.colorScheme.tertiaryContainer
    else MaterialTheme.colorScheme.errorContainer
    val fg = if (isDown) MaterialTheme.colorScheme.onTertiaryContainer
    else MaterialTheme.colorScheme.onErrorContainer

    Surface(
        color = bg,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text,
            fontSize = 12.sp,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ApplianceCard(
    appliance: Appliance,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Tinted icon tile
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) { Text(appliance.iconEmoji, fontSize = 20.sp) }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(appliance.name, fontWeight = FontWeight.SemiBold)
                    Text(
                        appliance.spec,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        appliance.costPerDay,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        appliance.kwh,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            var showDelete by remember { mutableStateOf(false) }
            if (showDelete) {
                AlertDialog(
                    onDismissRequest = { showDelete = false },
                    confirmButton = {
                        TextButton(onClick = { showDelete = false; onDelete() }) { Text("Delete") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDelete = false }) { Text("Don't delete") }
                    },
                    title = { Text("Delete appliance?") },
                    text = { Text("This action cannot be undone.") }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Text("Edit")
                }
                OutlinedButton(onClick = { showDelete = true }, modifier = Modifier.weight(1f)) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun SuggestionCard(suggestion: Suggestion) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) { Text(suggestion.leadingEmoji) }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    suggestion.title,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    suggestion.body,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f),
                    lineHeight = 18.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ActionCard(modifier: Modifier = Modifier, label: String) {
    Card(shape = RoundedCornerShape(16.dp), modifier = modifier.height(72.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(label, fontWeight = FontWeight.Medium)
        }
    }
}

/* -------------------------------- PREVIEW ---------------------------------- */

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ElectricityPreview() {
    FIT5046Lab4Group3ass2Theme {
        ElectricityScreen(
            usageKwh = "8.4 kWh",
            costEstimate = "$2.52 estimated cost",
            co2 = "CO₂: 4.2kg equivalent",
            changePercent = "-12%",
            appliances = listOf(
                ApplianceEntity(id = 1, name = "Demo TV", watt = 150, hours = 6f, category = "Entertainment")
            ),
            suggestions = demoSuggestions,
            onEdit = {},
            onDelete = {}
        )
    }
}
