package com.github.ecasept.ccsw.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.ecasept.ccsw.ui.theme.CCSWTheme


/**
 * A dialog that allows the user to input a string value.
 *
 * @param title The title of the dialog.
 * @param desc A description or instructions for the input.
 * @param placeholder A placeholder text for the input field.
 * @param initialValue The initial value to display in the input field.
 * @param onSubmit Callback function to be called when the user submits the input.
 * @param onDismiss Callback function to be called when the dialog is dismissed.
 */
@Composable
fun InputDialog(
    title: String,
    desc: String,
    placeholder: String,
    initialValue: String,
    onSubmit: (String) -> Unit,
    onDismiss: () -> Unit
) {

    var text by remember {
        mutableStateOf(
            // Initialize the text field with the initial value and put the cursor at the end
            TextFieldValue(
                initialValue, selection = TextRange(initialValue.length)
            )
        )
    }

    val requester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        // Request focus when dialog is shown
        requester.requestFocus()
    }

    Dialog(
        onDismissRequest = onDismiss
    ) {
        // See https://m3.material.io/components/dialogs/specs for dialog specs
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp,
            ),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = desc,
                    textAlign = TextAlign.Center,
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text(placeholder) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(requester),
                    singleLine = true,
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()
                ) {
                    TextButton(
                        onClick = onDismiss, modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Cancel")
                    }
                    TextButton(
                        onClick = {
                            onSubmit(text.text)
                        }, modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Preview(showBackground = true)
@Composable
fun InputDialogPreview() {
    CCSWTheme {
        Scaffold {
            InputDialog(
                title = "Change Value",
                desc = "Enter the new value for the setting.",
                placeholder = "Value",
                initialValue = "Default Value",
                onSubmit = {},
                onDismiss = {}
            )
        }
    }
}
