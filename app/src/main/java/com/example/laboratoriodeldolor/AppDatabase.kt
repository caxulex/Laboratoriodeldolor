package com.example.laboratoriodeldolor

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.example.laboratoriodeldolor.data.rehabilitation.*
import java.time.LocalDateTime
import java.time.ZoneOffset

@Database(
    entities = [
        MoodEntry::class, 
        PainPoint::class, 
        PainLog::class, 
        ExerciseLog::class, 
        Technique::class, 
        Routine::class, 
        RoutineStep::class,
        RehabilitationCategory::class,
        RehabilitationExercise::class,
        RehabilitationSession::class,
        RehabilitationPreferences::class,
        RehabilitationProgress::class
    ], 
    version = 11, 
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun moodDao(): MoodDao
    abstract fun painPointDao(): PainPointDao
    abstract fun painLogDao(): PainLogDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun techniqueDao(): TechniqueDao
    abstract fun routineDao(): RoutineDao
    abstract fun routineStepDao(): RoutineStepDao
    abstract fun rehabilitationDao(): RehabilitationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val MIGRATION_5_6 = object : androidx.room.migration.Migration(5, 6) {
                    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        // Add the new column with a default value of 3 (neutral)
                        // Then try to map legacy emoji values to a more accurate score where possible
                        db.execSQL("ALTER TABLE mood_entries ADD COLUMN moodScore INTEGER NOT NULL DEFAULT 3")

                        // For a safer mapping, update rows based on common legacy emojis
                        // Map very positive emojis
                        db.execSQL("UPDATE mood_entries SET moodScore = 5 WHERE emoji IN ('😄','😀','😊','😍')")
                        // Slightly positive
                        db.execSQL("UPDATE mood_entries SET moodScore = 4 WHERE emoji IN ('🙂')")
                        // Neutral stays 3
                        db.execSQL("UPDATE mood_entries SET moodScore = 3 WHERE emoji IN ('😐')")
                        // Slightly negative
                        db.execSQL("UPDATE mood_entries SET moodScore = 2 WHERE emoji IN ('😟')")
                        // Very negative
                        db.execSQL("UPDATE mood_entries SET moodScore = 1 WHERE emoji IN ('😥','😡','😢','😞','😠')")
                    }
                }

                val MIGRATION_6_7 = object : androidx.room.migration.Migration(6, 7) {
                    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        // Add new columns to pain_points for view and logId, with safe defaults
                        db.execSQL("ALTER TABLE pain_points ADD COLUMN view TEXT NOT NULL DEFAULT 'front'")
                        db.execSQL("ALTER TABLE pain_points ADD COLUMN logId INTEGER NOT NULL DEFAULT 0")

                        // Create new pain_logs table
                        db.execSQL("CREATE TABLE IF NOT EXISTS pain_logs (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestamp INTEGER NOT NULL)")
                    }
                }

                val MIGRATION_7_8 = object : androidx.room.migration.Migration(7, 8) {
                    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        // Drop the legacy diary_entries table which is now consolidated into mood_entries
                        db.execSQL("DROP TABLE IF EXISTS diary_entries")
                    }
                }

                val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
                    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        // Create techniques, routines, and routine_steps tables if they don't exist.
                        db.execSQL("CREATE TABLE IF NOT EXISTS techniques (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, description TEXT NOT NULL, videoUrl TEXT)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS routines (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, title TEXT NOT NULL, bodyRegion TEXT, summary TEXT)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS routine_steps (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, routineId INTEGER NOT NULL, stepOrder INTEGER NOT NULL, description TEXT NOT NULL, techniqueId INTEGER)")
                    }
                }

                val MIGRATION_10_11 = object : androidx.room.migration.Migration(10, 11) {
                    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        // Create rehabilitation tables
                        db.execSQL("""
                            CREATE TABLE IF NOT EXISTS rehabilitation_categories (
                                id TEXT PRIMARY KEY NOT NULL,
                                name TEXT NOT NULL,
                                description TEXT NOT NULL,
                                iconResource TEXT NOT NULL,
                                colorHex TEXT NOT NULL,
                                displayOrder INTEGER NOT NULL DEFAULT 0
                            )
                        """)
                        
                        db.execSQL("""
                            CREATE TABLE IF NOT EXISTS rehabilitation_exercises (
                                id TEXT PRIMARY KEY NOT NULL,
                                categoryId TEXT NOT NULL,
                                title TEXT NOT NULL,
                                description TEXT NOT NULL,
                                instructions TEXT NOT NULL,
                                durationSeconds INTEGER NOT NULL,
                                repetitions INTEGER NOT NULL,
                                difficultyLevel INTEGER NOT NULL,
                                imageResource TEXT,
                                videoUrl TEXT,
                                displayOrder INTEGER NOT NULL DEFAULT 0,
                                isActive INTEGER NOT NULL DEFAULT 1,
                                FOREIGN KEY(categoryId) REFERENCES rehabilitation_categories(id) ON DELETE CASCADE
                            )
                        """)
                        
                        db.execSQL("""
                            CREATE TABLE IF NOT EXISTS rehabilitation_sessions (
                                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                exerciseId TEXT NOT NULL,
                                completedAt INTEGER NOT NULL,
                                durationSeconds INTEGER NOT NULL,
                                repetitionsCompleted INTEGER NOT NULL,
                                difficultyRating INTEGER,
                                painLevel INTEGER,
                                notes TEXT,
                                FOREIGN KEY(exerciseId) REFERENCES rehabilitation_exercises(id) ON DELETE CASCADE
                            )
                        """)
                        
                        db.execSQL("""
                            CREATE TABLE IF NOT EXISTS rehabilitation_preferences (
                                userId TEXT PRIMARY KEY NOT NULL,
                                preferredDifficulty INTEGER NOT NULL DEFAULT 2,
                                reminderEnabled INTEGER NOT NULL DEFAULT 1,
                                reminderTime TEXT NOT NULL DEFAULT '09:00',
                                autoProgressEnabled INTEGER NOT NULL DEFAULT 1,
                                painThreshold INTEGER NOT NULL DEFAULT 6,
                                sessionDurationMinutes INTEGER NOT NULL DEFAULT 15
                            )
                        """)
                        
                        db.execSQL("""
                            CREATE TABLE IF NOT EXISTS rehabilitation_progress (
                                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                                categoryId TEXT NOT NULL,
                                currentLevel INTEGER NOT NULL DEFAULT 1,
                                totalSessions INTEGER NOT NULL DEFAULT 0,
                                lastSessionDate INTEGER,
                                averagePainLevel REAL NOT NULL DEFAULT 0.0,
                                averageDifficulty REAL NOT NULL DEFAULT 0.0,
                                streakDays INTEGER NOT NULL DEFAULT 0,
                                FOREIGN KEY(categoryId) REFERENCES rehabilitation_categories(id) ON DELETE CASCADE
                            )
                        """)
                        
                        // Create indexes
                        db.execSQL("CREATE INDEX IF NOT EXISTS index_rehabilitation_exercises_categoryId ON rehabilitation_exercises(categoryId)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS index_rehabilitation_sessions_exerciseId ON rehabilitation_sessions(exerciseId)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS index_rehabilitation_sessions_completedAt ON rehabilitation_sessions(completedAt)")
                        db.execSQL("CREATE INDEX IF NOT EXISTS index_rehabilitation_progress_categoryId ON rehabilitation_progress(categoryId)")
                    }
                }

                // Provide a deferred so the seeding callback can await the fully-built AppDatabase
                val dbDeferred = kotlinx.coroutines.CompletableDeferred<AppDatabase>()

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mood_database"
                ).addMigrations(MIGRATION_5_6, MIGRATION_6_7, MIGRATION_7_8, MIGRATION_8_9, MIGRATION_10_11)
                    .addCallback(DatabaseSeeder.createCallback(dbDeferred, context.applicationContext))
                    .build()

                // Complete the deferred so the seeder callback can access DAOs
                dbDeferred.complete(instance)

                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDateTime?): Long? {
        return date?.toEpochSecond(ZoneOffset.UTC)
    }
}