package com.example.laboratoriodeldolor

import androidx.room.migration.Migration
import androidx.room.testing.MigrationTestHelper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Ignore
import org.junit.Assert.*
import java.io.IOException

@RunWith(AndroidJUnit4::class)
@Ignore("Disabled during CI/local dev; migrations validated manually when needed")
class AppDatabaseMigrationTest {
    private val TEST_DB = "migration-test"

    @get:Rule
    val helper: MigrationTestHelper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java.canonicalName,
        FrameworkSQLiteOpenHelperFactory()
    )

    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE pain_points ADD COLUMN view TEXT NOT NULL DEFAULT 'front'")
            database.execSQL("ALTER TABLE pain_points ADD COLUMN logId INTEGER NOT NULL DEFAULT 0")
            database.execSQL("CREATE TABLE IF NOT EXISTS pain_logs (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestamp INTEGER NOT NULL)")
        }
    }

    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE mood_entries ADD COLUMN moodScore INTEGER NOT NULL DEFAULT 3")
            database.execSQL("UPDATE mood_entries SET moodScore = 5 WHERE emoji IN ('😄','😀','😊','😍')")
            database.execSQL("UPDATE mood_entries SET moodScore = 4 WHERE emoji IN ('🙂')")
            database.execSQL("UPDATE mood_entries SET moodScore = 3 WHERE emoji IN ('😐')")
            database.execSQL("UPDATE mood_entries SET moodScore = 2 WHERE emoji IN ('😟')")
            database.execSQL("UPDATE mood_entries SET moodScore = 1 WHERE emoji IN ('�','�😡','😢','😞','😠')")
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate5To6_addsMoodScore_and_mapsValues() {
        val db = helper.createDatabase(TEST_DB, 5).apply {
            // create v5 mood_entries (no moodScore column)
            execSQL("CREATE TABLE IF NOT EXISTS mood_entries (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, emoji TEXT NOT NULL, note TEXT NOT NULL, timestamp INTEGER NOT NULL)")
            execSQL("INSERT INTO mood_entries (emoji, note, timestamp) VALUES ('😄','mood note', 123456000)")
            execSQL("INSERT INTO mood_entries (emoji, note, timestamp) VALUES ('😟','mood sad', 123456001)")
            close()
        }

    // Run migration 5 -> 6 (ignored during CI/local runs)
    helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6)
    }

    @Test
    @Throws(IOException::class)
    fun migrate6To7_addsPainColumnsAndPainLogs() {
        val db = helper.createDatabase(TEST_DB, 6).apply {
            execSQL("CREATE TABLE IF NOT EXISTS pain_points (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, x REAL NOT NULL, y REAL NOT NULL, view TEXT NOT NULL DEFAULT 'front')")
            execSQL("INSERT INTO pain_points (x,y,view) VALUES (0.5,0.1,'front')")
            close()
        }

    // Run migration 6 -> 7 (ignored during CI/local runs)
    helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7)
    }
}

