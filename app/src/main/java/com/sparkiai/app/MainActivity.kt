package com.sparkiai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.sparkiai.app.ui.screens.ChatScreen
import com.sparkiai.app.ui.theme.SparkiAITheme
import com.sparkiai.app.viewmodel.ChatViewModel
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
            SparkiAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SparkiAIApp { viewModel ->
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

@Composable
fun SparkiAIApp(onViewModelCreated: (ChatViewModel) -> Unit = {}) {
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
fun SparkiAIAppPreview() {
    SparkiAITheme {
        SparkiAIApp()
    }
}