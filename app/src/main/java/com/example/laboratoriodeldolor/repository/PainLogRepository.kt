package com.example.laboratoriodeldolor.repository

import com.example.laboratoriodeldolor.PainLog

/**
 * Repository interface for pain log operations.
 * Provides a clean abstraction over data access for pain session logging.
 */
interface PainLogRepository {
    
    /**
     * Insert a new pain log and return its generated ID.
     * @param painLog The pain log to insert
     * @return The ID of the inserted pain log
     */
    suspend fun insertPainLog(painLog: PainLog): Long
    
    /**
     * Get all pain logs ordered by timestamp (newest first).
     * @return List of all pain logs
     */
    suspend fun getAllPainLogs(): List<PainLog>
}