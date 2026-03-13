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
 * Hilt module providing the Room database instance.
 *
 * ## Why This Module Exists
 *
 * Room databases require explicit initialization with context and configuration.
 * This module centralizes database creation to ensure:
 *
 * 1. **Single Instance**: Database is created once and shared across the app (Singleton)
 * 2. **Proper Configuration**: Migration strategy is consistently applied
 * 3. **Testability**: Tests can provide a fake database implementation
 * 4. **Lazy Initialization**: Database is only created when first needed
 *
 * ## Architecture Decision: Destructive Migration
 *
 * This template uses `.fallbackToDestructiveMigration(true)`, which means:
 *
 * ### What It Does
 * When the database schema changes (entity fields added/removed/modified):
 * 1. Room detects a schema mismatch
 * 2. **Deletes the existing database** (all local data is lost)
 * 3. Creates a new database with the new schema
 * 4. Starts fresh with no data
 *
 * ### Why This Is Acceptable During Development
 * - **Rapid Iteration**: Schema can change frequently during development
 * - **Firebase Sync**: This template syncs data with Firebase, so local data can be re-fetched
 * - **Simpler Development**: No need to write migration code for every schema change
 * - **Fresh Start**: Avoids corrupted data from incomplete migrations
 *
 * ### Why This Is NOT Acceptable for Production
 * - **Data Loss**: Users would lose all locally cached data on app update
 * - **Poor UX**: Users would see empty screens until re-sync completes
 * - **Network Usage**: All data must be re-downloaded after each schema change
 *
 * ## Migration Strategy for Production
 *
 * Before releasing to production, you MUST implement proper migrations:
 *
 * ```kotlin
 * val MIGRATION_1_2 = object : Migration(1, 2) {
 *     override fun migrate(db: SupportSQLiteDatabase) {
 *         db.execSQL("ALTER TABLE jetpack ADD COLUMN new_field TEXT")
 *     }
 * }
 *
 * Room.databaseBuilder(...)
 *     .addMigrations(MIGRATION_1_2)
 *     .build()
 * ```
 *
 * ### Testing Migrations
 * ```kotlin
 * @Test
 * fun testMigration1To2() {
 *     helper.createDatabase(DB_NAME, 1).apply {
 *         // Insert data with schema v1
 *         close()
 *     }
 *     helper.runMigrationsAndValidate(DB_NAME, 2, true, MIGRATION_1_2)
 * }
 * ```
 *
 * ## When to Remove Destructive Migration
 *
 * Remove `.fallbackToDestructiveMigration(true)` and add proper migrations when:
 * - Preparing for the first production release
 * - Users have accumulated significant local data
 * - Offline-first functionality becomes critical
 * - Network re-sync is too slow or expensive
 *
 * @see JetpackDatabase
 * @see Migration
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
     * Provides the singleton Room database instance.
     *
     * This database:
     * - Is created once and shared across the entire application
     * - Uses destructive migration (development-only - see class KDoc)
     * - Lives in the app's private storage directory
     * - Is automatically closed when the app is killed
     *
     * ## Development Configuration
     * - **Destructive Migration Enabled**: Schema changes delete and recreate the database
     * - **Safe for Development**: Firebase sync allows data to be re-fetched
     *
     * ## Production Configuration Required
     * Before production release:
     * 1. Remove `.fallbackToDestructiveMigration(true)`
     * 2. Add explicit migrations with `.addMigrations(MIGRATION_X_Y)`
     * 3. Test migrations thoroughly with `MigrationTestHelper`
     *
     * ## Example Migration (for production)
     * ```kotlin
     * val MIGRATION_1_2 = object : Migration(1, 2) {
     *     override fun migrate(db: SupportSQLiteDatabase) {
     *         db.execSQL("ALTER TABLE jetpack ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0")
     *     }
     * }
     * ```
     *
     * @param appContext The application context for database creation
     * @return The singleton [JetpackDatabase] instance
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
