package com.bluegenie.app.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.bluegenie.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Helper for Stripe checkout integration
 * Calls web app API to create checkout session, then opens Stripe
 */
object StripeCheckoutHelper {
    
    private const val TAG = "StripeCheckout"
    
    /**
     * Open Stripe checkout in browser
     * 
     * Calls the web app's API to create a Stripe checkout session,
     * then opens the returned Stripe URL in the browser.
     * 
     * @param context Android context
     * @param userId User ID from Supabase
     * @param userEmail User's email address
     */
    suspend fun openCheckout(context: Context, userId: String, userEmail: String) {
        try {
            Log.d(TAG, "🛒 Creating Stripe checkout session for user: $userId")
            Log.d(TAG, "   Email: $userEmail")
            Log.d(TAG, "   Web App URL: ${BuildConfig.WEB_APP_URL}")
            
            // Call web app API to create checkout session
            val checkoutUrl = createCheckoutSession(userId, userEmail)
            
            Log.d(TAG, "✅ Got Stripe checkout URL: $checkoutUrl")
            
            // Validate URL
            if (checkoutUrl.isBlank()) {
                throw Exception("Empty checkout URL received from server")
            }
            
            if (!checkoutUrl.startsWith("http")) {
                throw Exception("Invalid checkout URL format: $checkoutUrl")
            }
            
            // Open Stripe checkout in browser
            withContext(Dispatchers.Main) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(checkoutUrl))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    
                    // Try to find any app that can handle this URL
                    val packageManager = context.packageManager
                    val activities = packageManager.queryIntentActivities(intent, 0)
                    
                    Log.d(TAG, "🔍 Found ${activities.size} apps that can handle URL")
                    
                    // Log which apps were found
                    if (activities.isNotEmpty()) {
                        Log.d(TAG, "📱 Available browsers:")
                        activities.forEach { resolveInfo ->
                            val appName = resolveInfo.loadLabel(packageManager)
                            val packageName = resolveInfo.activityInfo.packageName
                            Log.d(TAG, "   - $appName ($packageName)")
                        }
                    } else {
                        Log.e(TAG, "❌ No apps found that can open URLs")
                        Log.e(TAG, "   Please install Chrome, Firefox, or any web browser")
                    }
                    
                    if (activities.isNotEmpty()) {
                        // Found an app that can open the URL
                        context.startActivity(intent)
                        Log.d(TAG, "✅ Stripe checkout opened successfully")
                    } else {
                        // No browser found - copy URL to clipboard as fallback
                        Log.e(TAG, "❌ No browser app found to open URL")
                        Log.d(TAG, "📋 Copying URL to clipboard as fallback")
                        
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Stripe Checkout URL", checkoutUrl)
                        clipboard.setPrimaryClip(clip)
                        
                        Toast.makeText(
                            context,
                            "No browser found! Payment link copied to clipboard.\n\nPlease install Chrome and paste the link.",
                            Toast.LENGTH_LONG
                        ).show()
                        
                        Log.d(TAG, "✅ URL copied to clipboard: $checkoutUrl")
                        
                        throw Exception("No browser app available.\n\nPayment link copied to clipboard.\n\nPlease:\n1. Install Chrome from Play Store\n2. Open Chrome\n3. Paste the link (long-press in address bar)")
                    }
                } catch (e: Exception) {
                    when {
                        e.message?.contains("clipboard") == true -> {
                            // Already handled clipboard copy
                            throw e
                        }
                        e.message?.contains("No browser") == true -> {
                            // Already handled no browser case
                            throw e
                        }
                        else -> {
                            Log.e(TAG, "❌ Failed to open browser with URL: $checkoutUrl", e)
                            throw Exception("Failed to open browser: ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to open Stripe checkout: ${e.message}", e)
            Log.e(TAG, "   Stack trace: ${e.stackTraceToString()}")
            throw e
        }
    }
    
    /**
     * Follow a redirect for POST requests
     */
    private suspend fun followRedirect(redirectUrl: String, requestBody: String): String = withContext(Dispatchers.IO) {
        Log.d(TAG, "🔄 Following redirect to: $redirectUrl")
        
        var connection: HttpURLConnection? = null
        try {
            val url = URL(redirectUrl)
            connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "SparkiFire-Android")
            connection.doOutput = true
            connection.doInput = true
            connection.connectTimeout = 30000
            connection.readTimeout = 30000
            connection.instanceFollowRedirects = true
            
            // Send the same request body
            connection.outputStream.use { os ->
                val bytes = requestBody.toByteArray(Charsets.UTF_8)
                os.write(bytes)
                os.flush()
            }
            
            val responseCode = connection.responseCode
            val responseMessage = connection.responseMessage
            
            Log.d(TAG, "📥 Redirect response code: $responseCode ($responseMessage)")
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "✅ Redirect response body: $response")
                
                val jsonResponse = JSONObject(response)
                
                if (!jsonResponse.has("url")) {
                    Log.e(TAG, "❌ Response missing 'url' field")
                    throw Exception("Invalid API response: missing 'url' field")
                }
                
                val checkoutUrl = jsonResponse.getString("url")
                
                if (checkoutUrl.isBlank()) {
                    Log.e(TAG, "❌ Empty checkout URL in response")
                    throw Exception("Empty checkout URL received from API")
                }
                
                Log.d(TAG, "✅ Checkout URL from redirect: $checkoutUrl")
                return@withContext checkoutUrl
            } else {
                val errorBody = try {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body"
                } catch (e: Exception) {
                    "Unable to read error body: ${e.message}"
                }
                
                Log.e(TAG, "❌ Redirect API error ($responseCode): $errorBody")
                throw Exception("Redirect API returned $responseCode: $errorBody")
            }
        } finally {
            connection?.disconnect()
        }
    }
    
    /**
     * Call web app API to create Stripe checkout session
     */
    private suspend fun createCheckoutSession(userId: String, userEmail: String): String = withContext(Dispatchers.IO) {
        val webAppUrl = BuildConfig.WEB_APP_URL.trimEnd('/')
        val apiUrl = "$webAppUrl/api/create-checkout"
        
        Log.d(TAG, "🌐 Calling API: $apiUrl")
        Log.d(TAG, "   userId: $userId")
        Log.d(TAG, "   customerEmail: $userEmail")
        
        var connection: HttpURLConnection? = null
        
        try {
            val url = URL(apiUrl)
            connection = url.openConnection() as HttpURLConnection
            
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "SparkiFire-Android")
            connection.doOutput = true
            connection.doInput = true
            connection.connectTimeout = 30000  // Increased to 30 seconds
            connection.readTimeout = 30000
            connection.instanceFollowRedirects = true  // Follow redirects automatically
            
            // Build request body
            val requestBody = JSONObject().apply {
                put("userId", userId)
                put("customerEmail", userEmail)
            }
            
            Log.d(TAG, "📤 Request body: $requestBody")
            
            // Send request
            connection.outputStream.use { os ->
                val bytes = requestBody.toString().toByteArray(Charsets.UTF_8)
                os.write(bytes)
                os.flush()
            }
            
            // Read response
            val responseCode = connection.responseCode
            val responseMessage = connection.responseMessage
            
            Log.d(TAG, "📥 API response code: $responseCode ($responseMessage)")
            
            // Handle redirects (307, 308) by following the Location header
            if (responseCode == 307 || responseCode == 308) {
                val redirectUrl = connection.getHeaderField("Location")
                Log.d(TAG, "🔄 Redirect detected to: $redirectUrl")
                
                if (redirectUrl.isNullOrBlank()) {
                    throw Exception("Redirect response missing Location header")
                }
                
                // Follow redirect manually for POST requests
                connection.disconnect()
                return@withContext followRedirect(redirectUrl, requestBody.toString())
            }
            
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                Log.d(TAG, "✅ API response body: $response")
                
                val jsonResponse = JSONObject(response)
                
                // Check if response has 'url' field
                if (!jsonResponse.has("url")) {
                    Log.e(TAG, "❌ Response missing 'url' field")
                    throw Exception("Invalid API response: missing 'url' field")
                }
                
                val checkoutUrl = jsonResponse.getString("url")
                
                if (checkoutUrl.isBlank()) {
                    Log.e(TAG, "❌ Empty checkout URL in response")
                    throw Exception("Empty checkout URL received from API")
                }
                
                Log.d(TAG, "✅ Checkout URL: $checkoutUrl")
                return@withContext checkoutUrl
            } else {
                val errorBody = try {
                    connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "No error body"
                } catch (e: Exception) {
                    "Unable to read error body: ${e.message}"
                }
                
                Log.e(TAG, "❌ API error ($responseCode): $errorBody")
                
                throw Exception("API returned $responseCode: $errorBody")
            }
        } catch (e: java.net.UnknownHostException) {
            Log.e(TAG, "❌ Network error: Cannot resolve host", e)
            throw Exception("Cannot connect to server. Please check your internet connection.")
        } catch (e: java.net.ConnectException) {
            Log.e(TAG, "❌ Network error: Connection failed", e)
            throw Exception("Cannot connect to server. Please check your internet connection.")
        } catch (e: java.net.SocketTimeoutException) {
            Log.e(TAG, "❌ Network error: Request timed out", e)
            throw Exception("Connection timed out. Please try again.")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Unexpected error creating checkout session", e)
            throw Exception("Failed to create checkout: ${e.message}")
        } finally {
            connection?.disconnect()
        }
    }
    
    /**
     * Get Stripe publishable key
     */
    fun getPublishableKey(): String {
        return BuildConfig.STRIPE_PUBLISHABLE_KEY
    }
}

