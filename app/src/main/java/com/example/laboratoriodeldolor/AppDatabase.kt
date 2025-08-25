package com.example.laboratoriodeldolor

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MoodEntry::class, PainPoint::class, PainLog::class, ExerciseLog::class], version = 7, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun moodDao(): MoodDao
    abstract fun painPointDao(): PainPointDao
    abstract fun painLogDao(): PainLogDao
    abstract fun exerciseDao(): ExerciseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
                    override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                        // Add the new column with a default value of 3 (neutral)
                        // Then try to map legacy emoji values to a more accurate score where possible
                        database.execSQL("ALTER TABLE mood_entries ADD COLUMN moodScore INTEGER NOT NULL DEFAULT 3")

                        // For a safer mapping, update rows based on common legacy emojis
                        // Map very positive emojis
                        database.execSQL("UPDATE mood_entries SET moodScore = 5 WHERE emoji IN ('😄','😀','😊','😍')")
                        // Slightly positive
                        database.execSQL("UPDATE mood_entries SET moodScore = 4 WHERE emoji IN ('🙂')")
                        // Neutral stays 3
                        database.execSQL("UPDATE mood_entries SET moodScore = 3 WHERE emoji IN ('😐')")
                        // Slightly negative
                        database.execSQL("UPDATE mood_entries SET moodScore = 2 WHERE emoji IN ('😟')")
                        // Very negative
                        database.execSQL("UPDATE mood_entries SET moodScore = 1 WHERE emoji IN ('😡','😢','😞','😠')")
                    }
                }

                val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
                    override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                        // Add new columns to pain_points for view and logId, with safe defaults
                        database.execSQL("ALTER TABLE pain_points ADD COLUMN view TEXT NOT NULL DEFAULT 'front'")
                        database.execSQL("ALTER TABLE pain_points ADD COLUMN logId INTEGER NOT NULL DEFAULT 0")

                        // Create new pain_logs table
                        database.execSQL("CREATE TABLE IF NOT EXISTS pain_logs (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestamp INTEGER NOT NULL)")
                    }
                }

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mood_database"
                ).addMigrations(MIGRATION_5_6, MIGRATION_6_7).build()
                INSTANCE = instance
                instance
            }
        }
    }
}