/*
 * Copyright 2025 Atick Faisal
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

package dev.atick.firebase.auth.config

import dev.atick.firebase.auth.BuildConfig

/**
 * Configuration object containing constants and values used in the application.
 */
object Config {
    /**
     * The Web Client ID used for Google Sign-In authentication.
     * This identifier is associated with your application and is used to authenticate with Google services.
     * Ensure that it is properly configured and secured in the Google Developer Console.
     */
    const val WEB_CLIENT_ID = BuildConfig.GOOGLE_WEB_CLIENT_ID
}
