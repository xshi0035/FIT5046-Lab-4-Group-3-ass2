package com.example.fit5046_lab4_group3_ass2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    name: String,
    email: String,
    memberSince: String,
    avatarUrl: String? = null,
    // Household
    householdSize: Int,
    location: String,
    focus: String = "Electricity",        // 仅电力
    // Settings
    electricityReminders: Boolean,
    themeLabel: String,
    onBack: () -> Unit = {},
    onMenu: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onToggleElectricity: (Boolean) -> Unit = {},
    onTapTheme: () -> Unit = {},
    onTapPrivacy: () -> Unit = {},
    onTapFaq: () -> Unit = {},
    onTapContact: () -> Unit = {},
    onTapAbout: () -> Unit = {},
    ecoPoints: Int
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onMenu) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                Surface(tonalElevation = 1.dp, shape = RoundedCornerShape(0.dp)) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AvatarPlaceholder(avatarUrl = avatarUrl, size = 56.dp)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Member since $memberSince", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Household Details
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Household Details", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(10.dp))
                        InfoKeyValue("Household Size", "$householdSize people")
                        Spacer(Modifier.height(6.dp))
                        InfoKeyValue("Location", location)
                        Spacer(Modifier.height(6.dp))
                        InfoKeyValue("Focus", focus)   // Electricity
                    }
                }
            }

            // Edit Profile
            item {
                Box(Modifier.padding(horizontal = 16.dp)) {
                    Button(
                        onClick = onEditProfile,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) { Text("Create Profile", fontSize = 16.sp) }
                }
            }

            // Settings
            item {
                SectionHeader("Settings")
                SettingSwitchRow(
                    title = "Electricity Reminders",
                    checked = electricityReminders,
                    onCheckedChange = onToggleElectricity
                )
            }

            // Theme
            item {
                Divider()
                SettingChevronRow(title = "Theme", value = themeLabel, onClick = onTapTheme)
            }

            // Privacy & Data
            item {
                Divider()
                SettingChevronRow(title = "Privacy & Data Sharing", onClick = onTapPrivacy)
            }

            // Help & Support
            item {
                SectionHeader("Help & Support")
                SettingChevronRow("FAQs", onClick = onTapFaq)
                Divider()
                SettingChevronRow("Contact Support", onClick = onTapContact)
            }

            // About
            item {
                SectionHeader("About App")
                SettingChevronRow(title = "Learn about UN SDG Goals", onClick = onTapAbout)
            }

            // Impact Summary
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Your Impact Summary", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        Text("%,d".format(ecoPoints), style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold))
                        Text("EcoPoints Earned", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

/* ---------- Reusable ---------- */

@Composable
private fun AvatarPlaceholder(avatarUrl: String?, size: Dp = 56.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .background(MaterialTheme.colorScheme.surface)
    )
}

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
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp)
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
            .padding(horizontal = 16.dp, vertical = 12.dp),
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
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            if (value.isNotBlank()) {
                Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/* ---------- Preview ---------- */

@Preview(showBackground = true, showSystemUi = true, widthDp = 411, heightDp = 1100)
@Composable
fun Preview_ProfileScreen() {
    var electricity by remember { mutableStateOf(true) }

    MaterialTheme {
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
