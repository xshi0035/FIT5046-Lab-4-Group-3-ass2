package com.example.fit5046_lab4_group3_ass2.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fit5046_lab4_group3_ass2.R
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme

/* ------------------------------- DATA MODELS ------------------------------- */

data class HomeFeature(
    val emoji: String,
    val title: String,
    val subtitle: String
)

/** Everything the screen needs comes from this simple UI model. */
data class HomeUi(
    val title: String = "Welcome to EcoTrack",
    val subtitle: String = "Track your environmental impact and earn rewards",
    val features: List<HomeFeature> = listOf(
        HomeFeature("⚡️", "Today's Electricity Price", "Track today’s price in your state (AUD/MWh)"),
        HomeFeature( emoji = "📈", "Usage & Cost", "Monitor your appliances and see your estimated cost"),
        HomeFeature("⭐", "Earn EcoPoints", "Get rewarded for saving energy during high-price hours")
    )
)

/* ------------------------------- SCREEN ------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScaffold(
    ui: HomeUi = HomeUi(),
    onNotificationsClick: () -> Unit = {},
    onGetStartedClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("EcoTrack") },
                navigationIcon = {
                    // Small circular logo — filled with your drawable
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ecotrack_logo_image),
                            contentDescription = "EcoTrack logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onNotificationsClick) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                    }
                }
            )
        }
        // no bottomBar on starting page
    ) { inner ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            HomeScreen(ui = ui, onGetStartedClick = onGetStartedClick)
        }
    }
}

@Composable
private fun HomeScreen(
    ui: HomeUi,
    onGetStartedClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item { Spacer(Modifier.height(16.dp)) }

        // HERO: big circular logo centered and covering its frame
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ecotrack_logo_image),
                        contentDescription = "EcoTrack Logo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // Welcome copy
        item {
            Text(
                ui.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
            Text(
                ui.subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(16.dp))
        }

        // Feature cards
        items(ui.features) { f ->
            FeatureCard(f)
            Spacer(Modifier.height(12.dp))
        }

        // Get Started button
        item {
            Button(
                onClick = onGetStartedClick,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(52.dp)
            ) {
                Text("Get Started", fontWeight = FontWeight.Medium)
            }
        }
    }
}

/* ------------------------------- COMPONENTS ------------------------------- */

@Composable
private fun FeatureCard(f: HomeFeature) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // icon tile
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(f.emoji, fontSize = 22.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        f.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        f.subtitle,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

/* -------------------------------- PREVIEWS -------------------------------- */

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScaffold_DefaultPreview() {
    FIT5046Lab4Group3ass2Theme {
        HomeScaffold()
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HomeScaffold_CustomPreview() {
    val custom = HomeUi(
        title = "Welcome back, Alex 👋",
        subtitle = "Pick a goal to start saving today",
        features = listOf(
            HomeFeature("💡", "Smart Schedules", "Auto-reduce usage at peak times"),
            HomeFeature("📊", "Usage Insights", "See what’s driving your bill"),
            HomeFeature("🎁", "Earn Rewards", "Redeem EcoPoints for perks")
        )
    )
    FIT5046Lab4Group3ass2Theme {
        HomeScaffold(ui = custom)
    }
}
