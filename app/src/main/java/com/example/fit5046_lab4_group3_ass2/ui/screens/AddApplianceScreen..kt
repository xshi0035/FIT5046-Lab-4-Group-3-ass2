package com.example.fit5046_lab4_group3_ass2.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.fit5046_lab4_group3_ass2.viewmodel.AddApplianceViewModel
import kotlin.math.round
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddApplianceScaffold(
    navController: NavController,
    viewModel: AddApplianceViewModel = viewModel(),
    onBack: () -> Unit,
    onSave: (String, Int, Float, String) -> Unit,
    onCancel: () -> Unit
) {
    val items = bottomNavItems()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Add Appliance") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route
                items.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            if (currentRoute == item.route) return@NavigationBarItem
                            navController.navigate(item.route) {
                                popUpTo(NavRoutes.HOME) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { inner ->
        AddApplianceContent(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            onSave = onSave,
            onCancel = onCancel
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AddApplianceContent(
    modifier: Modifier = Modifier,
    onSave: (String, Int, Float, String) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var wattText by remember { mutableStateOf("") }
    var hours by remember { mutableFloatStateOf(8f) }
    val categories = listOf("Cooling & Heating", "Kitchen", "Entertainment", "Lighting", "Cleaning", "Other")
    var category by remember { mutableStateOf("") }
    var catExpanded by remember { mutableStateOf(false) }

    val watt = wattText.filter { it.isDigit() }.take(5).toIntOrNull() ?: 0
    val saveEnabled = name.isNotBlank() && watt > 0 && category.isNotBlank()

    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Appliance Details", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Fill in the fields below. Name and category are required.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            SectionCard(title = "Basic Info") {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Muted("Appliance Name *") },
                    placeholder = { Muted("e.g., Air Conditioner") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = wattText,
                    onValueChange = { wattText = it },
                    label = { Muted("Wattage (W) *") },
                    placeholder = { Muted("e.g., 1500") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    trailingIcon = {
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
                        AssistChip(onClick = { wattText = w.toString() }, label = { Text("${w}W") })
                    }
                }
            }
        }

        item {
            SectionCard(title = "Usage") {
                Text("Hours used per day", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(6.dp))
                HoursSlider(hours = hours, onChange = { hours = it })
            }
        }

        item {
            SectionCard(title = "Category") {
                ExposedDropdownMenuBox(
                    expanded = catExpanded,
                    onExpandedChange = { catExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
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

        item {
            EstimateCard(watt = watt, hours = hours)
        }

        item {
            Row(Modifier.fillMaxWidth()) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Cancel") }

                Spacer(Modifier.width(12.dp))

                Button(
                    onClick = {
                        onSave(name.trim(), watt, hours, category)
                    },
                    enabled = saveEnabled,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Save Appliance") }
            }
        }
    }
}

/* ------------------------------ Helpers ---------------------------------- */

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
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
private fun HoursSlider(hours: Float, onChange: (Float) -> Unit) {
    val snapped = remember(hours) { round(hours * 2) / 2f }
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
                String.format(java.util.Locale.getDefault(), "%.2f kWh  (~$%.2f)", kWh, cost),
                fontWeight = FontWeight.Medium
            )
        }
    }
}
