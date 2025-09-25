package com.example.laboratoriodeldolor.repository

import com.example.laboratoriodeldolor.PainPoint
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for pain point tracking operations.
 * Provides a clean abstraction over data access for pain tracking functionality.
 */
interface PainPointRepository {
    
    /**
     * Get all pain points as a reactive stream, ordered by timestamp (newest first).
     * @return Flow of all pain points
     */
    fun getAllPainPoints(): Flow<List<PainPoint>>
    
    /**
     * Get all pain points as a one-time snapshot.
     * @return List of all pain points
     */
    suspend fun getAllPainPointsSnapshot(): List<PainPoint>
    
    /**
     * Insert multiple pain points in a single transaction.
     * @param painPoints List of pain points to insert
     */
    suspend fun insertPainPoints(painPoints: List<PainPoint>)
    
    /**
     * Delete all pain points.
     * Used when clearing current pain tracking session.
     */
    suspend fun clearAllPainPoints()
    
    /**
     * Delete a specific pain point by ID.
     * @param id The ID of the pain point to delete
     */
    suspend fun deletePainPoint(id: Long)
    
    /**
     * Get pain points associated with a specific pain log.
     * @param logId The ID of the pain log
     * @return List of pain points for the specified log
     */
    suspend fun getPainPointsByLogId(logId: Long): List<PainPoint>
    
    /**
     * Delete pain points older than the specified timestamp.
     * Useful for data cleanup and privacy compliance.
     * @param cutoffMillis Timestamp cutoff (entries older than this will be deleted)
     */
    suspend fun deleteOldPainPoints(cutoffMillis: Long)
}