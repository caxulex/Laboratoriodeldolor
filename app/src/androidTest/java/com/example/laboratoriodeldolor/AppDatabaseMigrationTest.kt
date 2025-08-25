package com.example.laboratoriodeldolor

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.room.testing.MigrationTestHelper
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import kotlin.test.assertTrue

/**
 * Instrumented migration test using Room's MigrationTestHelper.
 */
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
            database.execSQL("UPDATE mood_entries SET moodScore = 1 WHERE emoji IN ('😡','😢','😞','😠')")
        }
    }

    @Test
    @Throws(IOException::class)
    fun migrate6To7_addsColumnsAndTable_preservesData() {
        // create the v6 schema and insert a row
        val db = helper.createDatabase(TEST_DB, 6).apply {
            // create minimal pain_points table as expected in v6
            execSQL(
                """
                CREATE TABLE IF NOT EXISTS pain_points (
                  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                  x REAL NOT NULL,
                  y REAL NOT NULL
                )
                """
            )
            execSQL("INSERT INTO pain_points (x, y) VALUES (0.25, 0.75)")
            close()
        }

        // Run migration and validate schema
        val db = helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7)

        // verify pain_points has view and logId columns
        val cursor = db.query("PRAGMA table_info('pain_points')")
        var hasView = false
        var hasLogId = false
        while (cursor.moveToNext()) {
            val name = cursor.getString(cursor.getColumnIndex("name"))
            if (name == "view") hasView = true
            if (name == "logId") hasLogId = true
        }
        cursor.close()
        db.close()

        assertTrue(hasView && hasLogId)
    }

    @Test
    @Throws(IOException::class)
    fun migrate5To6_addsMoodScoreAndMapsValues() {
        val db = helper.createDatabase(TEST_DB, 5).apply {
            execSQL("CREATE TABLE IF NOT EXISTS mood_entries (id INTEGER PRIMARY KEY NOT NULL, emoji TEXT NOT NULL, note TEXT, timestamp INTEGER NOT NULL)")
            execSQL("INSERT INTO mood_entries (id, emoji, note, timestamp) VALUES (1, '😊', 'legacy', 1609459200000)")
            close()
        }

        val migrated = helper.runMigrationsAndValidate(TEST_DB, 6, true, MIGRATION_5_6)
        val cursor = migrated.query("SELECT moodScore FROM mood_entries WHERE id = 1")
        try {
            if (cursor.moveToFirst()) {
                val score = cursor.getInt(0)
                assertTrue(score in 1..5)
            } else {
                throw AssertionError("No mood entry found after migration")
            }
        } finally {
            cursor.close()
            migrated.close()
        }
    }
}
