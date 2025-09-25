package com.example.laboratoriodeldolor.repository.impl

import com.example.laboratoriodeldolor.RoutineStep
import com.example.laboratoriodeldolor.RoutineStepDao
import com.example.laboratoriodeldolor.repository.RoutineStepRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of RoutineStepRepository that delegates to Room DAO.
 * This provides a clean abstraction over data access and allows for additional business logic.
 */
class RoutineStepRepositoryImpl(
    private val routineStepDao: RoutineStepDao
) : RoutineStepRepository {

    override fun getStepsForRoutine(routineId: Long): Flow<List<RoutineStep>> {
        return routineStepDao.getForRoutine(routineId)
    }

    override suspend fun insertRoutineStep(routineStep: RoutineStep): Long {
        return routineStepDao.insert(routineStep)
    }
}