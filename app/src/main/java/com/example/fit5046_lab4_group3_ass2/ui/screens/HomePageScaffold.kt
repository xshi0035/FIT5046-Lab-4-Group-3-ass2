package com.example.fit5046_lab4_group3_ass2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fit5046_lab4_group3_ass2.EcoTrackScreenViewModel
import com.example.fit5046_lab4_group3_ass2.Quad
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme

/* ROUTE_* constants now come from EcoBottomBar.kt */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePageScaffold(
    viewModel: EcoTrackScreenViewModel,
    // Which tab should appear selected in the bar
    currentRoute: String = ROUTE_HOME,

    // Bottom bar navigation
    onTabSelected: (route: String) -> Unit = {},

    // Quick actions
    onAddAppliance: () -> Unit = {},
    onOpenEcoTrack: () -> Unit = {},
    onViewTips: () -> Unit = {},
    onViewRewards: () -> Unit = {},

    // Optional top-right action
    onNotificationsClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Home") },
                actions = {
                    Box {
                        IconButton(onClick = onNotificationsClick) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        )
                    }
                }
            )
        },
        bottomBar = {
            EcoBottomBar(
                currentRoute = currentRoute,
                onTabSelected = onTabSelected
            )
        }
    ) { inner ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            Home(
                viewModel = viewModel,
                onAddAppliance = onAddAppliance,
                onOpenEcoTrack = onOpenEcoTrack,
                onViewTips = onViewTips,
                onViewRewards = onViewRewards
            )
        }
    }
}

/* --------------------------------- SCREEN ---------------------------------- */

private data class NavItem(val route: String, val label: String, val icon: ImageVector)

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

private enum class PriceSeverity { Normal, High, Severe }
private enum class UsageSeverity { Low, Normal, High }

@Composable
private fun Home(
    viewModel: EcoTrackScreenViewModel,
    onAddAppliance: () -> Unit,
    onOpenEcoTrack: () -> Unit,
    onViewTips: () -> Unit,
    onViewRewards: () -> Unit
) {
    //get info for recent activity
    //For getting latest prices from API
    LaunchedEffect(Unit) {
        viewModel.customSearch()
    }
    val itemsReturned by viewModel.retrofitResponse
    val price = if (itemsReturned.data.isNotEmpty()) {
        itemsReturned.data[0].results[0].data
            .last { it.size > 1 && it[1] is Number }[1].toString().toFloat()
    } else {
        0f
    }

    val dayUseList by viewModel.allDayUses.collectAsState(emptyList())
    var latest_use : Float
    if (!dayUseList.isEmpty()) {
        latest_use = dayUseList.last().use
    } else {
        latest_use = 2.5f //so that the notification doesn't show
    }
    //latest_use = 1f //testing

    val currentUse = when {
        latest_use < 2f -> UsageSeverity.Low
        latest_use > 3f -> UsageSeverity.High
        else -> UsageSeverity.Normal
    }
    //val rrpAudPerMwh = 300f// testing
    val severity = when {
        price > 200f -> PriceSeverity.Severe
        price > 100f -> PriceSeverity.High
        else -> PriceSeverity.Normal
    }

    var selectedMode by remember { mutableStateOf(1f) }
    val modes = listOf(0.25f, 1f, 7f, 30f)
    val mode_names = mapOf(0.25f to "6 hours", 1f to "1 day", 7f to "Week", 30f to "Month")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
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
                        icon = androidx.compose.material.icons.Icons.Filled.Info,
                        label = "Open EcoTrack",
                        modifier = Modifier.weight(1f),
                        onClick = onOpenEcoTrack
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    QuickActionButton(
                        icon = Icons.Filled.Comment,
                        label = "View Tips",
                        modifier = Modifier.weight(1f),
                        onClick = onViewTips
                    )
                    QuickActionButton(
                        icon = androidx.compose.material.icons.Icons.Filled.Star,
                        label = "View Rewards",
                        modifier = Modifier.weight(1f),
                        onClick = onViewRewards
                    )
                }

                SectionTitle("Recent Activity")
                if (severity != PriceSeverity.Normal) {
                    PriceAlertBanner(price, severity)
                }
                // low or high usage banner
                if (currentUse != UsageSeverity.Normal) {
                    UsageAlertBanner(latest_use, currentUse)
                }
            }
        }
    }
}

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
            //AssistChip(onClick = { /* UI only */ }, label = { Text("Details") })
        }
    }
}

@Composable
private fun UsageAlertBanner(current_usage: Float, usageSeverity: UsageSeverity) {
    val (containerColor, title, message, icon) = when (usageSeverity) {
        UsageSeverity.Low -> Quad(
            MaterialTheme.colorScheme.tertiaryContainer,
            "Low energy use! ${current_usage} kW",
            "Good job!",
            Icons.Filled.Star
        )

        UsageSeverity.High -> Quad(
            MaterialTheme.colorScheme.errorContainer,
            "High energy use",
            "${current_usage} kW",
            Icons.Filled.Warning
        )

        UsageSeverity.Normal -> return
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
            //AssistChip(onClick = { /* UI only */ }, label = { Text("Details") })
        }
    }
}

/* -------------------------------- PREVIEW ---------------------------------- */
/*
@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun HomePageScaffoldPreview() {
    FIT5046Lab4Group3ass2Theme {
        HomePageScaffold(
            currentRoute = ROUTE_HOME,
            onTabSelected = {},
            onAddAppliance = {},
            onOpenEcoTrack = {},
            onViewTips = {},
            onViewRewards = {}
        )
    }
}
*/