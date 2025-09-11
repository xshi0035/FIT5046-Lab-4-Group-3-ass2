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

@Composable
fun ProfileSetup(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        //.padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 96.dp)
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
                Spacer(modifier = Modifier.height(10.dp))
                Card {
                    Spacer(modifier = Modifier.height(10.dp))
                    BoldText(text = "User Information")
                    SmallText(text = "Enter your name:")
                    OutlinedTextField(
                        value = "",
                        label = { SmallText(text = "Name") },
                        onValueChange = {},
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    SmallText(text = "Enter your email:")
                    OutlinedTextField(
                        value = "",
                        label = { SmallText(text = "Email") },
                        onValueChange = {},
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SmallText(text = "Enter your date of birth:")
                    DisplayDatePicker()
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Card {
                    Spacer(modifier = Modifier.height(10.dp))
                    BoldText(text = "Household Information")
                    SmallText(text = "How many people live in your household?")
                    OutlinedTextField(
                        value = "",
                        label = { SmallText(text = "Enter number of people") },
                        onValueChange = {},
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    SmallText(text = "What type of home do you live in?")

                    val radioOptions = listOf("Apartment", "Detached House", "Townhouse", "Other")
                    val (selectedOption, onOptionSelected) = remember { mutableStateOf(radioOptions[0]) }
                    // Note that Modifier.selectableGroup() is essential to ensure correct accessibility behavior
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
                    Spacer(modifier = Modifier.height(10.dp))
                    SmallText(text = "Which of the following do you use regularly?")
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
                Spacer(modifier = Modifier.height(10.dp))
                Card {
                    BoldText(text="Location and Utility Information")
                    SmallText(text = "Select your state:")
                    StateMenu()
                    SmallText(text = "Electricity Provider (e.g. AGL, Origin):")
                    OutlinedTextField(
                        value = "",
                        label = { SmallText(text = "Electricity Provider") },
                        onValueChange = {},
                        modifier = Modifier.padding(horizontal = 10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
                Spacer(modifier = Modifier.height(10.dp))
                Card {
                    BoldText(text="Eco Preferences")
                    SmallText(text = "Notification Preferences")
                    Switch("Energy Tips")
                    Switch("Weekly Progress Summary")
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
    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ){
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
        TextField(
            value = birthday,
            onValueChange = {},
            readOnly = true,
            label = { Text("Birthday") },
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
            TextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
                    .focusProperties {
                        canFocus = false
                    },
                readOnly = true,
                value = selectedState.value,
                onValueChange = {},
                label = { Text("State") },
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
                        text = { Text(selectionOption) },
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