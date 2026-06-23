package com.calorai.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.calorai.app.navigation.CalorAINavGraph
import com.calorai.app.ui.theme.CalorAITheme
import com.calorai.app.ui.theme.Surface0
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CalorAITheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Surface0
                ) {
                    CalorAINavGraph()
                }
            }
        }
    }
}
