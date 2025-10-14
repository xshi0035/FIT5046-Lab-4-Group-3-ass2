package com.example.fit5046_lab4_group3_ass2.ui.screens

import android.util.Patterns
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.fit5046_lab4_group3_ass2.R
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme

/* ------------------------------- SCAFFOLD ---------------------------------- */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScaffold(
    onGoToLogin: () -> Unit = {}     // <- any “Login / Sign in” action routes here
) {
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
                    IconButton(onClick = { /* optional */ }) {
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
            SignUpScreen(onGoToLogin = onGoToLogin)
        }
    }
}

/* --------------------------------- SCREEN ---------------------------------- */

@Composable
private fun SignUpScreen(
    onGoToLogin: () -> Unit
) {
    // state
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var tosChecked by rememberSaveable { mutableStateOf(false) }
    var loading by rememberSaveable { mutableStateOf(false) }

    // inline validation state
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var tosError by remember { mutableStateOf<String?>(null) }
    var serverError by remember { mutableStateOf<String?>(null) }

    // success dialog
    var showSuccess by remember { mutableStateOf(false) }

    val focus = LocalFocusManager.current
    val scroll = rememberScrollState()

    fun validateEmail(now: String = email) {
        emailError = when {
            now.isEmpty() -> "Email is required."
            !Patterns.EMAIL_ADDRESS.matcher(now).matches() -> "Please enter a valid email."
            else -> null
        }
    }

    fun validatePassword(now: String = password) {
        passwordError = when {
            now.isEmpty() -> "Password is required."
            now.length < 8 -> "Password must be at least 8 characters."
            else -> null
        }
    }

    fun validateTos() {
        tosError = if (!tosChecked) "Please agree to the Terms and Privacy Policy." else null
    }

    fun allValid(): Boolean {
        validateEmail()
        validatePassword()
        validateTos()
        return emailError == null && passwordError == null && tosError == null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
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
        SegmentedTabsSignUp(onLoginClick = onGoToLogin)

        Spacer(Modifier.height(20.dp))

        // Email
        Text("Email Address", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                // live validation as user types (if error already shown)
                if (emailError != null) validateEmail(it)
            },
            singleLine = true,
            isError = emailError != null,
            supportingText = { emailError?.let { msg -> Text(msg, color = MaterialTheme.colorScheme.error) } },
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
            value = password,
            onValueChange = {
                password = it
                if (passwordError != null) validatePassword(it)
            },
            singleLine = true,
            isError = passwordError != null,
            supportingText = { passwordError?.let { msg -> Text(msg, color = MaterialTheme.colorScheme.error) } },
            placeholder = { Text("Create a password") },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    val iconRes =
                        if (showPassword) R.drawable.show_password_icon
                        else R.drawable.hide_password_icon
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = if (showPassword) "Hide password" else "Show password",
                        tint = Color.Unspecified,
                        modifier = Modifier.size(28.dp)
                    )
                }
            },
            shape = RoundedCornerShape(11.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // Strength bar
        Spacer(Modifier.height(8.dp))
        val strength = (password.length.coerceAtMost(12)) / 12f
        Row(verticalAlignment = Alignment.CenterVertically) {
            LinearProgressIndicator(progress = { strength }, modifier = Modifier.weight(1f).height(6.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                when {
                    password.length >= 10 -> "Strong"
                    password.length >= 8 -> "Medium"
                    else -> "Weak"
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
            )
        }

        Spacer(Modifier.height(10.dp))
        PasswordTip("At least 8 characters")
        PasswordTip("Uppercase and lowercase letters")
        PasswordTip("At least one number")
        PasswordTip("At least one special symbol")

        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = tosChecked,
                onCheckedChange = {
                    tosChecked = it
                    if (tosError != null) validateTos()
                }
            )
            Spacer(Modifier.width(6.dp))
            Text("I agree to the Terms of Service and Privacy Policy", style = MaterialTheme.typography.bodySmall)
        }
        tosError?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        // server error (e.g., email already in use)
        serverError?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                focus.clearFocus()
                serverError = null
                if (!allValid()) return@Button
                loading = true
                // AuthRepo is in this same package. If not, import it.
                AuthRepo.signUp(email.trim(), password) { res ->
                    loading = false
                    res.onSuccess {
                        showSuccess = true         // show success dialog
                    }.onFailure { e ->
                        serverError = e.message ?: "Sign up failed."
                    }
                }
            },
            enabled = !loading,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (loading) CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            else Text("Create Account", fontWeight = FontWeight.Medium)
        }

        Spacer(Modifier.height(24.dp))

        // “Already have an account? Sign in”
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Already have an account? ")
            Text(
                text = "Sign in",
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onGoToLogin() }
            )
        }

        Spacer(Modifier.height(24.dp)) // breathing room at bottom
    }

    // Success dialog => always go to Login (also when dismissed)
    if (showSuccess) {
        AlertDialog(
            onDismissRequest = {
                showSuccess = false
                onGoToLogin()
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSuccess = false
                        onGoToLogin()
                    }
                ) { Text("Go to Login") }
            },
            title = { Text("Account created") },
            text = { Text("Your EcoTrack account was created successfully.") },
            properties = DialogProperties(dismissOnClickOutside = true)
        )
    }
}

/* ----------------------------- COMPONENTS ---------------------------------- */

@Composable
private fun SegmentedTabsSignUp(
    onLoginClick: () -> Unit
) {
    val container = MaterialTheme.colorScheme.surfaceVariant
    val selected = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(container, RoundedCornerShape(12.dp))
            .padding(4.dp)
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            // “Login” tab → navigates to Login
            Surface(
                color = container,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clickable { onLoginClick() }
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("Login", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.width(6.dp))
            // Selected “Sign Up”
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
    FIT5046Lab4Group3ass2Theme { SignUpScaffold() }
}
