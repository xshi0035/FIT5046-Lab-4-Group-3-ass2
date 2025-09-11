package com.example.fit5046_lab4_group3_ass2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FIT5046Lab4Group3ass2Theme {
                SignUpScaffold()
            }
        }
    }
}

/* ------------------------------- SCAFFOLD ---------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScaffold() {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("EcoTrack") },
                navigationIcon = {
                    // Small circular logo (same as other pages)
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
                    IconButton(onClick = { /* no-op (UI only) */ }) {
                        Icon(Icons.Filled.Notifications, contentDescription = "Notifications")
                    }
                }
            )
        }
    ) { inner ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            SignUpScreen()
        }
    }
}

/* --------------------------------- SCREEN ---------------------------------- */

@Composable
fun SignUpScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // HERO logo
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ecotrack_logo_image),
                    contentDescription = "EcoTrack logo",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            "Create Account",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Enter your details to get started",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )

        Spacer(Modifier.height(16.dp))

        // Segmented tabs – "Sign Up" selected (UI-only)
        SegmentedTabsSignUp()

        Spacer(Modifier.height(20.dp))

        // Email
        Text("Email Address", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            readOnly = true, // UI-only
            singleLine = true,
            placeholder = { Text("example@domain.com") },
            trailingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        // Password
        Text("Password", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            readOnly = true,              // UI-only
            singleLine = true,
            placeholder = { Text("Create a password") },
            trailingIcon = {
                // Use your drawable as the eye icon
                Icon(
                    painter = painterResource(id = R.drawable.show_password_icon),
                    contentDescription = "Show password",
                    modifier = Modifier.size(22.dp),
                    tint = Color.Unspecified    // keep the original PNG colors
                )
                // If you prefer a tappable feel (still no logic), wrap it:
                // IconButton(onClick = { /* no-op */ }) { Icon( ...same as above... ) }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )


        // Strength bar + label (static UI)
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(
                progress = { 0.25f }, // static "Weak" look
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text("Weak", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }

        // Password tips (UI-only)
        Spacer(Modifier.height(8.dp))
        PasswordTip("At least 8 characters")
        PasswordTip("Uppercase and lowercase letters")
        PasswordTip("At least one number")
        PasswordTip("At least one special symbol")

        Spacer(Modifier.height(16.dp))

        // ToS checkbox line (UI-only)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = false, onCheckedChange = null, enabled = false)
            Spacer(Modifier.width(6.dp))
            Text(
                text = "I agree to the Terms of Service and Privacy Policy",
                style = MaterialTheme.typography.bodySmall, // small, per your preference
            )
        }

        Spacer(Modifier.height(12.dp))

        // Create Account button
        Button(
            onClick = { /* no-op */ },
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("Create Account", fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(16.dp))

        // Link back to login
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already have an account? ")
            Text(
                text = "Sign in",
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        }
    }
}

/* ----------------------------- COMPONENTS ---------------------------------- */

@Composable
private fun SegmentedTabsSignUp() {
    val container = MaterialTheme.colorScheme.surfaceVariant
    val selected = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(container, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // Login (unselected)
            Surface(
                color = container,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        "Login",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(Modifier.width(6.dp))
            // Sign Up (selected)
            Surface(
                color = selected,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("Sign Up", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun PasswordTip(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("• ", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
    }
}

/* -------------------------------- PREVIEW ---------------------------------- */

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SignUpPreview() {
    FIT5046Lab4Group3ass2Theme {
        SignUpScaffold()
    }
}
