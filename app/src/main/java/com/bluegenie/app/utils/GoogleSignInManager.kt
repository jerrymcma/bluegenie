package com.bluegenie.app.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.bluegenie.app.BuildConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

private const val TAG = "GoogleSignInManager"

class GoogleSignInManager(private val context: Context) {

    private val googleSignInClient: GoogleSignInClient
    private val webClientId: String = listOf(
        BuildConfig.GOOGLE_WEB_CLIENT_ID,
        BuildConfig.GOOGLE_CLIENT_ID
    ).firstOrNull { it.isNotBlank() } ?: ""

    init {
        Log.d(TAG, "Initializing Google Sign-In for Supabase")
        if (webClientId.isBlank()) {
            Log.e(
                TAG,
                "❌ Web Client ID is NOT configured. Set GOOGLE_WEB_CLIENT_ID in local.properties"
            )
        } else {
            Log.d(TAG, "✅ Using Web Client ID for server auth")
        }
        
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode(webClientId) // Request server auth code for server exchanges
            .requestIdToken(webClientId) // Also request ID token for Supabase auth
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(context, gso)
        Log.d(TAG, "GoogleSignInClient initialized successfully")
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun handleSignInResult(task: Task<GoogleSignInAccount>): GoogleSignInAccount? {
        return try {
            val account = task.getResult(ApiException::class.java)
            Log.d(TAG, "✅ Sign-in successful")
            Log.d(TAG, "   Account email: ${account?.email}")
            Log.d(TAG, "   Display name: ${account?.displayName}")
            Log.d(TAG, "   ID Token present: ${account?.idToken != null}")
            Log.d(TAG, "   Server Auth Code present: ${account?.serverAuthCode != null}")
            if (account?.idToken != null) {
                Log.d(TAG, "   ID Token length: ${account.idToken?.length}")
                Log.d(TAG, "   ID Token preview: ${account.idToken?.take(50)}...")
            } else {
                Log.w(TAG, "⚠️  ID Token is NULL - this will cause sign-in to fail at Supabase")
                Log.w(TAG, "   Possible causes:")
                Log.w(TAG, "   1. Web Client ID not properly configured in Google Cloud Console")
                Log.w(TAG, "   2. Android app SHA-1 fingerprint not registered for this Web Client ID")
                Log.w(TAG, "   3. Google Cloud OAuth verification incomplete")
            }
            account
        } catch (e: ApiException) {
            Log.e(TAG, "❌ Sign-in failed with status code: ${e.statusCode}", e)
            
            // Provide more detailed error messages based on status code
            val errorMessage = when (e.statusCode) {
                10 -> "Developer error: Check your SHA-1 fingerprint and package name in Google Cloud Console"
                12500 -> "Google Play Services is out of date. Please update it."
                12501 -> "User cancelled the sign-in"
                12502 -> "Network error. Check your internet connection."
                else -> "Unknown error: ${e.message}"
            }
            
            Log.e(TAG, "   Error details: $errorMessage")
            null
        }
    }

    fun signOut(onComplete: () -> Unit) {
        googleSignInClient.signOut().addOnCompleteListener {
            onComplete()
        }
    }

    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
}

