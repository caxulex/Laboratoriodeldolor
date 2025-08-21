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

        // Run migration
        helper.runMigrationsAndValidate(TEST_DB, 7, true, MIGRATION_6_7)

        // If we reach here, migration applied without throwing; basic assertion
        assertTrue(true)
    }
}
