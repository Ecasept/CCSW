package com.github.ecasept.ccsw.ui.screens

import android.app.Application
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.ecasept.ccsw.data.preferences.AppPreferences
import com.github.ecasept.ccsw.navigation.AppNavigation
import com.github.ecasept.ccsw.ui.screens.login.LoginScreen
import com.github.ecasept.ccsw.ui.theme.CCSWTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun MainContent(
    viewModel: MainViewModel = viewModel()
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle();

    if (prefs == null) {
        // Show blank screen
        return
    }

    if (prefs!!.userId != null) {
        AppNavigation(onLogout=viewModel::logout)
    } else {
        LoginScreen {
            viewModel.updateUserId(it)
        }
    }
}
