package com.example.fit5046_lab4_group3_ass2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FIT5046Lab4Group3ass2Theme {
                ElectricityScaffold()
            }
        }
    }
}

/* ------------------------------- DATA (UI only) ------------------------------- */

data class Appliance(
    val iconEmoji: String,
    val name: String,
    val spec: String,      // e.g., "150W • 6h daily"
    val costPerDay: String,// e.g., "$0.27/day"
    val kwh: String        // e.g., "0.9 kWh"
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
        body = "High load expected at 7pm today. Consider running your washing machine earlier to save $0.15.",
        leadingEmoji = "💡"
    ),
    Suggestion(
        title = "Eco Tip",
        body = "Your TV has been on for 8+ hours. Consider using sleep mode to save 20W per hour.",
        leadingEmoji = "🪶"
    )
)

/* ------------------------------- MAIN SCAFFOLD ------------------------------- */

@OptIn(ExperimentalMaterial3Api::class) // fix experimental top app bar warning
@Composable
fun ElectricityScaffold() {
    // Use only default system icons (no extra deps)
    val navItems = listOf(
        NavItem("Home", Icons.Filled.Home),
        NavItem("Appliances", Icons.Filled.Add), // placeholder
        NavItem("Plastic", Icons.Filled.Delete),    // placeholder
        NavItem("Rewards", Icons.Filled.Star),
        NavItem("Profile", Icons.Filled.AccountCircle),
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Electricity") },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) { Text("←") }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) { Text("⋮") }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = index == 1, // Appliances selected (UI only)
                        onClick = { /* no-op (UI prototype) */ },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* no-op */ }) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { inner ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            ElectricityScreen(
                usageKwh = "8.4 kWh",
                costEstimate = "$2.52 estimated cost",
                co2 = "CO₂: 4.2kg equivalent",
                changePercent = "-12%",
                appliances = demoAppliances,
                suggestions = demoSuggestions
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
    appliances: List<Appliance>,
    suggestions: List<Suggestion>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        // Today's Usage card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Today's Usage", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            usageKwh,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f)
                        )
                        ChangePill(changePercent)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(costEstimate, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(co2, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        // My Appliances header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("My Appliances", fontWeight = FontWeight.SemiBold)
                Text("View Usage", color = MaterialTheme.colorScheme.primary)
            }
        }

        // Appliance list
        items(appliances) { appliance ->
            ApplianceCard(appliance)
            Spacer(Modifier.height(10.dp))
        }

        // Smart Suggestions header
        item {
            Text(
                "Smart Suggestions",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }

        // Suggestions list
        items(suggestions) { tip ->
            SuggestionCard(tip)
            Spacer(Modifier.height(10.dp))
        }

        // Bottom actions
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Weight applied here (RowScope) and ActionCard has modifier first
                ActionCard(modifier = Modifier.weight(1f), label = "Usage Graph")
                ActionCard(modifier = Modifier.weight(1f), label = "Cost Calculator")
            }
        }
    }
}

/* ------------------------------- COMPONENTS -------------------------------- */

@Composable
private fun ChangePill(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 12.sp)
    }
}

@Composable
private fun ApplianceCard(appliance: Appliance) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Simple emoji block to match your Figma quickly
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) { Text(appliance.iconEmoji, fontSize = 20.sp) }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(appliance.name, fontWeight = FontWeight.SemiBold)
                    Text(
                        appliance.spec,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(appliance.costPerDay, fontWeight = FontWeight.Medium)
                    Text(appliance.kwh, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { /* no-op */ },
                    modifier = Modifier.weight(1f)
                ) { Text("Edit") }

                OutlinedButton(
                    onClick = { /* no-op */ },
                    modifier = Modifier.weight(1f)
                ) { Text("Delete") }
            }
        }
    }
}

@Composable
private fun SuggestionCard(suggestion: Suggestion) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) { Text(suggestion.leadingEmoji) }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(suggestion.title, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text(
                    suggestion.body,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

// NOTE: modifier is the first optional parameter (avoids the lint warning)
@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    label: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(72.dp)
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontWeight = FontWeight.Medium)
        }
    }
}

private data class NavItem(val label: String, val icon: ImageVector)

/* -------------------------------- PREVIEW ---------------------------------- */

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ElectricityPreview() {
    FIT5046Lab4Group3ass2Theme {
        ElectricityScaffold()
    }
}
