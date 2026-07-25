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
import com.android.build.api.dsl.ManagedVirtualDevice
import org.gradle.kotlin.dsl.create

/**
 * Devices Gradle can create, boot and tear down itself, so benchmarks and instrumented tests do
 * not depend on whatever happens to be plugged in.
 *
 * @property device The Play Store device name, which has to match exactly.
 * @property apiLevel The API level to run on.
 * @property systemImageSource `aosp` for a plain image, `aosp-atd` for the smaller automated
 * test device image, `google` for one with Play Services.
 */
private enum class ManagedDevice(
    val device: String,
    val apiLevel: Int,
    val systemImageSource: String,
) {
    // Baseline profiles must be generated on an API 33+ image; the profile format changed and
    // older images produce one the installer silently ignores.
    PIXEL_6_API_33("Pixel 6", 33, "aosp"),
    PIXEL_4_API_30("Pixel 4", 30, "aosp-atd"),
    ;

    /** The task name prefix Gradle derives, e.g. `pixel6Api33`. */
    val taskPrefix: String
        get() = device.replace(" ", "").replaceFirstChar(Char::lowercase) + "Api" + apiLevel
}

/**
 * Registers the managed devices on [commonExtension].
 */
internal fun configureGradleManagedDevices(commonExtension: CommonExtension) {
    // AGP 9 replaced the deprecated `devices` container with `allDevices`, and exposes both
    // testOptions and managedDevices as properties rather than script blocks.
    val allDevices = commonExtension.testOptions.managedDevices.allDevices
    ManagedDevice.entries.forEach { managedDevice ->
        allDevices.create<ManagedVirtualDevice>(managedDevice.taskPrefix) {
            device = managedDevice.device
            apiLevel = managedDevice.apiLevel
            systemImageSource = managedDevice.systemImageSource
        }
    }
}
