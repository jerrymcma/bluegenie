package com.sparkiai.app.utils

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import com.sparkiai.app.BuildConfig
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlin.jvm.java

private const val TAG = "GoogleSignInManager"

class GoogleSignInManager(private val context: Context) {

    private val googleSignInClient: GoogleSignInClient

    init {
        Log.d(TAG, "Client ID: ${BuildConfig.GOOGLE_CLIENT_ID}")

        if (BuildConfig.GOOGLE_CLIENT_ID.isEmpty() || BuildConfig.GOOGLE_CLIENT_ID == "YOUR_CLIENT_ID_HERE.apps.googleusercontent.com") {
            throw IllegalStateException("Google Client ID is not configured properly")
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)
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
            Log.e(TAG, "Sign-in failed with status code: ${e.statusCode}", e)
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
