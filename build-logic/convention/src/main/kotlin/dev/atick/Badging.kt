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

import com.android.build.api.artifact.SingleArtifact
import com.android.build.api.variant.Aapt2
import com.android.build.api.variant.ApplicationAndroidComponentsExtension
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.register
import org.gradle.language.base.plugins.LifecycleBasePlugin
import org.gradle.process.ExecOperations
import javax.inject.Inject

/**
 * Dumps `aapt2 badging` for a built APK: the package name, version, minSdk and targetSdk,
 * permissions, supported screens and densities, and the launcher activity.
 */
@CacheableTask
abstract class GenerateBadgingTask : DefaultTask() {

    @get:OutputFile
    abstract val badging: RegularFileProperty

    @get:PathSensitive(PathSensitivity.NONE)
    @get:InputFile
    abstract val apk: RegularFileProperty

    @get:PathSensitive(PathSensitivity.NONE)
    @get:InputFile
    abstract val aapt2Executable: RegularFileProperty

    @get:Inject
    abstract val execOperations: ExecOperations

    @TaskAction
    fun taskAction() {
        badging.get().asFile.outputStream().use { output ->
            execOperations.exec {
                commandLine(
                    aapt2Executable.get().asFile.absolutePath,
                    "dump",
                    "badging",
                    apk.get().asFile.absolutePath,
                )
                standardOutput = output
            }
        }
    }
}

/**
 * Writes the generated badging over the committed golden file.
 *
 * A plain task rather than a `Copy` into the project directory: `Copy` would declare the whole
 * module directory as its output, which overlaps the inputs of other tasks in `:app` and makes
 * Gradle reject the build. This declares only the single file it actually writes.
 */
@CacheableTask
abstract class UpdateBadgingTask : DefaultTask() {

    @get:PathSensitive(PathSensitivity.NONE)
    @get:InputFile
    abstract val generatedBadging: RegularFileProperty

    @get:OutputFile
    abstract val goldenBadging: RegularFileProperty

    @TaskAction
    fun taskAction() {
        generatedBadging.get().asFile.copyTo(goldenBadging.get().asFile, overwrite = true)
    }
}

/**
 * Fails the build when the generated badging differs from the committed golden file.
 *
 * This catches a permission, SDK level or density change that arrived through a transitive
 * dependency rather than through a manifest edit — the kind of change nothing else in the build
 * would surface. It matters more for a template than for an app, because adopters inherit the
 * manifest as their starting point.
 */
@CacheableTask
abstract class CheckBadgingTask : DefaultTask() {

    // A task with no declared output is never considered up to date, so declare one even
    // though nothing reads it.
    @get:OutputDirectory
    abstract val output: DirectoryProperty

    @get:PathSensitive(PathSensitivity.NONE)
    @get:InputFile
    abstract val goldenBadging: RegularFileProperty

    @get:PathSensitive(PathSensitivity.NONE)
    @get:InputFile
    abstract val generatedBadging: RegularFileProperty

    @get:Input
    abstract val updateBadgingTaskName: Property<String>

    override fun getGroup(): String = LifecycleBasePlugin.VERIFICATION_GROUP

    @TaskAction
    fun taskAction() {
        val golden = goldenBadging.get().asFile.readText()
        val generated = generatedBadging.get().asFile.readText()

        if (golden != generated) {
            throw GradleException(
                buildString {
                    appendLine("APK badging does not match the golden file.")
                    appendLine()
                    appendLine("If this change is intended, run:")
                    appendLine("    ./gradlew ${updateBadgingTaskName.get()}")
                    appendLine()
                    appendLine("Expected:")
                    appendLine(golden.prependIndent("    "))
                    appendLine("Actual:")
                    appendLine(generated.prependIndent("    "))
                },
            )
        }
    }
}

/**
 * Registers `generate`, `update` and `check` badging tasks for every application variant.
 */
internal fun Project.configureBadgingTasks(
    componentsExtension: ApplicationAndroidComponentsExtension,
) {
    componentsExtension.onVariants { variant ->
        val variantName = variant.name.replaceFirstChar(Char::titlecase)

        val generateBadging = tasks.register<GenerateBadgingTask>("generate${variantName}Badging") {
            apk.set(variant.artifacts.get(SingleArtifact.APK_FROM_BUNDLE))
            // AGP 9 resolves aapt2 through sdkComponents rather than a hardcoded SDK path.
            aapt2Executable.set(componentsExtension.sdkComponents.aapt2.flatMap(Aapt2::executable))
            badging.set(
                project.layout.buildDirectory.file(
                    "outputs/apk_from_bundle/${variant.name}/${variant.name}-badging.txt",
                ),
            )
        }

        val goldenBadgingFile =
            project.layout.projectDirectory.file("${variant.name}-badging.txt")

        val updateBadgingTaskName = "update${variantName}Badging"
        tasks.register<UpdateBadgingTask>(updateBadgingTaskName) {
            generatedBadging.set(generateBadging.flatMap(GenerateBadgingTask::badging))
            goldenBadging.set(goldenBadgingFile)
        }

        val checkBadgingTaskName = "check${variantName}Badging"
        tasks.register<CheckBadgingTask>(checkBadgingTaskName) {
            goldenBadging.set(goldenBadgingFile)
            generatedBadging.set(generateBadging.flatMap(GenerateBadgingTask::badging))
            this.updateBadgingTaskName.set(updateBadgingTaskName)
            output.set(project.layout.buildDirectory.dir("intermediates/$checkBadgingTaskName"))
        }
    }
}
