package com.example.fit5046_lab4_group3_ass2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/* -------------------------------------------------------------------------- */
/*  Types kept so MainActivity doesn’t break, but they’re not used anymore.   */
/* -------------------------------------------------------------------------- */

data class Badge(val title: String, val subtitle: String, val date: String)
data class LeaderboardEntry(val rank: Int, val name: String, val points: Int, val isYou: Boolean = false)
data class MonthlyProgress(
    val pointsThisMonth: Int,
    val badgesEarned: Int,
    val daysActive: Int,
    val daysInMonth: Int,
    val monthlyGoal: Int
)

/* ------------------------------- Firestore model --------------------------- */

private data class RewardAction(
    val id: String,
    val title: String,
    val description: String,
    val points: Int,
    val cadence: Cadence = Cadence.Daily,
    val category: String = "electricity"
)

private enum class Cadence { Daily, Weekly }

private data class RewardEntry(
    val taskId: String = "",
    val taskTitle: String = "",
    val points: Int = 0,
    val timestamp: Long = 0L,
    val key: String = "",
    val periodKey: String = ""            // <-- for same-day/week detection (stable in first snapshot)
)

/* -------------------------- Actions to earn points ------------------------- */

private val ACTIONS = listOf(
    RewardAction(
        id = "reduce_peak",
        title = "Avoid Peak-Hour Usage",
        description = "Keep heavy appliances off during 6–9pm today.",
        points = 120,
        cadence = Cadence.Daily
    ),
    RewardAction(
        id = "turn_off_standby",
        title = "Eliminate Standby Power",
        description = "Turn off 3 devices at the wall before sleep.",
        points = 80,
        cadence = Cadence.Daily
    ),
    RewardAction(
        id = "optimize_washing",
        title = "Cold Wash & Full Load",
        description = "Wash clothes in cold water with a full load.",
        points = 150,
        cadence = Cadence.Weekly
    ),
    RewardAction(
        id = "lighting_audit",
        title = "Lighting Audit",
        description = "Replace/plan to replace 5 bulbs with LEDs.",
        points = 200,
        cadence = Cadence.Weekly
    )
)

/* ------------------------ Scaffold (keeps your navigation) ----------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScaffold(
    totalPoints: Int,
    electricityPoints: Int,
    badges: List<Badge>,
    leaderboard: List<LeaderboardEntry>,
    monthly: MonthlyProgress,
    currentRoute: String = ROUTE_REWARDS,
    onTabSelected: (route: String) -> Unit = {},
    onBack: () -> Unit = {},
    onNotifications: () -> Unit = {}
) {
    val snackbar = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Rewards") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = onNotifications) {
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
            EcoBottomBar(
                currentRoute = currentRoute,
                onTabSelected = onTabSelected
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { inner ->
        RewardsScreenFirestore(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp),
            snackbar = snackbar
        )
    }
}

/* --------------------------- Firestore-backed screen ----------------------- */

