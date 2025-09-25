package com.example.laboratoriodeldolor.repository

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.laboratoriodeldolor.AppDatabase
import com.example.laboratoriodeldolor.PainPoint
import com.example.laboratoriodeldolor.repository.impl.PainPointRepositoryImpl
import com.example.laboratoriodeldolor.testing.TestDataBuilder
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.*

/**
 * Integration tests for PainPointRepositoryImpl.
 * Uses in-memory Room database for fast, isolated testing.
 */
@RunWith(AndroidJUnit4::class)
class PainPointRepositoryImplTest {

    private lateinit var database: AppDatabase
    private lateinit var painPointRepository: PainPointRepositoryImpl

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()

        painPointRepository = PainPointRepositoryImpl(database.painPointDao())
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertPainPoint_shouldStoreInDatabase() = runTest {
        // Given: Test pain point
        val testPainPoint = TestDataBuilder.createPainPoint(
            bodyPart = "Lower Back",
            painLevel = 7,
            description = "Sharp pain after sitting"
        )

        // When: Inserting pain point
        painPointRepository.insertPainPoint(testPainPoint)

        // Then: Should be retrievable from database
        val allPainPoints = painPointRepository.getAllPainPoints().first()
        assertEquals(1, allPainPoints.size)
        assertEquals(testPainPoint.bodyPart, allPainPoints[0].bodyPart)
        assertEquals(testPainPoint.painLevel, allPainPoints[0].painLevel)
        assertEquals(testPainPoint.description, allPainPoints[0].description)
    }

    @Test
    fun getAllPainPoints_shouldReturnAllEntries() = runTest {
        // Given: Multiple pain points
        val testPainPoints = TestDataBuilder.createPainPoints(4)
        testPainPoints.forEach { painPointRepository.insertPainPoint(it) }

        // When: Getting all pain points
        val retrievedPainPoints = painPointRepository.getAllPainPoints().first()

        // Then: Should return all entries
        assertEquals(4, retrievedPainPoints.size)
    }

    @Test
    fun getPainPointsByBodyPart_shouldFilterCorrectly() = runTest {
        // Given: Pain points for different body parts
        val backPain = TestDataBuilder.createPainPoint(bodyPart = "Lower Back", painLevel = 6)
        val neckPain = TestDataBuilder.createPainPoint(bodyPart = "Neck", painLevel = 4)
        val shoulderPain = TestDataBuilder.createPainPoint(bodyPart = "Shoulder", painLevel = 5)
        val anotherBackPain = TestDataBuilder.createPainPoint(bodyPart = "Lower Back", painLevel = 8)

        listOf(backPain, neckPain, shoulderPain, anotherBackPain).forEach {
            painPointRepository.insertPainPoint(it)
        }

        // When: Getting pain points for specific body part
        val backPainPoints = painPointRepository.getPainPointsByBodyPart("Lower Back")

        // Then: Should return only matching entries
        assertEquals(2, backPainPoints.size)
        assertTrue(backPainPoints.all { it.bodyPart == "Lower Back" })
    }

    @Test
    fun getRecentPainPoints_shouldLimitAndOrderByTimestamp() = runTest {
        // Given: Pain points with different timestamps
        val painPoints = (1..5).map { index ->
            TestDataBuilder.createPainPoint(
                id = index,
                bodyPart = "Body Part $index",
                timestamp = System.currentTimeMillis() - (index * 1000L)
            )
        }
        painPoints.forEach { painPointRepository.insertPainPoint(it) }

        // When: Getting recent pain points with limit
        val recentPainPoints = painPointRepository.getRecentPainPoints(3)

        // Then: Should return limited results
        assertEquals(3, recentPainPoints.size)
    }

