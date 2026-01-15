package com.bluegenie.app

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bluegenie.app.ui.screens.ChatScreen
import com.bluegenie.app.ui.theme.BlueGenieTheme
import com.bluegenie.app.viewmodel.ChatViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf

class MainActivity : ComponentActivity() {
    
    // Store ViewModel reference to check premium status on resume
    private var chatViewModel: ChatViewModel? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlueGenieTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    BlueGenieApp { viewModel ->
                        chatViewModel = viewModel
                    }
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Check if user completed Stripe payment
        chatViewModel?.onAppResume()
    }
}

/**
 * Opens a URL in a Chrome Custom Tab.
 *
 * @param context The context to use for launching the Custom Tab.
 * @param url The URL to open.
 */
fun openUrlInCustomTab(context: Context, url: String) {
    val builder = CustomTabsIntent.Builder()
    val customTabsIntent = builder.build()
    customTabsIntent.launchUrl(context, Uri.parse(url))
}

@Composable
fun BlueGenieApp(onViewModelCreated: (ChatViewModel) -> Unit = {}) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "chat",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("chat") {
            val viewModel: ChatViewModel = viewModel()
            
            // Notify MainActivity that ViewModel is created
            androidx.compose.runtime.LaunchedEffect(viewModel) {
                onViewModelCreated(viewModel)
            }
            
            ChatScreen(viewModel = viewModel)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun BlueGenieAppPreview() {
    BlueGenieTheme {
        BlueGenieApp()
    }
}
