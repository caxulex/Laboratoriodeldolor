package com.example.laboratoriodeldolor.repository.impl

import com.example.laboratoriodeldolor.Routine
import com.example.laboratoriodeldolor.RoutineDao
import com.example.laboratoriodeldolor.RoutineWithSteps
import com.example.laboratoriodeldolor.repository.RoutineRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of RoutineRepository that delegates to Room DAO.
 * This provides a clean abstraction over data access and allows for additional business logic.
 */
class RoutineRepositoryImpl(
    private val routineDao: RoutineDao
) : RoutineRepository {

    override fun getAllRoutines(): Flow<List<Routine>> {
        return routineDao.getAll()
    }

    override suspend fun getRoutineById(id: Long): Routine? {
        return routineDao.getById(id)
    }

    override suspend fun insertRoutine(routine: Routine): Long {
        return routineDao.insert(routine)
    }

    override fun getRoutineWithSteps(id: Long): Flow<RoutineWithSteps?> {
        return routineDao.getWithSteps(id)
    }
}