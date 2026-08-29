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

package dev.atick.core.room.data

import androidx.room.Database
import androidx.room.RoomDatabase
import dev.atick.core.room.model.JetpackEntity

/**
 * Room database for Jetpack.
 */
@Database(
    version = 2,
    exportSchema = false,
    entities = [
        JetpackEntity::class,
    ],
)
abstract class JetpackDatabase : RoomDatabase() {
    /**
     * Get the data access object for [JetpackEntity] entity.
     *
     * @return The data access object for [JetpackEntity] entity.
     */
    abstract fun getJetpackDao(): JetpackDao
}
