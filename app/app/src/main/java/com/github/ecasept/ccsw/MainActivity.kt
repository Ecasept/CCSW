package com.github.ecasept.ccsw

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.github.ecasept.ccsw.ui.screens.MainContent
import com.github.ecasept.ccsw.ui.theme.CCSWTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CCSWTheme {
                MainContent()
            }
        }
    }
}





