package com.rootdeck.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

enum class RiskLevel(val label: String) {
    Low("Low risk"),
    Medium("Medium risk"),
    High("High risk — destructive"),
}

/** Mandatory confirmation dialog for any root command. */
@Composable
fun SafetyConfirmDialog(
    title: String,
    command: String?,
    risk: RiskLevel,
    showPreview: Boolean,
    body: String? = null,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                Text("Risk: ${risk.label}")
                if (body != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(body)
                }
                if (showPreview && command != null) {
                    Spacer(Modifier.height(8.dp))
                    Text("Command preview:")
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "$ $command",
                        fontFamily = FontFamily.Monospace,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text("This will execute through your installed root provider. Proceed?")
            }
        },
        confirmButton = { TextButton(onClick = onConfirm) { Text("Confirm") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
