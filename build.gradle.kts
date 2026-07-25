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

plugins {
    alias(libs.plugins.kotlin) apply (false)
    alias(libs.plugins.kotlin.jvm) apply (false)
    alias(libs.plugins.android.library) apply (false)
    alias(libs.plugins.android.application) apply (false)
    alias(libs.plugins.kotlin.serialization) apply (false)
    alias(libs.plugins.dagger.hilt.android) apply (false)
    alias(libs.plugins.firebase.crashlytics) apply (false)
    alias(libs.plugins.firebase.perf) apply (false)
    alias(libs.plugins.kotlin.compose.compiler) apply (false)
    alias(libs.plugins.secrets) apply (false)
    alias(libs.plugins.gms) apply (false)
    alias(libs.plugins.ksp) apply (false)
    alias(libs.plugins.google.oss.licenses) apply (false)
    alias(libs.plugins.dependency.guard) apply (false)
    alias(libs.plugins.dokka)
}

dependencies {
    dokka(projects.app)

    // ... Core
    dokka(projects.core.android)
    dokka(projects.core.network)
    dokka(projects.core.preferences)
    dokka(projects.core.room)
    dokka(projects.core.ui)

    // ... Data
    dokka(projects.data)

    // ... Feature
    dokka(projects.feature.auth)
    dokka(projects.feature.home)
    dokka(projects.feature.profile)
    dokka(projects.feature.settings)

    // ... Firebase
    dokka(projects.firebase.analytics)
    dokka(projects.firebase.firestore)
    dokka(projects.firebase.auth)

    // ... Sync
    dokka(projects.sync)

    // ... Dokka Plugins
    dokkaPlugin(libs.dokka.android.plugin)
    dokkaPlugin(libs.dokka.mermaid.plugin)
}

dokka {
    pluginsConfiguration.html {
        customAssets.from("docs/assets/logo-icon.svg")
        customStyleSheets.from("docs/assets/dokka.css")
        footerMessage.set("Made with ❤\uFE0F by Atick Faisal")
    }
}