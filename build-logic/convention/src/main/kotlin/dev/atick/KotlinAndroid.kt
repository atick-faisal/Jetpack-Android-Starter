/*
 * Copyright 2026 Atick Faisal
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

package dev.atick

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

/**
 * Configures the SDK levels, Java compatibility and Kotlin compiler options shared by every
 * Android module in the project.
 *
 * Application-only settings (targetSdk, buildConfig, packaging) stay in
 * `ApplicationConventionPlugin`; Compose-only settings live in [configureAndroidCompose].
 */
internal fun Project.configureKotlinAndroid(
    commonExtension: CommonExtension,
) {
    // AGP 9's base CommonExtension exposes these as properties rather than script blocks;
    // the block form only exists on the concrete Application/Library extensions.
    commonExtension.apply {
        compileSdk = libs.intVersion("compileSdk")
        defaultConfig.minSdk = libs.intVersion("minSdk")

        val javaVersion = JavaVersion.valueOf("VERSION_${libs.version("java")}")
        compileOptions.sourceCompatibility = javaVersion
        compileOptions.targetCompatibility = javaVersion
    }

    configureKotlin<KotlinAndroidProjectExtension>()
}

/**
 * Configures a JVM-only module, such as the lint checks module, to the same Java and Kotlin
 * levels as the Android modules.
 */
internal fun Project.configureKotlinJvm() {
    extensions.configure<JavaPluginExtension> {
        val javaVersion = JavaVersion.valueOf("VERSION_${libs.version("java")}")
        sourceCompatibility = javaVersion
        targetCompatibility = javaVersion
    }

    configureKotlin<KotlinJvmProjectExtension>()
}

/**
 * Shared Kotlin compiler configuration, generic over the Android and JVM extensions so both
 * module types stay on the same JVM target and language flags.
 */
private inline fun <reified T : KotlinBaseExtension> Project.configureKotlin() = configure<T> {
    // Set by .github/ci-gradle.properties so CI can relax this without touching the build.
    val warningsAsErrors = providers.gradleProperty("warningsAsErrors").orNull.toBoolean()

    when (this) {
        is KotlinAndroidProjectExtension -> compilerOptions
        is KotlinJvmProjectExtension -> compilerOptions
        else -> error("Unsupported project extension $this ${T::class}")
    }.apply {
        jvmTarget.set(JvmTarget.fromTarget(libs.version("java")))
        allWarningsAsErrors.set(warningsAsErrors)
        // -Xannotation-default-target=param-property and -Xcontext-parameters used to be set
        // here. Both are the default from Kotlin 2.4 and the compiler now warns that they are
        // redundant, so they were dropped when this config was centralised.
    }
}
