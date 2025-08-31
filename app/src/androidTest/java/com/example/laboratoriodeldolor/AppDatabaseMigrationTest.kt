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
import java.io.IOException
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
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

        // Run migration 5 -> 6
        helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6)

        val migrated = helper.openDatabase(TEST_DB, 6)
        val cursor = migrated.query("SELECT emoji, moodScore FROM mood_entries ORDER BY id")
        try {
            assertTrue(cursor.moveToFirst())
            assertTrue(cursor.getString(0) == "😄")
            // moodScore should have been set to 5 for 😄
            assertTrue(cursor.getInt(1) == 5)
            cursor.moveToNext()
            assertTrue(cursor.getString(0) == "😟")
            assertTrue(cursor.getInt(1) == 2)
        } finally {
            cursor.close()
            migrated.close()
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate6To7_addsPainColumnsAndPainLogs() {
        val db = helper.createDatabase(TEST_DB, 6).apply {
            execSQL("CREATE TABLE IF NOT EXISTS pain_points (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, x REAL NOT NULL, y REAL NOT NULL, view TEXT NOT NULL DEFAULT 'front')")
            execSQL("INSERT INTO pain_points (x,y,view) VALUES (0.5,0.1,'front')")
            close()
        }

        helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7)

        val migrated = helper.openDatabase(TEST_DB, 7)
        val cursor = migrated.query("SELECT name FROM sqlite_master WHERE type='table' AND name='pain_logs'")
        try {
            assertTrue(cursor.moveToFirst())
        } finally {
            cursor.close()
            migrated.close()
        }
    }
}

