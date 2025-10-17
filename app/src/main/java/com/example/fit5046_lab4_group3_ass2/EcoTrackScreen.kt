package com.example.fit5046_lab4_group3_ass2

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.fit5046_lab4_group3_ass2.ui.screens.EcoBottomBar
import com.example.fit5046_lab4_group3_ass2.ui.screens.ROUTE_ECOTRACK
import com.example.fit5046_lab4_group3_ass2.ui.screens.ROUTE_REWARDS
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

/* ------------------------------- SCAFFOLD ------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoTrackScaffold(
    viewModel: EcoTrackScreenViewModel,
    todayKwh: Float = 8.2f,
    avgKwh: Float = 12.6f,
    rrpAudPerMwh: Float = 132f,
    selectedPeriodIndex: Int = 0,
    kpiTodayText: String = "8.2",
    kpiVsYesterdayText: String = "-12%",
    kpiCostTodayText: String = "$2.46",
    onBack: () -> Unit = {},
    currentRoute: String = ROUTE_ECOTRACK,
    onTabSelected: (route: String) -> Unit = {},
    onGoToElectricity: () -> Unit = {}
) {
    LaunchedEffect(Unit) { viewModel.customSearch() }
    val itemsReturned by viewModel.retrofitResponse
    val price = if (itemsReturned.data.isNotEmpty()) {
        itemsReturned.data[0].results[0].data
            .last { it.size > 1 && it[1] is Number }[1].toString().toFloat()
    } else 0f

    viewModel.storeARecord()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("EcoTrack") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { /* UI only */ }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(10.dp)
                                .background(Color(0xFF8A2BE2), shape = RoundedCornerShape(50))
                        )
                    }
                }
            )
        },
        bottomBar = {
            EcoBottomBar(currentRoute = currentRoute, onTabSelected = onTabSelected)
        }
    ) { inner ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            EcoTrackScreen(
                viewModel,
                todayKwh = todayKwh,
                avgKwh = avgKwh,
                rrpAudPerMwh = price,
                selectedPeriodIndex = selectedPeriodIndex,
                kpiTodayText = kpiTodayText,
                kpiVsYesterdayText = kpiVsYesterdayText,
                kpiCostTodayText = kpiCostTodayText,
                onGoToElectricity = onGoToElectricity,
                onOpenRewards = { onTabSelected(ROUTE_REWARDS) }
            )
        }
    }
}

/* -------------------------------- SCREEN -------------------------------- */

private enum class PriceSeverity { Normal, High, Severe }
private enum class UsageSeverity { Low, Normal, High }

