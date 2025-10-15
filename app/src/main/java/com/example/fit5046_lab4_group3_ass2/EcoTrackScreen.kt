package com.example.fit5046_lab4_group3_ass2

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.max


/* ------------------------------- SCAFFOLD ------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EcoTrackScaffold(
    viewModel: EcoTrackScreenViewModel,
    // Forward these so previews can tweak values but still show full chrome
    todayKwh: Float = 8.2f,
    avgKwh: Float = 12.6f,
    rrpAudPerMwh: Float = 132f,
    selectedPeriodIndex: Int = 0,
    kpiTodayText: String = "8.2",
    kpiVsYesterdayText: String = "-12%",
    kpiCostTodayText: String = "$2.46",
    onBack: () -> Unit = {},
    // Which tab should appear selected in the bar
    currentRoute: String = ROUTE_ECOTRACK,

    // Bottom bar navigation
    onTabSelected: (route: String) -> Unit = {},
) {
    val navItems: List<Pair<String, ImageVector>> = listOf(
        "Home" to Icons.Filled.Home,
        "Appliances" to Icons.Filled.Add,
        "EcoTrack" to Icons.Filled.Info,
        "Rewards" to Icons.Filled.Star,   // <- plural for consistency
        "Profile" to Icons.Filled.AccountCircle
    )

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
                    // Use the same bell-with-dot pattern as Rewards screen
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
            EcoTrackScreen(
                viewModel,
                todayKwh = todayKwh,
                avgKwh = avgKwh,
                rrpAudPerMwh = price,
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
    viewModel: EcoTrackScreenViewModel,
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

    var selectedMode by remember { mutableStateOf(1) }
    val modes = listOf(1, 7, 30)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(top = 4.dp, bottom = 96.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                modes.forEach { mode ->
                    FilterChip(
                        modifier = Modifier.height(36.dp).weight(1f),
                        selected = selectedMode == mode,
                        onClick = { selectedMode = mode },
                        label = {
                            Text(
                                text = "$mode days",
                                color = Color.Black
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(MaterialTheme.colorScheme.surface.toArgb()),
                            containerColor = Color(MaterialTheme.colorScheme.surfaceVariant.toArgb())
                        )
                    )
                }
            }
        }
        item { PriceHeaderCard(rrpAudPerMwh, severity) }

        if (severity != PriceSeverity.Normal) {
            item { PriceAlertBanner(rrpAudPerMwh, severity) }
        }

        //item { ChartPlaceholderCard() }
        item { LineChartScreen(viewModel, selectedMode) }

        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                KpiCard(value = kpiTodayText, label = "Today (kWh)", modifier = Modifier.weight(1f))
                KpiCard(
                    value = kpiVsYesterdayText,
                    label = "vs Yesterday",
                    modifier = Modifier.weight(1f)
                )
                KpiCard(
                    value = kpiCostTodayText,
                    label = "Cost Today",
                    modifier = Modifier.weight(1f)
                )
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
    }
}

/* ------------------------------ COMPONENTS ------------------------------ */
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

    var price_text = ""

    if (rrpAudPerMwh == 0f) {
        price_text = "Price currently unavailable"
    }
    else {
        price_text = "${rrpAudPerMwh.toInt()} AUD/MWh"
    }

    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Latest Price", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = price_text,
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
            Text("Your Usage vs Average", style = MaterialTheme.typography.titleMedium)
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
fun LineChartScreen(viewModel: EcoTrackScreenViewModel, days_to_show: Int = 1) {
    val dayUseList by viewModel.allDayUses.collectAsState(emptyList())

    val dayUseList_to_show = dayUseList.takeLast(days_to_show * 4 * 24)

    if (dayUseList_to_show.isNotEmpty()) {
        val entries = dayUseList_to_show.mapIndexed { index, dayUse ->
            Entry(index.toFloat(), dayUse.use)
        }
        /*
        val dataSet = LineDataSet(entries, "Energy use (values recorded every 15 minutes)").apply {
            colors = ColorTemplate.COLORFUL_COLORS.toList()
        }

        val lineData = LineData(dataSet)
        lineData.setDrawValues(false)
*/
        val chartRef = remember { mutableStateOf<LineChart?>(null) }

        // Update chart data when entries change
        LaunchedEffect(entries) {
            chartRef.value?.let { chart ->
                val dataSet = LineDataSet(entries, "Energy use (values recorded every 15 minutes)").apply {
                    colors = ColorTemplate.COLORFUL_COLORS.toList()
                }
                val lineData = LineData(dataSet).apply {
                    setDrawValues(false)
                }
                chart.data = lineData
                chart.notifyDataSetChanged()
                chart.invalidate()
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
                    animateY(4000)
                    axisLeft.setAxisMinimum(0f)
                    axisLeft.setAxisMaximum(4f)
                }
            }
        )
    } else {
        Text("Loading chart data...")

    }
}

data class CsvPowerRecord(
    val index: String,
    val Date: String,
    val Time: String,
    val Global_active_power: String
)

object CsvRecordsObject {
    private var records: List<CsvPowerRecord>? = null
    fun setRecords(new_records: List<CsvPowerRecord>) {
        records = new_records
    }

    fun getRecords(): List<CsvPowerRecord>? {
        return records
    }
}

fun loadCsvData(context: Context, fileName: String) {
    val records = mutableListOf<CsvPowerRecord>()
    try {
        val inputStream = context.assets.open(fileName)
        val reader = BufferedReader(InputStreamReader(inputStream))
        reader.useLines { lines ->
            lines.drop(1).forEach { line -> // Skip header
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
    //set the records so they can be accessed by sensor data provider. this is not very neat
    CsvRecordsObject.setRecords(records)
    return
}