package com.example.laboratoriodeldolor.repository

import com.example.laboratoriodeldolor.ExerciseLog
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for exercise logging operations.
 * Provides a clean abstraction over data access for exercise tracking functionality.
 */
interface ExerciseRepository {
    
    /**
     * Insert a new exercise log entry.
     * @param exerciseLog The exercise log to insert
     */
    suspend fun insertExerciseLog(exerciseLog: ExerciseLog)
    
    /**
     * Get all exercise logs as a reactive stream, ordered by timestamp (newest first).
     * @return Flow of all exercise logs
     */
    fun getAllExerciseLogs(): Flow<List<ExerciseLog>>
    
    /**
     * Delete the most recent exercise log entry.
     * Useful for "undo" functionality.
     */
    suspend fun deleteMostRecentExerciseLog()
}