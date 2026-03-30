/*
 * Copyright 2023 Atick Faisal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("UnstableApiUsage")

import java.io.FileInputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Properties

val keystorePropertiesFile: File = rootProject.file("keystore.properties")
val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy_MM_dd_hh_mm_a")
val currentTime: String = LocalDateTime.now().format(formatter)

plugins {
    alias(libs.plugins.jetpack.application)
    alias(libs.plugins.jetpack.dagger.hilt)
    alias(libs.plugins.jetpack.firebase)
    alias(libs.plugins.jetpack.dokka)
}

android {
    // ... Application Version ...
    val majorUpdateVersion = 1
    val minorUpdateVersion = 2
    val patchVersion = 7

    val mVersionCode = majorUpdateVersion.times(10_000)
        .plus(minorUpdateVersion.times(100))
        .plus(patchVersion)

    val mVersionName = "$majorUpdateVersion.$minorUpdateVersion.$patchVersion"

    defaultConfig {
        versionCode = mVersionCode
        versionName = mVersionName
        applicationId = "dev.atick.compose"
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(FileInputStream(keystorePropertiesFile))
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isMinifyEnabled = true
            signingConfig = if (keystorePropertiesFile.exists()) {
                signingConfigs.getByName("release")
            } else {
                println(
                    "keystore.properties file not found. Using debug key. Read more here: " +
                            "https://atick.dev/Jetpack-Android-Starter/github",
                )
                signingConfigs.getByName("debug")

            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    buildFeatures {
        buildConfig = true
    }

    androidResources {
        generateLocaleConfig = true
    }

    namespace = "dev.atick.compose"
}

// TODO: AGP 9 Migration - Custom Output Filename
// FIXME: Implement proper AGP 9 approach for custom APK naming
// Previous behavior: Jetpack_release_v{version}_{timestamp}.apk
// Current: Using default AGP naming scheme
//
// AGP 9 removed direct outputFile manipulation. Recommended approaches:
// 1. Use variant.artifacts.use() with SingleArtifact.APK
// 2. Customize via tasks.named<PackageApplication>("package{Variant}")
//
// References:
// - https://github.com/android/gradle-recipes (variantOutput recipe)
// - https://developer.android.com/build/extend-agp
// Tracking: GitHub Issue #579
androidComponents {
    onVariants { variant ->
        // Placeholder for future custom filename logic
        variant.outputs.forEach { output ->
            output.versionName.set("${variant.outputs.first().versionName.getOrElse("1.0.0")}")
        }
    }
}

dependencies {
    // ... Core
    implementation(project(":core:ui"))
    implementation(project(":core:network"))
    implementation(project(":core:preferences"))

    // ... Features
    implementation(project(":feature:auth"))
    implementation(project(":feature:home"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:settings"))

    // ... Analytics
    implementation(project(":firebase:analytics"))

    // ... Sync
    implementation(project(":sync"))

    // ... Splash Screen
    implementation(libs.androidx.core.splashscreen)

    // ... OSS Licenses
    implementation(libs.google.oss.licenses)

    // ... LeakCanary
    // TODO: Comment out the following line to disable LeakCanary
    debugImplementation(libs.leakcanary.android)
}