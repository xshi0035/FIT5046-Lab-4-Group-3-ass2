package com.example.fit5046_lab4_group3_ass2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.max

/* ROUTE_* constants come from EcoBottomBar.kt */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePageScaffold(
    currentRoute: String = ROUTE_HOME,
    onTabSelected: (route: String) -> Unit = {},
    onAddAppliance: () -> Unit = {},
    onOpenEcoTrack: () -> Unit = {},
    onViewTips: () -> Unit = {},
    onViewRewards: () -> Unit = {},
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

/** Small util for pretty numbers like 2,450 */
private fun fmtInt(i: Int): String = NumberFormat.getIntegerInstance().format(i)
private fun fmtKwh(v: Double): String = String.format(Locale.getDefault(), "%.1f kWh", v)
private fun pctText(deltaPct: Double?): String =
    deltaPct?.let { (if (it >= 0) "+" else "") + String.format(Locale.getDefault(), "%.0f%%", it) } ?: ""

/** Same card renderer you already had */
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
    icon: ImageVector,
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

/* --------------------------- DATA-BACKED HOME ------------------------------ */

@Composable
private fun Home(
    onAddAppliance: () -> Unit,
    onOpenEcoTrack: () -> Unit,
    onViewTips: () -> Unit,
    onViewRewards: () -> Unit
) {
    // Firebase handles
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val uid = auth.currentUser?.uid

    // EcoPoints total (same as Rewards page: sum of users/{uid}/rewards points)
    var ecoPoints by remember { mutableStateOf<Int?>(null) }

    // Electricity today (same as EcoTrack estimation from appliances)
    var todayKwh by remember { mutableStateOf<Double?>(null) }

    // % vs last estimate (from metrics/lastEstimate.kwh)
    var deltaPct by remember { mutableStateOf<Double?>(null) }

    // Small loading states
    var isLoadingRewards by remember { mutableStateOf(true) }
    var isLoadingElectric by remember { mutableStateOf(true) }

    /* -------- Listen to rewards to compute EcoPoints total -------- */
    DisposableEffect(uid) {
        if (uid == null) return@DisposableEffect onDispose { }
        val reg: ListenerRegistration =
            db.collection("users").document(uid).collection("rewards")
                .addSnapshotListener { snap, e ->
                    if (e != null) {
                        isLoadingRewards = false
                        return@addSnapshotListener
                    }
                    val total = snap?.documents?.sumOf { (it.getLong("points") ?: 0L).toInt() } ?: 0
                    ecoPoints = total
                    isLoadingRewards = false
                }
        onDispose { reg.remove() }
    }

    /* -------- Read appliances to estimate today's kWh (same as EcoTrack) ---- */
    // And read metrics/lastEstimate to compute % change and a progress fraction
    LaunchedEffect(uid) {
        if (uid == null) return@LaunchedEffect
        // 1) appliances → sum(watt * hours / 1000)
        db.collection("users").document(uid).collection("appliances")
            .get()
            .addOnSuccessListener { docs ->
                var sum = 0.0
                docs.forEach { d ->
                    val watt = (d.getLong("watt") ?: 0L).toInt()
                    val hours = (d.getDouble("hours") ?: 0.0)
                    sum += max(0.0, watt * hours / 1000.0)
                }
                todayKwh = sum
                isLoadingElectric = false
            }
            .addOnFailureListener { isLoadingElectric = false }

        // 2) baseline for delta %
        db.collection("users").document(uid)
            .collection("metrics").document("lastEstimate")
            .get()
            .addOnSuccessListener { d ->
                val base = d.getDouble("kwh")
                val cur = todayKwh
                if (base != null && base > 0 && cur != null) {
                    deltaPct = ((cur - base) / base) * 100.0
                } else {
                    deltaPct = null
                }
            }
    }

    /* --------- UI ----------------------------------------------------------- */

    val pointsText = when {
        isLoadingRewards -> "—"
        ecoPoints == null -> "0"
        else -> fmtInt(ecoPoints!!)
    }

    val kwhText = when {
        isLoadingElectric -> "—"
        todayKwh == null -> "0.0 kWh"
        else -> fmtKwh(todayKwh!!)
    }

    // Progress bar for the electricity card: compare to baseline if present,
    // otherwise show no bar.
    val progressFraction: Float = run {
        val cur = todayKwh
        var base: Double? = null
        // We can't block; recompute base quickly from delta if available:
        if (cur != null && deltaPct != null && deltaPct!!.isFinite()) {
            // deltaPct = (cur - base) / base  => base = cur / (1 + deltaPct)
            val denom = 1.0 + (deltaPct!! / 100.0)
            if (denom != 0.0) base = cur / denom
        }
        if (cur != null && base != null && base!! > 0.0) {
            (cur / max(cur, base!!)).toFloat().coerceIn(0f, 1f)
        } else 0f
    }

    val rightDeltaText = pctText(deltaPct)

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

                // EcoPoints – shows the SAME total as Rewards
                HomeScreenCard(
                    title = "EcoPoints",
                    mainText = pointsText,
                    smallText = "Your total points",
                    rightText = "\uD83C\uDFC6",
                    tonal = true,
                    valueStyle = MaterialTheme.typography.displaySmall
                )

                // Electricity – shows the SAME 'today estimate' as EcoTrack
                HomeScreenCard(
                    title = "⚡ Electricity",
                    mainText = kwhText,
                    smallText = "Today's usage",
                    progress = progressFraction,
                    rightText = rightDeltaText,
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
                        icon = Icons.Filled.Comment,
                        label = "View Tips",
                        modifier = Modifier.weight(1f),
                        onClick = onViewTips
                    )
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
    }
}

/* -------------------------------- PREVIEW ---------------------------------- */

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
