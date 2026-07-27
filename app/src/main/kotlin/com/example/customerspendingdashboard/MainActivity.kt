package com.example.customerspendingdashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.customerspendingdashboard.core.designsystem.theme.SpendingTheme
import com.example.customerspendingdashboard.navigation.AppNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Installs the splash screen, then sets the themed Compose nav host as the activity content.
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SpendingTheme {
                AppNavHost()
            }
        }
    }
}
