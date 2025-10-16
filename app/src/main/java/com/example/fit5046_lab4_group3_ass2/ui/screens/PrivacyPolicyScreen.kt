@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fit5046_lab4_group3_ass2.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.sp

@Composable
fun PrivacyPolicyScreen(
    onBack: () -> Unit = {},
    onNotifications: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Privacy Policy") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
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
                            Icons.Filled.Description,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "Privacy Policy",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Last updated: January 2025",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Introduction
            item {
                PolicySection(
                    title = "Introduction",
                    content = "EcoTrack is committed to protecting your privacy and personal information. This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you use our mobile application and related services."
                )
            }

            // Information We Collect
            item {
                PolicySection(
                    title = "Information We Collect",
                    content = "We collect information you provide directly to us, information we obtain automatically when you use our services, and information from third parties."
                )
            }

            item {
                PolicySubSection(
                    title = "Personal Information",
                    items = listOf(
                        "Name and email address for account creation",
                        "Household size and location for energy calculations",
                        "Profile information and preferences",
                        "Communication preferences and settings"
                    )
                )
            }

            item {
                PolicySubSection(
                    title = "Usage Information",
                    items = listOf(
                        "Energy consumption data from connected appliances",
                        "App usage patterns and feature interactions",
                        "Device information and technical specifications",
                        "Performance metrics and crash reports"
                    )
                )
            }

            item {
                PolicySubSection(
                    title = "Automatically Collected Information",
                    items = listOf(
                        "Device identifiers and IP addresses",
                        "Operating system and app version information",
                        "Log files and analytics data",
                        "Location data (with your permission)"
                    )
                )
            }

            // How We Use Information
            item {
                PolicySection(
                    title = "How We Use Your Information",
                    content = "We use the information we collect to provide, maintain, and improve our services, communicate with you, and ensure the security of our platform."
                )
            }

            item {
                PolicySubSection(
                    title = "Service Provision",
                    items = listOf(
                        "Provide energy tracking and analytics features",
                        "Generate personalized energy-saving recommendations",
                        "Sync data across your devices",
                        "Process and display your energy consumption data"
                    )
                )
            }

            item {
                PolicySubSection(
                    title = "Communication",
                    items = listOf(
                        "Send important service updates and notifications",
                        "Provide customer support and respond to inquiries",
                        "Send energy-saving tips and educational content (with consent)",
                        "Notify you about new features and improvements"
                    )
                )
            }

            item {
                PolicySubSection(
                    title = "Research and Development",
                    items = listOf(
                        "Improve app performance and user experience",
                        "Develop new features and functionality",
                        "Conduct energy efficiency research (anonymized data)",
                        "Analyze usage patterns to optimize services"
                    )
                )
            }

            // Information Sharing
            item {
                PolicySection(
                    title = "Information Sharing and Disclosure",
                    content = "We do not sell, trade, or otherwise transfer your personal information to third parties without your consent, except as described in this policy."
                )
            }

            item {
                PolicySubSection(
                    title = "When We Share Information",
                    items = listOf(
                        "With your explicit consent",
                        "To comply with legal obligations or court orders",
                        "To protect our rights, property, or safety",
                        "In connection with a business transfer or merger"
                    )
                )
            }

            item {
                PolicySubSection(
                    title = "Service Providers",
                    items = listOf(
                        "Cloud storage and data processing services",
                        "Analytics and crash reporting services",
                        "Customer support and communication platforms",
                        "Third-party integrations (with your permission)"
                    )
                )
            }

            // Data Security
            item {
                PolicySection(
                    title = "Data Security",
                    content = "We implement appropriate technical and organizational measures to protect your personal information against unauthorized access, alteration, disclosure, or destruction."
                )
            }

            item {
                PolicySubSection(
                    title = "Security Measures",
                    items = listOf(
                        "End-to-end encryption for data transmission",
                        "Secure cloud storage with industry-standard encryption",
                        "Regular security audits and vulnerability assessments",
                        "Access controls and authentication protocols"
                    )
                )
            }

            // Your Rights
            item {
                PolicySection(
                    title = "Your Rights and Choices",
                    content = "You have certain rights regarding your personal information. You can exercise these rights through the app settings or by contacting us directly."
                )
            }

            item {
                PolicySubSection(
                    title = "Your Rights Include",
                    items = listOf(
                        "Access and review your personal data",
                        "Correct inaccurate or incomplete information",
                        "Delete your account and associated data",
                        "Export your data in a portable format",
                        "Withdraw consent for data processing",
                        "Opt out of marketing communications"
                    )
                )
            }

            // Data Retention
            item {
                PolicySection(
                    title = "Data Retention",
                    content = "We retain your personal information for as long as necessary to provide our services and fulfill the purposes outlined in this policy, unless a longer retention period is required by law."
                )
            }

            item {
                PolicySubSection(
                    title = "Retention Periods",
                    items = listOf(
                        "Account data: Until account deletion",
                        "Energy usage data: 7 years for analytics",
                        "Communication records: 3 years",
                        "Analytics data: 2 years (anonymized)"
                    )
                )
            }

            // International Transfers
            item {
                PolicySection(
                    title = "International Data Transfers",
                    content = "Your information may be transferred to and processed in countries other than your own. We ensure appropriate safeguards are in place for such transfers."
                )
            }

            // Children's Privacy
            item {
                PolicySection(
                    title = "Children's Privacy",
                    content = "Our services are not intended for children under 13. We do not knowingly collect personal information from children under 13. If we become aware of such collection, we will take steps to delete the information."
                )
            }

            // Changes to Policy
            item {
                PolicySection(
                    title = "Changes to This Privacy Policy",
                    content = "We may update this Privacy Policy from time to time. We will notify you of any material changes by posting the new policy in the app and updating the 'Last updated' date."
                )
            }

            // Contact Information
            item {
                PolicySection(
                    title = "Contact Us",
                    content = "If you have any questions about this Privacy Policy or our data practices, please contact us:"
                )
            }

            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ContactInfoItem(
                            icon = Icons.Filled.Email,
                            title = "Email",
                            value = "ecotrack@gmail.com"
                        )
                        Spacer(Modifier.height(12.dp))
                        ContactInfoItem(
                            icon = Icons.Filled.Phone,
                            title = "Phone",
                            value = "0433945383"
                        )
                        Spacer(Modifier.height(12.dp))
                        ContactInfoItem(
                            icon = Icons.Filled.LocationOn,
                            title = "Address",
                            value = "Monash University Clayton, VIC, 3168"
                        )
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun PolicySection(
    title: String,
    content: String
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun PolicySubSection(
    title: String,
    items: List<String>
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            
            items.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Stop,
                        contentDescription = null,
                        modifier = Modifier.size(8.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ContactInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
