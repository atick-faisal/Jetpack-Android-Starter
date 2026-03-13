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
    alias(libs.plugins.android.library) apply (false)
    alias(libs.plugins.android.application) apply (false)
    alias(libs.plugins.kotlin.serialization) apply (false)
    alias(libs.plugins.dagger.hilt.android) apply (false)
    alias(libs.plugins.firebase.crashlytics) apply (false)
    alias(libs.plugins.firebase.perf) apply false
    alias(libs.plugins.kotlin.compose.compiler) apply (false)
    alias(libs.plugins.secrets) apply (false)
    alias(libs.plugins.gms) apply (false)
    alias(libs.plugins.ksp) apply (false)
    alias(libs.plugins.google.oss.licenses) apply (false)
    alias(libs.plugins.dokka)
}

dependencies {
    dokka(project(":app"))

    // ... Core
    dokka(project(":core:android"))
    dokka(project(":core:network"))
    dokka(project(":core:preferences"))
    dokka(project(":core:room"))
    dokka(project(":core:ui"))

    // ... Data
    dokka(project(":data"))

    // ... Feature
    dokka(project(":feature:auth"))
    dokka(project(":feature:home"))
    dokka(project(":feature:profile"))
    dokka(project(":feature:settings"))

    // ... Firebase
    dokka(project(":firebase:analytics"))
    dokka(project(":firebase:firestore"))
    dokka(project(":firebase:auth"))

    // ... Sync
    dokka(project(":sync"))

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