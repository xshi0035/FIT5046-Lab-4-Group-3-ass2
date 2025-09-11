package com.example.fit5046_lab4_group3_ass2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FIT5046Lab4Group3ass2Theme {
                ScreenScaffold()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenScaffold() {
    // System icons only (placeholders where needed)
    val navItems = listOf(
        NavItem("Home", Icons.Filled.Home),
        NavItem("Appliances", Icons.Filled.Add),   // placeholder icon
        NavItem("Plastic", Icons.Filled.Delete),   // placeholder icon
        NavItem("Rewards", Icons.Filled.Star),
        NavItem("Profile", Icons.Filled.AccountCircle),
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Set up Profile") },
                navigationIcon = {
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) { Text("←") }
                },
                actions = {
                    Box(
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) { Text("⋮") }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                navItems.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = index == 4, // Appliances selected (UI-only)
                        onClick = { /* no-op */ },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        },
    ) { inner ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            ProfileSetup()
        }
    }
}

/* --------------------------------- SCREEN ---------------------------------- */

private data class NavItem(val label: String, val icon: ImageVector)

@Composable
fun BoldText(modifier: Modifier = Modifier, text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp),
        style = MaterialTheme.typography.labelLarge
    )
}

@Composable
fun SmallText(modifier: Modifier = Modifier, text: String) {
    Text(
        text = text,
        modifier = Modifier
            //.fillMaxWidth()
            .padding(horizontal = 10.dp),
        style = MaterialTheme.typography.bodyMedium
    )
}

//Profile setup screen
@Composable
fun ProfileSetup(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(bottom = 36.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Step 1",
                    modifier = Modifier
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "* means the field is required to be filled in.",
                    modifier = Modifier
                        .fillMaxWidth(),
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(10.dp))
                Card {
                    Spacer(modifier = Modifier.height(10.dp))
                    BoldText(text = "User Information")
                    OutlinedTextField(
                        value = "",
                        label = { SmallText(text = "Name *") },
                        onValueChange = {},
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = "",
                        label = { SmallText(text = "Email *") },
                        onValueChange = {},
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .fillMaxWidth()
                    )
                    DisplayDatePicker()
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))
                Card {
                    Spacer(modifier = Modifier.height(10.dp))
                    BoldText(text = "Household Information")
                    SmallText(text = "How many people live in your household?")
                    OutlinedTextField(
                        value = "",
                        label = { SmallText(text = "Number of people *") },
                        onValueChange = {},
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(thickness = 2.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    SmallText(text = "Which type of home do you live in?")
                    val homeRadioOptions =
                        listOf("Apartment", "Detached House", "Townhouse", "Other")
                    val (selectedOption, onOptionSelected) = remember {
                        mutableStateOf(homeRadioOptions[0])
                    }
                    Column(modifier.selectableGroup()) {
                        homeRadioOptions.forEach { text ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .selectable(
                                        selected = (text == selectedOption),
                                        onClick = { onOptionSelected(text) },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (text == selectedOption),
                                    onClick = null // null recommended for accessibility with screen readers
                                )
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(thickness = 2.dp)
                    Spacer(modifier = Modifier.height(10.dp))
                    SmallText(text = "Which of the following appliances do you use regularly?")
                    Row {
                        Column {
                            CheckboxItem("Refrigerator")
                            CheckboxItem("Air Conditioner")
                            CheckboxItem("Television")
                            CheckboxItem("Microwave")
                        }
                        Column {
                            CheckboxItem("Washing Machine")
                            CheckboxItem("Heater")
                            CheckboxItem("Computer")
                            CheckboxItem("Other")
                        }

                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))
                Card {
                    BoldText(text = "Location and Utility Information")
                    SmallText(text = "Select your state:")
                    StateMenu()

                    SmallText(text = "Electricity Provider (e.g. AGL, Origin):")
                    OutlinedTextField(
                        value = "",
                        label = { SmallText(text = "Electricity Provider") },
                        onValueChange = {},
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))
                Card {
                    BoldText(text = "Eco Preferences")
                    SmallText(text = "Notification Preferences")
                    Switch("Energy Tips *")
                    Switch("Weekly Progress Summary *")

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(thickness = 2.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    SmallText(text = "What is your preferred motivation style? *")
                    val radioOptions =
                        listOf("Financial Savings", "Environmental Impact", "Balanced")
                    val (selectedOption, onOptionSelected) = remember {
                        mutableStateOf(radioOptions[0])
                    }
                    Column(modifier.selectableGroup()) {
                        radioOptions.forEach { text ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .selectable(
                                        selected = (text == selectedOption),
                                        onClick = { onOptionSelected(text) },
                                        role = Role.RadioButton
                                    )
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = (text == selectedOption),
                                    onClick = null // null recommended for accessibility with screen readers
                                )
                                Text(
                                    text = text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
                Card {
                    BoldText(text = "Additional Personalisation")
                    SmallText(text = "Name for Dashboard:")
                    OutlinedTextField(
                        value = "",
                        label = { SmallText(text = "Dashboard Name") },
                        onValueChange = {},
                        modifier = Modifier
                            .padding(horizontal = 10.dp)
                            .fillMaxWidth()
                    )
                    SmallText(text = "This name will be used in various parts of the app, such as motivational messages")

                    Spacer(modifier = Modifier.height(10.dp))
                    SmallText(text = "Set Profile Avatar:")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    ) {
                        Button(
                            onClick = {}, modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 5.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = "\uD83C\uDF33")
                        }
                        Button(
                            onClick = {}, modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 5.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = "⚡")
                        }
                        Button(
                            onClick = {}, modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 5.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = "\uD83D\uDCA7")
                        }
                        Button(
                            onClick = {}, modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 5.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = "\uD83C\uDF3F")
                        }
                        Button(
                            onClick = {}, modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 5.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(text = "\uD83C\uDF31")
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Spacer(modifier = Modifier.height(30.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = {}, modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 5.dp),
                        contentPadding = PaddingValues(5.dp)
                    ) {
                        Text(text = "Skip for Now")
                    }
                    Button(
                        onClick = {}, modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 5.dp),
                        contentPadding = PaddingValues(5.dp)
                    ) {
                        Text(text = "Save & Continue")
                    }
                }
            }
        }
    }
}

