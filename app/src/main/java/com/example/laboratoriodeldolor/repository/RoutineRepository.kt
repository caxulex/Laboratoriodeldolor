package com.example.laboratoriodeldolor.repository

import com.example.laboratoriodeldolor.Routine
import com.example.laboratoriodeldolor.RoutineWithSteps
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for routine operations.
 * Provides a clean abstraction over data access for exercise routine management.
 */
interface RoutineRepository {
    
    /**
     * Get all routines as a reactive stream, ordered by title.
     * @return Flow of all routines
     */
    fun getAllRoutines(): Flow<List<Routine>>
    
    /**
     * Get a specific routine by ID.
     * @param id The ID of the routine to retrieve
     * @return The routine if found, null otherwise
     */
    suspend fun getRoutineById(id: Long): Routine?
    
    /**
     * Insert a new routine and return its generated ID.
     * @param routine The routine to insert
     * @return The ID of the inserted routine
     */
    suspend fun insertRoutine(routine: Routine): Long
    
    /**
     * Get a routine with all its steps as a reactive stream.
     * @param id The ID of the routine
     * @return Flow of the routine with steps, null if not found
     */
    fun getRoutineWithSteps(id: Long): Flow<RoutineWithSteps?>
}