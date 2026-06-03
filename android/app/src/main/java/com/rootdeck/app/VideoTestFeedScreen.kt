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
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.widget.VideoView

private const val VIDEO_MODULE_CLASS = "com.rootdeck.app.xposed.VideoInjectionXposedModule"
private const val VIDEO_FEED_PREFS = "rootdeck_video_feed_setup"

@Composable
fun VideoTestFeedScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedVideoLabel by remember { mutableStateOf<String?>(null) }
    var targetCamera by remember { mutableStateOf("Front Camera") }
    var fitMode by remember { mutableStateOf(VideoFitMode.FitBlackBars) }
    var cropAnchor by remember { mutableStateOf(VideoCropAnchor.Center) }
    var outputSize by remember { mutableStateOf(CameraOutputSize.MatchCamera) }
    var setupSaved by remember { mutableStateOf(false) }
    var testRunStatus by remember { mutableStateOf("Not started") }
    val repeatPlayback = true
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

    fun saveVideoFeedSetup() {
        context.getSharedPreferences(VIDEO_FEED_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putString("video_uri", selectedVideoUri?.toString())
            .putString("target_camera", targetCamera)
            .putString("fit_mode", fitMode.name)
            .putString("crop_anchor", cropAnchor.name)
            .putString("output_size", outputSize.name)
            .putBoolean("repeat_playback", repeatPlayback)
            .apply()
        setupSaved = true
    }

    fun startFirstTestRun() {
        saveVideoFeedSetup()
        context.getSharedPreferences(VIDEO_FEED_PREFS, Context.MODE_PRIVATE)
            .edit()
            .putLong("last_test_run_started_at", System.currentTimeMillis())
            .putBoolean("test_run_requested", true)
            .apply()
        testRunStatus = "Test run prepared. Now open your own LSPosed-scoped camera test app after confirming RootDeck is enabled in LSPosed. The video is saved with repeat playback always on."
    }

    LaunchedEffect(selectedVideoUri, targetCamera, fitMode, cropAnchor, outputSize) {
        setupSaved = false
        testRunStatus = "Not started"
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
            "Friendly LSPosed-linked setup for streaming a selected video file into your own front/back camera test flow.",
            style = MaterialTheme.typography.bodySmall,
        )
        SafetyPolicyLink()

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Setup guide", style = MaterialTheme.typography.titleSmall)
                Text("1. Install LSPosed with the guided installer and reboot.", style = MaterialTheme.typography.bodySmall)
                Text("2. Enable the RootDeck module in LSPosed and scope it only to your own test apps.", style = MaterialTheme.typography.bodySmall)
                Text("3. Pick a video file here, choose Front or Back camera, then run your test app.", style = MaterialTheme.typography.bodySmall)
                StatusRow("LSPosed Manager", if (lsposedStatus.managerInstalled) "Detected" else "Open LSPosed after reboot")
                StatusRow("RootDeck module entry", if (lsposedStatus.moduleEntryPackaged) "Packaged" else "Missing")
                StatusRow("Video file", if (selectedVideoUri != null) "Selected" else "Choose Gallery or Files")
                StatusRow("Camera target", targetCamera)
                StatusRow("Fit mode", fitMode.label)
                StatusRow("Crop / bars", fitMode.editingSummary(cropAnchor))
                StatusRow("Output size", outputSize.label)
                StatusRow("Repeat playback", if (repeatPlayback) "Always on" else "Off")
                StatusRow("Editor setup", if (setupSaved) "Saved for LSPosed test flow" else "Review and save below")
            }
        }

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
                Text("2. Choose Front or Back Camera", style = MaterialTheme.typography.titleSmall)
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
                    "RootDeck will prepare the selected video for: $targetCamera",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.AspectRatio, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("3. Fit Video to Camera Size", style = MaterialTheme.typography.titleSmall)
                }
                Text(
                    "Choose how RootDeck should prepare videos that do not match the camera shape. RootDeck saves this for the LSPosed video feed test flow in your own scoped test apps.",
                    style = MaterialTheme.typography.bodySmall,
                )
                AssistChip(onClick = {}, label = { Text("Repeat video playback: always on") })
                Text("Resize mode", style = MaterialTheme.typography.labelMedium)
                VideoFitMode.values().forEach { mode ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = fitMode == mode, onClick = { fitMode = mode })
                        Column {
                            Text(mode.label, style = MaterialTheme.typography.bodyMedium)
                            Text(mode.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                if (fitMode == VideoFitMode.FillCrop) {
                    Text("Crop position", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        VideoCropAnchor.values().forEach { anchor ->
                            FilterChip(
                                selected = cropAnchor == anchor,
                                onClick = { cropAnchor = anchor },
                                label = { Text(anchor.label) },
                            )
                        }
                    }
                    Text(
                        "Choose what stays visible when the video is larger than the camera frame after filling it.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Divider()
                Text("Camera output size", style = MaterialTheme.typography.labelMedium)
                CameraOutputSize.values().forEach { size ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = outputSize == size, onClick = { outputSize = size })
                        Column {
                            Text(size.label, style = MaterialTheme.typography.bodyMedium)
                            Text(size.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Preview preparation", style = MaterialTheme.typography.labelMedium)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(132.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center,
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Outlined.AspectRatio, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Text(fitMode.previewHeadline(cropAnchor), style = MaterialTheme.typography.bodyMedium)
                                Text(outputSize.label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Text("$targetCamera · ${fitMode.label} · ${outputSize.label}", style = MaterialTheme.typography.bodySmall)
                        Text(fitMode.previewText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Saved setup: ${fitMode.editingSummary(cropAnchor)}", style = MaterialTheme.typography.labelSmall)
                        Text("Looping: selected videos are saved with repeat playback enabled, so the feed restarts automatically when the file ends.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(
                            onClick = { saveVideoFeedSetup() },
                            enabled = selectedVideoUri != null,
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Text(if (setupSaved) "Video Feed Setup Saved" else "Save Video Feed Setup")
                        }
                        if (selectedVideoUri == null) {
                            Text(
                                "Select a video first, then save these crop/resize settings for the LSPosed test flow.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }


        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.PlayArrow, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("4. Start First Test Run", style = MaterialTheme.typography.titleSmall)
                }
                Text(
                    "This prepares the saved repeat-video setup for an app you own or control. Enable RootDeck in LSPosed and scope it only to that test app before opening the app's camera screen.",
                    style = MaterialTheme.typography.bodySmall,
                )
                StatusRow("Video setup", if (selectedVideoUri != null && setupSaved) "Saved" else "Select video and save setup first")
                StatusRow("Repeat playback", "Always on")
                StatusRow("LSPosed scope", "Enable RootDeck only for your own test app")
                StatusRow("Test run", testRunStatus)
                Button(
                    enabled = selectedVideoUri != null,
                    onClick = { startFirstTestRun() },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text("Prepare First Test Run")
                }
                Text(
                    "If a normal Camera app still shows the real lens, that means it is not an approved scoped test target or the replacement pipeline is not active for that app. Use a controlled test app for the first run.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }


        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Movie, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("5. Internal Test Camera Preview", style = MaterialTheme.typography.titleSmall)
                }
                Text(
                    "Controlled RootDeck-only test harness. This does not spoof another app. It plays your selected video as a local test feed and loops it automatically so you can verify source, repeat playback, and framing before LSPosed scoped testing.",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (selectedVideoUri != null) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        factory = { viewContext ->
                            VideoView(viewContext).apply {
                                setVideoURI(selectedVideoUri)
                                setOnPreparedListener { player ->
                                    player.isLooping = true
                                    start()
                                }
                                setOnCompletionListener { start() }
                            }
                        },
                        update = { videoView ->
                            videoView.setVideoURI(selectedVideoUri)
                            videoView.setOnPreparedListener { player ->
                                player.isLooping = true
                                videoView.start()
                            }
                            videoView.setOnCompletionListener { videoView.start() }
                        },
                    )
                    StatusRow("Internal preview", "Playing selected video on repeat")
                    StatusRow("LSPosed spoofing", "Not performed in RootDeck preview")
                } else {
                    Text(
                        "Select a video first to start the internal test camera preview.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.VerifiedUser, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(8.dp))
                    Text("6. LSPosed Test Readiness", style = MaterialTheme.typography.titleSmall)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Use this checklist after installing LSPosed. RootDeck prepares the video source and repeat setting, but you still enable and scope the module in LSPosed Manager. If a normal Camera app still shows the real lens, the current LSPosed module path is not actively replacing that app's camera stream yet; test only inside apps you own or control.",
                    style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(12.dp))
                StatusRow("LSPosed Manager", if (lsposedStatus.managerInstalled) "Detected" else "Not detected")
                StatusRow("Video module entry", if (lsposedStatus.moduleEntryPackaged) "Packaged" else "Missing")
                StatusRow("Selected video", selectedVideoLabel ?: "Not selected")
                StatusRow("Target camera", targetCamera)
                StatusRow("Fit mode", fitMode.label)
                StatusRow("Crop / bars", fitMode.editingSummary(cropAnchor))
                StatusRow("Output size", outputSize.label)
                StatusRow("Repeat playback", if (repeatPlayback) "Always on — video restarts when it ends" else "Off")
                StatusRow("Editor setup", if (setupSaved) "Saved" else "Not saved")
                StatusRow("Ready to test", if (lsposedStatus.managerInstalled && selectedVideoUri != null && setupSaved) "Yes — prepare first run, then open your scoped test app" else "Not yet")
                Spacer(Modifier.height(12.dp))
                OutlinedButton(onClick = { lsposedStatus = readLsposedStatus(context) }) {
                    Text("Refresh LSPosed Status")
                }
                if (!lsposedStatus.managerInstalled) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "LSPosed Manager was not detected yet. If you just installed LSPosed, reboot first, then open LSPosed Manager and enable RootDeck for your own test apps.",
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

private enum class VideoFitMode(
    val label: String,
    val description: String,
    val previewText: String,
) {
    FitBlackBars(
        "Fit with black bars",
        "Shows the whole video and adds black bars if the shape is different.",
        "Best when you do not want to crop anything from the original video.",
    ),
    FillCrop(
        "Fill and crop edges",
        "Fills the camera frame and crops the extra edges if needed.",
        "Best when you want the video to look full-screen in the camera frame.",
    ),
    Stretch(
        "Stretch to camera",
        "Resizes the video to the camera shape even if it changes proportions.",
        "Best only for quick tests where exact proportions do not matter.",
    ),
    MatchCamera(
        "Match original camera output",
        "Uses the chosen camera output size and preserves the camera feed shape.",
        "Best default for camera compatibility testing.",
    );

    fun editingSummary(anchor: VideoCropAnchor): String = when (this) {
        FitBlackBars -> "Preserve all video with black bars when needed"
        FillCrop -> "Fill frame and keep ${anchor.label.lowercase()} crop area"
        Stretch -> "Stretch video to exact camera frame"
        MatchCamera -> "Resize to the camera output shape"
    }

    fun previewHeadline(anchor: VideoCropAnchor): String = when (this) {
        FitBlackBars -> "Full video + black bars"
        FillCrop -> "Filled frame · ${anchor.label} crop"
        Stretch -> "Stretched to frame"
        MatchCamera -> "Matched to camera output"
    }
}

private enum class VideoCropAnchor(val label: String) {
    Center("Center"),
    Top("Top"),
    Bottom("Bottom"),
}

private enum class CameraOutputSize(
    val label: String,
    val description: String,
) {
    MatchCamera("Auto: match camera", "Use the size reported by the selected front/back camera."),
    Hd("HD 720p", "Prepare a 1280 × 720 landscape feed."),
    FullHd("Full HD 1080p", "Prepare a 1920 × 1080 landscape feed."),
    Square("Square", "Prepare a 1:1 feed for square video tests."),
    Portrait("Portrait 9:16", "Prepare a vertical feed for portrait video tests."),
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
