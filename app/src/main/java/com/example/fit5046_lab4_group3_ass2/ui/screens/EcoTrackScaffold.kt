package com.example.fit5046_lab4_group3_ass2.ui.screens

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
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.fit5046_lab4_group3_ass2.R
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme
import kotlin.math.max

/* ------------------------------- SCAFFOLD ------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoTrackScaffold(
    // Forward these so previews can tweak values but still keep full chrome (app bar + nav)
    todayKwh: Float = 8.2f,
    avgKwh: Float = 12.6f,
    rrpAudPerMwh: Float = 132f,
    selectedPeriodIndex: Int = 0,
    kpiTodayText: String = "8.2",
    kpiVsYesterdayText: String = "-12%",
    kpiCostTodayText: String = "$2.46"
) {
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
                title = { Text("EcoTrack") },
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
            EcoTrackScreen(
                todayKwh = todayKwh,
                avgKwh = avgKwh,
                rrpAudPerMwh = rrpAudPerMwh,
                selectedPeriodIndex = selectedPeriodIndex,
                kpiTodayText = kpiTodayText,
                kpiVsYesterdayText = kpiVsYesterdayText,
                kpiCostTodayText = kpiCostTodayText
            )
        }
    }
}

/* -------------------------------- SCREEN -------------------------------- */

private enum class PriceSeverity { Normal, High, Severe }

/** Parameterized so previews (and Scaffold) can pass values. */
@Composable
fun EcoTrackScreen(
    todayKwh: Float = 8.2f,
    avgKwh: Float = 12.6f,
    rrpAudPerMwh: Float = 132f,
    selectedPeriodIndex: Int = 0,
    kpiTodayText: String = "8.2",
    kpiVsYesterdayText: String = "-12%",
    kpiCostTodayText: String = "$2.46"
) {
    val severity = when {
        rrpAudPerMwh > 200f -> PriceSeverity.Severe
        rrpAudPerMwh > 100f -> PriceSeverity.High
        else -> PriceSeverity.Normal
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp)
    ) {
        item { PeriodChips(selectedIndex = selectedPeriodIndex) }
        item { PriceHeaderCard(rrpAudPerMwh, severity) }

        if (severity != PriceSeverity.Normal) {
            item { PriceAlertBanner(rrpAudPerMwh, severity) }
        }

        item { ChartPlaceholderCard() }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                KpiCard(value = kpiTodayText,       label = "Today (kWh)",  modifier = Modifier.weight(1f))
                KpiCard(value = kpiVsYesterdayText, label = "vs Yesterday", modifier = Modifier.weight(1f))
                KpiCard(value = kpiCostTodayText,   label = "Cost Today",   modifier = Modifier.weight(1f))
            }
        }

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
            SegChip("Daily",   selectedIndex == 0, Modifier.weight(1f), sel, bg)
            SegChip("Weekly",  selectedIndex == 1, Modifier.weight(1f), sel, bg)
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

/** compact “today’s price” card */
@Composable
private fun PriceHeaderCard(rrpAudPerMwh: Float, severity: PriceSeverity) {
    val (badgeColor, badgeText, badgeIcon) = when (severity) {
        PriceSeverity.Severe -> Triple(
            MaterialTheme.colorScheme.errorContainer, "Severe", Icons.Filled.Warning
        )
        PriceSeverity.High -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer, "High", Icons.Filled.Info
        )
        PriceSeverity.Normal -> Triple(
            MaterialTheme.colorScheme.surfaceVariant, "Normal", Icons.Filled.Info
        )
    }

    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Today’s Price", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${rrpAudPerMwh.toInt()} AUD/MWh",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f)
                )
                AssistChip(
                    onClick = { /* UI only */ },
                    label = { Text(badgeText) },
                    leadingIcon = { Icon(badgeIcon, contentDescription = null) },
                    colors = AssistChipDefaults.assistChipColors(containerColor = badgeColor)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Alerts: High > 100 • Severe > 200",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Optional banner — only when High/Severe */
@Composable
private fun PriceAlertBanner(rrpAudPerMwh: Float, severity: PriceSeverity) {
    val (containerColor, title, message, icon) = when (severity) {
        PriceSeverity.Severe -> Quad(
            MaterialTheme.colorScheme.errorContainer,
            "Severe price alert",
            "Prices are very high (> 200 AUD/MWh). Cutting back now can save a lot.",
            Icons.Filled.Warning
        )
        PriceSeverity.High -> Quad(
            MaterialTheme.colorScheme.tertiaryContainer,
            "High price alert",
            "Electricity prices are high (> 100 AUD/MWh). Consider reducing usage.",
            Icons.Filled.Info
        )
        PriceSeverity.Normal -> return
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

/** tiny helper to return 4 values */
private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

@Composable
private fun ChartPlaceholderCard() {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.line_chart_image),
                contentDescription = "Daily usage chart",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop  // or ContentScale.Fit if you prefer no cropping
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

/* -------------------------------- PREVIEWS -------------------------------- */

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EcoTrackPreview() {
    FIT5046Lab4Group3ass2Theme {
        EcoTrackScaffold()
    }
}

@Preview(name = "EcoTrack – Tunable", showBackground = true, showSystemUi = true)
@Composable
fun EcoTrackPreview_Tunable() {
    // 19/01/2019 dataset - Daily Electricity Price and Demand Data
    FIT5046Lab4Group3ass2Theme {
        EcoTrackScaffold(
            todayKwh = 9.6f,
            avgKwh = 12.0f,
            rrpAudPerMwh = 80f,         // Normal tier
            selectedPeriodIndex = 0,
            kpiTodayText = "9.6",
            kpiVsYesterdayText = "-8%",
            kpiCostTodayText = "$2.75"
        )
    }
}

@Preview(name = "EcoTrack – High Price", showBackground = true, showSystemUi = true)
@Composable
fun EcoTrackPreview_High() {
    FIT5046Lab4Group3ass2Theme {
        // 23/01/2019 dataset - Daily Electricity Price and Demand Data
        EcoTrackScaffold(rrpAudPerMwh = 154f) // shows High alert
    }
}

@Preview(name = "EcoTrack – Severe Price", showBackground = true, showSystemUi = true)
@Composable
fun EcoTrackPreview_Severe() {
    FIT5046Lab4Group3ass2Theme {
        // 15/01/2019 dataset - Daily Electricity Price and Demand Data
        EcoTrackScaffold(rrpAudPerMwh = 222f) // shows Severe alert
    }
}
