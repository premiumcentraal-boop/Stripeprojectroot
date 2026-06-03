package com.rootdeck.app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.net.Uri
import android.view.Surface
import android.view.TextureView
import android.widget.VideoView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

private const val SANDBOX_VIDEO_FEED_PREFS = "rootdeck_video_feed_setup"

@Composable
fun SandboxCameraScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(SANDBOX_VIDEO_FEED_PREFS, Context.MODE_PRIVATE) }
    val initialMode = remember {
        if (prefs.getBoolean("video_injection_enabled", false) || prefs.getBoolean("test_run_requested", false) || prefs.getString("sandbox_mode", null) == "TestFeed") SandboxMode.TestFeed else SandboxMode.RealCamera
    }
    val initialLens = remember {
        if ((prefs.getString("target_camera", "Front Camera") ?: "Front Camera").contains("Back", ignoreCase = true)) CameraCharacteristics.LENS_FACING_BACK else CameraCharacteristics.LENS_FACING_FRONT
    }
    var selectedMode by remember { mutableStateOf(initialMode) }
    var lensFacing by remember { mutableStateOf(initialLens) }
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }
    val cameraController = remember { SandboxCameraController() }
    DisposableEffect(lensFacing, selectedMode) {
        onDispose { cameraController.close() }
    }
    val savedVideoUri = remember { prefs.getString("video_uri", null)?.let(Uri::parse) }
    val repeatPlayback = prefs.getBoolean("repeat_playback", true)
    val targetCamera = prefs.getString("target_camera", "Front Camera") ?: "Front Camera"
    val fitMode = prefs.getString("fit_mode", "FitBlackBars") ?: "FitBlackBars"
    val outputSize = prefs.getString("output_size", "MatchCamera") ?: "MatchCamera"

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("← Back to Basic Tools") }
        Text("Sandbox Camera App", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Internal RootDeck-only camera sandbox. Normal mode uses the real device camera. Test-feed mode is now directly connected to Video Test Feed and plays the selected video on repeat through the in-app camera test surface. Enable RootDeck in Vector / LSPosed for RootDeck only.",
            style = MaterialTheme.typography.bodySmall,
        )
        SafetyPolicyLink()

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Sandbox controls", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedMode == SandboxMode.RealCamera,
                        onClick = { selectedMode = SandboxMode.RealCamera },
                        label = { Text("Real camera") },
                        leadingIcon = { Icon(Icons.Outlined.Videocam, contentDescription = null) },
                    )
                    FilterChip(
                        selected = selectedMode == SandboxMode.TestFeed,
                        onClick = { selectedMode = SandboxMode.TestFeed },
                        label = { Text("Test video feed") },
                        leadingIcon = { Icon(Icons.Outlined.Movie, contentDescription = null) },
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    AssistChip(onClick = {}, label = { Text(if (lensFacing == CameraCharacteristics.LENS_FACING_FRONT) "Front camera" else "Back camera") })
                    OutlinedButton(onClick = {
                        lensFacing = if (lensFacing == CameraCharacteristics.LENS_FACING_FRONT) CameraCharacteristics.LENS_FACING_BACK else CameraCharacteristics.LENS_FACING_FRONT
                    }) {
                        Icon(Icons.Outlined.Videocam, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Switch")
                    }
                }
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (selectedMode == SandboxMode.RealCamera) "Normal camera preview" else "Controlled test video preview", style = MaterialTheme.typography.titleSmall)
                if (selectedMode == SandboxMode.RealCamera) {
                    if (!hasCameraPermission) {
                        Text("Camera permission is needed to show the normal front/back camera inside this sandbox.", style = MaterialTheme.typography.bodySmall)
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) { Text("Allow Camera Permission") }
                    } else {
                        AndroidView(
                            modifier = Modifier.fillMaxWidth().aspectRatio(9f / 16f),
                            factory = { viewContext -> TextureView(viewContext).also { texture -> startSandboxCamera(viewContext, texture, lensFacing, cameraController) } },
                            update = { texture -> startSandboxCamera(texture.context, texture, lensFacing, cameraController) },
                        )
                    }
                } else {
                    if (savedVideoUri == null) {
                        Text("No saved Video Test Feed source yet. Go to Video Test Feed, select a video, then tap Save Video Feed Setup.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        AndroidView(
                            modifier = Modifier.fillMaxWidth().aspectRatio(9f / 16f),
                            factory = { viewContext ->
                                VideoView(viewContext).apply {
                                    setVideoURI(savedVideoUri)
                                    setOnPreparedListener { player ->
                                        player.isLooping = true
                                        start()
                                    }
                                    setOnCompletionListener { start() }
                                }
                            },
                            update = { videoView ->
                                videoView.setVideoURI(savedVideoUri)
                                videoView.setOnPreparedListener { player ->
                                    player.isLooping = true
                                    videoView.start()
                                }
                                videoView.setOnCompletionListener { videoView.start() }
                            },
                        )
                    }
                }
            }
        }

        ElevatedCard(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Saved Video Test Feed config", style = MaterialTheme.typography.titleSmall)
                StatusLine("Saved video", savedVideoUri?.lastPathSegment ?: "Not selected")
                StatusLine("Target camera", targetCamera)
                StatusLine("Fit mode", fitMode)
                StatusLine("Output size", outputSize)
                StatusLine("Repeat playback", if (repeatPlayback) "Always on" else "Off")
                val realConnected = hasCameraPermission
                val sandboxConnected = prefs.getBoolean("video_injection_enabled", false)
                StatusLine("Vector / LSPosed scope", "Enable RootDeck package only")
                StatusLine("Real camera injection", if (realConnected) "Connected to in-app phone camera" else "Waiting for camera permission")
                SandboxConnectionDot("Real camera", realConnected)
                StatusLine("Sandbox video injection", if (sandboxConnected) "Connected to Test video feed" else "Not connected yet")
                SandboxConnectionDot("Sandbox test feed", sandboxConnected)
                StatusLine("Scope", "RootDeck internal sandbox only")
            }
        }
    }
}

