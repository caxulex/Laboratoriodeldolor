package com.example.laboratoriodeldolor.repository.impl

import com.example.laboratoriodeldolor.ExerciseDao
import com.example.laboratoriodeldolor.ExerciseLog
import com.example.laboratoriodeldolor.repository.ExerciseRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of ExerciseRepository that delegates to Room DAO.
 * This provides a clean abstraction over data access and allows for additional business logic.
 */
class ExerciseRepositoryImpl(
    private val exerciseDao: ExerciseDao
) : ExerciseRepository {

    override suspend fun insertExerciseLog(exerciseLog: ExerciseLog) {
        exerciseDao.insert(exerciseLog)
    }

    override fun getAllExerciseLogs(): Flow<List<ExerciseLog>> {
        return exerciseDao.getAll()
    }

    override suspend fun deleteMostRecentExerciseLog() {
        exerciseDao.deleteMostRecent()
    }
}