package com.example.laboratoriodeldolor.repository.impl

import com.example.laboratoriodeldolor.Technique
import com.example.laboratoriodeldolor.TechniqueDao
import com.example.laboratoriodeldolor.repository.TechniqueRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of TechniqueRepository that delegates to Room DAO.
 * This provides a clean abstraction over data access and allows for additional business logic.
 */
class TechniqueRepositoryImpl(
    private val techniqueDao: TechniqueDao
) : TechniqueRepository {

    override fun getAllTechniques(): Flow<List<Technique>> {
        return techniqueDao.getAll()
    }

    override suspend fun getTechniqueById(id: Long): Technique? {
        return techniqueDao.getById(id)
    }

    override suspend fun insertTechnique(technique: Technique): Long {
        return techniqueDao.insert(technique)
    }
}