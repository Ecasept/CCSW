package com.github.ecasept.ccsw.ui.screens.login

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.ecasept.ccsw.ui.components.MainTopAppBar
import com.github.ecasept.ccsw.ui.theme.CCSWTheme

@Composable
fun LoginScreen(
    onLoginNav: () -> Unit, viewModel: LoginViewModel = viewModel()
) {
    val state = viewModel.loginState.collectAsStateWithLifecycle().value

    Scaffold(
        topBar = { MainTopAppBar(title = "CCSW Login") },
    ) { innerPadding ->
        Permission(
            onContinue = { viewModel.login(onLoginNav) },
            permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.POST_NOTIFICATIONS
            } else {
                null
            },
        ) { showRationale, onRationaleClick, onButtonClick ->
            LoginContent(
                Modifier
                    .padding(innerPadding)
                    .padding(16.dp),
                showRationale,
                { onButtonClick(Unit) },
                state.instanceId,
                state.accessCode,
                state.serverUrl,
                viewModel.isServerUrlDirty.collectAsStateWithLifecycle(false).value,
                viewModel::resetServerUrl,
                viewModel::updateInstanceId,
                viewModel::updateAccessCode,
                viewModel::updateServerUrl,
                onRationaleClick,
                state.loadState
            )
        }
    }
}


@Composable
fun LoginContent(
    modifier: Modifier = Modifier,
    showRationale: Boolean,
    onButtonClick: () -> Unit,
    instanceId: String,
    accessCode: String,
    serverUrl: TextFieldValue,
    isServerUrlDirty: Boolean,
    resetServerUrl: () -> Unit,
    updateInstanceId: (userId: String) -> Unit,
    updateAccessCode: (accessCode: String) -> Unit,
    updateServerUrl: (serverUrl: TextFieldValue) -> Unit,
    onRationaleClick: (Boolean) -> Unit,
    loadState: LoadState
) {

    if (showRationale) {
        RationaleDialog(onRationaleClick)
    }

    val isInfoEntered = instanceId.isNotBlank() && accessCode.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 16.dp),
            imageVector = Icons.AutoMirrored.Filled.Login,
            contentDescription = "Login Icon",
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = "Welcome to Cookie Clicker Stock Watcher",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Log in to the instance you created with the analyzer",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = instanceId,
            onValueChange = updateInstanceId,
            label = { Text("Instance ID") },
            singleLine = true,
            modifier = Modifier
                .widthIn(0.dp, 400.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = accessCode,
            onValueChange = updateAccessCode,
            label = { Text("Access Code") },
            singleLine = true,
            modifier = Modifier
                .widthIn(0.dp, 400.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = serverUrl,
            onValueChange = updateServerUrl,
            label = { Text("Server URL") },
            singleLine = true,
            modifier = Modifier
                .widthIn(0.dp, 400.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            suffix = {
                if (isServerUrlDirty) {
                    IconButton(
                        onClick = resetServerUrl,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "Reset Server URL",
                        )
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        ContinueButton(
            modifier = Modifier
                .widthIn(0.dp, 400.dp)
                .fillMaxWidth(),

            onClick = onButtonClick,
            isInfoEntered = isInfoEntered,
            loadState = loadState
        )

        if (loadState is LoadState.Failure) {
            Text(
                text = loadState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun RationaleDialog(onRationaleClick: (Boolean) -> Unit) {
    AlertDialog(
        onDismissRequest = { },
        confirmButton = {
            TextButton(onClick = { onRationaleClick(true) }) {
                Text("Allow")
            }
        },
        dismissButton = {
            TextButton(onClick = { onRationaleClick(false) }) {
                Text("Continue without")
            }
        },
        title = { Text("Notification Permission Required") },
        text = {
            Text("To receive stock updates, please allow notification access.")
        },
    )
}

@Composable
fun ContinueButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    isInfoEntered: Boolean,
    loadState: LoadState
) {
    Button(
        onClick = onClick,
        enabled = isInfoEntered && loadState !is LoadState.Loading,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (loadState == LoadState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
            )
        } else {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Login,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Continue", style = MaterialTheme.typography.labelLarge
        )
    }
}

@Preview(apiLevel = 34, showBackground = true)
@Composable
fun LoginContentPreview() {
    CCSWTheme {
        LoginContent(
            modifier = Modifier.fillMaxSize(),
            showRationale = false,
            onButtonClick = {},
            instanceId = "example-instance-id",
            accessCode = "example-access-code",
            serverUrl = TextFieldValue("https://example.com"),
            isServerUrlDirty = true,
            resetServerUrl = {},
            updateInstanceId = {},
            updateAccessCode = {},
            updateServerUrl = {},
            onRationaleClick = {},
            loadState = LoadState.None
        )
    }
}

@Preview(apiLevel = 34, showBackground = true)
@Composable
fun ButtonsPreview() {
    CCSWTheme {
        Column(
            modifier = Modifier
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("No info entered")
            ContinueButton(
                onClick = {},
                isInfoEntered = false,
                loadState = LoadState.None,
                modifier = Modifier.fillMaxWidth()
            )
            Text("Normal")
            ContinueButton(
                onClick = {},
                isInfoEntered = true,
                loadState = LoadState.None,
                modifier = Modifier.fillMaxWidth()
            )
            Text("Loading")
            ContinueButton(
                onClick = {},
                isInfoEntered = true,
                loadState = LoadState.Loading,
                modifier = Modifier.fillMaxWidth()
            )
            Text("Failure")
            ContinueButton(
                onClick = {},
                isInfoEntered = true,
                loadState = LoadState.Failure("Login failed"),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}