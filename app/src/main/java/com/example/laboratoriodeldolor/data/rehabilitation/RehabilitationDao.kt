package com.example.laboratoriodeldolor.data.rehabilitation

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface RehabilitationDao {
    
    // Category operations
    @Query("SELECT * FROM rehabilitation_categories ORDER BY displayOrder ASC, name ASC")
    fun getAllCategories(): Flow<List<RehabilitationCategory>>
    
    @Query("SELECT * FROM rehabilitation_categories WHERE id = :categoryId")
    suspend fun getCategoryById(categoryId: String): RehabilitationCategory?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<RehabilitationCategory>)
    
    // Exercise operations
    @Query("SELECT * FROM rehabilitation_exercises WHERE categoryId = :categoryId AND isActive = 1 ORDER BY displayOrder ASC, title ASC")
    fun getExercisesByCategory(categoryId: String): Flow<List<RehabilitationExercise>>
    
    @Query("SELECT * FROM rehabilitation_exercises WHERE id = :exerciseId")
    suspend fun getExerciseById(exerciseId: String): RehabilitationExercise?
    
    @Query("SELECT * FROM rehabilitation_exercises WHERE difficultyLevel <= :maxDifficulty AND isActive = 1 ORDER BY displayOrder ASC")
    fun getExercisesByDifficulty(maxDifficulty: Int): Flow<List<RehabilitationExercise>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExercises(exercises: List<RehabilitationExercise>)
    
    // Session operations
    @Insert
    suspend fun insertSession(session: RehabilitationSession): Long
    
    @Query("SELECT * FROM rehabilitation_sessions WHERE exerciseId = :exerciseId ORDER BY completedAt DESC LIMIT :limit")
    suspend fun getRecentSessions(exerciseId: String, limit: Int = 10): List<RehabilitationSession>
    
    @Query("SELECT * FROM rehabilitation_sessions WHERE DATE(completedAt) = DATE(:date) ORDER BY completedAt DESC")
    suspend fun getSessionsByDate(date: LocalDateTime): List<RehabilitationSession>
    
    @Query("SELECT COUNT(*) FROM rehabilitation_sessions WHERE DATE(completedAt) = DATE('now')")
    suspend fun getTodaySessionCount(): Int
    
    @Query("""
        SELECT 
            re.title as exerciseTitle, 
            rc.name as categoryName,
            rs.completedAt,
            rs.durationSeconds,
            rs.repetitionsCompleted,
            rs.painLevel,
            rs.difficultyRating
        FROM rehabilitation_sessions rs
        JOIN rehabilitation_exercises re ON rs.exerciseId = re.id
        JOIN rehabilitation_categories rc ON re.categoryId = rc.id
        WHERE DATE(rs.completedAt) >= DATE('now', '-7 days')
        ORDER BY rs.completedAt DESC
    """)
    fun getRecentSessionsWithDetails(): Flow<List<SessionSummary>>
    
    // Progress operations
    @Query("SELECT * FROM rehabilitation_progress WHERE categoryId = :categoryId")
    suspend fun getProgressByCategory(categoryId: String): RehabilitationProgress?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: RehabilitationProgress)
    
    @Query("SELECT * FROM rehabilitation_progress ORDER BY lastSessionDate DESC")
    fun getAllProgress(): Flow<List<RehabilitationProgress>>
    
    // Preferences operations
    @Query("SELECT * FROM rehabilitation_preferences WHERE userId = :userId")
    suspend fun getPreferences(userId: String = "default"): RehabilitationPreferences?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePreferences(preferences: RehabilitationPreferences)
    
    // Complex queries for analytics
    @Query("""
        SELECT 
            COUNT(*) as totalSessions,
            AVG(CASE WHEN DATE(completedAt) >= DATE('now', '-7 days') THEN 1.0 ELSE 0.0 END) * 7 as weeklyAverage,
            COALESCE(MAX(streakDays), 0) as currentStreak
        FROM rehabilitation_sessions rs
        LEFT JOIN rehabilitation_progress rp ON rs.exerciseId IN (
            SELECT id FROM rehabilitation_exercises WHERE categoryId = rp.categoryId
        )
    """)
    suspend fun getOverallStats(): RehabilitationStats?
    
    @Query("""
        SELECT 
            rc.id as categoryId,
            rc.name as categoryName,
            rc.description as categoryDescription,
            rc.iconResource,
            rc.colorHex,
            COALESCE(rp.currentLevel, 1) as currentLevel,
            COALESCE(rp.totalSessions, 0) as totalSessions,
            CASE WHEN DATE(rs.completedAt) = DATE('now') THEN 1 ELSE 0 END as completedToday
        FROM rehabilitation_categories rc
        LEFT JOIN rehabilitation_progress rp ON rc.id = rp.categoryId
        LEFT JOIN rehabilitation_sessions rs ON rs.exerciseId IN (
            SELECT id FROM rehabilitation_exercises WHERE categoryId = rc.id
        )
        GROUP BY rc.id
        ORDER BY rc.displayOrder ASC, rc.name ASC
    """)
    fun getCategoryProgressOverview(): Flow<List<CategoryProgress>>
    
    @Query("""
        UPDATE rehabilitation_progress 
        SET streakDays = CASE 
            WHEN DATE(lastSessionDate) = DATE('now', '-1 day') THEN streakDays + 1
            WHEN DATE(lastSessionDate) = DATE('now') THEN streakDays
            ELSE 1
        END,
        lastSessionDate = :sessionDate,
        totalSessions = totalSessions + 1,
        averagePainLevel = (averagePainLevel * (totalSessions - 1) + :painLevel) / totalSessions,
        averageDifficulty = (averageDifficulty * (totalSessions - 1) + :difficulty) / totalSessions
        WHERE categoryId = :categoryId
    """)
    suspend fun updateProgressAfterSession(
        categoryId: String, 
        sessionDate: LocalDateTime, 
        painLevel: Float, 
        difficulty: Float
    )
    
    // Data cleanup
    @Query("DELETE FROM rehabilitation_sessions WHERE completedAt < :cutoffDate")
    suspend fun deleteOldSessions(cutoffDate: LocalDateTime)
    
    @Query("DELETE FROM rehabilitation_progress WHERE totalSessions = 0 AND lastSessionDate IS NULL")
    suspend fun cleanupEmptyProgress()
    
    // Search functionality
    @Query("""
        SELECT DISTINCT re.* FROM rehabilitation_exercises re
        JOIN rehabilitation_categories rc ON re.categoryId = rc.id
        WHERE (re.title LIKE '%' || :query || '%' 
            OR re.description LIKE '%' || :query || '%'
            OR rc.name LIKE '%' || :query || '%')
        AND re.isActive = 1
        ORDER BY re.displayOrder ASC, re.title ASC
    """)
    suspend fun searchExercises(query: String): List<RehabilitationExercise>
}