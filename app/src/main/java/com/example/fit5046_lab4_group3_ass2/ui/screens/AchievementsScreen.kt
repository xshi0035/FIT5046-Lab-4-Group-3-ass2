package com.example.fit5046_lab4_group3_ass2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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

/* ------------------------ Data models (demo) ------------------------ */

data class Badge(
    val title: String,
    val subtitle: String,
    val date: String
)

data class LeaderboardEntry(
    val rank: Int,
    val name: String,
    val points: Int,
    val isYou: Boolean = false
)

data class MonthlyProgress(
    val pointsThisMonth: Int,
    val badgesEarned: Int,
    val daysActive: Int,
    val daysInMonth: Int,
    val monthlyGoal: Int
)

/* ------------------------ Screen ------------------------ */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    totalPoints: Int,
    electricityPoints: Int,
    plasticPoints: Int,
    badges: List<Badge>,
    leaderboard: List<LeaderboardEntry>,
    monthly: MonthlyProgress,
    onBack: () -> Unit = {},
    onNotifications: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Rewards") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = onNotifications) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                        }

                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8A2BE2)) // 紫色
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // EcoPoints Tracker
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
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "%,d".format(totalPoints),
                            style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.SemiBold),
                        )
                        Spacer(Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            StatChip(
                                title = "Electricity Savings",
                                value = electricityPoints,
                                icon = Icons.Default.Bolt,
                                modifier = Modifier.weight(1f)
                            )
                            StatChip(
                                title = "Plastic Reduction",
                                value = plasticPoints,
                                icon = Icons.Default.EmojiEvents,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Your Badges
            item {
                SectionCard(
                    title = "Your Badges",
                    trailing = {
                        Icon(
                            Icons.Default.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                ) {
                    badges.forEach { b ->
                        ListRow(
                            leading = {
                                Surface(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(
                                        Modifier.fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(Icons.Default.EmojiEvents, contentDescription = null)
                                    }
                                }
                            },
                            headline = b.title,
                            supporting = b.subtitle,
                            trailing = b.date
                        )
                        Divider()
                    }
                }
            }

            // Community Leaderboard
            item {
                SectionCard(
                    title = "Community Leaderboard",
                    trailing = {
                        Icon(
                            Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                ) {
                    leaderboard.forEach { e ->
                        ListRow(
                            leading = { RankAvatar(rank = e.rank, isYou = e.isYou) },
                            headline = if (e.isYou) "Your Rank" else e.name,
                            supporting = "%,d points".format(e.points),
                            trailing = if (e.isYou) null else " "
                        )
                        Divider()
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Community Average", style = MaterialTheme.typography.bodyMedium)
                        val avg = leaderboard.map { it.points }.average().toInt()
                        Text("%,d points".format(avg), style = MaterialTheme.typography.bodyMedium)
                    }
                    Text(
                        text = "You're 54% above average!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            // Monthly Progress
            item {
                SectionCard(title = "Monthly Progress", trailing = {
                    Icon(
                        Icons.Default.Shield,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }) {
                    InfoRow("Points This Month", "%,d pts".format(monthly.pointsThisMonth))
                    InfoRow("Badges Earned", "${monthly.badgesEarned} badges")
                    InfoRow("Days Active", "${monthly.daysActive} / ${monthly.daysInMonth} days")
                    Spacer(Modifier.height(8.dp))
                    val progress = (monthly.pointsThisMonth.toFloat() / monthly.monthlyGoal)
                        .coerceIn(0f, 1f)
                    Text(
                        "Monthly Goal  ${monthly.pointsThisMonth} / ${monthly.monthlyGoal}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 2.dp)
                    )
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth()
                            .height(10.dp),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

/* ------------------------ Reusable pieces ------------------------ */

@Composable
fun InfoRow(label: String, value: String) {
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
                trailing?.invoke()
            }
            Column(Modifier.padding(bottom = contentPadding)) {
                content()
            }
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
                    maxLines = 1
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
                Text("%,d".format(value), fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Text(
                    title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RankAvatar(rank: Int, isYou: Boolean) {
    val bg =
        if (isYou) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val fg =
        if (isYou) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Surface(shape = CircleShape, color = bg) {
        Box(Modifier.size(36.dp), contentAlignment = Alignment.Center) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = fg, modifier = Modifier.size(16.dp))
                Text("#$rank", color = fg, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun Preview_AchievementsScreen() {
    val badges = remember {
        listOf(
            Badge("Plastic-Free Week", "No plastic purchases for 7 days", "Jan 15"),
            Badge("100kWh Saved", "Reduced electricity consumption", "Jan 10"),
            Badge("CO₂ Reducer", "Prevented 50kg CO₂ emissions", "Dec 28")
        )
    }
    val leaderboard = remember {
        listOf(
            LeaderboardEntry(rank = 7, name = "Your Rank", points = 2_847, isYou = true),
            LeaderboardEntry(rank = 1, name = "EcoWarrior_2025", points = 4_892),
            LeaderboardEntry(rank = 2, name = "GreenThumb_42", points = 4_156),
            LeaderboardEntry(rank = 3, name = "PlasticFree_Hero", points = 3_924)
        )
    }
    val monthly = MonthlyProgress(
        pointsThisMonth = 847,
        badgesEarned = 3,
        daysActive = 18,
        daysInMonth = 31,
        monthlyGoal = 1000
    )

    MaterialTheme {
        AchievementsScreen(
            totalPoints = 2_847,
            electricityPoints = 1_523,
            plasticPoints = 1_324,
            badges = badges,
            leaderboard = leaderboard,
            monthly = monthly
        )
    }
}
