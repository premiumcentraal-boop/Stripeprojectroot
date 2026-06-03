plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.rootdeck.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rootdeck.app"
        minSdk = 34
        targetSdk = 34
        versionCode = 8
        versionName = "0.8.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.02")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.8.1")

    // Xposed/LSPosed API for Doppelganger module
    compileOnly("de.robv.android.xposed:api:82")

    debugImplementation("androidx.compose.ui:ui-tooling")
}

tasks.register("fixXposedInit") {
    doLast {
        val xposedInitFile = file("src/main/assets/xposed_init")
        if (xposedInitFile.isDirectory) {
            xposedInitFile.deleteRecursively()
        }
        xposedInitFile.writeText("com.rootdeck.app.xposed.DoppelgangerXposedModule\ncom.rootdeck.app.xposed.VideoInjectionXposedModule\n")
    }
}

tasks.named("preBuild") {
    dependsOn("fixXposedInit")
}
