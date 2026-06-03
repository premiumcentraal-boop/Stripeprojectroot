package com.rootdeck.app.xposed

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.File

/**
 * RootDeck internal-only Video Test Feed bridge.
 *
 * This module intentionally scopes itself to RootDeck's own package. It connects
 * LSPosed loading, Camera1/Camera2 hook visibility, and the saved Video Test Feed
 * configuration used by the internal Sandbox Camera screen. It does not spoof or
 * replace camera streams in third-party apps.
 */
class VideoInjectionXposedModule : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName != ROOTDECK_PACKAGE) return

        XposedBridge.log("RootDeck Video Test Feed module active in internal sandbox package: ${lpparam.packageName}")
        logSavedVideoFeedConfig(lpparam)

        try {
            hookCamera1(lpparam)
            hookCamera2(lpparam)
        } catch (t: Throwable) {
            XposedBridge.log("RootDeck Video Test Feed module error: $t")
        }
    }

    private fun hookCamera1(lpparam: LoadPackageParam) {
        try {
            val cameraClass = XposedHelpers.findClass("android.hardware.Camera", lpparam.classLoader)

            XposedBridge.hookAllMethods(cameraClass, "open", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    XposedBridge.log("RootDeck sandbox Camera1 open observed; internal video feed config is available to RootDeck only.")
                    logSavedVideoFeedConfig(lpparam)
                }
            })

            XposedBridge.hookAllMethods(cameraClass, "setPreviewCallback", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    XposedBridge.log("RootDeck sandbox Camera1 preview callback observed. External camera replacement is disabled.")
                }
            })

            XposedBridge.hookAllMethods(cameraClass, "setPreviewTexture", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    XposedBridge.log("RootDeck sandbox Camera1 preview texture observed. Use Sandbox Camera test-feed mode for looping video validation.")
                }
            })
        } catch (e: Throwable) {
            XposedBridge.log("RootDeck sandbox Camera1 hooks unavailable: ${e.message}")
        }
    }

    private fun hookCamera2(lpparam: LoadPackageParam) {
        try {
            val cameraManagerClass = XposedHelpers.findClass("android.hardware.camera2.CameraManager", lpparam.classLoader)
            XposedBridge.hookAllMethods(cameraManagerClass, "openCamera", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    XposedBridge.log("RootDeck sandbox Camera2 openCamera observed; scoped internal test config follows.")
                    logSavedVideoFeedConfig(lpparam)
                }
            })

            val cameraDeviceClass = XposedHelpers.findClass("android.hardware.camera2.CameraDevice", lpparam.classLoader)
            XposedBridge.hookAllMethods(cameraDeviceClass, "createCaptureSession", object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    XposedBridge.log("RootDeck sandbox Camera2 capture session observed. External camera replacement is disabled; internal sandbox test-feed mode loops the selected video.")
                }
            })
        } catch (e: Throwable) {
            XposedBridge.log("RootDeck sandbox Camera2 hooks unavailable: ${e.message}")
        }
    }

    private fun logSavedVideoFeedConfig(lpparam: LoadPackageParam) {
        runCatching {
            val prefsFile = File(lpparam.appInfo.dataDir, "shared_prefs/$VIDEO_FEED_PREFS.xml")
            if (!prefsFile.exists()) {
                XposedBridge.log("RootDeck internal Video Test Feed config has not been saved yet.")
                return@runCatching
            }
            val xml = prefsFile.readText()
            val video = xmlPref(xml, "video_uri") ?: "not selected"
            val camera = xmlPref(xml, "target_camera") ?: "Front Camera"
            val fit = xmlPref(xml, "fit_mode") ?: "FitBlackBars"
            val crop = xmlPref(xml, "crop_anchor") ?: "Center"
            val output = xmlPref(xml, "output_size") ?: "MatchCamera"
            val repeat = xmlBool(xml, "repeat_playback", true)
            val requested = xmlBool(xml, "test_run_requested", false)
            XposedBridge.log(
                "RootDeck internal Video Test Feed config: video=$video, camera=$camera, fit=$fit, crop=$crop, output=$output, repeat=$repeat, testRunRequested=$requested"
            )
        }.onFailure { error ->
            XposedBridge.log("RootDeck could not read internal Video Test Feed config yet: ${error.message}")
        }
    }

    private fun xmlPref(xml: String, name: String): String? {
        val escapedName = Regex.escape(name)
        return Regex("<string name=\"$escapedName\">(.*?)</string>", RegexOption.DOT_MATCHES_ALL)
            .find(xml)
            ?.groupValues
            ?.getOrNull(1)
            ?.replace("&amp;", "&")
            ?.replace("&lt;", "<")
            ?.replace("&gt;", ">")
            ?.replace("&quot;", "\"")
            ?.replace("&apos;", "'")
    }

    private fun xmlBool(xml: String, name: String, default: Boolean): Boolean {
        val escapedName = Regex.escape(name)
        return Regex("<boolean name=\"$escapedName\" value=\"(true|false)\" />")
            .find(xml)
            ?.groupValues
            ?.getOrNull(1)
            ?.toBooleanStrictOrNull()
            ?: default
    }


    private companion object {
        const val ROOTDECK_PACKAGE = "com.rootdeck.app"
        const val VIDEO_FEED_PREFS = "rootdeck_video_feed_setup"
    }
}
