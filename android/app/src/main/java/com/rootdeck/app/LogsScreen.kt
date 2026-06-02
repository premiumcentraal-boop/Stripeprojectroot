package com.rootdeck.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LogsScreen(repo: RootRepository) {
    var confirmClear by remember { mutableStateOf(false) }
    val fmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Logs", style = MaterialTheme.typography.headlineSmall)
            TextButton(
                onClick = { confirmClear = true },
                enabled = repo.logs.isNotEmpty(),
            ) { Text("Clear") }
        }
        Spacer(Modifier.height(8.dp))

        if (repo.logs.isEmpty()) {
            Text("No activity yet. Run a root check or change a policy to populate the log.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(repo.logs) { entry ->
                    ElevatedCard(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(12.dp)) {
                            Text(fmt.format(Date(entry.timestamp)),
                                style = MaterialTheme.typography.labelSmall)
                            Text(
                                entry.title + if (entry.isError) "  (error)" else "",
                                style = MaterialTheme.typography.titleSmall,
                            )
                            if (entry.detail.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(entry.detail,
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Clear logs?") },
            text = { Text("This removes all locally recorded activity. Cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    repo.clearLogs()
                    confirmClear = false
                }) { Text("Clear") }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text("Cancel") }
            },
        )
    }
}
