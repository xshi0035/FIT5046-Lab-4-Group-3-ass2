package com.example.fit5046_lab4_group3_ass2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
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
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/* ------------------------------- SCAFFOLD ---------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScaffold(startStep: Int = 1) {
    // 1..2
    var step by rememberSaveable { mutableStateOf(startStep.coerceIn(1, 2)) }

    // Public pairs to avoid private-type lint errors
    val navItems: List<Pair<String, ImageVector>> = listOf(
        "Home" to Icons.Filled.Home,
        "Appliances" to Icons.Filled.Add,
        "EcoTrack" to Icons.Filled.Info,
        "Rewards" to Icons.Filled.Star,      // plural for consistency across app
        "Profile" to Icons.Filled.AccountCircle,
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Set Up Profile") },
                navigationIcon = {
                    IconButton(onClick = { /* UI only */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Notifications bell with unread dot (same pattern as Home/Rewards)
                    Box {
                        IconButton(onClick = { /* UI only */ }) {
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
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                navItems.forEachIndexed { index, (label, icon) ->
                    NavigationBarItem(
                        selected = index == 4,  // Profile selected (UI-only)
                        onClick = { /* UI only */ },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { inner ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            ProfileSetup(
                step = step,
                onStepPrev = { if (step > 1) step-- },
                onStepNext = { if (step < 2) step++ },
                onStepJump = { s -> step = s.coerceIn(1, 2) },
            )
        }
    }
}

/* --------------------------------- SCREEN ---------------------------------- */

@Composable
private fun SectionCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun SmallText(text: String) {
    Text(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall
    )
}

/** Step chips + progress bar (segmented look used elsewhere) */
@Composable
private fun SetupProgress(
    step: Int,           // 1..2
    total: Int = 2,
    onStepClick: (Int) -> Unit
) {
    val bg = MaterialTheme.colorScheme.surfaceVariant
    val sel = MaterialTheme.colorScheme.surface

    Column(Modifier.fillMaxWidth()) {
        // Chips
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(bg, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StepChip("Step 1", step == 1, Modifier.weight(1f), sel, bg) { onStepClick(1) }
                StepChip("Step 2", step == 2, Modifier.weight(1f), sel, bg) { onStepClick(2) }
            }
        }
        Spacer(Modifier.height(10.dp))
        // Linear progress (lambda form for consistency with other screens)
        LinearProgressIndicator(
            progress = { step / total.toFloat() },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun StepChip(
    label: String,
    selected: Boolean,
    modifier: Modifier,
    selectedColor: Color,
    unselectedColor: Color,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) selectedColor else unselectedColor,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .height(40.dp)
            .clickable { onClick() }
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                label,
                fontWeight = FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProfileSetup(
    step: Int,
    onStepPrev: () -> Unit,
    onStepNext: () -> Unit,
    onStepJump: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // Progress header
        item {
            SetupProgress(step = step, onStepClick = onStepJump)
        }

        // Small helper text
        item {
            SmallText("* means the field is required to be filled in.")
        }

        // STEP 1 ---------------------------------------------------------------
        if (step == 1) {
            // User Information
            item {
                SectionCard("User Information") {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { SmallText("Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { SmallText("Email *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    DisplayDatePicker()
                }
            }

            // Household Information
            item {
                SectionCard("Household Information") {
                    SmallText("How many people live in your household?")
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { SmallText("Number of people *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))
                    Divider()
                    Spacer(Modifier.height(16.dp))

                    SmallText("Which type of home do you live in?")
                    Spacer(Modifier.height(6.dp))

                    val homeOptions = listOf("Apartment", "Detached House", "Townhouse", "Other")
                    var selectedHome by remember { mutableStateOf(homeOptions.first()) }

                    Column(Modifier.selectableGroup()) {
                        homeOptions.forEach { option ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .selectable(
                                        selected = (option == selectedHome),
                                        onClick = { selectedHome = option },
                                        role = Role.RadioButton
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = option == selectedHome, onClick = null)
                                Spacer(Modifier.width(12.dp))
                                Text(option, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // Footer buttons (Step 1)
            item {
                Row(Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { /* UI only */ },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 5.dp),
                        contentPadding = PaddingValues(12.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Skip for Now") }
                    Button(
                        onClick = onStepNext,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 5.dp),
                        contentPadding = PaddingValues(12.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Next") }
                }
            }
        }

        // STEP 2 ---------------------------------------------------------------
        if (step == 2) {
            // Location and Utility Information
            item {
                SectionCard("Location and Utility Information") {
                    SmallText("Select your state:")
                    Spacer(Modifier.height(6.dp))
                    StateMenu()
                    Spacer(Modifier.height(12.dp))
                    SmallText("Electricity Provider (e.g. AGL, Origin):")
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { SmallText("Electricity Provider") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Eco Preferences
            item {
                SectionCard("Eco Preferences") {
                    SmallText("Notification Preferences")
                    Spacer(Modifier.height(8.dp))
                    LabeledSwitch("Energy Tips *")
                    LabeledSwitch("Weekly Progress Summary *")

                    Spacer(Modifier.height(16.dp))
                    Divider()
                    Spacer(Modifier.height(16.dp))

                    SmallText("What is your preferred motivation style? *")
                    Spacer(Modifier.height(6.dp))

                    val options = listOf("Financial Savings", "Environmental Impact", "Balanced")
                    var selected by remember { mutableStateOf(options.first()) }

                    Column(Modifier.selectableGroup()) {
                        options.forEach { o ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .selectable(
                                        selected = (o == selected),
                                        onClick = { selected = o },
                                        role = Role.RadioButton
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = o == selected, onClick = null)
                                Spacer(Modifier.width(12.dp))
                                Text(o, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // Additional Personalisation
            item {
                SectionCard("Additional Personalisation") {
                    SmallText("Name for Dashboard:")
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { SmallText("Dashboard Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    SmallText("This name will be used in various parts of the app, such as motivational messages")

                    Spacer(Modifier.height(16.dp))
                    SmallText("Set Profile Avatar:")
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth()) {
                        val btnMod = Modifier
                            .weight(1f)
                            .padding(horizontal = 5.dp)
                        Button(onClick = { }, modifier = btnMod, contentPadding = PaddingValues(0.dp)) { Text("\uD83C\uDF33") }
                        Button(onClick = { }, modifier = btnMod, contentPadding = PaddingValues(0.dp)) { Text("⚡") }
                        Button(onClick = { }, modifier = btnMod, contentPadding = PaddingValues(0.dp)) { Text("\uD83D\uDCA7") }
                        Button(onClick = { }, modifier = btnMod, contentPadding = PaddingValues(0.dp)) { Text("\uD83C\uDF3F") }
                        Button(onClick = { }, modifier = btnMod, contentPadding = PaddingValues(0.dp)) { Text("\uD83C\uDF31") }
                    }
                }
            }

            // Footer buttons (Step 2)
            item {
                Row(Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onStepPrev,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 5.dp),
                        contentPadding = PaddingValues(12.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Back") }
                    Button(
                        onClick = { /* submit/save */ },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 5.dp),
                        contentPadding = PaddingValues(12.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Save & Continue") }
                }
            }
        }
    }
}

/* ----------------------------- PARTS & INPUTS ------------------------------ */

@Composable
fun CheckboxItem(name: String) {
    var checked by remember { mutableStateOf(true) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = { checked = it })
        Spacer(Modifier.width(8.dp))
        Text(name, style = MaterialTheme.typography.bodyMedium)
    }
}

/** renamed to avoid shadowing Material3.Switch */
@Composable
fun LabeledSwitch(name: String) {
    var checked by remember { mutableStateOf(true) }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SmallText(name)
        Switch(
            modifier = Modifier.padding(horizontal = 4.dp),
            checked = checked,
            onCheckedChange = { checked = it }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayDatePicker() {
    val calendar = Calendar.getInstance()
    var birthday by remember { mutableStateOf("") }
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = birthday,
        onValueChange = {},
        readOnly = true,
        label = { SmallText("Date of Birth") },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        trailingIcon = {
            Icon(
                Icons.Filled.DateRange,
                contentDescription = "Select Date",
                modifier = Modifier
                    .size(24.dp)
                    .clickable { showDialog = true }
            )
        }
    )

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    val chosen = datePickerState.selectedDateMillis ?: calendar.timeInMillis
                    birthday = "DoB: ${formatter.format(Date(chosen))}"
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StateMenu() {
    val states = listOf("VIC", "QLD", "NSW", "SA", "TAS", "WA", "ACT", "NT")
    var isExpanded by remember { mutableStateOf(false) }
    var selectedState by remember { mutableStateOf(states.first()) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .focusProperties { canFocus = false },
            readOnly = true,
            value = selectedState,
            onValueChange = {},
            label = { SmallText("State") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded) }
        )
        ExposedDropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false }
        ) {
            states.forEach { option ->
                DropdownMenuItem(
                    text = { SmallText(option) },
                    onClick = {
                        selectedState = option
                        isExpanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

/* -------------------------------- PREVIEWS ---------------------------------- */

@Preview(showBackground = true, showSystemUi = true, name = "Profile Setup – Step 1")
@Composable
fun ProfilePreview_Step1() {
    FIT5046Lab4Group3ass2Theme { ProfileScaffold(startStep = 1) }
}

@Preview(showBackground = true, showSystemUi = true, name = "Profile Setup – Step 2")
@Composable
fun ProfilePreview_Step2() {
    FIT5046Lab4Group3ass2Theme { ProfileScaffold(startStep = 2) }
}
