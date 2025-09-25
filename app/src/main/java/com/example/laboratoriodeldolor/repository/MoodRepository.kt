package com.example.laboratoriodeldolor.repository

import com.example.laboratoriodeldolor.MoodEntry
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for mood-related operations.
 * Provides a clean abstraction over data access for mood tracking functionality.
 */
interface MoodRepository {
    
    /**
     * Insert a new mood entry.
     * @param moodEntry The mood entry to insert
     */
    suspend fun insertMoodEntry(moodEntry: MoodEntry)
    
    /**
     * Get all mood entries as a reactive stream, ordered by timestamp (newest first).
     * @return Flow of all mood entries
     */
    fun getAllMoodEntries(): Flow<List<MoodEntry>>
    
    /**
     * Get recent mood entries (limited count).
     * @param limit Maximum number of entries to return
     * @return List of recent mood entries
     */
    suspend fun getRecentMoodEntries(limit: Int): List<MoodEntry>
    
    /**
     * Delete mood entries older than the specified timestamp.
     * Useful for data cleanup and privacy compliance.
     * @param cutoffMillis Timestamp cutoff (entries older than this will be deleted)
     */
    suspend fun deleteOldMoodEntries(cutoffMillis: Long)
}