@Composable
fun CheckboxItem(name: String) {
    var checked by remember { mutableStateOf(true) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = { checked = it }
        )
        Text(
            name,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
fun Switch(name: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        SmallText(text = name)
        var checked by remember { mutableStateOf(true) }

        Switch(
            modifier = Modifier.padding(horizontal = 20.dp),
            checked = checked,
            onCheckedChange = {
                checked = it
            }
        )
    }
}

@RequiresApi(0)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayDatePicker() {
    val calendar = Calendar.getInstance()
    var birthday by remember { mutableStateOf("") }
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = Instant.now().toEpochMilli()
    )
    var showDatePicker by remember {
        mutableStateOf(false)
    }
    var selectedDate by remember {
        mutableStateOf(calendar.timeInMillis)
    }
    Column(modifier = Modifier.padding(10.dp)) {
        OutlinedTextField(
            value = birthday,
            onValueChange = {},
            readOnly = true,
            label = { SmallText(text = "Date of Birth") },
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true },
            trailingIcon = {
                Icon(
                    Icons.Filled.DateRange,
                    contentDescription = "Select Date",
                    modifier = Modifier
                        .clickable { showDatePicker = true }
                        .size(40.dp)
                )
            }
        )
        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = {
                    showDatePicker = false
                },
                confirmButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                        selectedDate = datePickerState.selectedDateMillis!!
                        birthday = "DoB: ${formatter.format(Date(selectedDate))}"
                    }) {
                        Text(text = "OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDatePicker = false
                    }) {
                        Text(text = "Cancel")
                    }
                }
            ) //end of dialog
            { //still column scope
                DatePicker(
                    state = datePickerState
                )
            }
        }// end of if
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StateMenu() {
    val states = listOf("VIC", "QLD", "NSW", "SA", "TAS", "WA", "ACT", "NT")
    var isExpanded by remember { mutableStateOf(false) }
    var selectedState = remember { mutableStateOf(states[0]) }
    Column(
        modifier = Modifier
            .fillMaxSize()
    )
    {
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            modifier = Modifier.padding(10.dp),
            onExpandedChange = { isExpanded = it },
        ) {
            OutlinedTextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .focusProperties {
                        canFocus = false
                    },
                readOnly = true,
                value = selectedState.value,
                onValueChange = {},
                label = { SmallText(text = "State") },
//manages the arrow icon up and down
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                },
            )
            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            )
            {
                states.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { SmallText(text = selectionOption) },
                        onClick = {
                            selectedState.value = selectionOption
                            isExpanded = false
                        },
                        contentPadding =
                            ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}