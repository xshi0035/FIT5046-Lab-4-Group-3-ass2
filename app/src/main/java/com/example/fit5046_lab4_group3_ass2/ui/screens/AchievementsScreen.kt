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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.Timestamp
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

/** What we store for every claim in Firestore. */
private data class RewardEntry(
    val taskId: String = "",
    val taskTitle: String = "",
    val points: Int = 0,
    val timestamp: Long = 0L, // millis since epoch
    val key: String = ""
)

/* -------------------------- Our “earn points” actions ---------------------- */

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
    // Legacy params kept for compatibility; they are ignored by the Firestore UI.
    totalPoints: Int,
    electricityPoints: Int,
    badges: List<Badge>,
    leaderboard: List<LeaderboardEntry>,
    monthly: MonthlyProgress,
    // NAV – unchanged
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

    // UI state
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var entries by remember { mutableStateOf<List<RewardEntry>>(emptyList()) }
    var todayClaimed by remember { mutableStateOf<Set<String>>(emptySet()) }   // taskIds claimed today
    var weekClaimed by remember { mutableStateOf<Set<String>>(emptySet()) }    // taskIds claimed this week

    // Listen to rewards collection (always return a DisposableEffectResult)
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
                        // Robust timestamp extraction: supports Long and com.google.firebase.Timestamp
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
                            key = d.id
                        )
                    } ?: emptyList()

                    entries = list
                    isLoading = false
                    error = null

                    // compute today's and this week's claimed sets
                    val c = Calendar.getInstance()
                    val todayKey = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(c.time)
                    val weekOfYear = c.get(Calendar.WEEK_OF_YEAR)
                    val year = c.get(Calendar.YEAR)

                    todayClaimed = list
                        .filter {
                            val key = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
                                .format(Date(it.timestamp))
                            key == todayKey
                        }
                        .map { it.taskId }
                        .toSet()

                    weekClaimed = list
                        .filter {
                            val cal = Calendar.getInstance().apply { time = Date(it.timestamp) }
                            cal.get(Calendar.WEEK_OF_YEAR) == weekOfYear &&
                                    cal.get(Calendar.YEAR) == year
                        }
                        .map { it.taskId }
                        .toSet()
                }

        onDispose { reg.remove() }
    }

    // Derived totals
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
    val monthlyGoal = 1000 // tweak if you want to pull from user profile
    val totalPoints = entries.sumOf { it.points }

    // Claim handler
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
        val docId = "${task.id}_$periodKey" // Prevent duplicate claim in the period.

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

    // UI
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // EcoPoints tracker (header)
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

        // Earn more points (actions)
        item {
            SectionCard(
                title = "Earn More Points",
                trailing = { }
            ) {
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

        // Monthly Progress (kept)
        item {
            SectionCard(
                title = "Monthly Progress",
                trailing = {}
            ) {
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
