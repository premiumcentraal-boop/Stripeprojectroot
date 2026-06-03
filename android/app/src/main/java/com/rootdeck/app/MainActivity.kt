package com.rootdeck.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel

/** Top-level destinations + the tool sub-routes. */
private sealed class Dest(val title: String) {
    object Dashboard : Dest("Dashboard")
    object Tools : Dest("Tools")
    object Logs : Dest("Logs")
    object Settings : Dest("Settings")
    object RootManagement : Dest("Root Management")
    object MemoryMonitoring : Dest("Memory Monitoring")
    object ScheduledReboot : Dest("Scheduled Reboot")
    object ProcessPrivacy : Dest("Process Privacy Guard")
    object Doppelganger : Dest("Doppelganger")
    object VideoTestFeed : Dest("Video Test Feed")
    object SandboxCamera : Dest("Sandbox Camera")
    object LsposedInstaller : Dest("LSPosed Installer")
    data class Planned(val tool: BasicTool) : Dest(tool.title)
    data class PlannedModule(val tool: PlannedBasicTool) : Dest(tool.safeName)
}

private data class Tab(val dest: Dest, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab(Dest.Dashboard, "Dashboard", Icons.Outlined.Dashboard),
    Tab(Dest.Tools, "Tools", Icons.Outlined.Build),
    Tab(Dest.Logs, "Logs", Icons.Outlined.Description),
    Tab(Dest.Settings, "Settings", Icons.Outlined.Settings),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val repo: RootRepository = viewModel(factory = RootRepository.factory(application))
            val scope = rememberCoroutineScope()
            val privacy = remember(repo) { ProcessPrivacyRepository(applicationContext, repo, scope) }
            val doppel = remember(repo) { DoppelgangerRepository(repo, scope) }
            val scheme = when (repo.theme.value) {
                ThemeMode.Black -> darkColorScheme(background = Color.Black, surface = Color.Black)
                ThemeMode.Dark -> darkColorScheme()
                ThemeMode.System -> if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()
            }
            MaterialTheme(colorScheme = scheme) {
                RootDeckApp(repo, privacy, doppel)
            }
        }
    }
}

@Composable
private fun RootDeckApp(
    repo: RootRepository,
    privacy: ProcessPrivacyRepository,
    doppel: DoppelgangerRepository,
) {
    // Top-level tab selection + the current sub-screen pushed on top of Tools.
    var tab by remember { mutableStateOf<Dest>(Dest.Dashboard) }
    var subscreen by remember { mutableStateOf<Dest?>(null) }
    val context = LocalContext.current
    val setupPrefs = remember { context.getSharedPreferences("rootdeck_startup_setup", android.content.Context.MODE_PRIVATE) }
    var setupFinishedForSession by remember {
        mutableStateOf(setupPrefs.getBoolean("completed_or_skipped", false) || LsposedInstaller.shouldSkipStartupSetup(context))
    }

    fun navigateTool(tool: BasicTool) {
        subscreen = when (tool.id) {
            "root" -> Dest.RootManagement
            "memory" -> Dest.MemoryMonitoring
            "reboot" -> Dest.ScheduledReboot
            "privacy" -> Dest.ProcessPrivacy
            "doppelganger" -> Dest.Doppelganger
            "video-injection" -> Dest.VideoTestFeed
            "sandbox-camera" -> Dest.SandboxCamera
            "lsposed-installer" -> Dest.LsposedInstaller
            else -> Dest.Planned(tool)
        }
    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabs.forEach { t ->
                    NavigationBarItem(
                        selected = tab == t.dest && subscreen == null,
                        onClick = {
                            tab = t.dest
                            subscreen = null
                        },
                        icon = { Icon(t.icon, contentDescription = t.label) },
                        label = { Text(t.label) },
                    )
                }
            }
        },
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (!setupFinishedForSession) {
                StartupSetupScreen(
                    repo = repo,
                    onFinish = {
                        setupPrefs.edit().putBoolean("completed_or_skipped", true).apply()
                        setupFinishedForSession = true
                    },
                    onOpenVideoTestFeed = {
                        setupPrefs.edit().putBoolean("completed_or_skipped", true).apply()
                        setupFinishedForSession = true
                        tab = Dest.Tools
                        subscreen = Dest.VideoTestFeed
                    },
                )
            } else {
                val current = subscreen ?: tab
                when (current) {
                Dest.Dashboard -> DashboardScreen(
                    repo,
                    privacy,
                    onOpenRootManagement = {
                        tab = Dest.Tools
                        subscreen = Dest.RootManagement
                    },
                    onOpenProcessPrivacy = {
                        tab = Dest.Tools
                        subscreen = Dest.ProcessPrivacy
                    },
                    onOpenBasicTools = {
                        tab = Dest.Tools
                        subscreen = null
                    },
                )
                Dest.Tools -> BasicToolsScreen(
                    onPick = ::navigateTool,
                    onPickPlanned = { planned -> subscreen = Dest.PlannedModule(planned) },
                )
                Dest.Logs -> LogsScreen(repo)
                Dest.Settings -> SettingsScreen(repo)
                Dest.RootManagement -> RootManagementScreen(
                    repo,
                    onOpenLogs = {
                        tab = Dest.Logs
                        subscreen = null
                    },
                )
                Dest.MemoryMonitoring -> MemoryMonitoringScreen(repo, onBack = { subscreen = null })
                Dest.ScheduledReboot -> ScheduledRebootScreen(repo, onBack = { subscreen = null })
                Dest.ProcessPrivacy -> ProcessPrivacyScreen(repo, privacy, onBack = { subscreen = null })
                Dest.Doppelganger -> DoppelgangerScreen(repo, doppel, onBack = { subscreen = null })
                Dest.VideoTestFeed -> VideoTestFeedScreen(onBack = { subscreen = null })
                Dest.SandboxCamera -> SandboxCameraScreen(onBack = { subscreen = null })
                Dest.LsposedInstaller -> LsposedInstallerScreen(repo, onBack = { subscreen = null })
                is Dest.Planned -> PlannedToolScreen(current.tool, onBack = { subscreen = null })
                    is Dest.PlannedModule -> PlannedBasicToolDetailScreen(current.tool, onBack = { subscreen = null })
                }
            }
        }
    }
}
