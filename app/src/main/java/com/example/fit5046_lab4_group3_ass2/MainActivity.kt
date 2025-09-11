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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

/* ------------------------------- DATA (UI only) ------------------------------- */

data class Appliance(
    val iconEmoji: String,
    val name: String,
    val spec: String,      // e.g., "150W • 6h daily"
    val costPerDay: String,// e.g., "$0.27/day"
    val kwh: String        // e.g., "0.9 kWh"
)

data class Suggestion(
    val title: String,
    val body: String,
    val leadingEmoji: String = "💡"
)

private val demoAppliances = listOf(
    Appliance("📺", "Living Room TV", "150W • 6h daily", "$0.27/day", "0.9 kWh"),
    Appliance("❄️", "Refrigerator", "200W • 24h daily", "$1.44/day", "4.8 kWh"),
    Appliance("👕", "Washing Machine", "500W • 2h daily", "$0.30/day", "1.0 kWh"),
    Appliance("💡", "LED Lights (8)", "80W • 8h daily", "$0.19/day", "0.64 kWh")
)

private val demoSuggestions = listOf(
    Suggestion(
        title = "Peak Hour Alert",
        body = "High load expected at 7pm today. Consider running your washing machine earlier to save $0.15."
    ),
    Suggestion(
        title = "Eco Tip",
        body = "Your TV has been on for 8+ hours. Consider using sleep mode to save 20W per hour.",
        leadingEmoji = "🪶"
    )
)

/* ------------------------------- MAIN SCAFFOLD ------------------------------- */

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
            /*ElectricityScreen(
                usageKwh = "8.4 kWh",
                costEstimate = "$2.52 estimated cost",
                co2 = "CO₂: 4.2kg equivalent",
                changePercent = "-12%",
                appliances = demoAppliances,
                suggestions = demoSuggestions
            )*/
            //Home()
            ProfileSetup()
        }
    }
}

/* --------------------------------- SCREEN ---------------------------------- */

@Composable
fun ElectricityScreen(
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
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item { Spacer(Modifier.height(8.dp)) }

        // Today's Usage card (colorful)
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
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            usageKwh,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
                            modifier = Modifier.weight(1f)
                        )
                        ChangePill(changePercent)
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        costEstimate,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                    )
                    Text(
                        co2,
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
                Text("My Appliances", fontWeight = FontWeight.SemiBold)
                Text("View Usage", color = MaterialTheme.colorScheme.primary)
            }
        }

        // Appliance list
        items(appliances) { appliance ->
            ApplianceCard(appliance)
            Spacer(Modifier.height(10.dp))
        }

        // Smart Suggestions header
        item {
            Text(
                "Smart Suggestions",
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
            )
        }

        // Suggestions list
        items(suggestions) { tip ->
            SuggestionCard(tip)
            Spacer(Modifier.height(10.dp))
        }

        // Bottom actions
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ActionCard(modifier = Modifier.weight(1f), label = "Usage Graph")
                ActionCard(modifier = Modifier.weight(1f), label = "Cost Calculator")
            }
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

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(text, fontSize = 12.sp, color = fg)
    }
}

@Composable
private fun ApplianceCard(appliance: Appliance) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Tinted icon tile
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
                        fontSize = 12.sp,
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
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { /* no-op */ },
                    modifier = Modifier.weight(1f)
                ) { Text("Edit") }

                OutlinedButton(
                    onClick = { /* no-op */ },
                    modifier = Modifier.weight(1f)
                ) { Text("Delete") }
            }
        }
    }
}

@Composable
private fun SuggestionCard(suggestion: Suggestion) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
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
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun ActionCard(
    modifier: Modifier = Modifier,
    label: String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(72.dp)
    ) {
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

private data class NavItem(val label: String, val icon: ImageVector)

/* -------------------------------- PREVIEW ---------------------------------- */

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ElectricityPreview() {
    FIT5046Lab4Group3ass2Theme {
        ScreenScaffold()
    }
}

@Composable
fun GeneralCard(
    modifier: Modifier = Modifier,
    title: String,
    mainText: String,
    smallText: String,
    progress: Float = 0f
) {
    Card(modifier = Modifier.padding(bottom = 10.dp)) {
        Spacer(modifier = Modifier.height(10.dp))
        if (title != "")
            BoldText(text = title)
        if (mainText != "")
            BoldText(text = mainText)
        if (smallText != "")
            SmallText(text = smallText)
        if (progress != 0f) {
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                color = ProgressIndicatorDefaults.linearColor,
                trackColor = ProgressIndicatorDefaults.linearTrackColor,
                strokeCap = ProgressIndicatorDefaults.LinearStrokeCap,
            )
        } else {
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

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
fun Home(modifier: Modifier = Modifier) {
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
                //Spacer(modifier = Modifier.height(36.dp))
                Text(
                    text = "Good morning!",
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Lets track your eco impact today",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
                GeneralCard(title = "EcoPoints", mainText = "2,450", smallText = "🔥 7-day streak")
                GeneralCard(
                    title = "Electricity",
                    mainText = "8.4 kWh",
                    smallText = "Today's usage",
                    progress = 0.8f
                )
                GeneralCard(
                    title = "Plastic Saved",
                    mainText = "2.1 kg",
                    smallText = "This week = 7 bottles",
                    progress = 0.5f
                )

                BoldText(text = "Quick Actions")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Button(onClick = {}, modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Add Appliance"
                        )
                    }
                    Button(onClick = {}, modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Log Purchase"
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Button(onClick = {}, modifier = Modifier.weight(1f)) {
                        Text(
                            text = "View Tips"
                        )
                    }
                    Button(onClick = {}, modifier = Modifier.weight(1f)) {
                        Text(
                            text = "View Stats"
                        )
                    }
                }
                BoldText(text = "Recent Activity")
                GeneralCard(
                    title = "Plastic bottle avoided",
                    mainText = "2 hours ago",
                    smallText = "+50 pts"
                )
                GeneralCard(
                    title = "Washing machine usage logged",
                    mainText = "5 hours ago",
                    smallText = "+25 pts"
                )
                GeneralCard(
                    title = "Today's Eco Tip",
                    mainText = "",
                    smallText = "Unplug devices when not in use"
                )

            }
        }
    }

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