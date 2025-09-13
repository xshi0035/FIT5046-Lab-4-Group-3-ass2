package com.example.fit5046_lab4_group3_ass2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme

/* ------------------------------- SCAFFOLD ------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    // Account
    name: String,
    email: String,
    memberSince: String,
    avatarUrl: String? = null,

    // Household
    householdSize: Int,
    location: String,
    focus: String = "Electricity",

    // Settings
    electricityReminders: Boolean,
    themeLabel: String,

    // Actions
    onBack: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onToggleElectricity: (Boolean) -> Unit = {},
    onTapTheme: () -> Unit = {},
    onTapPrivacy: () -> Unit = {},
    onTapFaq: () -> Unit = {},
    onTapContact: () -> Unit = {},
    onTapAbout: () -> Unit = {},

    // Stats
    ecoPoints: Int
) {
    // Bottom nav (consistent with other screens)
    val navItems = listOf(
        "Home" to Icons.Filled.Home,
        "Appliances" to Icons.Filled.Add,
        "EcoTrack" to Icons.Filled.Info,
        "Rewards" to Icons.Filled.Star,   // plural for consistency
        "Profile" to Icons.Filled.AccountCircle
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Bell + unread dot (same pattern as Achievements/Home)
                    Box {
                        IconButton(onClick = onNotifications) {
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
            NavigationBar {
                navItems.forEachIndexed { index, (label, icon) ->
                    NavigationBarItem(
                        selected = index == 4,     // Profile selected
                        onClick = { /* UI only */ },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label) }
                    )
                }
            }
        }
    ) { inner ->
        ProfileContent(
            name = name,
            email = email,
            memberSince = memberSince,
            avatarUrl = avatarUrl,
            householdSize = householdSize,
            location = location,
            focus = focus,
            electricityReminders = electricityReminders,
            themeLabel = themeLabel,
            onEditProfile = onEditProfile,
            onToggleElectricity = onToggleElectricity,
            onTapTheme = onTapTheme,
            onTapPrivacy = onTapPrivacy,
            onTapFaq = onTapFaq,
            onTapContact = onTapContact,
            onTapAbout = onTapAbout,
            ecoPoints = ecoPoints,
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        )
    }
}

/* -------------------------------- CONTENT -------------------------------- */

@Composable
private fun ProfileContent(
    name: String,
    email: String,
    memberSince: String,
    avatarUrl: String?,
    householdSize: Int,
    location: String,
    focus: String,
    electricityReminders: Boolean,
    themeLabel: String,
    onEditProfile: () -> Unit,
    onToggleElectricity: (Boolean) -> Unit,
    onTapTheme: () -> Unit,
    onTapPrivacy: () -> Unit,
    onTapFaq: () -> Unit,
    onTapContact: () -> Unit,
    onTapAbout: () -> Unit,
    ecoPoints: Int,
    modifier: Modifier = Modifier
) {
    val isEmptyProfile = name.isBlank() && email.isBlank()

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header (Card for consistency)
        item {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarPlaceholder(avatarUrl = avatarUrl, size = 56.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (name.isNotBlank()) name else "Set up your profile",
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                if (email.isNotBlank()) email else "Add your email",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (memberSince.isNotBlank()) {
                                Text(
                                    "Member since $memberSince",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Household Details (tonal like other summary sections)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Household Details", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(10.dp))
                    InfoKeyValue("Household Size", if (householdSize > 0) "$householdSize people" else "—")
                    Spacer(Modifier.height(6.dp))
                    InfoKeyValue("Location", valueOrDash(location))
                    Spacer(Modifier.height(6.dp))
                    InfoKeyValue("Focus", valueOrDash(focus))   // Electricity
                }
            }
        }

        // Primary action
        item {
            Button(
                onClick = onEditProfile,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(if (isEmptyProfile) "Set Up Profile" else "Edit Profile", fontSize = 16.sp)
            }
        }

        // Settings
        item { SectionHeader("Settings") }
        item {
            SettingSwitchRow(
                title = "Electricity Reminders",
                checked = electricityReminders,
                onCheckedChange = onToggleElectricity
            )
        }
        item { Divider() }
        item { SettingChevronRow(title = "Theme", value = themeLabel, onClick = onTapTheme) }
        item { Divider() }
        item { SettingChevronRow(title = "Privacy & Data Sharing", onClick = onTapPrivacy) }

        // Help & Support
        item { SectionHeader("Help & Support") }
        item { SettingChevronRow("FAQs", onClick = onTapFaq) }
        item { Divider() }
        item { SettingChevronRow("Contact Support", onClick = onTapContact) }

        // About
        item { SectionHeader("About App") }
        item { SettingChevronRow(title = "Learn about UN SDG Goals", onClick = onTapAbout) }

        // Impact Summary
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Column(
                    Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Your Impact Summary", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "%,d".format(ecoPoints),
                        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        "EcoPoints Earned",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

/* ----------------------------- Reusables ----------------------------- */

@Composable
private fun AvatarPlaceholder(avatarUrl: String?, size: Dp = 56.dp) {
    // UI-only placeholder (no image loading)
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .background(MaterialTheme.colorScheme.surface)
    )
}

private fun valueOrDash(value: String) = if (value.isBlank()) "—" else value

@Composable
private fun InfoKeyValue(key: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(key, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingChevronRow(
    title: String,
    value: String = "",
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (value.isNotBlank()) {
                Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/* ------------------------------- PREVIEWS ------------------------------- */

@Preview(showBackground = true, showSystemUi = true, widthDp = 411, heightDp = 1100)
@Composable
fun Preview_Profile_Filled() {
    var electricity by remember { mutableStateOf(true) }
    FIT5046Lab4Group3ass2Theme {
        ProfileScreen(
            name = "Sarah Johnson",
            email = "sarah.johnson@email.com",
            memberSince = "Jan 2025",
            avatarUrl = null,
            householdSize = 4,
            location = "Melbourne, Australia",
            focus = "Electricity",
            electricityReminders = electricity,
            themeLabel = "Light",
            onToggleElectricity = { electricity = it },
            ecoPoints = 2847
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, widthDp = 411, heightDp = 1100)
@Composable
fun Preview_Profile_Empty() {
    var electricity by remember { mutableStateOf(false) }
    FIT5046Lab4Group3ass2Theme {
        // Empty name/email shows the “Set Up Profile” CTA and dashes in details
        ProfileScreen(
            name = "",
            email = "",
            memberSince = "",
            avatarUrl = null,
            householdSize = 0,
            location = "",
            focus = "Electricity",
            electricityReminders = electricity,
            themeLabel = "System",
            onToggleElectricity = { electricity = it },
            ecoPoints = 0
        )
    }
}
