package com.rootdeck.app.xposed

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam

class DoppelgangerXposedModule : IXposedHookLoadPackage {

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        // Only hook specific target apps or system frameworks as needed.
        // We log the loading to verify the module is active.
        XposedBridge.log("RootDeck Doppelganger Module loading in: ${lpparam.packageName}")

        try {
            hookPackageManager(lpparam)
            hookUserManager(lpparam)
            hookAndroidId(lpparam)
            hookAttestation(lpparam)
        } catch (t: Throwable) {
            XposedBridge.log("RootDeck Doppelganger Module Error: $t")
        }
    }

    private fun hookPackageManager(lpparam: LoadPackageParam) {
        // Example: Hook PackageManager to isolate apps inside the Doppelganger profile.
        // E.g., hiding other cloned apps from the current app's view.
        try {
            val pmClass = XposedHelpers.findClass("android.app.ApplicationPackageManager", lpparam.classLoader)
            XposedHelpers.findAndHookMethod(
                pmClass,
                "getInstalledPackages",
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        // Logic to filter the installed packages list for isolation
                        // XposedBridge.log("PackageManager.getInstalledPackages hooked")
                    }
                }
            )
        } catch (e: Exception) {
            // Ignore if not found
        }
    }

    private fun hookUserManager(lpparam: LoadPackageParam) {
        // Example: Hook UserManager to spoof user profiles or hide the fact that
        // it's running in a secondary clone user.
        try {
            val umClass = XposedHelpers.findClass("android.os.UserManager", lpparam.classLoader)
            XposedHelpers.findAndHookMethod(
                umClass,
                "isSystemUser",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        // Some apps refuse to run in secondary users. This bypasses that check.
                        // param.result = true
                    }
                }
            )
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun hookAndroidId(lpparam: LoadPackageParam) {
        // Example: Hook Settings.Secure to provide a unique ANDROID_ID for the clone,
        // so it appears as a separate device or instance.
        try {
            val secureClass = XposedHelpers.findClass("android.provider.Settings.Secure", lpparam.classLoader)
            XposedHelpers.findAndHookMethod(
                secureClass,
                "getString",
                android.content.ContentResolver::class.java,
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val name = param.args[1] as? String
                        if (name == "android_id") {
                            // param.result = "custom_android_id_for_clone"
                        }
                    }
                }
            )
        } catch (e: Exception) {
            // Ignore
        }
    }

    private fun hookAttestation(lpparam: LoadPackageParam) {
        // Example: Bypass hardware attestation or Play Integrity checks
        // for legitimate ethical usage like social media streaming.
        try {
            // Commonly hooked paths for KeyStore attestation bypassing
            val keyStoreClass = XposedHelpers.findClass("java.security.KeyStore", lpparam.classLoader)
            XposedBridge.hookAllMethods(keyStoreClass, "getCertificateChain", object : XC_MethodHook() {
                override fun afterHookedMethod(param: MethodHookParam) {
                    // Logic to manipulate the attestation certificate chain
                }
            })
        } catch (e: Exception) {
            // Ignore
        }
    }
}