    @Test
    fun deleteOldPainPoints_shouldRemoveOldEntries() = runTest {
        // Given: Pain points with different timestamps
        val oldTimestamp = System.currentTimeMillis() - (10 * 24 * 60 * 60 * 1000L) // 10 days ago
        val recentTimestamp = System.currentTimeMillis() - (2 * 24 * 60 * 60 * 1000L) // 2 days ago
        
        val oldPainPoint = TestDataBuilder.createPainPoint(timestamp = oldTimestamp)
        val recentPainPoint = TestDataBuilder.createPainPoint(timestamp = recentTimestamp)
        
        painPointRepository.insertPainPoint(oldPainPoint)
        painPointRepository.insertPainPoint(recentPainPoint)

        // When: Deleting entries older than 7 days
        val cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        painPointRepository.deleteOldPainPoints(cutoffTime)

        // Then: Only recent entry should remain
        val remainingPainPoints = painPointRepository.getAllPainPoints().first()
        assertEquals(1, remainingPainPoints.size)
        assertEquals(recentPainPoint.timestamp, remainingPainPoints[0].timestamp)
    }

    @Test
    fun painPointFlow_shouldEmitUpdates() = runTest {
        // Given: Repository with flow observer
        val flow = painPointRepository.getAllPainPoints()

        // When: Adding entries over time
        val initialPainPoints = flow.first()
        assertEquals(0, initialPainPoints.size)

        painPointRepository.insertPainPoint(TestDataBuilder.createPainPoint())
        val afterFirstInsert = flow.first()
        assertEquals(1, afterFirstInsert.size)

        painPointRepository.insertPainPoint(TestDataBuilder.createPainPoint(bodyPart = "Different part"))
        val afterSecondInsert = flow.first()
        assertEquals(2, afterSecondInsert.size)
    }

    @Test
    fun updatePainPoint_shouldModifyExistingEntry() = runTest {
        // Given: Existing pain point
        val originalPainPoint = TestDataBuilder.createPainPoint(
            bodyPart = "Neck",
            painLevel = 5,
            description = "Original description"
        )
        painPointRepository.insertPainPoint(originalPainPoint)

        // When: Updating pain point
        val updatedPainPoint = originalPainPoint.copy(
            painLevel = 8,
            description = "Updated description - much worse"
        )
        painPointRepository.updatePainPoint(updatedPainPoint)

        // Then: Should reflect changes
        val allPainPoints = painPointRepository.getAllPainPoints().first()
        assertEquals(1, allPainPoints.size)
        assertEquals(8, allPainPoints[0].painLevel)
        assertEquals("Updated description - much worse", allPainPoints[0].description)
        assertEquals("Neck", allPainPoints[0].bodyPart) // Unchanged field
    }

    @Test
    fun deletePainPoint_shouldRemoveSpecificEntry() = runTest {
        // Given: Multiple pain points
        val painPoint1 = TestDataBuilder.createPainPoint(bodyPart = "Back")
        val painPoint2 = TestDataBuilder.createPainPoint(bodyPart = "Neck")
        
        painPointRepository.insertPainPoint(painPoint1)
        painPointRepository.insertPainPoint(painPoint2)

        // When: Deleting specific pain point
        painPointRepository.deletePainPoint(painPoint1)

        // Then: Only the other entry should remain
        val remainingPainPoints = painPointRepository.getAllPainPoints().first()
        assertEquals(1, remainingPainPoints.size)
        assertEquals("Neck", remainingPainPoints[0].bodyPart)
    }

    @Test
    fun databaseConsistency_shouldMaintainDataIntegrity() = runTest {
        // Given: Pain points with various data
        val painPoints = listOf(
            TestDataBuilder.createPainPoint(bodyPart = "Back", painLevel = 1),
            TestDataBuilder.createPainPoint(bodyPart = "Neck", painLevel = 10),
            TestDataBuilder.createPainPoint(bodyPart = "Shoulder", painLevel = 5)
        )
        
        // When: Performing multiple operations
        painPoints.forEach { painPointRepository.insertPainPoint(it) }
        
        val backPainPoints = painPointRepository.getPainPointsByBodyPart("Back")
        assertEquals(1, backPainPoints.size)
        
        val allPainPoints = painPointRepository.getAllPainPoints().first()
        assertEquals(3, allPainPoints.size)
        
        // Verify pain level constraints
        assertTrue(allPainPoints.all { it.painLevel in 1..10 })
        
        // Then: Data integrity should be maintained
        val bodyParts = allPainPoints.map { it.bodyPart }.toSet()
        assertEquals(3, bodyParts.size) // All unique body parts
    }
}