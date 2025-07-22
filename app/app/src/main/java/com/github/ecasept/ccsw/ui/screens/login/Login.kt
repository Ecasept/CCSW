package com.github.ecasept.ccsw.ui.screens.login

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
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.ecasept.ccsw.ui.components.MainTopAppBar
import com.github.ecasept.ccsw.ui.theme.CCSWTheme


@Composable
fun LoginScreen(onLogin: (userId: String) -> Unit) {
    Scaffold(
        topBar = { MainTopAppBar(title = "CCSW Login") },
    ) { innerPadding ->
        LoginContent(
            Modifier
                .padding(innerPadding)
                .padding(16.dp), onLogin)
    }
}


@Composable
fun LoginContent(modifier: Modifier = Modifier, onLogin: (userId: String) -> Unit) {
    var userId by remember { mutableStateOf("") }

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
            onValueChange = { userId = it },
            label = { Text("User ID") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            shape = RoundedCornerShape(12.dp)
        )

        Button(
            onClick = {
                onLogin(userId)
            },
            enabled = userId.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Login,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Continue",
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    CCSWTheme {
        LoginScreen {}
    }
}
