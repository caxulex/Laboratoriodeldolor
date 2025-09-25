package com.example.laboratoriodeldolor.repository

import com.example.laboratoriodeldolor.PainPoint
import com.example.laboratoriodeldolor.repository.impl.PainPointRepositoryImpl
import com.example.laboratoriodeldolor.testing.TestDataBuilder
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for PainPointRepositoryImpl.
 * Uses mocked DAO for fast, isolated testing.
 */
class PainPointRepositoryImplUnitTest {

    private val mockDao = mockk<com.example.laboratoriodeldolor.PainPointDao>()
    private lateinit var painPointRepository: PainPointRepositoryImpl

    @Before
    fun setup() {
        painPointRepository = PainPointRepositoryImpl(mockDao)
        clearAllMocks()
    }

    @Test
    fun getAllPainPoints_shouldReturnDaoFlow() = runTest {
        // Given: Mock DAO returns flow
        val testPainPoints = TestDataBuilder.createPainPoints(3)
        every { mockDao.getAll() } returns flowOf(testPainPoints)

        // When: Getting all pain points
        val result = painPointRepository.getAllPainPoints().first()

        // Then: Should return DAO result
        assertEquals(testPainPoints, result)
        verify { mockDao.getAll() }
    }

    @Test
    fun getAllPainPointsSnapshot_shouldCallDao() = runTest {
        // Given: Mock DAO returns snapshot
        val testPainPoints = TestDataBuilder.createPainPoints(2)
        coEvery { mockDao.getAllSnapshot() } returns testPainPoints

        // When: Getting snapshot
        val result = painPointRepository.getAllPainPointsSnapshot()

        // Then: Should return DAO result
        assertEquals(testPainPoints, result)
        coVerify { mockDao.getAllSnapshot() }
    }

    @Test
    fun insertPainPoints_shouldCallDaoInsertAll() = runTest {
        // Given: Multiple pain points
        val testPainPoints = TestDataBuilder.createPainPoints(3)
        coJustRun { mockDao.insertAll(testPainPoints) }

        // When: Inserting pain points
        painPointRepository.insertPainPoints(testPainPoints)

        // Then: Should call DAO insertAll
        coVerify { mockDao.insertAll(testPainPoints) }
    }

    @Test
    fun clearAllPainPoints_shouldCallDaoDeleteAll() = runTest {
        // Given: Mock DAO
        coJustRun { mockDao.deleteAll() }

        // When: Clearing all pain points
        painPointRepository.clearAllPainPoints()

        // Then: Should call DAO deleteAll
        coVerify { mockDao.deleteAll() }
    }

    @Test
    fun deletePainPoint_shouldCallDaoDeleteById() = runTest {
        // Given: Pain point ID
        val painPointId = 123L
        coJustRun { mockDao.deleteById(painPointId) }

        // When: Deleting specific pain point
        painPointRepository.deletePainPoint(painPointId)

        // Then: Should call DAO deleteById
        coVerify { mockDao.deleteById(painPointId) }
    }

    @Test
    fun getPainPointsByLogId_shouldCallDaoWithCorrectLogId() = runTest {
        // Given: Log ID and expected results
        val logId = 456L
        val testPainPoints = TestDataBuilder.createPainPoints(2)
        coEvery { mockDao.getByLogId(logId) } returns testPainPoints

        // When: Getting pain points by log ID
        val result = painPointRepository.getPainPointsByLogId(logId)

        // Then: Should return DAO result and verify correct parameters
        assertEquals(testPainPoints, result)
        coVerify { mockDao.getByLogId(logId) }
    }

    @Test
    fun deleteOldPainPoints_shouldCallDaoWithCutoff() = runTest {
        // Given: Cutoff timestamp
        val cutoffTime = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L) // 30 days ago
        coJustRun { mockDao.deleteOlderThan(cutoffTime) }

        // When: Deleting old pain points
        painPointRepository.deleteOldPainPoints(cutoffTime)

        // Then: Should call DAO with correct cutoff
        coVerify { mockDao.deleteOlderThan(cutoffTime) }
    }

    @Test
    fun insertEmptyPainPointsList_shouldStillCallDao() = runTest {
        // Given: Empty list
        val emptyList = emptyList<PainPoint>()
        coJustRun { mockDao.insertAll(emptyList) }

        // When: Inserting empty list
        painPointRepository.insertPainPoints(emptyList)

        // Then: Should still call DAO (might be no-op)
        coVerify { mockDao.insertAll(emptyList) }
    }

    @Test
    fun getPainPointsByLogId_shouldHandleEmptyResults() = runTest {
        // Given: Log ID with no associated pain points
        val logId = 999L
        coEvery { mockDao.getByLogId(logId) } returns emptyList()

        // When: Getting pain points by log ID
        val result = painPointRepository.getPainPointsByLogId(logId)

        // Then: Should return empty list
        assertTrue(result.isEmpty())
        coVerify { mockDao.getByLogId(logId) }
    }

    @Test
    fun getAllPainPoints_shouldHandleEmptyDatabase() = runTest {
        // Given: Empty database
        every { mockDao.getAll() } returns flowOf(emptyList())

        // When: Getting all pain points
        val result = painPointRepository.getAllPainPoints().first()

        // Then: Should return empty list
        assertTrue(result.isEmpty())
        verify { mockDao.getAll() }
    }

    @Test
    fun deleteOldPainPoints_shouldHandleFutureTimestamp() = runTest {
        // Given: Future timestamp (should delete all entries)
        val futureTime = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)
        coJustRun { mockDao.deleteOlderThan(futureTime) }

        // When: Deleting with future timestamp
        painPointRepository.deleteOldPainPoints(futureTime)

        // Then: Should call DAO with future timestamp
        coVerify { mockDao.deleteOlderThan(futureTime) }
    }

    @Test
    fun repositoryMethodsCallCorrectDaoMethods() = runTest {
        // This test verifies that all repository methods map to correct DAO methods
        val testPainPoints = TestDataBuilder.createPainPoints(1)
        val testLogId = 100L
        val testId = 50L
        val testCutoff = System.currentTimeMillis()

        every { mockDao.getAll() } returns flowOf(testPainPoints)
        coEvery { mockDao.getAllSnapshot() } returns testPainPoints
        coEvery { mockDao.getByLogId(testLogId) } returns testPainPoints
        coJustRun { mockDao.insertAll(testPainPoints) }
        coJustRun { mockDao.deleteAll() }
        coJustRun { mockDao.deleteById(testId) }
        coJustRun { mockDao.deleteOlderThan(testCutoff) }

        // When: Calling various repository methods
        painPointRepository.getAllPainPoints().first()
        painPointRepository.getAllPainPointsSnapshot()
        painPointRepository.insertPainPoints(testPainPoints)
        painPointRepository.clearAllPainPoints()
        painPointRepository.deletePainPoint(testId)
        painPointRepository.getPainPointsByLogId(testLogId)
        painPointRepository.deleteOldPainPoints(testCutoff)

        // Then: All corresponding DAO methods should be called
        verify { mockDao.getAll() }
        coVerify { mockDao.getAllSnapshot() }
        coVerify { mockDao.insertAll(testPainPoints) }
        coVerify { mockDao.deleteAll() }
        coVerify { mockDao.deleteById(testId) }
        coVerify { mockDao.getByLogId(testLogId) }
        coVerify { mockDao.deleteOlderThan(testCutoff) }
    }
}