enum class SandboxMode { RealCamera, TestFeed }

@Composable
private fun SandboxConnectionDot(label: String, connected: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(if (connected) Color(0xFF22C55E) else MaterialTheme.colorScheme.outline, CircleShape),
        )
        Spacer(Modifier.width(8.dp))
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium)
        Text(if (connected) "Connected" else "Waiting", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun StatusLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(0.42f), style = MaterialTheme.typography.labelMedium)
        Text(value, modifier = Modifier.weight(0.58f), style = MaterialTheme.typography.bodySmall)
    }
}

fun startSandboxCamera(context: Context, textureView: TextureView, lensFacing: Int, controller: SandboxCameraController) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return
    if (!textureView.isAvailable) {
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                openSandboxCamera(context, textureView, lensFacing, controller)
            }
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) = Unit
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
        }
    } else {
        openSandboxCamera(context, textureView, lensFacing, controller)
    }
}

private fun openSandboxCamera(context: Context, textureView: TextureView, lensFacing: Int, controller: SandboxCameraController) {
    val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val cameraId = manager.cameraIdList.firstOrNull { id ->
        manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == lensFacing
    } ?: return
    controller.close()
    val surfaceTexture = textureView.surfaceTexture ?: return
    surfaceTexture.setDefaultBufferSize(720, 1280)
    val surface = Surface(surfaceTexture)
    try {
        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                controller.camera = camera
                val request = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                    addTarget(surface)
                    set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
                }
                camera.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        controller.session = session
                        session.setRepeatingRequest(request.build(), null, null)
                    }
                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        controller.close()
                    }
                }, null)
            }
            override fun onDisconnected(camera: CameraDevice) { controller.close() }
            override fun onError(camera: CameraDevice, error: Int) { controller.close() }
        }, null)
    } catch (_: SecurityException) {
        // Permission revoked while opening camera.
    }
}


class SandboxCameraController {
    var camera: CameraDevice? = null
    var session: CameraCaptureSession? = null

    fun close() {
        runCatching { session?.stopRepeating() }
        runCatching { session?.close() }
        runCatching { camera?.close() }
        session = null
        camera = null
    }
}
