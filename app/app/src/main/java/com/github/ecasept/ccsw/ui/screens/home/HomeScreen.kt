package com.github.ecasept.ccsw.ui.screens.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.ecasept.ccsw.ui.components.MainTopAppBar

@Composable
fun HomeScreen(onPrefClick: () -> Unit, onLogoutClick: () -> Unit) {
    var dropdownMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            MainTopAppBar(
                title = "CCSW",
                actions = {
                    IconButton(onClick = { dropdownMenuExpanded = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options"
                        )
                    }
                    HomeDropdownMenu(
                        dropdownMenuExpanded,
                        { dropdownMenuExpanded = false },
                        onPrefClick,
                        onLogoutClick
                    )
                }
            )
        },
    ) { innerPadding ->
        Text(modifier = Modifier.padding(innerPadding), text = "abc")
    }
}

@Composable
fun HomeDropdownMenu(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onPrefClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    DropdownMenu(expanded, onDismissRequest) {
        DropdownMenuItem(
            text = { Text("Settings") },
            onClick = {
                onPrefClick()
                onDismissRequest()
            }
        )
        DropdownMenuItem(
            text = { Text("Logout") },
            onClick = {
                onLogoutClick()
                onDismissRequest()
            }
        )
    }
}


@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen({}, {})
}