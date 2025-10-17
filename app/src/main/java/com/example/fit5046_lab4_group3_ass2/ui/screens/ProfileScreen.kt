@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fit5046_lab4_group3_ass2.data.ProfileRepo
import com.example.fit5046_lab4_group3_ass2.data.UserProfile
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/* ───────────── ROUTE / CONTAINER ───────────── */

@Composable
fun ProfileRoute(
    onBack: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onLogout: () -> Unit = {},                      // NEW: callback to AppNav
    // NAV for bottom bar from Profile
    onTabSelected: (route: String) -> Unit = {}
) {
    val snackbar = remember { SnackbarHostState() }
    val auth = remember { FirebaseAuth.getInstance() }

    var loading by remember { mutableStateOf(true) }
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Screen state management
    var currentScreen by remember { mutableStateOf("profile") }

    // Fetch profile on first composition
    LaunchedEffect(Unit) {
        ProfileRepo.get { res ->
            res.onSuccess { p ->
                profile = p
                loading = false
            }.onFailure { e ->
                error = e.message ?: "Failed to load profile."
                loading = false
            }
        }
    }

    fun reload() {
        error = null
        loading = true
        ProfileRepo.get { res ->
            res.onSuccess { p -> profile = p; loading = false }
                .onFailure { e -> error = e.message ?: "Failed to load profile."; loading = false }
        }
    }

    // When user toggles reminders, update Firestore doc
    fun updateElectricityReminders(checked: Boolean) {
        val current = profile ?: UserProfile()
        val updated = current.copy(energyTips = checked)
        ProfileRepo.upsert(updated) { res ->
            res.onSuccess { profile = updated }
                .onFailure { e ->
                    profile = current // revert
                    error = e.message ?: "Failed to update setting."
                }
        }
    }

    // Loading
    if (loading) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Profile") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            bottomBar = {
                EcoBottomBar(currentRoute = ROUTE_PROFILE, onTabSelected = onTabSelected)
            }
        ) { inner ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(inner),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }

    // Error
    if (error != null) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text("Profile") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbar) },
            bottomBar = {
                EcoBottomBar(currentRoute = ROUTE_PROFILE, onTabSelected = onTabSelected)
            }
        ) { inner ->
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(inner),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { reload() }) { Text("Retry") }
                }
            }
        }
        return
    }

    // Derive UI fields (with sensible fallbacks)
    val firebaseUser = auth.currentUser
    val email = profile?.email?.ifBlank { firebaseUser?.email.orEmpty() } ?: firebaseUser?.email.orEmpty()
    val name = profile?.name?.ifBlank { profile?.dashboardName ?: email.substringBefore("@") }
        ?: email.substringBefore("@")
    val memberSince = firebaseUser?.metadata?.creationTimestamp
        ?.let { ts -> SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(Date(ts)) }
        ?: ""

    val householdSize = profile?.householdSize ?: 0
    val location = profile?.state ?: ""
    val electricityReminders = profile?.energyTips ?: true
    val avatarEmoji = profile?.avatar ?: "🌳"

    // Handle screen navigation
    when (currentScreen) {
        "privacy" -> {
            PrivacyScreen(
                onBack = { currentScreen = "profile" },
                onNotifications = onNotifications
            )
        }
        "help_support" -> {
            HelpSupportScreen(
                onBack = { currentScreen = "profile" },
                onNotifications = onNotifications
            )
        }
        "faq" -> {
            FAQScreen(
                onBack = { currentScreen = "profile" },
                onNotifications = onNotifications
            )
        }
        else -> {
            ProfileScreen(
                name = name,
                email = email,
                memberSince = memberSince,
                avatarEmoji = avatarEmoji,
                householdSize = householdSize,
                location = location,
                electricityReminders = electricityReminders,
                onBack = onBack,
                onNotifications = onNotifications,
                onEditProfile = onEditProfile,
                onToggleElectricity = ::updateElectricityReminders,
                onTapPrivacy = { currentScreen = "privacy" },
                onTapFaq = { currentScreen = "faq" },
                onTapContact = { currentScreen = "help_support" },
                ecoPoints = 0,
                // NAV
                currentRoute = ROUTE_PROFILE,
                onTabSelected = onTabSelected
            )
        }
    }
    // Perform sign-out here, then bubble to AppNav for navigation
    val performLogout = {
        auth.signOut()
        onLogout()
    }

    ProfileScreen(
        name = name,
        email = email,
        memberSince = memberSince,
        avatarEmoji = avatarEmoji,
        householdSize = householdSize,
        location = location,
        electricityReminders = electricityReminders,
        themeLabel = "System",
        onBack = onBack,
        onNotifications = onNotifications,
        onEditProfile = onEditProfile,
        onToggleElectricity = ::updateElectricityReminders,
        onLogout = performLogout,                   // NEW
        ecoPoints = 0,
        // NAV
        currentRoute = ROUTE_PROFILE,
        onTabSelected = onTabSelected
    )
}

