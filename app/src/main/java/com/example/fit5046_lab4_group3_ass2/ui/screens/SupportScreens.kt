@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fit5046_lab4_group3_ass2.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import android.os.Build
import android.content.pm.PackageManager


const val ROUTE_CONTACT_SUPPORT = "contact_support"
const val ROUTE_ABOUT_APP = "about_app"
const val ROUTE_SDG_LEARN = "sdg_learn"

/* ========= ContactSupportScreen ========= */
data class SupportContact(
    val name: String,
    val role: String,
    val phone: String,
    val email: String,
    val hours: String
)

@Composable
fun ContactSupportScreen(
    onBack: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val contacts = remember {
        listOf(
            SupportContact(
                name = "Alex Chen",
                role = "Technical Support",
                phone = "+61 3 8000 1234",
                email = "alex.chen@example.com",
                hours = "Mon–Fri 9:00–18:00 (AEST)"
            ),
            SupportContact(
                name = "Priya Sharma",
                role = "Account & Billing",
                phone = "+61 3 8000 5678",
                email = "priya.sharma@example.com",
                hours = "Mon–Fri 10:00–17:00 (AEST)"
            ),
            SupportContact(
                name = "Support Desk",
                role = "General Enquiries",
                phone = "+61 3 8000 0000",
                email = "support@example.com",
                hours = "7 days 9:00–21:00"
            )
        )
    }

    fun dial(number: String) {
        val i = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${number.replace(" ", "")}"))
        try { ctx.startActivity(i) } catch (_: ActivityNotFoundException) {}
    }

    fun email(to: String, subject: String = "App Support") {
        val i = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$to")
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        try { ctx.startActivity(i) } catch (_: ActivityNotFoundException) {}
    }

    fun copy(text: String) {
        clipboard.setText(AnnotatedString(text))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Contact Support") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    "How can we help?",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    "Choose a contact below or use the quick actions.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            items(contacts) { c ->
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // 标题
                        Column {
                            Text(c.name, style = MaterialTheme.typography.titleMedium)
                            Text("${c.role} • ${c.hours}", style = MaterialTheme.typography.bodySmall)
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Phone: ${c.phone}", modifier = Modifier.weight(1f))
                            IconButton(onClick = { dial(c.phone) }) {
                                Icon(Icons.Filled.Phone, contentDescription = "Call")
                            }
                            IconButton(onClick = { copy(c.phone) }) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy phone")
                            }
                        }

                        // 邮件行：发邮件 / 复制
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Email: ${c.email}", modifier = Modifier.weight(1f))
                            IconButton(onClick = { email(c.email) }) {
                                Icon(Icons.Filled.Email, contentDescription = "Email")
                            }
                            IconButton(onClick = { copy(c.email) }) {
                                Icon(Icons.Filled.ContentCopy, contentDescription = "Copy email")
                            }
                        }

                        // 快捷按钮（可选）
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { email(c.email, subject = "Support – ${c.role}") },
                                modifier = Modifier.weight(1f)
                            ) { Text("Email ${c.name.split(" ").first()}") }

                            OutlinedButton(
                                onClick = { dial(c.phone) },
                                modifier = Modifier.weight(1f)
                            ) { Text("Call") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AboutAppScreen(
    onBack: () -> Unit = {}
) {
    val ctx = LocalContext.current

    val pkgName = ctx.packageName
    val pm = ctx.packageManager
    val (versionName, versionCode) = try {
        if (Build.VERSION.SDK_INT >= 33) {
            val pi = pm.getPackageInfo(pkgName, PackageManager.PackageInfoFlags.of(0))
            val code = pi.longVersionCode
            val name = pi.versionName ?: "1.0"
            name to code
        } else {
            @Suppress("DEPRECATION")
            val pi = pm.getPackageInfo(pkgName, 0)
            @Suppress("DEPRECATION")
            val code = pi.versionCode.toLong()
            val name = pi.versionName ?: "1.0"
            name to code
        }
    } catch (e: Exception) {
        "1.0" to 1L
    }

    val today = LocalDate.now()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("About App") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("EcoTracker", style = MaterialTheme.typography.titleLarge)
            Text("Package: $pkgName")
            Text("Version: $versionName ($versionCode)")
            Text("Build date: $today")

            Spacer(Modifier.height(12.dp))
            Text(
                "This app helps households track energy usage and habits. " +
                        "It is a student project for FIT5046 (Assessment 4).",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/* ========= LearnSdgScreen ========= */
@Composable
fun LearnSdgScreen(
    onBack: () -> Unit = {}
) {
    val ctx = LocalContext.current
    val sdgUrl = "https://www.un.org/sustainabledevelopment/sustainable-development-goals/"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Learn about UN SDG Goals") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                "United Nations Sustainable Development Goals",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                "Explore the 17 global goals and see how our app aligns with SDG 7 (Affordable and Clean Energy)."
            )

            Button(
                onClick = {
                    val i = Intent(Intent.ACTION_VIEW, Uri.parse(sdgUrl))
                    try { ctx.startActivity(i) } catch (_: ActivityNotFoundException) {}
                }
            ) {
                Text("Open UN SDG Website")
            }
        }
    }
}
