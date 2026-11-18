package com.julesmeister.sangfroid.core.designsystem.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight

@Composable
fun SangfroidConfirmationDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    isDestructive: Boolean = true
) {
    AlertDialog(
        onDismissRequest =
            onDismiss,
        title = {
            Text(
                text =
                    title,
                style =
                    MaterialTheme
                        .typography
                        .titleLarge,
                fontWeight =
                    FontWeight.Bold
            )
        },
        text = {
            Text(
                text =
                    message,
                style =
                    MaterialTheme
                        .typography
                        .bodyMedium,
                color =
                    MaterialTheme
                        .colorScheme
                        .onSurfaceVariant
            )
        },
        confirmButton = {
            TextButton(
                onClick =
                    onConfirm
            ) {
                Text(
                    text =
                        confirmText,
                    fontWeight =
                        FontWeight.Bold,
                    color =
                        if (isDestructive) {
                            MaterialTheme
                                .colorScheme
                                .error
                        } else {
                            MaterialTheme
                                .colorScheme
                                .primary
                        }
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick =
                    onDismiss
            ) {
                Text(
                    text =
                        "Cancel",
                    fontWeight =
                        FontWeight.SemiBold
                )
            }
        },
        containerColor =
            MaterialTheme
                .colorScheme
                .surface,
        titleContentColor =
            MaterialTheme
                .colorScheme
                .onSurface,
        textContentColor =
            MaterialTheme
                .colorScheme
                .onSurfaceVariant,
        shape =
            MaterialTheme
                .shapes
                .extraLarge
    )
}