@Composable
private fun RewardsScreenFirestore(
    modifier: Modifier = Modifier,
    snackbar: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val uid = auth.currentUser?.uid

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var entries by remember { mutableStateOf<List<RewardEntry>>(emptyList()) }
    var todayClaimed by remember { mutableStateOf<Set<String>>(emptySet()) }
    var weekClaimed by remember { mutableStateOf<Set<String>>(emptySet()) }

    // Daily / weekly flags + streak
    var dailyCollected by remember { mutableStateOf(false) }
    var weeklyCollected by remember { mutableStateOf(false) }
    var weeklyStreak by remember { mutableStateOf(0) } // 0..7

    // Track last weekly reset day (yyyyMMdd)
    var streakResetDayKey by remember { mutableStateOf<String?>(null) }

    /* -------- Helpers for date keys -------- */
    fun dayKeyOf(date: Date = Date()): String =
        SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(date)

    fun weekKeyOf(date: Date = Date()): String =
        SimpleDateFormat("yyyy-'W'ww", Locale.getDefault()).format(date)

    fun recomputeDailyFlagsFor(key: String, list: List<RewardEntry>) {
        // Claimed tasks for *today* by periodKey (timestamp can be null early)
        todayClaimed = list
            .filter { it.periodKey == key }
            .map { it.taskId }
            .toSet()
        // Daily collect doc id pattern
        dailyCollected = list.any { it.key == "daily_${key}" }
    }

    /* -------- Rewards collection listener -------- */
    DisposableEffect(uid) {
        if (uid == null) {
            isLoading = false
            error = "You’re not signed in."
            return@DisposableEffect onDispose { }
        }

        val reg: ListenerRegistration =
            db.collection("users").document(uid).collection("rewards")
                .addSnapshotListener { snap, e ->
                    if (e != null) {
                        error = e.localizedMessage ?: "Failed to load rewards."
                        isLoading = false
                        return@addSnapshotListener
                    }
                    val list = snap?.documents?.mapNotNull { d ->
                        val tsMillis: Long = when (val raw = d.get("timestamp")) {
                            is Long -> raw
                            is Timestamp -> raw.toDate().time
                            else -> 0L
                        }
                        RewardEntry(
                            taskId = d.getString("taskId") ?: return@mapNotNull null,
                            taskTitle = d.getString("taskTitle") ?: "",
                            points = (d.getLong("points") ?: 0L).toInt(),
                            timestamp = tsMillis,
                            key = d.id,
                            periodKey = d.getString("periodKey") ?: ""     // <-- read periodKey
                        )
                    } ?: emptyList()

                    entries = list
                    isLoading = false
                    error = null

                    val c = Calendar.getInstance()
                    val currentDayKey = dayKeyOf(c.time)
                    val currentWeekKey = weekKeyOf(c.time)

                    // Daily flags
                    recomputeDailyFlagsFor(currentDayKey, list)

                    // Weekly claimed set for actions by periodKey
                    weekClaimed = list
                        .filter { it.periodKey == currentWeekKey }
                        .map { it.taskId }
                        .toSet()

                    // Weekly reward collected? (doc id is deterministic)
                    weeklyCollected = list.any { it.key == "weekly_streak_${currentWeekKey}" }

                    // Latest reset marker (yyyyMMdd in key)
                    streakResetDayKey = list
                        .filter { it.key.startsWith("weekly_reset_") }
                        .maxByOrNull { it.timestamp }
                        ?.key
                        ?.removePrefix("weekly_reset_")
                }

        onDispose { reg.remove() }
    }

    /* -------- LIVE midnight watcher: auto-refresh daily flags -------- */
    var lastSeenDayKey by remember { mutableStateOf(dayKeyOf()) }
    LaunchedEffect(entries) {
        val k = dayKeyOf()
        lastSeenDayKey = k
        recomputeDailyFlagsFor(k, entries)
    }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            val currentKey = dayKeyOf()
            if (currentKey != lastSeenDayKey) {
                lastSeenDayKey = currentKey
                recomputeDailyFlagsFor(currentKey, entries)
            }
        }
    }

    /* -------- Streak computation (savingsLog + optional dailyUsage) -------- */

    fun last7Ids(from: Date = Date()): List<String> {
        val now = Calendar.getInstance().apply { time = from }
        return (0..6).map {
            val c = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, -it) }
            SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(c.time)
        }
    }

    fun docSavedPct(doc: Map<String, Any?>): Double? {
        (doc["savedPct"] as? Number)?.toDouble()?.let { return it }
        (doc["pct"] as? Number)?.toDouble()?.let { return it }
        (doc["deltaPct"] as? Number)?.toDouble()?.let { return it }

        val kwh = (doc["kwh"] as? Number)?.toDouble()
            ?: (doc["estimate"] as? Number)?.toDouble()
        val base = (doc["baselineKwh"] as? Number)?.toDouble()
            ?: (doc["baseline"] as? Number)?.toDouble()
            ?: (doc["lastEstimateKwh"] as? Number)?.toDouble()

        return if (kwh != null && base != null && base > 0.0) ((kwh - base) / base) * 100.0 else null
    }

    fun recomputeStreak(vararg snapshots: QuerySnapshot?) {
        val recentIds = last7Ids()
        val effectiveIds = streakResetDayKey?.let { reset ->
            recentIds.filter { it >= reset }
        } ?: recentIds

        var count = 0
        snapshots.filterNotNull().forEach { snap ->
            snap.documents.forEach { d ->
                val id = d.id
                if (id in effectiveIds) {
                    val pct = docSavedPct(d.data ?: emptyMap())
                    if (pct != null && pct <= -1.0) count += 1
                }
            }
        }
        weeklyStreak = count.coerceIn(0, 7)
    }

    var snapSavings by remember { mutableStateOf<QuerySnapshot?>(null) }
    var snapDaily by remember { mutableStateOf<QuerySnapshot?>(null) }

    // metrics/savingsLog/days/*
    DisposableEffect(uid) {
        if (uid == null) return@DisposableEffect onDispose { }
        val reg = db.collection("users").document(uid)
            .collection("metrics").document("savingsLog")
            .collection("days")
            .addSnapshotListener { snap, _ ->
                snapSavings = snap
                recomputeStreak(snapSavings, snapDaily)
            }
        onDispose { reg.remove() }
    }

    // (optional) dailyUsage/*
    DisposableEffect(uid) {
        if (uid == null) return@DisposableEffect onDispose { }
        val reg = db.collection("users").document(uid)
            .collection("dailyUsage")
            .addSnapshotListener { snap, _ ->
                snapDaily = snap
                recomputeStreak(snapSavings, snapDaily)
            }
        onDispose { reg.remove() }
    }

    /* ------------------------ Derived totals for header ------------------------ */
    val calendar = remember { Calendar.getInstance() }
    val month = calendar.get(Calendar.MONTH)
    val year = calendar.get(Calendar.YEAR)
    val pointsThisMonth = entries.filter {
        val c = Calendar.getInstance().apply { time = Date(it.timestamp) }
        c.get(Calendar.MONTH) == month && c.get(Calendar.YEAR) == year
    }.sumOf { it.points }

    val daysActive = entries.map {
        SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date(it.timestamp))
    }.toSet().size

    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val totalPoints = entries.sumOf { it.points }

    /* ----------------------------- Claim handlers ----------------------------- */
    fun claim(task: RewardAction) {
        val u = auth.currentUser?.uid
        if (u == null) {
            scope.launch { snackbar.showSnackbar("Please sign in to claim points.") }
            return
        }
        val sdf = when (task.cadence) {
            Cadence.Daily -> SimpleDateFormat("yyyyMMdd", Locale.getDefault())
            Cadence.Weekly -> SimpleDateFormat("yyyy-'W'ww", Locale.getDefault())
        }
        val periodKey = sdf.format(Date())
        val docId = "${task.id}_$periodKey"

        val data = mapOf(
            "taskId" to task.id,
            "taskTitle" to task.title,
            "points" to task.points,
            "timestamp" to FieldValue.serverTimestamp(),
            "periodKey" to periodKey
        )

        db.collection("users").document(u).collection("rewards").document(docId)
            .get()
            .addOnSuccessListener { snap ->
                if (snap.exists()) {
                    scope.launch {
                        snackbar.showSnackbar("Already claimed for this ${task.cadence.name.lowercase()}")
                    }
                } else {
                    db.collection("users").document(u).collection("rewards").document(docId)
                        .set(data)
                        .addOnSuccessListener {
                            // Optimistic UI: disable button immediately
                            when (task.cadence) {
                                Cadence.Daily -> todayClaimed = todayClaimed + task.id
                                Cadence.Weekly -> weekClaimed = weekClaimed + task.id
                            }
                            scope.launch { snackbar.showSnackbar("You earned +${task.points} EcoPoints!") }
                        }
                        .addOnFailureListener { e ->
                            scope.launch { snackbar.showSnackbar(e.localizedMessage ?: "Failed to claim") }
                        }
                }
            }
            .addOnFailureListener { e ->
                scope.launch { snackbar.showSnackbar(e.localizedMessage ?: "Failed to claim") }
            }
    }

    fun collectDaily(points: Int = 50) {
        val u = auth.currentUser?.uid ?: return
        val key = dayKeyOf()
        val doc = "daily_${key}"
        val data = mapOf(
            "taskId" to "daily",
            "taskTitle" to "Daily energy saver",
            "points" to points,
            "timestamp" to FieldValue.serverTimestamp(),
            "periodKey" to key
        )
        db.collection("users").document(u).collection("rewards").document(doc)
            .get()
            .addOnSuccessListener { s ->
                if (s.exists()) {
                    scope.launch { snackbar.showSnackbar("Already collected today.") }
                } else {
                    db.collection("users").document(u).collection("rewards").document(doc)
                        .set(data)
                        .addOnSuccessListener {
                            dailyCollected = true
                            scope.launch { snackbar.showSnackbar("Collected +$points points!") }
                        }
                        .addOnFailureListener { e ->
                            scope.launch { snackbar.showSnackbar(e.localizedMessage ?: "Failed to collect") }
                        }
                }
            }
    }

    fun collectWeekly(points: Int = 300) {
        val u = auth.currentUser?.uid ?: return
        if (weeklyStreak < 7) {
            scope.launch { snackbar.showSnackbar("Keep it up! ${7 - weeklyStreak} more day(s) to go.") }
            return
        }

        val wk = weekKeyOf()
        val doc = "weekly_streak_${wk}"
        val data = mapOf(
            "taskId" to "weekly_streak",
            "taskTitle" to "Weekly energy-saver streak",
            "points" to points,
            "timestamp" to FieldValue.serverTimestamp(),
            "periodKey" to wk
        )
        db.collection("users").document(u).collection("rewards").document(doc)
            .get()
            .addOnSuccessListener { s ->
                if (s.exists()) {
                    scope.launch { snackbar.showSnackbar("Weekly reward already collected.") }
                } else {
                    db.collection("users").document(u).collection("rewards").document(doc)
                        .set(data)
                        .addOnSuccessListener {
                            // Write a reset marker + reset UI immediately
                            val todayKey = dayKeyOf()
                            val resetDocId = "weekly_reset_${todayKey}"
                            val resetData = mapOf(
                                "taskId" to "weekly_reset",
                                "taskTitle" to "Reset weekly streak",
                                "points" to 0,
                                "timestamp" to FieldValue.serverTimestamp(),
                                "periodKey" to todayKey
                            )
                            db.collection("users").document(u)
                                .collection("rewards").document(resetDocId)
                                .set(resetData)
                                .addOnCompleteListener {
                                    streakResetDayKey = todayKey
                                    weeklyStreak = 0
                                    weeklyCollected = true
                                    scope.launch { snackbar.showSnackbar("Nice! +$points points. Streak restarted.") }
                                }
                        }
                        .addOnFailureListener { e ->
                            scope.launch { snackbar.showSnackbar(e.localizedMessage ?: "Failed to collect") }
                        }
                }
            }
    }

    /* -------------------------------- UI ---------------------------------- */
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Header
        item {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("EcoPoints Tracker", style = MaterialTheme.typography.titleMedium)
                        Icon(
                            imageVector = Icons.Filled.Bolt,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = String.format(Locale.getDefault(), "%,d", totalPoints),
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        StatChip(
                            title = "This Month",
                            value = pointsThisMonth,
                            icon = Icons.Filled.Bolt,
                            modifier = Modifier.weight(1f)
                        )
                        StatChip(
                            title = "Days Active",
                            value = daysActive,
                            icon = Icons.Filled.Bolt,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Daily reward
        item {
            SectionCard(title = "Daily Reward") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Bolt, contentDescription = null)
                        }
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Follow today’s tips", fontWeight = FontWeight.Medium)
                        Text(
                            "Complete any recommended action and collect your daily reward.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Button(
                        enabled = !isLoading && !dailyCollected,
                        onClick = { collectDaily(50) },
                        shape = RoundedCornerShape(12.dp)
                    ) { Text(if (dailyCollected) "Collected" else "Collect +50") }
                }
                Spacer(Modifier.height(4.dp))
            }
        }

        // Weekly streak
        item {
            SectionCard(title = "Weekly Energy-Saver") {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Saved day streak", fontWeight = FontWeight.Medium)
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(999.dp)
                        ) {
                            Text(
                                "${weeklyStreak}/7 days",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                fontSize = 12.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { (weeklyStreak / 7f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Save ≥1% vs last estimate on 7 days to collect.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            enabled = (weeklyStreak >= 7) && !isLoading && !weeklyCollected,
                            onClick = { collectWeekly(300) },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text(if (weeklyCollected) "Collected" else "Collect +300") }
                    }
                }
                Spacer(Modifier.height(4.dp))
            }
        }

        // Earn more points
        item {
            SectionCard(title = "Earn More Points", trailing = { }) {
                ACTIONS.forEach { action ->
                    val alreadyClaimed = when (action.cadence) {
                        Cadence.Daily -> todayClaimed.contains(action.id)
                        Cadence.Weekly -> weekClaimed.contains(action.id)
                    }
                    ListRow(
                        headline = action.title,
                        supporting = "${action.description} • +${action.points}",
                        trailing = if (alreadyClaimed) "Claimed" else null,
                        leading = {
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = CircleShape,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(Icons.Filled.Bolt, contentDescription = null)
                                }
                            }
                        }
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            enabled = !alreadyClaimed && !isLoading,
                            onClick = { claim(action) },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text(if (alreadyClaimed) "Claimed" else "Claim") }
                    }
                    HorizontalDivider()
                }
            }
        }

        // Monthly Progress
        item {
            SectionCard(title = "Monthly Progress", trailing = {}) {
                InfoRow(
                    "Points This Month",
                    String.format(Locale.getDefault(), "%,d pts", pointsThisMonth)
                )
                InfoRow("Days Active", "$daysActive / $daysInMonth days")
                Spacer(Modifier.height(8.dp))
                val progress = (pointsThisMonth.toFloat() / 1000f).coerceIn(0f, 1f)
                Text(
                    "Monthly Goal  ${String.format(Locale.getDefault(), "%,d", pointsThisMonth)} / 1,000",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                )
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .height(10.dp),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.height(8.dp))
            }
        }

        item { Spacer(Modifier.height(8.dp)) }
    }

    if (isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
    error?.let {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            AssistChip(onClick = { /* no-op */ }, label = { Text(it) })
        }
    }
}

