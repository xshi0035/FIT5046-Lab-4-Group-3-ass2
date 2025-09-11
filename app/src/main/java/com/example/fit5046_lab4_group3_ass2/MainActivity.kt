package com.example.fit5046_lab4_group3_ass2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme

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
                title = { Text("EcoTrack") },
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
                        selected = index == 0, // Appliances selected (UI-only)
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
            Home()
        }
    }
}

/* --------------------------------- SCREEN ---------------------------------- */

private data class NavItem(val label: String, val icon: ImageVector)

@Composable
fun HomeScreenCard(
    modifier: Modifier = Modifier,
    title: String,
    mainText: String,
    smallText: String,
    progress: Float = 0f,
    rightText: String
) {
    Card(modifier = Modifier.padding(bottom = 10.dp)) {
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (title != "")
                BoldText(text = title)
            if (rightText != "")
                SmallText(text = rightText)
        }
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
            .padding(horizontal = 10.dp),
        style = MaterialTheme.typography.labelLarge
    )
}

@Composable
fun SmallText(modifier: Modifier = Modifier, text: String) {
    Text(
        text = text,
        modifier = Modifier
            .padding(horizontal = 10.dp),
        style = MaterialTheme.typography.bodyMedium
    )
}

//Home screen
@Composable
fun Home(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        contentPadding = PaddingValues(bottom = 36.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
            ) {
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

                HomeScreenCard(
                    title = "EcoPoints",
                    mainText = "2,450",
                    smallText = "🔥 7-day streak",
                    rightText = "\uD83C\uDFC6"
                )
                HomeScreenCard(
                    title = "⚡ Electricity",
                    mainText = "8.4 kWh",
                    smallText = "Today's usage",
                    progress = 0.8f,
                    rightText = "-12%"
                )
                HomeScreenCard(
                    title = "♻\uFE0F Plastic Saved",
                    mainText = "2.1 kg",
                    smallText = "This week = 7 bottles",
                    progress = 0.5f,
                    rightText = "+2.1kg"
                )

                BoldText(text = "Quick Actions")
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = {}, modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 5.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Add, contentDescription = "Add")
                            Text(
                                text = "Add Appliance"
                            )
                        }
                    }
                    Button(
                        onClick = {}, modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 5.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Filled.ShoppingCart,
                                contentDescription = "Log purchase"
                            )
                            Text(
                                text = "Log Purchase"
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Button(
                        onClick = {}, modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 5.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Info, contentDescription = "View Tips")
                            Text(
                                text = "View Tips"
                            )
                        }
                    }
                    Button(
                        onClick = {}, modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 5.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.Star, contentDescription = "View stats")
                            Text(
                                text = "View Stats"
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                BoldText(text = "Recent Activity")
                HomeScreenCard(
                    title = "♻\uFE0F Plastic bottle avoided",
                    mainText = "",
                    smallText = "2 hours ago",
                    rightText = "+50 pts"
                )
                HomeScreenCard(
                    title = "⚡ TV usage logged",
                    mainText = "",
                    smallText = "5 hours ago",
                    rightText = "+25 pts"
                )
                HomeScreenCard(
                    title = "\uD83C\uDFC6 Achievement unlocked!",
                    mainText = "",
                    smallText = "Yesterday",
                    rightText = "+100 pts"
                )
                Spacer(modifier = Modifier.height(10.dp))
                BoldText(text = "Information")
                HomeScreenCard(
                    title = "\uD83D\uDCA1 Today's Eco Tip",
                    mainText = "",
                    smallText = "Unplug devices when not in use",
                    rightText = ""
                )

            }
        }
    }

}
