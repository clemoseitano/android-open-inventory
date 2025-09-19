package com.coptimize.openinventory.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp

@Composable
fun PasswordPromptDialog(
    title: String,
    onConfirm: (String) -> Unit,
    error: String?,
    onDismiss: () -> Unit
) {
    var password by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    val submit = {
        if (password.isNotBlank()) {
            focusManager.clearFocus()
            onConfirm(password)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column {
                Text("Please enter your password to continue.")
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = error != null,
                    supportingText = {
                        if (error != null) {
                            Text(error, color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() })
                )
            }
        },
        confirmButton = {
            Button(onClick = { submit() }, enabled = password.isNotBlank()) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}