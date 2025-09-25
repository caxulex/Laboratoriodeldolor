package com.example.laboratoriodeldolor.repository.impl

import com.example.laboratoriodeldolor.PainLog
import com.example.laboratoriodeldolor.PainLogDao
import com.example.laboratoriodeldolor.repository.PainLogRepository

/**
 * Implementation of PainLogRepository that delegates to Room DAO.
 * This provides a clean abstraction over data access and allows for additional business logic.
 */
class PainLogRepositoryImpl(
    private val painLogDao: PainLogDao
) : PainLogRepository {

    override suspend fun insertPainLog(painLog: PainLog): Long {
        return painLogDao.insertLog(painLog)
    }

    override suspend fun getAllPainLogs(): List<PainLog> {
        return painLogDao.getAllLogs()
    }
}