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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
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
import java.util.Locale
import kotlin.math.max

/* ----------------------------- UI model (derived) ---------------------------- */

data class Appliance(
    val id: String,
    val iconEmoji: String,
    val name: String,
    val spec: String,       // "1500W • 6h daily"
    val costPerDay: String, // "$0.27/day"
    val kwh: String,        // "0.90 kWh"
    // Parsed helpers for UI/ordering
    val watt: Int = 0,
    val hours: Float = 0f,
    val kwhValue: Float = 0f,
    val costValue: Float = 0f
)

/* ------------------------------- MAIN SCAFFOLD ------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ElectricityScaffold(
    currentRoute: String = ROUTE_APPLIANCES,
    onTabSelected: (route: String) -> Unit = {},
    onBack: () -> Unit = {},
    onAddAppliance: () -> Unit = {},
    onEditAppliance: (id: String) -> Unit = {}
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
            ElectricityScreenFromFirestore(
                onEdit = onEditAppliance,
                onDeleted = { /* list live-updates via snapshot */ }
            )
        }
    }
}

/* ------------------------------- FIRESTORE UI -------------------------------- */

@Composable
private fun ElectricityScreenFromFirestore(
    onEdit: (id: String) -> Unit,
    onDeleted: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val uid = auth.currentUser?.uid

    var appliances by remember { mutableStateOf(listOf<Appliance>()) }
    var totalKwh by remember { mutableStateOf(0.0) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    var pendingDeleteId by remember { mutableStateOf<String?>(null) }
    var pendingDeleteName by remember { mutableStateOf<String?>(null) }

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
                            id = d.id,
                            iconEmoji = emoji,
                            name = name,
                            spec = "${watt}W • ${hours}h daily",
                            costPerDay = String.format(Locale.getDefault(), "$%.2f/day", cost),
                            kwh = String.format(Locale.getDefault(), "%.2f kWh", kwh),
                            watt = watt,
                            hours = hours,
                            kwhValue = kwh,
                            costValue = cost
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

    val usageKwhText = String.format(Locale.getDefault(), "%.2f kWh", totalKwh)
    val costText     = String.format(Locale.getDefault(), "$%.2f estimated cost", totalKwh * 0.30)
    val co2KgText    = String.format(Locale.getDefault(), "CO\u2082: %.2f kg equivalent", totalKwh * 0.50)

    ElectricityScreen(
        usageKwh = usageKwhText,
        costEstimate = costText,
        co2 = co2KgText,
        appliances = appliances,
        onEdit = { onEdit(it) },
        onDelete = { id, name ->
            pendingDeleteId = id
            pendingDeleteName = name
        }
    )

    if (isLoading && appliances.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
    errorMsg?.let {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            AssistChip(onClick = { }, label = { Text(it) })
        }
    }

    // Delete confirm — RED filled button
    val toDeleteId = pendingDeleteId
    val toDeleteName = pendingDeleteName
    if (toDeleteId != null && uid != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteId = null; pendingDeleteName = null },
            title = { Text("Delete appliance") },
            text = { Text("Delete “${toDeleteName ?: ""}”? This can’t be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        pendingDeleteId = null
                        pendingDeleteName = null
                        FirebaseFirestore.getInstance()
                            .collection("users").document(uid)
                            .collection("appliances").document(toDeleteId)
                            .delete()
                            .addOnSuccessListener { onDeleted() }
                            .addOnFailureListener { }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteId = null; pendingDeleteName = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/* --------------------------------- SCREEN ---------------------------------- */

@Composable
private fun ElectricityScreen(
    usageKwh: String,
    costEstimate: String,
    co2: String,
    appliances: List<Appliance>,
    onEdit: (id: String) -> Unit,
    onDelete: (id: String, name: String) -> Unit
) {
    // Collapsible info: start hidden as requested
    var showInfo by remember { mutableStateOf(false) }
    // Sorting toggle (true = Cost, false = Name)
    var sortByCost by remember { mutableStateOf(true) }

    val sorted = remember(appliances, sortByCost) {
        if (sortByCost) appliances.sortedByDescending { it.costValue }
        else appliances.sortedBy { it.name.lowercase(Locale.getDefault()) }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp)
    ) {
        item { SummaryHeaderCard(usageKwh, costEstimate, co2) }
        item { InfoCollapsible(expanded = showInfo, onToggle = { showInfo = !showInfo }) }

        // Single-choice segmented toggle for sorting
        item {
            SortToggle(
                sortByCost = sortByCost,
                onSortChange = { sortByCost = it },
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        items(sorted) { a ->
            ApplianceCard(
                appliance = a,
                onEdit = { onEdit(a.id) },
                onDelete = { onDelete(a.id, a.name) }
            )
            Spacer(Modifier.height(12.dp))
        }

        if (sorted.isNotEmpty()) {
            val totalCost = sorted.sumOf { it.costValue.toDouble() }.toFloat()
            val totalKwh = sorted.sumOf { it.kwhValue.toDouble() }.toFloat()
            item {
                TotalsFooter(totalKwh, totalCost)
                Spacer(Modifier.height(8.dp))
            }
        }

        if (sorted.isEmpty()) {
            item {
                Text(
                    "No appliances yet. Tap the + button to add your first appliance.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        }
    }
}

/* ------------------------------- COMPONENTS -------------------------------- */

@Composable
private fun SortToggle(
    sortByCost: Boolean,
    onSortChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Sort", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.width(8.dp))

        Row(
            modifier = Modifier
                .height(36.dp)
                .background(
                    MaterialTheme.colorScheme.surfaceVariant,
                    RoundedCornerShape(12.dp)
                )
                .padding(4.dp)
        ) {
            val activeColors = ButtonDefaults.filledTonalButtonColors()
            val inactiveColors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            )

            FilledTonalButton(
                onClick = { if (!sortByCost) onSortChange(true) },
                colors = if (sortByCost) activeColors else inactiveColors,
                contentPadding = PaddingValues(horizontal = 12.dp),
                shape = RoundedCornerShape(topStart = 8.dp, bottomStart = 8.dp)
            ) { Text("By Cost") }

            Spacer(Modifier.width(6.dp))

            FilledTonalButton(
                onClick = { if (sortByCost) onSortChange(false) },
                colors = if (!sortByCost) activeColors else inactiveColors,
                contentPadding = PaddingValues(horizontal = 12.dp),
                shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp)
            ) { Text("By Name") }
        }
    }
}

@Composable
private fun SummaryHeaderCard(usageKwh: String, costEstimate: String, co2: String) {
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
                Surface(
                    color = MaterialTheme.colorScheme.tertiaryContainer,
                    shape = RoundedCornerShape(999.dp)
                ) {
                    Text(
                        "Estimates",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
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

@Composable
private fun InfoCollapsible(expanded: Boolean, onToggle: () -> Unit) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Info, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("About these estimates", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onToggle) {
                    Icon(if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore, null)
                }
            }
            if (expanded) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "Each device cost is estimated from power (W) × usage hours ÷ 1000 × rate (≈ $0.30/kWh). " +
                            "It’s a guide only. To be more accurate, double-check each appliance’s power rating " +
                            "and keep daily usage hours up to date.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
private fun ApplianceCard(
    appliance: Appliance,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

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

            Spacer(Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FactChip("${appliance.hours} h")
                FactChip("${appliance.watt} W")
                FactChip(String.format(Locale.getDefault(), "%.2f kWh", appliance.kwhValue))
            }

            Spacer(Modifier.height(2.dp))
            TextButton(
                onClick = { expanded = !expanded },
                contentPadding = PaddingValues(0.dp)
            ) {
                Icon(
                    if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = null
                )
                Spacer(Modifier.width(6.dp))
                Text(if (expanded) "Hide details" else "Why this estimate?")
            }

            if (expanded) {
                Text(
                    "Estimate ≈ ${appliance.watt}W × ${appliance.hours}h ÷ 1000 × $0.30 = " +
                            String.format(Locale.getDefault(), "$%.2f/day", appliance.costValue),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
            }

            // Actions — primary + DESTRUCTIVE red filled button
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilledTonalButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Filled.Edit, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Adjust usage")
                }
                Button(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete")
                }
            }
        }
    }
}

@Composable
private fun FactChip(text: String) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(999.dp)
    ) {
        Text(
            text,
            fontSize = 12.sp,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun TotalsFooter(totalKwh: Float, totalCost: Float) {
    Card(
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.08f)
                        )
                    )
                )
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Appliance totals", fontWeight = FontWeight.Medium)
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        String.format(Locale.getDefault(), "%.2f kWh", totalKwh),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        String.format(Locale.getDefault(), "$%.2f/day", totalCost),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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
