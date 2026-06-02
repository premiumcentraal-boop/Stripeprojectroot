package com.rootdeck.app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SettingsScreen(repo: RootRepository) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall)

        SettingRow(
            title = "Advanced mode",
            subtitle = "Enables custom command fields in future tools.",
            checked = repo.advancedMode.value,
            onCheckedChange = repo::setAdvancedMode,
        )

        SettingRow(
            title = "Show command previews",
            subtitle = "Display the raw command in confirmation dialogs.",
            checked = repo.showCommandPreviews.value,
            onCheckedChange = repo::setShowCommandPreviews,
        )

        SettingRow(
            title = "Always require confirmation",
            subtitle = "Locked on by design.",
            checked = true,
            onCheckedChange = {},
            enabled = false,
        )

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Theme", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                ThemeMode.values().forEach { mode ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = repo.theme.value == mode,
                            onClick = { repo.setTheme(mode) },
                        )
                        Text(mode.name)
                    }
                }
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("About RootDeck", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(4.dp))
                Text("Root dashboard for Android 14+.", style = MaterialTheme.typography.bodySmall)
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Safety policy", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                SafetyPolicyLink()
            }
        }
    }
}

@Composable
private fun SettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true,
) {
    ElevatedCard(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
        }
    }
}
