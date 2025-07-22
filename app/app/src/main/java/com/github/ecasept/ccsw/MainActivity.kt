package com.github.ecasept.ccsw

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.ecasept.ccsw.data.preferences.PreferencesDataStore
import com.github.ecasept.ccsw.navigation.AppNavigation
import com.github.ecasept.ccsw.ui.screens.MainContent
import com.github.ecasept.ccsw.ui.screens.login.LoginScreen
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





