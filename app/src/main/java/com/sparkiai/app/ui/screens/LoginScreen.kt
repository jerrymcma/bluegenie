package com.sparkiai.app.ui.screens

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sparkiai.app.R
import com.sparkiai.app.utils.GoogleSignInManager
import com.google.android.gms.auth.api.signin.GoogleSignIn

private const val TAG = "LoginScreen"

@Composable
fun LoginScreen(
    onLoginSuccess: (String, String) -> Unit // userName, userEmail
) {
    val context = LocalContext.current
    val googleSignInManager = remember {
        try {
            Log.d(TAG, "Initializing GoogleSignInManager")
            GoogleSignInManager(context)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize GoogleSignInManager", e)
            null
        }
    }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Check if user is already signed in
    LaunchedEffect(Unit) {
        try {
            val account = googleSignInManager?.getLastSignedInAccount()
            if (account != null) {
                Log.d(TAG, "User already signed in: ${account.email}")
                onLoginSuccess(
                    account.displayName ?: "User",
                    account.email ?: ""
                )
            } else {
                Log.d(TAG, "No existing sign-in found")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error checking existing sign-in", e)
            errorMessage = "Google Play Services not available"
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d(TAG, "Sign-in result received: ${result.resultCode}")
        isLoading = true
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = googleSignInManager?.handleSignInResult(task)

            if (account != null) {
                Log.d(TAG, "Sign-in successful: ${account.email}")
                onLoginSuccess(
                    account.displayName ?: "User",
                    account.email ?: ""
                )
            } else {
                Log.e(TAG, "Sign-in failed: account is null")
                errorMessage = "Sign-in failed. Please try again."
            }
        } catch (e: Exception) {
            Log.e(TAG, "Sign-in error", e)
            errorMessage = "Error: ${e.message}"
        }
        isLoading = false
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Logo/Icon
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = "SparkiFire Logo",
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(24.dp))
            )

            Spacer(modifier = Modifier.height(32.dp))

            // App Title
            Text(
                text = "SparkiFire",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your AI Companion",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(64.dp))

            // Sign In Button
            Button(
                onClick = {
                    Log.d(TAG, "Sign-in button clicked")
                    isLoading = true
                    errorMessage = null
                    if (googleSignInManager != null) {
                        Log.d(TAG, "Launching sign-in intent")
                        launcher.launch(googleSignInManager.getSignInIntent())
                    } else {
                        Log.e(TAG, "GoogleSignInManager is null")
                        errorMessage = "Google Sign-In not available"
                        isLoading = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Sign in with Google",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Error Message
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "By signing in, you agree to our Terms of Service and Privacy Policy",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}
