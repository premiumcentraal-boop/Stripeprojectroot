package com.rootdeck.app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

private const val VIDEO_MODULE_CLASS = "com.rootdeck.app.xposed.VideoInjectionXposedModule"

@Composable
fun VideoTestFeedScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedVideoLabel by remember { mutableStateOf<String?>(null) }
    var targetCamera by remember { mutableStateOf("Front Camera") }
    var lsposedStatus by remember { mutableStateOf(readLsposedStatus(context)) }

    fun setSelectedVideo(uri: Uri?) {
        selectedVideoUri = uri
        selectedVideoLabel = uri?.let { videoUri ->
            videoUri.lastPathSegment?.substringAfterLast('/') ?: videoUri.toString()
        }
    }

    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        setSelectedVideo(uri)
    }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
    ) { uri ->
        if (uri != null) {
            runCatching {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            }
        }
        setSelectedVideo(uri)
    }

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
            "Explicit media source setup for controlled, user-owned testing environments.",
            style = MaterialTheme.typography.bodySmall,
        )
        SafetyPolicyLink()

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("1. Select Video Source", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                
                if (selectedVideoUri != null) {
                    Text(
                        "Selected video source",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        selectedVideoLabel ?: selectedVideoUri.toString(),
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        selectedVideoUri.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.Outlined.Movie,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            "Video source selected",
                            modifier = Modifier.padding(top = 80.dp),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    VideoPickerButtons(
                        onPickGallery = {
                            galleryPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly),
                            )
                        },
                        onPickFiles = { filePicker.launch(arrayOf("video/*")) },
                    )
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { setSelectedVideo(null) }) {
                        Text("Clear Selection")
                    }
                } else {
                    VideoPickerButtons(
                        onPickGallery = {
                            galleryPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly),
                            )
                        },
                        onPickFiles = { filePicker.launch(arrayOf("video/*")) },
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Choose a video from Photos/Gallery or browse Files. Supports Android content URIs for .mp4, .mkv, and other video formats.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("2. Target Camera", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = targetCamera == "Front Camera",
                        onClick = { targetCamera = "Front Camera" },
                    )
                    Text("Front Camera")
                    Spacer(Modifier.width(16.dp))
                    RadioButton(
                        selected = targetCamera == "Back Camera",
                        onClick = { targetCamera = "Back Camera" },
                    )
                    Text("Back Camera")
                }
                Text(
                    "Selected target: $targetCamera",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("3. LSPosed Integration Check", style = MaterialTheme.typography.titleSmall)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "RootDeck packages the Video Test Feed module entry for LSPosed. Enable the RootDeck module inside LSPosed Manager and scope it only to your own test apps.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(12.dp))
                StatusRow("LSPosed Manager", if (lsposedStatus.managerInstalled) "Detected" else "Not detected")
                StatusRow("Video module entry", if (lsposedStatus.moduleEntryPackaged) "Packaged" else "Missing")
                StatusRow("Selected video", selectedVideoLabel ?: "Not selected")
                StatusRow("Target camera", targetCamera)
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { lsposedStatus = readLsposedStatus(context) }) {
                    Text("Refresh LSPosed Status")
                }
                if (!lsposedStatus.managerInstalled) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Install and enable LSPosed separately on the rooted test device. This app can detect the manager package, but Android apps cannot confirm every LSPosed scope/activation state without LSPosed-side support.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoPickerButtons(onPickGallery: () -> Unit, onPickFiles: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedButton(onClick = onPickGallery, modifier = Modifier.weight(1f)) {
            Icon(Icons.Outlined.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Gallery")
        }
        OutlinedButton(onClick = onPickFiles, modifier = Modifier.weight(1f)) {
            Icon(Icons.Outlined.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Files")
        }
    }
}

@Composable
private fun StatusRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(label, modifier = Modifier.weight(0.42f), style = MaterialTheme.typography.labelMedium)
        Text(value, modifier = Modifier.weight(0.58f), style = MaterialTheme.typography.bodySmall)
    }
}

private data class LsposedStatus(
    val managerInstalled: Boolean,
    val moduleEntryPackaged: Boolean,
)

private fun readLsposedStatus(context: Context): LsposedStatus {
    val possibleManagerPackages = listOf(
        "org.lsposed.manager",
        "org.lsposed.manager.debug",
    )
    val managerInstalled = possibleManagerPackages.any { packageName ->
        runCatching {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
        }.isSuccess
    }
    val moduleEntryPackaged = runCatching {
        context.assets.open("xposed_init").bufferedReader().use { reader ->
            reader.readText().lineSequence().any { it.trim() == VIDEO_MODULE_CLASS }
        }
    }.getOrDefault(false)
    return LsposedStatus(managerInstalled, moduleEntryPackaged)
}
