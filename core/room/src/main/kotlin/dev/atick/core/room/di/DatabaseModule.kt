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
 * Dagger module for database.
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private const val ROOM_DATABASE_NAME = "dev.atick.jetpack.room"

    /**
     * Get the database for Jetpack.
     *
     * @param appContext The application context.
     * @return The database for Jetpack.
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
        ).fallbackToDestructiveMigration(true).build()
    }
}
