package com.example.laboratoriodeldolor.repository.impl

import com.example.laboratoriodeldolor.PainPoint
import com.example.laboratoriodeldolor.PainPointDao
import com.example.laboratoriodeldolor.repository.PainPointRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of PainPointRepository that delegates to Room DAO.
 * This provides a clean abstraction over data access and allows for additional business logic.
 */
class PainPointRepositoryImpl(
    private val painPointDao: PainPointDao
) : PainPointRepository {

    override fun getAllPainPoints(): Flow<List<PainPoint>> {
        return painPointDao.getAll()
    }

    override suspend fun getAllPainPointsSnapshot(): List<PainPoint> {
        return painPointDao.getAllSnapshot()
    }

    override suspend fun insertPainPoints(painPoints: List<PainPoint>) {
        painPointDao.insertAll(painPoints)
    }

    override suspend fun clearAllPainPoints() {
        painPointDao.deleteAll()
    }

    override suspend fun deletePainPoint(id: Long) {
        painPointDao.deleteById(id)
    }

    override suspend fun getPainPointsByLogId(logId: Long): List<PainPoint> {
        return painPointDao.getByLogId(logId)
    }

    override suspend fun deleteOldPainPoints(cutoffMillis: Long) {
        painPointDao.deleteOlderThan(cutoffMillis)
    }
}