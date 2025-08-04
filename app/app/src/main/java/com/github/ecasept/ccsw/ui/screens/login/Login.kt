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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                onButtonClick,
                state.userId,
                viewModel::updateUserId,
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
    userId: String,
    updateUserId: (userId: String) -> Unit,
    onRationaleClick: (Boolean) -> Unit,
    loadState: LoadState
) {

    if (showRationale) {
        RationaleDialog(onRationaleClick)
    }

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
            text = "Enter your user ID",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = "Use the same ID from your Python app configuration",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp),
            textAlign = TextAlign.Center
        )

        OutlinedTextField(
            value = userId,
            onValueChange = updateUserId,
            label = { Text("User ID") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(12.dp)
        )

        ContinueButton(
            onClick = onButtonClick, enabled = userId.isNotBlank(), loadState = loadState
        )
        if (loadState is LoadState.Failure) {
            Text(
                text = loadState.message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
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
    onClick: () -> Unit, enabled: Boolean, loadState: LoadState
) {

    Button(
        onClick = onClick,
        enabled = enabled && loadState != LoadState.Loading,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        if (loadState == LoadState.Loading) {
            // Animated loading icon
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


//@Preview
//@Composable
//fun LoginContentPreview() {
//    CCSWTheme {
//        LoginContent(
//            modifier = Modifier.fillMaxSize(),
//            showRationale = false,
//            onButtonClick = {},
//            userId = "123456789",
//            updateUserId = {},
//            onRationaleClick = {},
//            loadState = LoadState.None
//        )
//    }
//}

@Preview
@Composable
fun ContinueButtonPreview() {
    CCSWTheme {
//        ContinueButton(
//            onClick = {},
//            enabled = true,
//            loadState = LoadState.None
//        )
        Text("a")
    }
}