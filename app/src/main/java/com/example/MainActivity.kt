package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.firebase.configureAppCheck
import com.example.notifications.PinkoraNotificationManager
import com.example.ui.AppNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure Firebase App Check with launch intent before any UI or Firebase calls
        configureAppCheck(this, intent)
        PinkoraNotificationManager.ensureChannel(this)
        enableEdgeToEdge()

        setContent {
            AppNavigation()
        }
    }
}
