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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Cameraswitch
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

private const val SANDBOX_VIDEO_FEED_PREFS = "rootdeck_video_feed_setup"

@Composable
fun SandboxCameraScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedMode by remember { mutableStateOf(SandboxMode.RealCamera) }
    var lensFacing by remember { mutableStateOf(CameraCharacteristics.LENS_FACING_FRONT) }
    var hasCameraPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        hasCameraPermission = granted
    }
    val prefs = remember { context.getSharedPreferences(SANDBOX_VIDEO_FEED_PREFS, Context.MODE_PRIVATE) }
    val savedVideoUri = remember { prefs.getString("video_uri", null)?.let(Uri::parse) }
    val repeatPlayback = prefs.getBoolean("repeat_playback", true)
    val targetCamera = prefs.getString("target_camera", "Front Camera") ?: "Front Camera"
    val fitMode = prefs.getString("fit_mode", "FitBlackBars") ?: "FitBlackBars"
    val outputSize = prefs.getString("output_size", "MatchCamera") ?: "MatchCamera"

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        TextButton(onClick = onBack) { Text("← Back to Basic Tools") }
        Text("Sandbox Camera App", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Internal RootDeck-only camera sandbox. Normal mode uses the real device camera. Test-feed mode plays the saved Video Test Feed video on repeat so you can verify the controlled replacement flow without targeting external apps.",
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
                        Icon(Icons.Outlined.Cameraswitch, contentDescription = null, modifier = Modifier.size(18.dp))
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
                            modifier = Modifier.fillMaxWidth().height(320.dp),
                            factory = { viewContext -> TextureView(viewContext).also { texture -> startSandboxCamera(viewContext, texture, lensFacing) } },
                            update = { texture -> startSandboxCamera(texture.context, texture, lensFacing) },
                        )
                    }
                } else {
                    if (savedVideoUri == null) {
                        Text("No saved Video Test Feed source yet. Go to Video Test Feed, select a video, then tap Save Video Feed Setup.", style = MaterialTheme.typography.bodySmall)
                    } else {
                        AndroidView(
                            modifier = Modifier.fillMaxWidth().height(320.dp),
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
                StatusLine("Scope", "RootDeck internal sandbox only")
            }
        }
    }
}

private enum class SandboxMode { RealCamera, TestFeed }

@Composable
private fun StatusLine(label: String, value: String) {
    Row(Modifier.fillMaxWidth()) {
        Text(label, modifier = Modifier.weight(0.42f), style = MaterialTheme.typography.labelMedium)
        Text(value, modifier = Modifier.weight(0.58f), style = MaterialTheme.typography.bodySmall)
    }
}

private fun startSandboxCamera(context: Context, textureView: TextureView, lensFacing: Int) {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return
    if (!textureView.isAvailable) {
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
                openSandboxCamera(context, textureView, lensFacing)
            }
            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) = Unit
            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true
            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
        }
    } else {
        openSandboxCamera(context, textureView, lensFacing)
    }
}

private fun openSandboxCamera(context: Context, textureView: TextureView, lensFacing: Int) {
    val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val cameraId = manager.cameraIdList.firstOrNull { id ->
        manager.getCameraCharacteristics(id).get(CameraCharacteristics.LENS_FACING) == lensFacing
    } ?: return
    val surfaceTexture = textureView.surfaceTexture ?: return
    surfaceTexture.setDefaultBufferSize(1280, 720)
    val surface = Surface(surfaceTexture)
    try {
        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(camera: CameraDevice) {
                val request = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
                    addTarget(surface)
                    set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO)
                }
                camera.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
                    override fun onConfigured(session: CameraCaptureSession) {
                        session.setRepeatingRequest(request.build(), null, null)
                    }
                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        camera.close()
                    }
                }, null)
            }
            override fun onDisconnected(camera: CameraDevice) { camera.close() }
            override fun onError(camera: CameraDevice, error: Int) { camera.close() }
        }, null)
    } catch (_: SecurityException) {
        // Permission revoked while opening camera.
    }
}
