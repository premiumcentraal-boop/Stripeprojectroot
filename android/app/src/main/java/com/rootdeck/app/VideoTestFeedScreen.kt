package com.rootdeck.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun VideoTestFeedScreen(onBack: () -> Unit) {
    var selectedVideo by remember { mutableStateOf<String?>(null) }
    var targetCamera by remember { mutableStateOf("Front Camera") }
    var isInjectionActive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TextButton(onClick = onBack) { Text("← Back to Basic Tools") }
        
        Text("Video Test Feed", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Planned media test feed for user-owned testing environments",
            style = MaterialTheme.typography.bodySmall,
        )
        SafetyPolicyLink()

        // Video selection card
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("1. Select Video Source", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                
                if (selectedVideo != null) {
                    Text("Selected: $selectedVideo", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    
                    // Local preview mock
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Movie, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Local Preview Active", modifier = Modifier.padding(top = 80.dp), style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { selectedVideo = null }) {
                        Text("Clear Selection")
                    }
                } else {
                    OutlinedButton(onClick = { selectedVideo = "/storage/emulated/0/Movies/test_feed.mp4" }) {
                        Text("Upload Video File")
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("Supports local .mp4 and .mkv formats.", style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        // Camera target selection
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("2. Target Camera", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = targetCamera == "Front Camera",
                        onClick = { targetCamera = "Front Camera" }
                    )
                    Text("Front Camera")
                    Spacer(Modifier.width(16.dp))
                    RadioButton(
                        selected = targetCamera == "Back Camera",
                        onClick = { targetCamera = "Back Camera" }
                    )
                    Text("Back Camera")
                }
            }
        }

        // Injection Controls
        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("3. Hidden Injection", style = MaterialTheme.typography.titleSmall)
                Text(
                    "Hooks Camera and Camera2 APIs via LSPosed to feed the selected video into the stream.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = isInjectionActive,
                        onCheckedChange = { isActive ->
                            if (selectedVideo != null) {
                                isInjectionActive = isActive
                            }
                        },
                        enabled = selectedVideo != null
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        if (isInjectionActive) "Injection Active" else "Injection Disabled",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                if (selectedVideo == null) {
                    Spacer(Modifier.height(4.dp))
                    Text("Select a video first.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
