package com.sparkiai.app.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import com.sparkiai.app.BuildConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

private const val TAG = "GoogleSignInManager"
private const val DEFAULT_WEB_CLIENT_ID =
    "904707581552-2jjbaem1erkm56mc75trk75mcragkn6g.apps.googleusercontent.com"

class GoogleSignInManager(private val context: Context) {

    private val googleSignInClient: GoogleSignInClient
    private val webClientId: String = listOf(
        BuildConfig.GOOGLE_WEB_CLIENT_ID,
        BuildConfig.GOOGLE_CLIENT_ID,
        DEFAULT_WEB_CLIENT_ID
    ).firstOrNull { it.isNotBlank() } ?: DEFAULT_WEB_CLIENT_ID

    init {
        Log.d(TAG, "Initializing Google Sign-In for Supabase")
        if (webClientId == DEFAULT_WEB_CLIENT_ID) {
            Log.w(
                TAG,
                "Using default fallback Web Client ID. Set GOOGLE_WEB_CLIENT_ID in local.properties for production builds."
            )
        } else {
            Log.d(TAG, "Using Web Client ID for server auth: $webClientId")
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
            task.getResult(ApiException::class.java)
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
