package com.github.ecasept.ccsw

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.github.ecasept.ccsw.fcm.ACTION_NOTIFICATION_CHANNEL_ID
import com.github.ecasept.ccsw.fcm.ACTION_NOTIFICATION_CHANNEL_NAME
import com.github.ecasept.ccsw.navigation.AppNavigation
import com.github.ecasept.ccsw.ui.theme.CCSWTheme
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannels()

        Firebase.messaging.isAutoInitEnabled = true

        setContent {
            CCSWTheme {
                AppNavigation()
            }
        }
    }

    private fun createNotificationChannels() {
        val channels = listOf(
            NotificationChannel(
                ACTION_NOTIFICATION_CHANNEL_ID,
                ACTION_NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
        )
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        for (channel in channels) {
            notificationManager.createNotificationChannel(channel)
        }
    }
}





