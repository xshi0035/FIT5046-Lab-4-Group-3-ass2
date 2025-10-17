@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.fit5046_lab4_group3_ass2.data.ProfileRepo
import com.example.fit5046_lab4_group3_ass2.data.UserProfile
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* ------------------------------- SCAFFOLD ---------------------------------- */

@Composable
fun ProfileScaffold(
    startStep: Int = 1,
    onSaved: () -> Unit = {},
    onBack: () -> Unit = {}                 // Step 1 back -> Profile
) {
    var step by rememberSaveable { mutableStateOf(startStep.coerceIn(1, 2)) }
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }

    // ---------------- Form state shared across steps ----------------
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var dob by rememberSaveable { mutableStateOf("") } // "yyyy-MM-dd"

    var householdSize by rememberSaveable { mutableStateOf("1") }
    var homeType by rememberSaveable { mutableStateOf("Apartment") }

    var state by rememberSaveable { mutableStateOf("VIC") }
    var electricityProvider by rememberSaveable { mutableStateOf("") }
    var energyTips by rememberSaveable { mutableStateOf(true) }
    var weeklySummary by rememberSaveable { mutableStateOf(true) }
    var motivationStyle by rememberSaveable { mutableStateOf("Balanced") }
    var dashboardName by rememberSaveable { mutableStateOf("") }
    var avatar by rememberSaveable { mutableStateOf("🌳") }

    var saving by remember { mutableStateOf(false) }
    var loading by remember { mutableStateOf(true) }

    /* --------- Fetch current profile FIRST (nullable-safe) --------- */
    LaunchedEffect(Unit) {
        loading = true
        ProfileRepo.get { res ->
            res.onSuccess { maybe ->
                // maybe: UserProfile?  (handle safely)
                val p = maybe
                if (p != null) {
                    if (p.name.isNotBlank()) name = p.name
                    email = if (p.email.isNotBlank()) p.email else auth.currentUser?.email.orEmpty()
                    dob = p.dob.orEmpty()

                    if (p.householdSize > 0) householdSize = p.householdSize.toString()
                    if (p.homeType.isNotBlank()) homeType = p.homeType

                    if (p.state.isNotBlank()) state = p.state
                    electricityProvider = p.electricityProvider.orEmpty()
                    energyTips = p.energyTips
                    weeklySummary = p.weeklySummary
                    if (p.motivationStyle.isNotBlank()) motivationStyle = p.motivationStyle
                    dashboardName = p.dashboardName.orEmpty()
                    if (p.avatar.isNotBlank()) avatar = p.avatar
                } else {
                    // No existing doc; at least prefill email if logged in
                    email = auth.currentUser?.email.orEmpty()
                }
            }.onFailure { e ->
                email = auth.currentUser?.email.orEmpty()
                scope.launch { snackbarHost.showSnackbar(e.message ?: "Failed to load profile.") }
            }
            loading = false
        }
    }

    fun handleBack() {
        if (step > 1) step-- else onBack()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Set Up Profile") },
                navigationIcon = {
                    IconButton(onClick = ::handleBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Notifications bell with unread dot (UI-only)
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
        snackbarHost = { SnackbarHost(snackbarHost) },
        bottomBar = { /* no bottom bar here */ }
    ) { inner ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                ProfileSetup(
                    step = step,
                    // STEP 1
                    name = name, onName = { name = it },
                    email = email, onEmail = { email = it },
                    dob = dob, onDob = { dob = it },
                    householdSize = householdSize, onHouseholdSize = { s ->
                        householdSize = s.filter { it.isDigit() }.ifEmpty { "0" }
                    },
                    homeType = homeType, onHomeType = { homeType = it },

                    // STEP 2
                    state = state, onState = { state = it },
                    electricityProvider = electricityProvider, onProvider = { electricityProvider = it },
                    energyTips = energyTips, onEnergyTips = { energyTips = it },
                    weeklySummary = weeklySummary, onWeeklySummary = { weeklySummary = it },
                    motivationStyle = motivationStyle, onMotivation = { motivationStyle = it },
                    dashboardName = dashboardName, onDashboardName = { dashboardName = it },
                    avatar = avatar, onAvatar = { avatar = it },

                    onStepPrev = { handleBack() },
                    onStepNext = { if (step < 2) step++ },
                    onStepJump = { s -> step = s.coerceIn(1, 2) },

                    onSave = {
                        val hh = householdSize.toIntOrNull()
                        if (name.isBlank() || email.isBlank() || hh == null || hh <= 0) {
                            scope.launch { snackbarHost.showSnackbar("Please fill all required fields.") }
                            return@ProfileSetup
                        }
                        saving = true
                        val payload = UserProfile(
                            name = name.trim(),
                            email = email.trim(),
                            dob = dob,
                            householdSize = hh,
                            homeType = homeType,
                            state = state,
                            electricityProvider = electricityProvider.trim(),
                            energyTips = energyTips,
                            weeklySummary = weeklySummary,
                            motivationStyle = motivationStyle,
                            dashboardName = dashboardName.trim(),
                            avatar = avatar
                        )
                        ProfileRepo.upsert(payload) { up ->
                            saving = false
                            up.onSuccess {
                                scope.launch { snackbarHost.showSnackbar("Profile saved!") }
                                onSaved()
                            }.onFailure { e ->
                                scope.launch { snackbarHost.showSnackbar(e.message ?: "Failed to save profile.") }
                            }
                        }
                    },
                    saving = saving
                )
            }
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

/** Step chips + progress bar */
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

/* -------------------------- The main content --------------------------- */

@Composable
private fun ProfileSetup(
    step: Int,
    // step 1
    name: String, onName: (String) -> Unit,
    email: String, onEmail: (String) -> Unit,
    dob: String, onDob: (String) -> Unit,
    householdSize: String, onHouseholdSize: (String) -> Unit,
    homeType: String, onHomeType: (String) -> Unit,
    // step 2
    state: String, onState: (String) -> Unit,
    electricityProvider: String, onProvider: (String) -> Unit,
    energyTips: Boolean, onEnergyTips: (Boolean) -> Unit,
    weeklySummary: Boolean, onWeeklySummary: (Boolean) -> Unit,
    motivationStyle: String, onMotivation: (String) -> Unit,
    dashboardName: String, onDashboardName: (String) -> Unit,
    avatar: String, onAvatar: (String) -> Unit,

    onStepPrev: () -> Unit,
    onStepNext: () -> Unit,
    onStepJump: (Int) -> Unit,
    onSave: () -> Unit,
    saving: Boolean,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        item { SetupProgress(step = step, onStepClick = onStepJump) }

        item { SmallText("* means the field is required to be filled in.") }

        if (step == 1) {
            item {
                SectionCard("User Information") {
                    OutlinedTextField(
                        value = name, onValueChange = onName,
                        label = { SmallText("Name *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = email, onValueChange = onEmail,
                        label = { SmallText("Email *") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                    DisplayDatePicker(value = dob, onPicked = onDob)
                }
            }

            item {
                SectionCard("Household Information") {
                    SmallText("How many people live in your household?")
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = householdSize, onValueChange = onHouseholdSize,
                        label = { SmallText("Number of people *") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(16.dp))

                    SmallText("Which type of home do you live in?")
                    Spacer(Modifier.height(6.dp))

                    val homeOptions = listOf("Apartment", "Detached House", "Townhouse", "Other")

                    Column(Modifier.selectableGroup()) {
                        homeOptions.forEach { option ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .selectable(
                                        selected = (option == homeType),
                                        onClick = { onHomeType(option) },
                                        role = Role.RadioButton
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = option == homeType, onClick = null)
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
                        onClick = onStepNext,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(12.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) { Text("Next") }
                }
            }
        }

        if (step == 2) {
            item {
                SectionCard("Location and Utility Information") {
                    SmallText("Select your state:")
                    Spacer(Modifier.height(6.dp))
                    StateMenu(value = state, onValueChange = onState)
                    Spacer(Modifier.height(12.dp))
                    SmallText("Electricity Provider (e.g. AGL, Origin):")
                    Spacer(Modifier.height(6.dp))
                    OutlinedTextField(
                        value = electricityProvider, onValueChange = onProvider,
                        label = { SmallText("Electricity Provider") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                SectionCard("Eco Preferences") {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SmallText("Energy Tips *")
                        Switch(checked = energyTips, onCheckedChange = onEnergyTips)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SmallText("Weekly Progress Summary *")
                        Switch(checked = weeklySummary, onCheckedChange = onWeeklySummary)
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
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
                                        selected = (o == motivationStyle),
                                        onClick = { onMotivation(o) },
                                        role = Role.RadioButton
                                    ),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = o == motivationStyle, onClick = null)
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
                        value = dashboardName, onValueChange = onDashboardName,
                        label = { SmallText("Dashboard Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    SmallText("Set Profile Avatar:")
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth()) {
                        listOf("🌳","⚡","💧","🌿","🌱").forEach { em ->
                            val selected = avatar == em
                            Button(
                                onClick = { onAvatar(em) },
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 5.dp),
                                contentPadding = PaddingValues(0.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor =
                                        if (selected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) { Text(em) }
                        }
                    }
                }
            }

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
                        onClick = onSave,
                        enabled = !saving,
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 5.dp),
                        contentPadding = PaddingValues(12.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        if (saving)
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                        else
                            Text("Save & Continue")
                    }
                }
            }
        }
    }
}

/* ----------------------------- PARTS & INPUTS ------------------------------ */

@Composable
private fun CheckboxItem(name: String) {
    var checked by remember { mutableStateOf(true) }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(checked = checked, onCheckedChange = { checked = it })
        Spacer(Modifier.width(8.dp))
        Text(name, style = MaterialTheme.typography.bodyMedium)
    }
}

/* Date picker that returns "yyyy-MM-dd" via onPicked */
@Composable
private fun DisplayDatePicker(value: String, onPicked: (String) -> Unit) {
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var showDialog by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = value,
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
                    val millis = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
                    onPicked(formatter.format(Date(millis)))
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun StateMenu(value: String, onValueChange: (String) -> Unit) {
    val states = listOf("VIC", "QLD", "NSW", "SA", "TAS", "WA", "ACT", "NT")
    var isExpanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        onExpandedChange = { isExpanded = it },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            modifier = Modifier
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
                .focusProperties { canFocus = false },
            readOnly = true,
            value = value,
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
                        onValueChange(option)
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
fun ProfilePreview_Step_2() {
    FIT5046Lab4Group3ass2Theme { ProfileScaffold(startStep = 2) }
}
