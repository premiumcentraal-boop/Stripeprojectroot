package com.rootdeck.app

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class PlannedToolStatus { Planned, Restricted, ResearchOnly }

data class PlannedBasicTool(
    val id: String,
    val referenceName: String,
    val safeName: String,
    val subtitle: String,
    val status: PlannedToolStatus,
    val addedIn: String? = null,
    val icon: ImageVector,
)

val plannedBasicTools: List<PlannedBasicTool> = listOf(
    PlannedBasicTool(
        id = "magisk-mask",
        referenceName = "Magisk Mask",
        safeName = "Magisk Manager Integration",
        subtitle = "Open and inspect your installed Magisk root manager",
        status = PlannedToolStatus.Restricted,
        addedIn = "v0.3.0",
        icon = Icons.Outlined.Shield,
    ),
    PlannedBasicTool(
        id = "lsposed",
        referenceName = "LSPosed",
        safeName = "LSPosed Module Center",
        subtitle = "Planned compatibility page for user-installed LSPosed modules",
        status = PlannedToolStatus.Restricted,
        addedIn = "v0.3.0",
        icon = Icons.Outlined.Extension,
    ),
    PlannedBasicTool(
        id = "gps-mock",
        referenceName = "GPS Mock",
        safeName = "Mock Location Lab",
        subtitle = "Developer mock location controls for testing",
        status = PlannedToolStatus.Planned,
        addedIn = "v0.3.0",
        icon = Icons.Outlined.MyLocation,
    ),
    PlannedBasicTool(
        id = "image-injection",
        referenceName = "Image Injection",
        safeName = "Image Test Feed",
        subtitle = "Planned static image feed for controlled testing",
        status = PlannedToolStatus.ResearchOnly,
        addedIn = "v0.3.0",
        icon = Icons.Outlined.Image,
    ),
    PlannedBasicTool(
        id = "camera-injection",
        referenceName = "Camera Injection Configuration",
        safeName = "Live Camera Spoof Studio",
        subtitle = "Planned front/back virtual camera feed for streaming workflows",
        status = PlannedToolStatus.ResearchOnly,
        addedIn = "v0.3.0",
        icon = Icons.Outlined.Videocam,
    ),
)

fun plannedToolById(id: String): PlannedBasicTool? = plannedBasicTools.firstOrNull { it.id == id }
