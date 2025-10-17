@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fit5046_lab4_group3_ass2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.unit.sp

@Composable
fun FAQScreen(
    onBack: () -> Unit = {},
    onNotifications: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var expandedFAQ by remember { mutableStateOf<Int?>(null) }

    val faqCategories = listOf("All", "Getting Started", "Account", "Technical", "Privacy", "Billing")

    val allFAQs = listOf(
        FAQItem(
            id = 1,
            category = "Getting Started",
            question = "How do I set up my first appliance?",
            answer = "To add your first appliance, go to the Appliances tab and tap the '+' button. Select your appliance type, enter the model information, and follow the setup wizard. Make sure your device is connected to the same Wi-Fi network as your smart appliances."
        ),
        FAQItem(
            id = 2,
            category = "Getting Started",
            question = "What appliances are supported?",
            answer = "We support a wide range of smart appliances including refrigerators, washing machines, dishwashers, air conditioners, water heaters, and smart plugs. Check our compatibility list in the app settings for the most up-to-date list of supported devices."
        ),
        FAQItem(
            id = 3,
            category = "Account",
            question = "How do I reset my password?",
            answer = "To reset your password, go to the Profile tab, tap 'Account Settings', then 'Change Password'. Enter your current password and create a new one. Make sure your new password is at least 8 characters long and includes both letters and numbers."
        ),
        FAQItem(
            id = 4,
            category = "Account",
            question = "Can I use the app without creating an account?",
            answer = "No, an account is required to use EcoTrack. This allows us to sync your data across devices, provide personalized energy insights, and ensure your data is securely backed up. Creating an account is free and takes less than 2 minutes."
        ),
        FAQItem(
            id = 5,
            category = "Technical",
            question = "Why isn't my appliance connecting?",
            answer = "First, ensure your appliance is in pairing mode and your phone is connected to the same Wi-Fi network. Check that your router supports 2.4GHz (required for most smart devices). If issues persist, try restarting both your router and the appliance, then attempt pairing again."
        ),
        FAQItem(
            id = 6,
            category = "Technical",
            question = "The app keeps crashing. What should I do?",
            answer = "Try these steps: 1) Force close the app and reopen it, 2) Restart your device, 3) Check for app updates in your app store, 4) Clear the app's cache in your device settings, 5) If problems continue, contact our support team with your device model and app version."
        ),
        FAQItem(
            id = 7,
            category = "Technical",
            question = "How accurate is the energy usage data?",
            answer = "Our energy calculations are based on manufacturer specifications and real-time usage patterns. Accuracy depends on your appliance's smart capabilities and our algorithms. For the most accurate readings, ensure your appliances are properly connected and regularly updated."
        ),
        FAQItem(
            id = 8,
            category = "Privacy",
            question = "Is my data secure?",
            answer = "Yes, we use industry-standard encryption to protect your data. All data transmission is encrypted with SSL/TLS, and we never share your personal information with third parties without your explicit consent. You can control your data sharing preferences in the Privacy settings."
        ),
        FAQItem(
            id = 9,
            category = "Privacy",
            question = "Can I delete my account and data?",
            answer = "Yes, you can delete your account and all associated data at any time. Go to Profile > Privacy & Data Sharing > Delete My Account. This action is irreversible and will remove all your energy data, settings, and account information."
        ),
        FAQItem(
            id = 10,
            category = "Billing",
            question = "Is EcoTrack free to use?",
            answer = "Yes, EcoTrack is completely free to use. We offer premium features like advanced analytics and priority support, but all core functionality including energy tracking, tips, and basic insights are available at no cost."
        ),
        FAQItem(
            id = 11,
            category = "Billing",
            question = "Do you offer premium features?",
            answer = "Yes, our EcoTrack Pro subscription includes advanced energy analytics, detailed reports, priority customer support, and exclusive energy-saving tips. Pro features are optional and clearly marked throughout the app."
        ),
        FAQItem(
            id = 12,
            category = "Getting Started",
            question = "How do I earn EcoPoints?",
            answer = "EcoPoints are earned by using energy-efficient appliances, following our energy-saving tips, and maintaining good energy habits. You can earn points for reducing peak-hour usage, completing energy challenges, and sharing your progress with friends."
        )
    )

    val filteredFAQs = allFAQs.filter { faq ->
        val matchesCategory = selectedCategory == "All" || faq.category == selectedCategory
        val matchesSearch = searchQuery.isEmpty() || 
            faq.question.contains(searchQuery, ignoreCase = true) ||
            faq.answer.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("FAQs") },
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
                                .background(Color(0xFF8A2BE2), RoundedCornerShape(5.dp))
                        )
                    }
                }
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.Quiz,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Frequently Asked Questions",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Find quick answers to common questions about EcoTrack.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { 
                        Text(
                            "Search FAQs...",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Filled.Search, 
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    Icons.Filled.Clear, 
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        cursorColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            // Category Filter - Horizontal Scrolling
            item {
                Column {
                    Text(
                        "Filter by Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        items(faqCategories) { category ->
                            CategoryChip(
                                category = category,
                                isSelected = selectedCategory == category,
                                onClick = { selectedCategory = category }
                            )
                        }
                    }
                }
            }

            // Results Count
            item {
                if (searchQuery.isNotEmpty() || selectedCategory != "All") {
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Text(
                            "${filteredFAQs.size} result${if (filteredFAQs.size != 1) "s" else ""} found",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            // FAQ Items
            items(filteredFAQs) { faq ->
                FAQCard(
                    faq = faq,
                    isExpanded = expandedFAQ == faq.id,
                    onToggleExpanded = {
                        expandedFAQ = if (expandedFAQ == faq.id) null else faq.id
                    }
                )
            }

            // No Results
            item {
                if (filteredFAQs.isEmpty()) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "No FAQs found",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Try adjusting your search or filter",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Contact Support
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Help,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Still need help?",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Contact our support team",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Filled.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

// Data class for FAQ items
data class FAQItem(
    val id: Int,
    val category: String,
    val question: String,
    val answer: String
)

// Composable components
@Composable
private fun FAQCard(
    faq: FAQItem,
    isExpanded: Boolean,
    onToggleExpanded: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpanded() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isExpanded) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isExpanded) 4.dp else 1.dp
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Category badge
                    Card(
                        modifier = Modifier.padding(bottom = 8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            faq.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        faq.question,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
                Icon(
                    if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            
            if (isExpanded) {
                Spacer(Modifier.height(16.dp))
                Divider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    faq.answer,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        enabled = true,
        label = { 
            Text(
                category,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
            ) 
        },
        selected = isSelected,
        leadingIcon = if (isSelected) {
            { 
                Icon(
                    Icons.Filled.Check, 
                    contentDescription = null, 
                    modifier = Modifier.size(16.dp)
                ) 
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            selectedBorderColor = MaterialTheme.colorScheme.primary,
            selectedBorderWidth = 1.dp
        )
    )
}
