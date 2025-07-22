package com.github.ecasept.ccsw.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.ecasept.ccsw.ui.components.InputDialog
import com.github.ecasept.ccsw.ui.components.MainTopAppBar
import com.github.ecasept.ccsw.ui.theme.CCSWTheme

@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val prefs by viewModel.prefs.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "Settings",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                onNavigationIconClick = onBackClick,
            )
        },
    ) { innerPadding ->

        if (prefs == null) {
            // Show blank screen if preferences are not loaded
            return@Scaffold
        }

        SettingsContent(
            Modifier.padding(innerPadding),
            prefs!!.serverUrl,
            viewModel::updateServerUrl
        )
    }
}

@Composable
fun SettingsContent(
    modifier: Modifier = Modifier,
    serverUrl: String,
    updateServerUrl: (String) -> Unit
) {
    Column(
        modifier
    ) {
        // Clickable box to change server URL
        var showInputDialog by remember { mutableStateOf(false) }
        if (showInputDialog) {
            InputDialog(
                title = "Change Server URL",
                desc = "Enter the new server URL for the CCSW app.",
                placeholder = "Server URL",
                initialValue = serverUrl,
                onSubmit = { newUrl ->
                    updateServerUrl(newUrl)
                    showInputDialog = false
                },
                onDismiss = { showInputDialog = false }
            )
        }
        SettingsItem(
            title = "Change Server URL",
            desc = "Current URL: $serverUrl",
            onClick = { showInputDialog = true }
        )
    }
}

@Composable
fun SettingsItem(
    title: String,
    desc: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            text = desc,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}


@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    CCSWTheme {
        SettingsContent(
            serverUrl = "http://example.com",
            updateServerUrl = {})
    }
}