/* ───────────── PRESENTATIONAL UI ───────────── */

@Composable
fun ProfileScreen(
    // Account
    name: String,
    email: String,
    memberSince: String,
    avatarEmoji: String? = null,

    // Household
    householdSize: Int,
    location: String,
    focus: String = "Electricity",

    // Settings
    electricityReminders: Boolean,

    // Actions
    onBack: () -> Unit = {},
    onNotifications: () -> Unit = {},
    onEditProfile: () -> Unit = {},
    onToggleElectricity: (Boolean) -> Unit = {},
    onTapPrivacy: () -> Unit = {},
    onTapFaq: () -> Unit = {},
    onTapContact: () -> Unit = {},
    onTapAbout: () -> Unit = {},
    onLogout: () -> Unit = {},                     // NEW

    // Stats
    ecoPoints: Int,

    // NAV
    currentRoute: String = ROUTE_PROFILE,
    onTabSelected: (route: String) -> Unit = {}
) {
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
            EcoBottomBar(
                currentRoute = currentRoute,
                onTabSelected = onTabSelected
            )
        }
    ) { inner ->
        ProfileContent(
            name = name,
            email = email,
            memberSince = memberSince,
            avatarEmoji = avatarEmoji,
            householdSize = householdSize,
            location = location,
            focus = focus,
            electricityReminders = electricityReminders,
            onEditProfile = onEditProfile,
            onToggleElectricity = onToggleElectricity,
            onTapPrivacy = onTapPrivacy,
            onTapFaq = onTapFaq,
            onTapContact = onTapContact,
            onTapAbout = onTapAbout,
            onLogout = onLogout,                    // NEW
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
    avatarEmoji: String?,
    householdSize: Int,
    location: String,
    focus: String,
    electricityReminders: Boolean,
    onEditProfile: () -> Unit,
    onToggleElectricity: (Boolean) -> Unit,
    onTapPrivacy: () -> Unit,
    onTapFaq: () -> Unit,
    onTapContact: () -> Unit,
    onTapAbout: () -> Unit,
    onLogout: () -> Unit,                          // NEW
    ecoPoints: Int,
    modifier: Modifier = Modifier
) {
    val isEmptyProfile = name.isBlank() && email.isBlank()

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header
        item {
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AvatarPlaceholder(avatarEmoji = avatarEmoji, size = 56.dp)
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

        // Household Details
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
                    InfoKeyValue("Focus", valueOrDash(focus))
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

        // 🔴 Solid red Log out button just under Edit Profile
        item {
            Button(
                onClick = onLogout,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text("Log out", fontSize = 16.sp)
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
        item { SettingChevronRow(title = "Privacy & Data Sharing", onClick = onTapPrivacy) }
        item { Divider() }

        // Help & Support
        item { SettingChevronRow("Help & Support", onClick = onTapContact) }
        item { Divider() }
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
private fun AvatarPlaceholder(avatarEmoji: String?, size: Dp = 56.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        val emoji = avatarEmoji?.takeIf { it.isNotBlank() }
        if (emoji != null) {
            Text(emoji, fontSize = 24.sp)
        }
    }
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
            avatarEmoji = "🌳",
            householdSize = 4,
            location = "VIC",
            focus = "Electricity",
            electricityReminders = electricity,
            onToggleElectricity = { electricity = it },
            onLogout = {},
            ecoPoints = 2847
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, widthDp = 411, heightDp = 1100)
@Composable
fun Preview_Profile_Empty() {
    var electricity by remember { mutableStateOf(false) }
    FIT5046Lab4Group3ass2Theme {
        ProfileScreen(
            name = "",
            email = "",
            memberSince = "",
            avatarEmoji = null,
            householdSize = 0,
            location = "",
            focus = "Electricity",
            electricityReminders = electricity,
            onToggleElectricity = { electricity = it },
            onLogout = {},
            ecoPoints = 0
        )
    }
}
