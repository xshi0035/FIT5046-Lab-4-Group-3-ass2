package com.example.fit5046_lab4_group3_ass2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlin.math.max
import java.util.Locale

/* ----------------------------- UI model (derived) ---------------------------- */

data class Appliance(
    val iconEmoji: String,
    val name: String,
    val spec: String,       // e.g., "1500W • 6h daily"
    val costPerDay: String, // e.g., "$0.27/day"
    val kwh: String         // e.g., "0.90 kWh"
)

/* ------------------------------- MAIN SCAFFOLD ------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectricityScaffold(
    currentRoute: String = ROUTE_APPLIANCES,
    onTabSelected: (route: String) -> Unit = {},
    onBack: () -> Unit = {},
    onAddAppliance: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Appliances") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { /* optional */ }) {
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
        floatingActionButton = {
            FloatingActionButton(onClick = onAddAppliance) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
    ) { inner ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            ElectricityScreenFromFirestore()
        }
    }
}

/* ------------------------------- FIRESTORE UI -------------------------------- */

@Composable
private fun ElectricityScreenFromFirestore() {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val uid = auth.currentUser?.uid

    var appliances by remember { mutableStateOf(listOf<Appliance>()) }
    var totalKwh by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    // Listen to Firestore: users/{uid}/appliances
    DisposableEffect(uid) {
        var reg: ListenerRegistration? = null
        if (uid == null) {
            isLoading = false
            appliances = emptyList()
            errorMsg = "You're not signed in."
        } else {
            reg = db.collection("users")
                .document(uid)
                .collection("appliances")
                .addSnapshotListener { snap, e ->
                    if (e != null) {
                        errorMsg = e.localizedMessage ?: "Failed to load appliances."
                        isLoading = false
                        return@addSnapshotListener
                    }
                    var kwhTotal = 0.0
                    val items = snap?.documents?.mapNotNull { d ->
                        // expected fields written by AddApplianceScaffold
                        val name = d.getString("name") ?: return@mapNotNull null
                        val watt = (d.getLong("watt") ?: 0L).toInt()
                        val hours = (d.getDouble("hours") ?: 0.0).toFloat()
                        val category = d.getString("category") ?: "Other"

                        val kwh = max(0f, watt * hours / 1000f)
                        val cost = kwh * 0.30f
                        kwhTotal += kwh.toDouble()

                        val emoji = when (category) {
                            "Lighting" -> "💡"
                            "Entertainment" -> "🎮"
                            "Cooling & Heating" -> "❄️"
                            "Cleaning" -> "🧺"
                            "Kitchen" -> "🍳"
                            else -> "🔌"
                        }
                        Appliance(
                            iconEmoji = emoji,
                            name = name,
                            spec = "${watt}W • ${hours}h daily",
                            costPerDay = String.format(Locale.getDefault(), "$%.2f/day", cost),
                            kwh = String.format(Locale.getDefault(), "%.2f kWh", kwh)
                        )
                    } ?: emptyList()

                    appliances = items
                    totalKwh = kwhTotal
                    isLoading = false
                    errorMsg = null
                }
        }
        onDispose { reg?.remove() }
    }

    // Derived header stats
    val usageKwhText = String.format(Locale.getDefault(), "%.2f kWh", totalKwh)
    val costText = String.format(Locale.getDefault(), "$%.2f estimated cost", totalKwh * 0.30)
    val co2KgText = String.format(Locale.getDefault(), "CO\u2082: %.2fkg equivalent", totalKwh * 0.5)
    val changePercent = "-0%" // placeholder (no baseline yet)

    ElectricityScreen(
        usageKwh = usageKwhText,
        costEstimate = costText,
        co2 = co2KgText,
        changePercent = changePercent,
        appliances = appliances,
        suggestions = emptyList()
    )

    if (isLoading && appliances.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
    errorMsg?.let {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            AssistChip(onClick = { /* no-op */ }, label = { Text(it) })
        }
    }
}

/* --------------------------------- SCREEN ---------------------------------- */

@Composable
private fun ElectricityScreen(
    usageKwh: String,
    costEstimate: String,
    co2: String,
    changePercent: String,
    appliances: List<Appliance>,
    suggestions: List<Suggestion>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        // "Today's Usage"
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Today's Usage",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            usageKwh,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f)
                        )
                        ChangePill(changePercent)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        costEstimate,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                    Text(
                        co2,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // My Appliances header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("My Appliances", style = MaterialTheme.typography.titleMedium)
                if (appliances.isNotEmpty()) {
                    Text(
                        "View Usage",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Appliance list (may be empty)
        items(appliances) { appliance ->
            ApplianceCard(appliance)
            Spacer(Modifier.height(12.dp))
        }

        if (appliances.isEmpty()) {
            item {
                Text(
                    "No appliances yet. Tap the + button to add your first appliance.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }

        // (tips omitted)
        items(suggestions) { tip ->
            SuggestionCard(tip)
            Spacer(Modifier.height(12.dp))
        }
    }
}

/* ------------------------------- COMPONENTS -------------------------------- */

@Composable
private fun ChangePill(text: String) {
    val isDown = text.trim().startsWith("-")
    val bg = if (isDown) MaterialTheme.colorScheme.tertiaryContainer
    else MaterialTheme.colorScheme.errorContainer
    val fg = if (isDown) MaterialTheme.colorScheme.onTertiaryContainer
    else MaterialTheme.colorScheme.onErrorContainer

    Surface(
        color = bg,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text,
            fontSize = 12.sp,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun ApplianceCard(appliance: Appliance) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) { Text(appliance.iconEmoji, fontSize = 20.sp) }

                Spacer(Modifier.width(12.dp))

                Column(Modifier.weight(1f)) {
                    Text(appliance.name, fontWeight = FontWeight.SemiBold)
                    Text(
                        appliance.spec,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        appliance.costPerDay,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        appliance.kwh,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { /* edit – to implement */ }, modifier = Modifier.weight(1f)) {
                    Text("Edit")
                }
                OutlinedButton(onClick = { /* delete – to implement */ }, modifier = Modifier.weight(1f)) {
                    Text("Delete")
                }
            }
        }
    }
}

data class Suggestion(val title: String, val body: String, val leadingEmoji: String = "💡")

@Composable
private fun SuggestionCard(suggestion: Suggestion) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) { Text(suggestion.leadingEmoji) }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    suggestion.title,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    suggestion.body,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f),
                    lineHeight = 18.sp,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun ActionCard(modifier: Modifier = Modifier, label: String) {
    Card(shape = RoundedCornerShape(16.dp), modifier = modifier.height(72.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(label, fontWeight = FontWeight.Medium)
        }
    }
}

/* -------------------------------- PREVIEW ---------------------------------- */

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ElectricityPreview() {
    FIT5046Lab4Group3ass2Theme {
        ElectricityScaffold()
    }
}
