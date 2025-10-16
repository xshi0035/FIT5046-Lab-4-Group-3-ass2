package com.example.fit5046_lab4_group3_ass2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme
import java.util.Locale
import kotlin.math.round
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

/* ------------------------------- SCAFFOLD ---------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddApplianceScaffold(
    applianceId: String? = null, // null = add new, non-null = edit existing
    onBack: () -> Unit = {},
    onSave: (name: String, watt: Int, hours: Float, category: String) -> Unit = { _,_,_,_ -> },
    onCancel: () -> Unit = {},
    // NAV
    currentRoute: String = ROUTE_APPLIANCES,
    onTabSelected: (route: String) -> Unit = {}
) {
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    // Handler that writes to Firestore under users/{uid}/appliances
    fun saveToFirestore(name: String, watt: Int, hours: Float, category: String) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            scope.launch { snackbarHost.showSnackbar("You’re not signed in.") }
            return
        }
        val data = hashMapOf(
            "name" to name,
            "watt" to watt,
            "hours" to hours,
            "category" to category,
            "updatedAt" to FieldValue.serverTimestamp()
        )

        val col = db.collection("users").document(uid).collection("appliances")

        val task = if (applianceId == null) {
            // create new
            col.add(data + ("createdAt" to FieldValue.serverTimestamp()))
        } else {
            // update existing
            col.document(applianceId).set(data, com.google.firebase.firestore.SetOptions.merge())
        }

        task.addOnSuccessListener {
            scope.launch {
                snackbarHost.showSnackbar(
                    if (applianceId == null) "Appliance saved" else "Changes saved"
                )
            }
            onSave(name, watt, hours, category)
            onBack()
        }.addOnFailureListener { e ->
            scope.launch {
                snackbarHost.showSnackbar(e.localizedMessage ?: "Failed to save appliance")
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (applianceId == null) "Add Appliance" else "Edit Appliance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // bell + unread dot (matches other screens)
                    Box {
                        IconButton(onClick = { /* UI only */ }) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(8.dp)
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
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { inner ->
        AddApplianceContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .imePadding() // when keyboard shows, keep content visible
                .padding(horizontal = 16.dp, vertical = 12.dp),
            applianceId = applianceId,
            onSave = ::saveToFirestore,
            onCancel = onCancel
        )
    }
}

/* -------------------------------- CONTENT --------------------------------- */

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddApplianceContent(
    modifier: Modifier = Modifier,
    applianceId: String?,
    onSave: (String, Int, Float, String) -> Unit,
    onCancel: () -> Unit
) {
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val uid = auth.currentUser?.uid

    // UI-only state
    var name by remember { mutableStateOf("") }
    var wattText by remember { mutableStateOf("") }
    var hours by remember { mutableFloatStateOf(8f) }
    val categories = listOf("Cooling & Heating", "Kitchen", "Entertainment", "Lighting", "Cleaning", "Other")
    var category by remember { mutableStateOf("") }
    var catExpanded by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }

    // Prefill when editing
    LaunchedEffect(applianceId, uid) {
        if (applianceId != null && uid != null) {
            loading = true
            db.collection("users").document(uid)
                .collection("appliances").document(applianceId)
                .get()
                .addOnSuccessListener { d ->
                    name = d.getString("name") ?: ""
                    wattText = (d.getLong("watt") ?: 0L).toString()
                    hours = (d.getDouble("hours") ?: 0.0).toFloat()
                    category = d.getString("category") ?: ""
                }
                .addOnCompleteListener { loading = false }
        }
    }

    val watt = wattText.filter { it.isDigit() }.take(5).toIntOrNull() ?: 0
    val saveEnabled = name.isNotBlank() && watt > 0 && category.isNotBlank() && !loading

    // >>> Scrollable list
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 96.dp) // leave room above bottom bar
    ) {
        // Intro / banner
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        if (applianceId == null) "Appliance Details" else "Edit Appliance",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Fill in the fields below. Name and category are required.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Basic Info
        item {
            SectionCard(title = "Basic Info") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { if (!loading) name = it },
                    label = { Muted("Appliance Name *") },
                    placeholder = { Muted("e.g., Air Conditioner") },
                    singleLine = true,
                    enabled = !loading,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = wattText,
                    onValueChange = { if (!loading) wattText = it },
                    label = { Muted("Wattage (W) *") },
                    placeholder = { Muted("e.g., 1500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    enabled = !loading,
                    trailingIcon = {
                        // keep this compact and single-line
                        Text("W", style = MaterialTheme.typography.bodySmall, softWrap = false, maxLines = 1)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    maxItemsInEachRow = 3,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(100, 500, 800, 1500, 2200).forEach { w ->
                        AssistChip(
                            onClick = { if (!loading) wattText = w.toString() },
                            label = { Text("${w}W") }
                        )
                    }
                }
            }
        }

        // Usage slider
        item {
            SectionCard(title = "Usage") {
                Text("Hours used per day", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(6.dp))
                HoursSlider(hours = hours, onChange = { if (!loading) hours = it })
            }
        }

        // Category
        item {
            SectionCard(title = "Category") {
                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { if (!loading) catExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        enabled = !loading,
                        label = { Muted("Category *") },
                        placeholder = { Muted("Select a category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                        modifier = Modifier
                            .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                            .focusProperties { canFocus = false }
                    )
                    ExposedDropdownMenu(
                        expanded = catExpanded,
                        onDismissRequest = { catExpanded = false }
                    ) {
                        categories.forEach { c ->
                            DropdownMenuItem(
                                text = { Text(c, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                onClick = { category = c; catExpanded = false },
                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }
            }
        }

        // Estimation
        item { EstimateCard(watt = watt, hours = hours) }

        // Actions
        item {
            Row(Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = !loading,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Cancel") }

                Spacer(Modifier.width(12.dp))

                Button(
                    onClick = { onSave(name.trim(), watt, hours, category) },
                    enabled = saveEnabled,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text(if (applianceId == null) "Save Appliance" else "Save Changes") }
            }
        }
    }
}

/* ------------------------------ COMPONENTS -------------------------------- */

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun Muted(text: String) {
    Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
private fun HoursSlider(
    hours: Float,
    onChange: (Float) -> Unit
) {
    val snapped = remember(hours) { round(hours * 2) / 2f } // 0.5h increments
    Card(shape = RoundedCornerShape(14.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Muted("0 hours"); Text("$snapped", fontWeight = FontWeight.SemiBold); Muted("24 hours")
            }
            Slider(
                value = snapped,
                onValueChange = { onChange(it.coerceIn(0f, 24f)) },
                valueRange = 0f..24f,
                steps = 47
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Muted("0"); Muted("6"); Muted("12"); Muted("18"); Muted("24")
            }
        }
    }
}

@Composable
private fun EstimateCard(watt: Int, hours: Float) {
    val kWh = (watt * hours / 1000f).coerceAtLeast(0f)
    val cost = kWh * 0.30f
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Estimated daily energy", style = MaterialTheme.typography.bodyMedium)
            Text(
                String.format(Locale.getDefault(), "%.2f kWh  (~$%.2f)", kWh, cost),
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/* -------------------------------- PREVIEW ---------------------------------- */

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AddAppliancePreview() {
    FIT5046Lab4Group3ass2Theme { AddApplianceScaffold() }
}
