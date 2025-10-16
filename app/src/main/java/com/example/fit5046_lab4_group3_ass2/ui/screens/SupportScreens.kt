@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.fit5046_lab4_group3_ass2.ui.screens

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.time.LocalDate

/* ========= Routes (供 MainActivity 注册，非底部 Tab) ========= */
const val ROUTE_CONTACT_SUPPORT = "contact_support"
const val ROUTE_ABOUT_APP = "about_app"
const val ROUTE_SDG_LEARN = "sdg_learn"

/* ========= ContactSupportScreen ========= */
@Composable
fun ContactSupportScreen(
    onBack: () -> Unit = {}
) {
    val ctx = LocalContext.current

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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "How can we help?",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )

            Button(
                onClick = {
                    // Email support
                    val email = "support@example.com"
                    val subject = "App Support"
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:$email")
                        putExtra(Intent.EXTRA_SUBJECT, subject)
                    }
                    try { ctx.startActivity(intent) } catch (_: ActivityNotFoundException) {}
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Email us") }

            OutlinedButton(
                onClick = {
                    // Open FAQ (替换为你们自己的链接)
                    val url = "https://example.com/faq"
                    val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    try { ctx.startActivity(i) } catch (_: ActivityNotFoundException) {}
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Open FAQs") }

            OutlinedButton(
                onClick = {
                    // 可选：GitHub issues
                    val url = "https://github.com/xshi0035/FIT5046-Lab-4-Group-3-ass2/issues"
                    val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    try { ctx.startActivity(i) } catch (_: ActivityNotFoundException) {}
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Report an Issue") }
        }
    }
}

/* ========= AboutAppScreen（不依赖 BuildConfig） ========= */
@Composable
fun AboutAppScreen(
    onBack: () -> Unit = {}
) {
    val ctx = LocalContext.current

    // 读取版本信息（兼容 API 33+ 与旧版）
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
