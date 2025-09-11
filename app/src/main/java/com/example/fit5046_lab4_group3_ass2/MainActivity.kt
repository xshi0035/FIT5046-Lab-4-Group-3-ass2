package com.example.fit5046_lab4_group3_ass2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme
import kotlin.math.max

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FIT5046Lab4Group3ass2Theme {
                EcoTrackScaffold()
            }
        }
    }
}

/* ------------------------------- SCAFFOLD ------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoTrackScaffold() {
    val navItems = listOf(
        "Home" to Icons.Filled.Home,
        "Appliances" to Icons.Filled.Add,
        "EcoTrack" to Icons.Filled.Info,
        "Reward" to Icons.Filled.Star,
        "Profile" to Icons.Filled.AccountCircle
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Usage Analysis") },
                navigationIcon = {
                    IconButton(onClick = { /* UI only */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* UI only */ }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                navItems.forEachIndexed { index, (label, icon) ->
                    NavigationBarItem(
                        selected = index == 2, // EcoTrack selected
                        onClick = { /* UI only */ },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
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
            EcoTrackScreen()
        }
    }
}

/* -------------------------------- SCREEN -------------------------------- */

@Composable
fun EcoTrackScreen() {
    // Static demo values — UI only
    val todayKwh = 8.2f
    val avgKwh = 12.6f
    val rrpAudPerMwh = 132f // drives alert banner style (UI only)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp) // keep last card above nav bar
    ) {
        item { PeriodChips(selectedIndex = 0) }  // Daily

        item { ChartPlaceholderCard() }          // empty chart holder

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                KpiCard(value = "8.2", label = "Today (kWh)", modifier = Modifier.weight(1f))
                KpiCard(value = "-12%", label = "vs Yesterday", modifier = Modifier.weight(1f))
                KpiCard(value = "$2.46", label = "Cost Today", modifier = Modifier.weight(1f))
            }
        }

        item { PriceAlertBanner(rrpAudPerMwh = rrpAudPerMwh) }

        item {
            UsageVsAverageCard(
                yourLabel = "Your Usage",
                yourText = "${todayKwh} kWh",
                yourValue = todayKwh,
                avgLabel = "Average Household",
                avgText = "${avgKwh} kWh",
                avgValue = avgKwh
            )
        }

        item { Text("Environmental Impact", fontWeight = FontWeight.SemiBold) }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                SmallImpactCard(
                    title = "4.4 kWh",
                    subtitle = "Energy Saved\nvs Last Week",
                    modifier = Modifier.weight(1f)
                )
                SmallImpactCard(
                    title = "2.1 kg",
                    subtitle = "CO₂ Reduced\nThis Week",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item { ImpactLine(label = "Equivalent Impact", valueRight = "= Planting 2 trees") }
        item { ImpactLine(label = "Carbon Offset", valueRight = "= 8.5 km less driving") }
    }
}

/* ------------------------------ COMPONENTS ------------------------------ */

@Composable
private fun PeriodChips(selectedIndex: Int) {
    val bg = MaterialTheme.colorScheme.surfaceVariant
    val sel = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            SegChip("Daily", selectedIndex == 0, Modifier.weight(1f), sel, bg)
            SegChip("Weekly", selectedIndex == 1, Modifier.weight(1f), sel, bg)
            SegChip("Monthly", selectedIndex == 2, Modifier.weight(1f), sel, bg)
        }
    }
}

@Composable
private fun SegChip(
    label: String,
    isSelected: Boolean,
    modifier: Modifier,
    selectedColor: Color,
    unselectedColor: Color
) {
    Surface(
        color = if (isSelected) selectedColor else unselectedColor,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier.height(36.dp)
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(label, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ChartPlaceholderCard() {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Placeholder only – no real chart
            Text("Daily Usage Chart", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                "kWh consumption over time",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun KpiCard(value: String, label: String, modifier: Modifier = Modifier) {
    Card(shape = RoundedCornerShape(14.dp), modifier = modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(2.dp))
            Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PriceAlertBanner(rrpAudPerMwh: Float) {
    val containerColor: Color
    val icon: ImageVector
    val title: String
    val message: String

    when {
        rrpAudPerMwh > 200f -> {
            containerColor = MaterialTheme.colorScheme.errorContainer
            icon = Icons.Filled.Warning
            title = "Severe price alert"
            message = "Prices are very high (> 200 AUD/MWh). Cutting back now can save a lot."
        }
        rrpAudPerMwh > 100f -> {
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
            icon = Icons.Filled.Info
            title = "High price alert"
            message = "Electricity prices are high (> 100 AUD/MWh). Consider reducing usage."
        }
        else -> {
            containerColor = MaterialTheme.colorScheme.surfaceVariant
            icon = Icons.Filled.Info
            title = "Normal price"
            message = "Prices look normal today. No action needed."
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(message, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AssistChip(onClick = { /* UI only */ }, label = { Text("Details") })
        }
    }
}

@Composable
private fun UsageVsAverageCard(
    yourLabel: String,
    yourText: String,
    yourValue: Float,
    avgLabel: String,
    avgText: String,
    avgValue: Float
) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Your Usage vs Average", fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))

            val base = max(yourValue, avgValue).coerceAtLeast(0.0001f)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(yourLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(yourText)
            }
            LinearProgressIndicator(
                progress = { (yourValue / base).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(avgLabel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(avgText)
            }
            LinearProgressIndicator(
                progress = { (avgValue / base).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )

            Spacer(Modifier.height(12.dp))
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "You're using 35% less than average!",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun SmallImpactCard(title: String, subtitle: String, modifier: Modifier = Modifier) {
    Card(shape = RoundedCornerShape(14.dp), modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ImpactLine(label: String, valueRight: String) {
    Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(label, fontWeight = FontWeight.Medium)
                Text(
                    "Environmental benefit",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(valueRight, fontWeight = FontWeight.Medium)
        }
    }
}

/* -------------------------------- PREVIEW -------------------------------- */

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EcoTrackPreview() {
    FIT5046Lab4Group3ass2Theme {
        EcoTrackScaffold()
    }
}
