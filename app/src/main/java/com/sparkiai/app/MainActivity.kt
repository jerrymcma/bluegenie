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
import com.sparkiai.app.ui.theme.SparkiFireTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SparkiFireTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SparkiFireApp()
                }
            }
        }
    }
}

@Composable
fun SparkiFireApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "chat",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("chat") {
            ChatScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SparkiFireAppPreview() {
    SparkiFireTheme {
        SparkiFireApp()
    }
}