/* ------------------------ Reusable bits (unchanged style) ------------------ */

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SectionCard(
    title: String,
    trailing: (@Composable () -> Unit)? = null,
    contentPadding: Dp = 8.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                trailing?.let { it() }
            }
            Column(Modifier.padding(bottom = contentPadding)) { content() }
        }
    }
}

@Composable
private fun ListRow(
    leading: @Composable (() -> Unit)? = null,
    headline: String,
    supporting: String? = null,
    trailing: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leading != null) {
            Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) { leading() }
            Spacer(Modifier.width(12.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(
                headline,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (supporting != null) {
                Text(
                    supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            Text(
                trailing,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatChip(
    title: String,
    value: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 2.dp,
        modifier = modifier
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceVariant) {
                Icon(icon, contentDescription = null, modifier = Modifier.padding(8.dp))
            }
            Column {
                Text(
                    String.format(Locale.getDefault(), "%,d", value),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
                Text(
                    title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/* -------------------------------- PREVIEW ---------------------------------- */

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun Preview_Rewards() {
    FIT5046Lab4Group3ass2Theme {
        AchievementsScaffold(
            totalPoints = 0,
            electricityPoints = 0,
            badges = emptyList(),
            leaderboard = emptyList(),
            monthly = MonthlyProgress(0, 0, 0, 30, 1000)
        )
    }
}
