package com.example.fit5046_lab4_group3_ass2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme

/* ------------------------------- DATA (UI only) ------------------------------- */

data class Tip(
    val id: String,
    val iconEmoji: String,
    val title: String,
    val body: String,
    val tags: List<String> = emptyList(),
    val saved: Boolean = false,
    val likes: Int = 0
)

private val demoTips = listOf(
    Tip(
        id = "1", iconEmoji = "🔌",
        title = "Reduce Standby Power",
        body = "Unplug chargers and devices when not in use. Many draw power even when 'off'.",
        tags = listOf("Energy"),
        likes = 128
    ),
    Tip(
        id = "2", iconEmoji = "💡",
        title = "Use LED Bulbs",
        body = "Replace incandescent bulbs with LEDs to cut lighting energy by ~75%.",
        tags = listOf("Energy"),
        likes = 96
    ),
    Tip(
        id = "3", iconEmoji = "🌡️",
        title = "Adjust Thermostat",
        body = "Lower heating by 1–2°C in winter and raise cooling by 1–2°C in summer.",
        tags = listOf("Energy", "Comfort"),
        likes = 211, saved = true
    ),
    Tip(
        id = "4", iconEmoji = "🕒",
        title = "Shift Use Off-Peak",
        body = "Run high-load appliances outside peak hours to save money and grid strain.",
        tags = listOf("Appliances"),
        likes = 73
    ),
    Tip(
        id = "5", iconEmoji = "🧺",
        title = "Cold-Wash Laundry",
        body = "Most detergents work in cold water—heating water is often the biggest energy cost.",
        tags = listOf("Appliances"),
        likes = 154
    ),
)

/* ------------------------------- SCAFFOLD ------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsScaffold(
    onBack: () -> Unit = {},
    onNotifications: () -> Unit = {},
    // NAV: Tips shows bottom bar for quick tab-jumps (no tab is selected here)
    onTabSelected: (route: String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Tips") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Bell with small unread dot (same treatment as Rewards/Home)
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
                currentRoute = "",
                onTabSelected = onTabSelected
            )
        }
    ) { inner ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            TipsScreen()
        }
    }
}

/* --------------------------------- SCREEN ---------------------------------- */

@Composable
private fun TipsScreen() {
    var query by rememberSaveable { mutableStateOf("") }
    var savedOnly by rememberSaveable { mutableStateOf(false) }

    val filtered = remember(query, savedOnly) {
        demoTips
            .filter { !savedOnly || it.saved }
            .filter {
                query.isBlank() ||
                        it.title.contains(query, ignoreCase = true) ||
                        it.body.contains(query, ignoreCase = true) ||
                        it.tags.any { t -> t.contains(query, ignoreCase = true) }
            }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Search + Saved toggle row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        if (query.isNotBlank()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    placeholder = { Text("Search energy tips") },
                    shape = RoundedCornerShape(12.dp)
                )
                // Saved only toggle styled to match app
                FilterChip(
                    selected = savedOnly,
                    onClick = { savedOnly = !savedOnly },
                    label = { Text("Saved") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Bookmark,
                            contentDescription = null
                        )
                    }
                )
            }
        }

        // Optional helper
        item {
            Text(
                "Eco Tips for You",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 4.dp)
            )
        }

        // Cards
        items(filtered, key = { it.id }) { tip ->
            TipCard(
                tip = tip,
                onClick = { /* UI only */ },
                onToggleSave = { /* UI only */ },
                onLike = { /* UI only */ }
            )
        }

        item { Spacer(Modifier.height(72.dp)) }
    }
}

/* -------------------------------- CARDS -------------------------------- */

@Composable
private fun TipCard(
    tip: Tip,
    onClick: () -> Unit,
    onToggleSave: () -> Unit,
    onLike: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(14.dp)) {
            // Header row: icon + title + save
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(tip.iconEmoji, fontSize = 18.sp)
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    tip.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                IconButton(onClick = onToggleSave) {
                    Icon(
                        imageVector = Icons.Filled.Bookmark,
                        contentDescription = "Save",
                        tint = if (tip.saved) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                tip.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            if (tip.tags.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    tip.tags.forEach { tag ->
                        AssistChip(
                            onClick = { /* UI only */ },
                            label = { Text(tag) },
                            colors = AssistChipDefaults.assistChipColors(
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${tip.likes} likes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    IconButton(onClick = onLike) {
                        Icon(Icons.Filled.ThumbUp, contentDescription = "Like")
                    }
                    IconButton(onClick = { /* UI only */ }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share")
                    }
                }
            }
        }
    }
}

/* -------------------------------- PREVIEW -------------------------------- */

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TipsPreview() {
    FIT5046Lab4Group3ass2Theme {
        TipsScaffold()
    }
}
