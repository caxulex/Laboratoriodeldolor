package com.example.laboratoriodeldolor.repository

import com.example.laboratoriodeldolor.RoutineStep
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for routine step operations.
 * Provides a clean abstraction over data access for routine step management.
 */
interface RoutineStepRepository {
    
    /**
     * Get all steps for a specific routine as a reactive stream, ordered by step order.
     * @param routineId The ID of the routine
     * @return Flow of routine steps for the specified routine
     */
    fun getStepsForRoutine(routineId: Long): Flow<List<RoutineStep>>
    
    /**
     * Insert a new routine step and return its generated ID.
     * @param routineStep The routine step to insert
     * @return The ID of the inserted routine step
     */
    suspend fun insertRoutineStep(routineStep: RoutineStep): Long
}