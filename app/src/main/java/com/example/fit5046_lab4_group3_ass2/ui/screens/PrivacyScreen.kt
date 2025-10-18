@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fit5046_lab4_group3_ass2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@Composable
fun PrivacyScreen(
    onBack: () -> Unit = {},
    onNotifications: () -> Unit = {},
    // Optional: we still expose this; global auth listener will also navigate.
    onAccountDeleted: () -> Unit = {}
) {
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var dataSharingEnabled by remember { mutableStateOf(true) }
    var analyticsEnabled by remember { mutableStateOf(true) }
    var marketingEnabled by remember { mutableStateOf(false) }
    var locationTrackingEnabled by remember { mutableStateOf(true) }
    var showPrivacyPolicy by remember { mutableStateOf(false) }

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    var deleted by remember { mutableStateOf(false) }   // one-shot flag

    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }

    fun snack(msg: String) = scope.launch { snackbarHost.showSnackbar(msg) }

    fun deleteAccount() {
        val user = auth.currentUser
        val uid = user?.uid
        if (uid == null) {
            deleted = true
            return
        }

        isDeleting = true
        val userDoc = db.collection("users").document(uid)

        // 1) delete subcollection documents
        userDoc.collection("appliances").get()
            .addOnSuccessListener { qs ->
                val batch = db.batch()
                qs.documents.forEach { batch.delete(it.reference) }
                batch.commit()
                    .addOnSuccessListener {
                        // 2) delete root user doc
                        userDoc.delete()
                            .addOnSuccessListener {
                                // 3) delete auth user, then sign out
                                auth.currentUser?.delete()
                                    ?.addOnSuccessListener {
                                        auth.signOut()
                                        isDeleting = false
                                        showDeleteConfirm = false
                                        deleted = true
                                    }
                                    ?.addOnFailureListener { e ->
                                        // deleting the auth user often needs recent login — sign out anyway
                                        auth.signOut()
                                        isDeleting = false
                                        showDeleteConfirm = false
                                        snack(
                                            e.message?.takeIf { it.contains("recent", true) }
                                                ?: (e.localizedMessage ?: "Failed to delete account.")
                                        )
                                        deleted = true
                                    }
                            }
                            .addOnFailureListener { e ->
                                isDeleting = false
                                showDeleteConfirm = false
                                snack(e.localizedMessage ?: "Failed to delete profile data.")
                            }
                    }
                    .addOnFailureListener { e ->
                        isDeleting = false
                        showDeleteConfirm = false
                        snack(e.localizedMessage ?: "Failed to delete appliances.")
                    }
            }
            .addOnFailureListener { e ->
                isDeleting = false
                showDeleteConfirm = false
                snack(e.localizedMessage ?: "Failed to load appliances.")
            }
    }

    // If you still want to run a local callback (in addition to global auth listener)
    LaunchedEffect(deleted) { if (deleted) onAccountDeleted() }

    if (showPrivacyPolicy) {
        PrivacyPolicyScreen(
            onBack = { showPrivacyPolicy = false },
            onNotifications = onNotifications
        )
        return
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Privacy & Data Sharing") },
                navigationIcon = {
                    IconButton(onClick = onBack, enabled = !isDeleting) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = onNotifications, enabled = !isDeleting) {
                            Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                        }
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .size(10.dp)
                                .background(Color(0xFF8A2BE2), RoundedCornerShape(5.dp))
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { inner ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(inner)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                /* Header */
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Security, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(12.dp))
                            Text("Your Privacy Matters", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "We're committed to protecting your personal information and giving you control over your data.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                /* Data overview */
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("What Data We Collect", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(12.dp))
                            listOf(
                                "Energy usage patterns and appliance data",
                                "Household information (size, location)",
                                "App usage analytics and performance metrics",
                                "Device information and technical specifications",
                                "Contact information (email, name) for account management"
                            ).forEach {
                                Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                                    Spacer(Modifier.width(8.dp))
                                    Text(it, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }

                /* Privacy controls */
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Privacy Controls", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(16.dp))

                            SettingToggleRow(
                                title = "Data Sharing for Research",
                                subtitle = "Help improve energy efficiency research",
                                checked = dataSharingEnabled,
                                enabled = !isDeleting,
                                onCheckedChange = { dataSharingEnabled = it }
                            )
                            Spacer(Modifier.height(16.dp)); HorizontalDivider(); Spacer(Modifier.height(16.dp))

                            SettingToggleRow(
                                title = "Usage Analytics",
                                subtitle = "Help us improve app performance",
                                checked = analyticsEnabled,
                                enabled = !isDeleting,
                                onCheckedChange = { analyticsEnabled = it }
                            )
                            Spacer(Modifier.height(16.dp)); HorizontalDivider(); Spacer(Modifier.height(16.dp))

                            SettingToggleRow(
                                title = "Marketing Communications",
                                subtitle = "Receive tips and energy-saving updates",
                                checked = marketingEnabled,
                                enabled = !isDeleting,
                                onCheckedChange = { marketingEnabled = it }
                            )
                            Spacer(Modifier.height(16.dp)); HorizontalDivider(); Spacer(Modifier.height(16.dp))

                            SettingToggleRow(
                                title = "Location Services",
                                subtitle = "For regional energy pricing and tips",
                                checked = locationTrackingEnabled,
                                enabled = !isDeleting,
                                onCheckedChange = { locationTrackingEnabled = it }
                            )
                        }
                    }
                }

                /* Your rights */
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(Modifier.padding(20.dp)) {
                            Text("Your Data Rights", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(16.dp))
                            listOf(
                                "Access your personal data",
                                "Correct inaccurate information",
                                "Delete your account and data",
                                "Withdraw consent at any time"
                            ).forEach {
                                Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Box(Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                                    Spacer(Modifier.width(12.dp))
                                    Text(it, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                /* Delete button */
                item {
                    Button(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isDeleting,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Icon(Icons.Filled.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Delete My Account")
                    }
                }

                /* Policy link */
                item {
                    Card(
                        modifier = Modifier.clickable(enabled = !isDeleting) { showPrivacyPolicy = true },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Description, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Full Privacy Policy", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                Text("Read our complete privacy policy", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }

            if (isDeleting) {
                Surface(
                    modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.25f)),
                    color = Color.Transparent
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text("Deleting your account…")
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteConfirm = false },
            icon = { Icon(Icons.Filled.DeleteForever, contentDescription = null) },
            title = { Text("Delete Account") },
            text = { Text("This will permanently delete your account and all associated data. This action cannot be undone. Continue?") },
            confirmButton = {
                TextButton(
                    onClick = { if (!isDeleting) deleteAccount() },
                    enabled = !isDeleting,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { if (!isDeleting) showDeleteConfirm = false }, enabled = !isDeleting) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
    }
}
