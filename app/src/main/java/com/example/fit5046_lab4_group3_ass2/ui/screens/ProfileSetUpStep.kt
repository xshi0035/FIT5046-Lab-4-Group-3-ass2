package com.example.fit5046_lab4_group3_ass2.screens

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.example.fit5046_lab4_group3_ass2.ui.profile.ProfileViewModel
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme
import kotlinx.coroutines.flow.collectLatest
import java.util.Calendar
import java.util.Date
import java.util.Locale

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed

import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup

import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScaffold(
    startStep: Int = 1,
    viewModel: ProfileViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    var step by remember { mutableIntStateOf(startStep) }

    val navItems: List<Pair<String, androidx.compose.ui.graphics.vector.ImageVector>> = listOf(
        "Home" to Icons.Filled.Home,
        "Appliances" to Icons.Filled.Add,
        "EcoTrack" to Icons.Filled.Info,
        "Rewards" to Icons.Filled.Star,
        "Profile" to Icons.Filled.AccountCircle,
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Set Up Profile") },
                navigationIcon = {
                    IconButton(onClick = { /* navController.popBackStack() 以后接导航 */ }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { /* notifications */ }) {
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
                        selected = index == 4,
                        onClick = { /* nav */ },
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
                viewModel = viewModel,
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

@Composable private fun SmallText(text: String) =
    Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)

/** Step chips + progress bar */
@Composable
private fun SetupProgress(
    step: Int,
    total: Int = 2,
    onStepClick: (Int) -> Unit
) {
    val bg = MaterialTheme.colorScheme.surfaceVariant
    val sel = MaterialTheme.colorScheme.surface

    Column(Modifier.fillMaxWidth()) {
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
        LinearProgressIndicator(
            progress = { step / total.toFloat() },
            modifier = Modifier.fillMaxWidth().height(8.dp),
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
        modifier = modifier.height(40.dp).clickable { onClick() }
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
    viewModel: ProfileViewModel,
    onStepPrev: () -> Unit,
    onStepNext: () -> Unit,
    onStepJump: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.profile.collectAsState()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item { SetupProgress(step = step, onStepClick = onStepJump) }
        item { SmallText("* means the field is required to be filled in.") }

        // STEP 1
        if (step == 1) {
            item {
                SectionCard("User Information") {
                    OutlinedTextField(
                        value = profile.name,
                        onValueChange = viewModel::updateName,
                        label = { SmallText("Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = profile.email,
                        onValueChange = viewModel::updateEmail,
                        label = { SmallText("Email *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    DisplayDatePicker(
                        selectedDate = profile.dob,
                        onDateSelected = viewModel::updateDob
                    )
                }
            }

            item {
                SectionCard("Household Information") {
                    SmallText("How many people live in your household?")
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = profile.householdSize,
                        onValueChange = viewModel::updateHouseholdSize,
                        label = { SmallText("Number of people *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))
                    Divider()
                    Spacer(Modifier.height(16.dp))

                    SmallText("Which type of home do you live in?")
                    Spacer(Modifier.height(6.dp))

                    val options = listOf("Apartment", "Detached House", "Townhouse", "Other")
                    Column(Modifier.selectableGroup()) {
                        options.forEach { option ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .selectable(
                                        selected = (option == profile.homeType),
                                        onClick = { viewModel.updateHomeType(option) },
                                        role = Role.RadioButton
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = option == profile.homeType, onClick = null)
                                Spacer(Modifier.width(12.dp))
                                Text(option, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            item {
                Row(Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { /* optional skip */ },
                        modifier = Modifier.weight(1f).padding(horizontal = 5.dp),
                        contentPadding = PaddingValues(12.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Skip for Now") }
                    Button(
                        onClick = onStepNext,
                        modifier = Modifier.weight(1f).padding(horizontal = 5.dp),
                        contentPadding = PaddingValues(12.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Next") }
                }
            }
        }

        // STEP 2
        if (step == 2) {
            item {
                SectionCard("Location and Utility Information") {
                    SmallText("Select your state:")
                    Spacer(Modifier.height(6.dp))
                    StateMenu(
                        selected = profile.state,
                        onSelect = viewModel::updateState
                    )
                    Spacer(Modifier.height(12.dp))
                    SmallText("Electricity Provider (e.g. AGL, Origin):")
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = profile.electricityProvider,
                        onValueChange = viewModel::updateElectricityProvider,
                        label = { SmallText("Electricity Provider") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                SectionCard("Eco Preferences") {
                    SmallText("Notification Preferences")
                    Spacer(Modifier.height(8.dp))
                    LabeledSwitch("Energy Tips *", profile.notifEnergyTips, viewModel::updateNotifEnergyTips)
                    LabeledSwitch("Weekly Progress Summary *", profile.notifWeeklySummary, viewModel::updateNotifWeeklySummary)

                    Spacer(Modifier.height(16.dp))
                    Divider()
                    Spacer(Modifier.height(16.dp))

                    SmallText("What is your preferred motivation style? *")
                    Spacer(Modifier.height(6.dp))

                    val options = listOf("Financial Savings", "Environmental Impact", "Balanced")
                    Column(Modifier.selectableGroup()) {
                        options.forEach { o ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .selectable(
                                        selected = (o == profile.motivation),
                                        onClick = { viewModel.updateMotivation(o) },
                                        role = Role.RadioButton
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = o == profile.motivation, onClick = null)
                                Spacer(Modifier.width(12.dp))
                                Text(o, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            item {
                SectionCard("Additional Personalisation") {
                    SmallText("Name for Dashboard:")
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = profile.dashboardName,
                        onValueChange = viewModel::updateDashboardName,
                        label = { SmallText("Dashboard Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Row(Modifier.fillMaxWidth()) {
                    Button(
                        onClick = onStepPrev,
                        modifier = Modifier.weight(1f).padding(horizontal = 5.dp),
                        contentPadding = PaddingValues(12.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Back") }
                    Button(
                        onClick = {
                            viewModel.saveProfile()
                            // 这里未来可 navController.popBackStack() 返回 Profile
                        },
                        modifier = Modifier.weight(1f).padding(horizontal = 5.dp),
                        contentPadding = PaddingValues(12.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Save & Continue") }
                }
            }
        }
    }
}

/* ----------------------------- Reusables ----------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StateMenu(
    selected: String,
    onSelect: (String) -> Unit
) {
    val states = listOf("VIC", "QLD", "NSW", "SA", "TAS", "WA", "ACT", "NT")
    var isExpanded by remember { mutableStateOf(false) }

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
            value = selected,
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
                        onSelect(option)
                        isExpanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayDatePicker(
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    val calendar = remember { Calendar.getInstance() }
    val formatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val datePickerState = rememberDatePickerState()

    OutlinedTextField(
        value = if (selectedDate.isNotBlank()) selectedDate else "",
        onValueChange = {},
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        readOnly = true,
        label = { SmallText("Date of Birth") }
    )

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    val chosen = datePickerState.selectedDateMillis ?: calendar.timeInMillis
                    onDateSelected(formatter.format(Date(chosen)))
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun LabeledSwitch(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

/* -------------------------------- PREVIEWS ---------------------------------- */
@Preview(showBackground = true, showSystemUi = true, name = "Profile Setup – Step 1")
@Composable fun ProfilePreview_Step1() {
    FIT5046Lab4Group3ass2Theme { ProfileScaffold(startStep = 1) }
}
@Preview(showBackground = true, showSystemUi = true, name = "Profile Setup – Step 2")
@Composable fun ProfilePreview_Step2() {
    FIT5046Lab4Group3ass2Theme { ProfileScaffold(startStep = 2) }
}
