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

package dev.atick.core.room.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.atick.core.room.data.JetpackDatabase
import javax.inject.Singleton

/**
 * Provides the singleton [JetpackDatabase] in app-private storage.
 *
 * No migration path is registered. A version upgrade without a path uses destructive fallback,
 * which can erase pending local edits and deletes. Remote re-fetch cannot recover unsent data.
 * Same-version schema-integrity failures are not handled by this fallback.
 *
 * Before upgrading existing installs, register and test a data-preserving migration, including
 * pending rows. See the data guide's Room persistence section for the extension steps.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * The database name used for Room persistence.
     *
     * This name is used to create the SQLite database file on disk.
     * Changing this name will create a new database file.
     */
    private const val ROOM_DATABASE_NAME = "dev.atick.jetpack.room"

    /**
     * Provides the database with destructive fallback for missing version migration paths.
     *
     * @param appContext Application context used to locate the database file.
     * @return The singleton [JetpackDatabase].
     */
    @Singleton
    @Provides
    fun provideRoomDatabase(
        @ApplicationContext appContext: Context,
    ): JetpackDatabase {
        return Room.databaseBuilder(
            appContext,
            JetpackDatabase::class.java,
            ROOM_DATABASE_NAME,
        ).fallbackToDestructiveMigration(true).build() // TODO: Replace with proper migrations before production
    }
}