@Composable
fun EcoTrackScreen(
    viewModel: EcoTrackScreenViewModel,
    todayKwh: Float = 8.2f,
    avgKwh: Float = 12.6f,
    rrpAudPerMwh: Float = 132f,
    selectedPeriodIndex: Int = 0,
    kpiTodayText: String = "8.2",
    kpiVsYesterdayText: String = "-12%",
    kpiCostTodayText: String = "$2.46",
    onGoToElectricity: () -> Unit = {},
    onOpenRewards: () -> Unit = {}
) {
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
        rrpAudPerMwh > 200f -> PriceSeverity.Severe
        rrpAudPerMwh > 100f -> PriceSeverity.High
        else -> PriceSeverity.Normal
    }

    var selectedMode by remember { mutableStateOf(1f) }
    val modes = listOf(0.25f, 1f, 7f, 30f)
    val modeNames = mapOf(0.25f to "6 hours", 1f to "1 day", 7f to "Week", 30f to "Month")

    var latestKw by remember { mutableStateOf(0f) }
    var showOveruse by remember { mutableStateOf(false) }
    LaunchedEffect(latestKw) { showOveruse = latestKw > 3.0f }

    // ---------- KPIs from Firestore + baseline compare ----------
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val uid = auth.currentUser?.uid

    var kpiToday by remember { mutableStateOf(kpiTodayText) }
    var kpiCost by remember { mutableStateOf(kpiCostTodayText) }
    var kpiVsLast by remember { mutableStateOf("—") }

    var currentEstimateKwh by remember { mutableStateOf(0.0) }
    var baselineKwh by remember { mutableStateOf<Double?>(null) }
    var backfilled by remember { mutableStateOf(false) }

    var pendingPoints by remember { mutableStateOf(0) }
    var pendingBadges by remember { mutableStateOf(0) }

    // 1) Read appliances -> current estimate
    LaunchedEffect(uid) {
        if (uid == null) return@LaunchedEffect
        db.collection("users").document(uid).collection("appliances")
            .get()
            .addOnSuccessListener { docs ->
                var sumKwh = 0.0
                docs.forEach { d ->
                    val watt = (d.getLong("watt") ?: 0L).toInt()
                    val hours = (d.getDouble("hours") ?: 0.0).toFloat()
                    val kwh = max(0f, watt * hours / 1000f)
                    sumKwh += kwh
                }
                currentEstimateKwh = sumKwh
                val cost = sumKwh * 0.30
                kpiToday = String.format(Locale.getDefault(), "%.2f", sumKwh)
                kpiCost = String.format(Locale.getDefault(), "$%.2f", cost)
            }
            .addOnFailureListener { e -> Log.w("EcoTrack", "Failed to read appliances", e) }

        db.collection("users").document(uid)
            .collection("metrics").document("pendingRewards")
            .get()
            .addOnSuccessListener { d ->
                pendingPoints = (d.getLong("points") ?: 0L).toInt()
                pendingBadges = (d.getLong("badges") ?: 0L).toInt()
            }
    }

    // 2) Listen to baseline (metrics/lastEstimate)
    DisposableEffect(uid) {
        var reg: ListenerRegistration? = null
        if (uid != null) {
            reg = db.collection("users").document(uid)
                .collection("metrics").document("lastEstimate")
                .addSnapshotListener { doc, _ ->
                    baselineKwh = doc?.getDouble("kwh")
                }
        }
        onDispose { reg?.remove() }
    }

    // 3) Compute % vs baseline, and LOG daily savedPct once/day
    fun dateKey(d: Date = Date()) =
        SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(d)

    var savedLoggedKey by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentEstimateKwh, baselineKwh, uid) {
        val base = baselineKwh
        val id = uid ?: return@LaunchedEffect
        if (base == null || base <= 0.0) {
            kpiVsLast = "—"
            return@LaunchedEffect
        }
        val pct = ((currentEstimateKwh - base) / base) * 100.0
        val sign = if (pct >= 0) "+" else ""
        kpiVsLast = String.format(Locale.getDefault(), "%s%.0f%%", sign, pct)

        // Write one record per day to metrics/savingsLog/days/{yyyyMMdd}
        val key = dateKey()
        if (savedLoggedKey != key) {
            savedLoggedKey = key
            val path = db.collection("users").document(id)
                .collection("metrics").document("savingsLog")
                .collection("days").document(key)

            val data = mapOf(
                "kwh" to currentEstimateKwh,
                "baselineKwh" to base,
                "savedPct" to pct,
                "ts" to FieldValue.serverTimestamp()
            )
            path.set(data)
        }
    }

    // 4) Auto-backfill baseline ONCE
    LaunchedEffect(currentEstimateKwh, baselineKwh, uid) {
        val id = uid ?: return@LaunchedEffect
        if (!backfilled && baselineKwh == null && currentEstimateKwh > 0.0) {
            backfilled = true
            db.collection("users").document(id)
                .collection("metrics").document("lastEstimate")
                .set(mapOf("kwh" to currentEstimateKwh, "ts" to System.currentTimeMillis()))
        }
    }

    fun saveBaselineAndNavigate() {
        val id = uid ?: return onGoToElectricity()
        db.collection("users").document(id)
            .collection("metrics").document("lastEstimate")
            .set(mapOf("kwh" to currentEstimateKwh, "ts" to System.currentTimeMillis()))
            .addOnCompleteListener { onGoToElectricity() }
    }
    // -------------------------------------------------------------------------

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp)
    ) {
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val sc = MaterialTheme.colorScheme
                modes.forEach { mode ->
                    FilterChip(
                        modifier = Modifier
                            .height(36.dp)
                            .weight(1f),
                        selected = selectedMode == mode,
                        onClick = { selectedMode = mode },
                        label = { Text("${modeNames[mode]}", color = Color.Black) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(sc.surface.toArgb()),
                            containerColor = Color(sc.surfaceVariant.toArgb())
                        )
                    )
                }
            }
        }

        item { PriceHeaderCard(rrpAudPerMwh, severity) }
        if (severity != PriceSeverity.Normal) item { PriceAlertBanner(rrpAudPerMwh, severity) }

        item {
            LineChartScreen(
                viewModel = viewModel,
                days_to_show = selectedMode,
                onLatestValue = { latestKw = it }
            )
        }
        // low or high usage banner
        if (currentUse != UsageSeverity.Normal) {
            item { UsageAlertBanner(latest_use, currentUse) }
        }

        item {
            OverusePrompt(
                visible = showOveruse,
                currentKw = latestKw,
                onDismiss = { showOveruse = false },
                onBeforeNavigate = { saveBaselineAndNavigate() },
                onGoToElectricity = { /* unused here */ }
            )
        }

        // Estimated KPIs
        item {
            Text(
                "Estimated (from appliances)",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
            Spacer(Modifier.height(6.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                KpiCard(value = kpiToday, label = "Today (kWh)", modifier = Modifier.weight(1f))
                KpiCard(value = kpiVsLast, label = "vs Last estimate", modifier = Modifier.weight(1f))
                KpiCard(value = kpiCost, label = "Cost Today", modifier = Modifier.weight(1f))
            }
        }

        // Rewards teaser (Open button: dark blue bg + white text)
        item {
            RewardsTeaserCard(
                points = pendingPoints,
                badges = pendingBadges,
                onOpenRewards = onOpenRewards
            )
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
    }
}

/* ------------------------------ COMPONENTS ------------------------------ */

@Composable
private fun PriceHeaderCard(rrpAudPerMwh: Float, severity: PriceSeverity) {
    val (badgeColor, badgeText, badgeIcon) = when (severity) {
        PriceSeverity.Severe -> Triple(MaterialTheme.colorScheme.errorContainer, "Severe", Icons.Filled.Warning)
        PriceSeverity.High -> Triple(MaterialTheme.colorScheme.tertiaryContainer, "High", Icons.Filled.Info)
        PriceSeverity.Normal -> Triple(MaterialTheme.colorScheme.surfaceVariant, "Normal", Icons.Filled.Info)
    }
    val priceText = if (rrpAudPerMwh == 0f) "Price currently unavailable" else "${rrpAudPerMwh.toInt()} AUD/MWh"

    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Latest Price", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = priceText,
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
            Text("Alerts: High > 100 • Severe > 200", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

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

///** tiny helper to return 4 values */
//data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)

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

/** Rewards CTA card — Open button dark blue bg + white text */
@Composable
private fun RewardsTeaserCard(
    points: Int,
    badges: Int,
    onOpenRewards: () -> Unit
) {
    val hasSomething = points > 0 || badges > 0
    val title = if (hasSomething) "You’ve got rewards waiting" else "Check your rewards"
    val subtitle = if (hasSomething) {
        buildString {
            if (points > 0) append("$points pts")
            if (points > 0 && badges > 0) append(" • ")
            if (badges > 0) append("$badges badge${if (badges > 1) "s" else ""}")
            if (isEmpty()) append("See what you’ve earned")
        }
    } else {
        "See your points, badges and progress"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                Spacer(Modifier.height(2.dp))
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                )
            }
            Spacer(Modifier.width(10.dp))
            Button(
                onClick = onOpenRewards,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF1E3A8A), // dark blue
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Open")
            }
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
            Text("Your Usage vs Average", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))

            val base = max(yourValue, avgValue).coerceAtLeast(0.0001f)

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(yourLabel, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(yourText)
            }
            LinearProgressIndicator(
                progress = { (yourValue / base).coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
            )
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(avgLabel, color = MaterialTheme.colorScheme.onSurfaceVariant); Text(avgText)
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

/* ----------------------------- CHART ---------------------------- */

@Composable
fun LineChartScreen(
    viewModel: EcoTrackScreenViewModel,
    days_to_show: Float = 1f,
    onLatestValue: (Float) -> Unit = {}
) {
    val dayUseList by viewModel.allDayUses.collectAsState(emptyList())
    val dayUseListToShow = dayUseList.takeLast((days_to_show * 4 * 24).toInt())

    if (dayUseListToShow.isNotEmpty()) {
        val entries = dayUseListToShow.mapIndexed { index, dayUse -> Entry(index.toFloat(), dayUse.use) }
        val latest = dayUseListToShow.last().use
        LaunchedEffect(latest) { onLatestValue(latest) }

        val chartRef = remember { mutableStateOf<LineChart?>(null) }

        LaunchedEffect(entries) {
            chartRef.value?.let { chart ->
                val dataSet = LineDataSet(entries, "Energy use (values recorded every 15 minutes)")
                    .apply { colors = ColorTemplate.COLORFUL_COLORS.toList() }
                val lineData = LineData(dataSet).apply { setDrawValues(false) }
                chart.data = lineData; chart.notifyDataSetChanged(); chart.invalidate()
            }
        }

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            factory = { context ->
                LineChart(context).apply {
                    chartRef.value = this
                    description.isEnabled = false
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.setDrawLabels(false)
                    animateY(400)
                    axisLeft.axisMinimum = 0f
                    axisLeft.axisMaximum = 4f
                }
            }
        )
    } else {
        Text("Loading chart data...")
    }
}

/* ----------------------------- OVERUSE PROMPT ----------------------------- */

@Composable
private fun OverusePrompt(
    visible: Boolean,
    currentKw: Float,
    onDismiss: () -> Unit,
    onBeforeNavigate: () -> Unit = {},
    onGoToElectricity: () -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Filled.Warning, contentDescription = null) },
        title = { Text("Usage spike detected") },
        text = {
            Text(
                "Your current power draw (~${"%.1f".format(currentKw)} kW) is above a typical household right now. " +
                        "Would you like to check your appliances to see which one might be costing you most?"
            )
        },
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onBeforeNavigate()
            }) { Text("Check appliances") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Not now") } }
    )
}

/* --------------------------- CSV utilities (unchanged) -------------------- */

data class CsvPowerRecord(
    val index: String,
    val Date: String,
    val Time: String,
    val Global_active_power: String
)

object CsvRecordsObject {
    private var records: List<CsvPowerRecord>? = null
    fun setRecords(new_records: List<CsvPowerRecord>) { records = new_records }
    fun getRecords(): List<CsvPowerRecord>? = records
}

fun loadCsvData(context: Context, fileName: String) {
    val records = mutableListOf<CsvPowerRecord>()
    try {
        val inputStream = context.assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))
        reader.useLines { lines ->
            lines.drop(1).forEach { line ->
                val values = line.split(',', ';', '\t')
                if (values.size >= 4) {
                    records.add(
                        CsvPowerRecord(
                            index = values[0].trim(),
                            Date = values[1].trim(),
                            Time = values[2].trim(),
                            Global_active_power = values[3].trim()
                        )
                    )
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    CsvRecordsObject.setRecords(records)
}
