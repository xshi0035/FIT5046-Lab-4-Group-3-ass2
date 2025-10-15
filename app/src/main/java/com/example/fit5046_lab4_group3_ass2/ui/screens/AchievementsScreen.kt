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
import com.example.fit5046_lab4_group3_ass2.data.GeneratedTask
import com.example.fit5046_lab4_group3_ass2.data.RewardEntry
import com.example.fit5046_lab4_group3_ass2.data.RewardsRepo
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/* -------------------------------------------------------------------------- */
/*  Types kept so MainActivity doesn’t break, but they’re not used directly.  */
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

/* ------------------------ Scaffold (navigation unchanged) ------------------ */

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
        RewardsScreen(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp),
            snackbar = snackbar
        )
    }
}

/* --------------------------- Rewards screen (repo) ------------------------- */

@Composable
private fun RewardsScreen(
    modifier: Modifier = Modifier,
    snackbar: SnackbarHostState
) {
    val scope = rememberCoroutineScope()
    val repo = remember { RewardsRepo() }

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    var entries by remember { mutableStateOf<List<RewardEntry>>(emptyList()) }
    var tasks by remember { mutableStateOf<List<GeneratedTask>>(emptyList()) }

    // Claims
    DisposableEffect(Unit) {
        val stop = repo.listenClaims(
            onChange = { list ->
                entries = list
                isLoading = false
                error = null
            },
            onError = { e ->
                error = e.localizedMessage ?: "Failed to load rewards."
                isLoading = false
            }
        )
        onDispose { stop() }
    }

    // Tasks (today + current week)
    DisposableEffect(Unit) {
        val stop = repo.listenTasks(
            onChange = { tasks = it },
            onError = { e -> error = e.localizedMessage }
        )
        onDispose { stop() }
    }

    // Header stats
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

    fun claim(t: GeneratedTask) {
        repo.claimTask(t) { ok, msg ->
            scope.launch { snackbar.showSnackbar(msg ?: if (ok) "Claimed" else "Failed") }
        }
    }

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

        // Earn More Points
        item {
            SectionCard(
                title = "Earn More Points",
                trailing = {
                    AssistChip(
                        onClick = {
                            repo.seedNow { ok, msg ->
                                scope.launch { snackbar.showSnackbar(msg ?: if (ok) "Seeded." else "Failed.") }
                            }
                        },
                        label = { Text("Get today’s tasks") }
                    )
                }
            ) {
                if (tasks.isEmpty() && !isLoading) {
                    Text(
                        "No tasks yet. Try again later.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                tasks.forEach { t ->
                    val already = t.claimed
                    ListRow(
                        headline = t.title,
                        supporting = "${t.description} • +${t.points}",
                        trailing = if (already) "Claimed" else null,
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
                            enabled = !already && !isLoading,
                            onClick = { claim(t) },
                            shape = RoundedCornerShape(12.dp)
                        ) { Text(if (already) "Claimed" else "Claim") }
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
            AssistChip(onClick = { }, label = { Text(it) })
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
