package com.example.fit5046_lab4_group3_ass2.ui.screens

import android.content.Context.MODE_PRIVATE
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.example.fit5046_lab4_group3_ass2.R
import com.example.fit5046_lab4_group3_ass2.ui.theme.FIT5046Lab4Group3ass2Theme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import androidx.core.content.edit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScaffold(
    onLoginSuccess: () -> Unit = {},   // navigate to Home
    onGoToSignUp: () -> Unit = {}      // navigate to SignUp
) {
    val snackbarHost = remember { SnackbarHostState() }

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
        },
        snackbarHost = { SnackbarHost(snackbarHost) }
    ) { inner ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
        ) {
            LoginScreen(
                onLoginSuccess = onLoginSuccess,
                onGoToSignUp = onGoToSignUp,
                snackbarHost = snackbarHost
            )
        }
    }
}

@Composable
private fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onGoToSignUp: () -> Unit,
    snackbarHost: SnackbarHostState
) {
    val focus = LocalFocusManager.current
    val scroll = rememberScrollState()
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope() // for launching snackbar coroutines

    // simple persistence using SharedPreferences (no extra deps)
    val prefs = remember { ctx.getSharedPreferences("auth_prefs", MODE_PRIVATE) }

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }
    var rememberMe by rememberSaveable { mutableStateOf(false) }
    var loading by rememberSaveable { mutableStateOf(false) }
    var resetting by rememberSaveable { mutableStateOf(false) }

    // inline validation + server error
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var serverError by remember { mutableStateOf<String?>(null) }

    // load saved “remember me” state once
    LaunchedEffect(Unit) {
        rememberMe = prefs.getBoolean("remember_me", false)
        if (rememberMe) {
            email = prefs.getString("remember_email", "") ?: ""
        }
    }

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
            else -> null
        }
    }

    fun allValid(): Boolean {
        validateEmail(); validatePassword()
        return emailError == null && passwordError == null
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        // HERO
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
            "EcoTrack",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            "Track your eco-friendly journey",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )

        Spacer(Modifier.height(16.dp))
        SegmentedTabsLogin(onSignUpClick = onGoToSignUp)

        Spacer(Modifier.height(20.dp))

        // Email
        Text("Email", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (emailError != null) validateEmail(it)
            },
            isError = emailError != null,
            supportingText = { emailError?.let { m -> Text(m, color = MaterialTheme.colorScheme.error) } },
            singleLine = true,
            placeholder = { Text("Enter your email") },
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
            isError = passwordError != null,
            supportingText = { passwordError?.let { m -> Text(m, color = MaterialTheme.colorScheme.error) } },
            singleLine = true,
            placeholder = { Text("Enter your password") },
            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                        painter = painterResource(id = R.drawable.show_password_icon),
                        contentDescription = if (showPassword) "Hide password" else "Show password"
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // Remember me  |  Forgot Password?
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { checked ->
                        rememberMe = checked
                        // persist the toggle immediately (KTX)
                        prefs.edit { putBoolean("remember_me", checked) }
                        if (!checked) {
                            prefs.edit { remove("remember_email") }
                        }
                    }
                )
                Text("Remember me", style = MaterialTheme.typography.bodySmall)
            }

            TextButton(
                enabled = !resetting,
                onClick = {
                    // need a valid email to send reset
                    validateEmail()
                    if (emailError != null) return@TextButton
                    resetting = true
                    FirebaseAuth.getInstance()
                        .sendPasswordResetEmail(email.trim())
                        .addOnCompleteListener { t ->
                            resetting = false
                            if (t.isSuccessful) {
                                scope.launch {
                                    snackbarHost.showSnackbar("Password reset email sent.")
                                }
                            } else {
                                val msg = t.exception?.localizedMessage
                                    ?: "Couldn't send reset email."
                                scope.launch {
                                    snackbarHost.showSnackbar(msg)
                                }
                            }
                        }
                }
            ) {
                Text("Forgot Password?")
            }
        }

        // backend error (e.g., wrong-password, user-not-found)
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
                // Do sign-in via your AuthRepo or directly with Firebase
                AuthRepo.signIn(email.trim(), password) { res ->
                    loading = false
                    res.onSuccess {
                        // persist email if rememberMe
                        if (rememberMe) {
                            prefs.edit { putString("remember_email", email.trim()) }
                        }
                        onLoginSuccess()
                    }.onFailure { e ->
                        serverError = e.message ?: "Login failed."
                    }
                }
            },
            enabled = !loading,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (loading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text("Login", fontWeight = FontWeight.Medium)
            }
        }

        Spacer(Modifier.height(20.dp))

        // footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("New to EcoTrack? ")
            Text(
                text = "Create an account",
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { onGoToSignUp() }
            )
        }
    }
}

/* ----------------------------- COMPONENTS ---------------------------------- */

@Composable
private fun SegmentedTabsLogin(
    onSignUpClick: () -> Unit
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
            Surface(
                color = selected,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Login", fontWeight = FontWeight.Medium)
                }
            }
            Spacer(Modifier.width(6.dp))
            Surface(
                color = container,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clickable { onSignUpClick() }
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Sign Up",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginPreview() {
    FIT5046Lab4Group3ass2Theme { LoginScaffold() }
}
