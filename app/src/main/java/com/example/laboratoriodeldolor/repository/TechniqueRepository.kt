package com.example.laboratoriodeldolor.repository

import com.example.laboratoriodeldolor.Technique
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for technique operations.
 * Provides a clean abstraction over data access for exercise technique management.
 */
interface TechniqueRepository {
    
    /**
     * Get all techniques as a reactive stream, ordered by title.
     * @return Flow of all techniques
     */
    fun getAllTechniques(): Flow<List<Technique>>
    
    /**
     * Get a specific technique by ID.
     * @param id The ID of the technique to retrieve
     * @return The technique if found, null otherwise
     */
    suspend fun getTechniqueById(id: Long): Technique?
    
    /**
     * Insert a new technique and return its generated ID.
     * @param technique The technique to insert
     * @return The ID of the inserted technique
     */
    suspend fun insertTechnique(technique: Technique): Long
}