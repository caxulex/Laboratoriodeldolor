package com.example.laboratoriodeldolor.repository

import com.example.laboratoriodeldolor.MoodEntry
import com.example.laboratoriodeldolor.repository.impl.MoodRepositoryImpl
import com.example.laboratoriodeldolor.testing.TestDataBuilder
import io.mockk.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for MoodRepositoryImpl.
 * Uses mocked DAO for fast, isolated testing.
 */
class MoodRepositoryImplUnitTest {

    private val mockDao = mockk<com.example.laboratoriodeldolor.MoodDao>()
    private lateinit var moodRepository: MoodRepositoryImpl

    @Before
    fun setup() {
        moodRepository = MoodRepositoryImpl(mockDao)
        clearAllMocks()
    }

    @Test
    fun insertMoodEntry_shouldCallDao() = runTest {
        // Given: Test mood entry
        val testMoodEntry = TestDataBuilder.createMoodEntry(
            emoji = "😊",
            note = "Test note",
            moodScore = 4
        )
        coJustRun { mockDao.insert(testMoodEntry) }

        // When: Inserting mood entry
        moodRepository.insertMoodEntry(testMoodEntry)

        // Then: Should call DAO insert method
        coVerify { mockDao.insert(testMoodEntry) }
    }

    @Test
    fun getAllMoodEntries_shouldReturnDaoFlow() = runTest {
        // Given: Mock DAO returns flow
        val testEntries = TestDataBuilder.createMoodEntries(3)
        every { mockDao.getAllEntries() } returns flowOf(testEntries)

        // When: Getting all entries
        val result = moodRepository.getAllMoodEntries().first()

        // Then: Should return DAO result
        assertEquals(testEntries, result)
        verify { mockDao.getAllEntries() }
    }

    @Test
    fun getRecentMoodEntries_shouldCallDaoWithLimit() = runTest {
        // Given: Mock DAO returns limited results
        val testEntries = TestDataBuilder.createMoodEntries(3)
        coEvery { mockDao.getRecent(3) } returns testEntries

        // When: Getting recent entries with limit
        val result = moodRepository.getRecentMoodEntries(3)

        // Then: Should return DAO result and verify correct parameters
        assertEquals(testEntries, result)
        coVerify { mockDao.getRecent(3) }
    }

    @Test
    fun deleteOldMoodEntries_shouldCallDaoWithCutoff() = runTest {
        // Given: Cutoff timestamp
        val cutoffTime = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        coJustRun { mockDao.deleteOlderThan(cutoffTime) }

        // When: Deleting old entries
        moodRepository.deleteOldMoodEntries(cutoffTime)

        // Then: Should call DAO with correct cutoff
        coVerify { mockDao.deleteOlderThan(cutoffTime) }
    }

    @Test
    fun getAllMoodEntries_flowShouldReturnCorrectData() = runTest {
        // Given: Multiple test entries
        val entries = listOf(
            TestDataBuilder.createMoodEntry(emoji = "😊", timestamp = 1000L),
            TestDataBuilder.createMoodEntry(emoji = "😐", timestamp = 2000L),
            TestDataBuilder.createMoodEntry(emoji = "😢", timestamp = 3000L)
        )
        every { mockDao.getAllEntries() } returns flowOf(entries)

        // When: Getting flow
        val flow = moodRepository.getAllMoodEntries()
        val result = flow.first()

        // Then: Should return all entries
        assertEquals(3, result.size)
        assertEquals("😊", result[0].emoji)
        assertEquals("😐", result[1].emoji)
        assertEquals("😢", result[2].emoji)
    }

    @Test
    fun insertMoodEntry_shouldHandleEmptyNote() = runTest {
        // Given: Mood entry with empty note
        val testEntry = TestDataBuilder.createMoodEntry(note = "")
        coJustRun { mockDao.insert(testEntry) }

        // When: Inserting entry
        moodRepository.insertMoodEntry(testEntry)

        // Then: Should still call DAO
        coVerify { mockDao.insert(testEntry) }
    }

    @Test
    fun getRecentMoodEntries_shouldHandleZeroLimit() = runTest {
        // Given: Zero limit
        coEvery { mockDao.getRecent(0) } returns emptyList()

        // When: Getting recent entries with zero limit
        val result = moodRepository.getRecentMoodEntries(0)

        // Then: Should return empty list
        assertTrue(result.isEmpty())
        coVerify { mockDao.getRecent(0) }
    }

    @Test
    fun deleteOldMoodEntries_shouldHandleFutureTimestamp() = runTest {
        // Given: Future timestamp (should delete all entries)
        val futureTime = System.currentTimeMillis() + (7 * 24 * 60 * 60 * 1000L)
        coJustRun { mockDao.deleteOlderThan(futureTime) }

        // When: Deleting with future timestamp
        moodRepository.deleteOldMoodEntries(futureTime)

        // Then: Should call DAO with future timestamp
        coVerify { mockDao.deleteOlderThan(futureTime) }
    }

    @Test
    fun repositoryMethodsCallCorrectDaoMethods() = runTest {
        // This test verifies that all repository methods map to correct DAO methods
        val testEntry = TestDataBuilder.createMoodEntry()
        val testEntries = listOf(testEntry)
        val testLimit = 5
        val testCutoff = System.currentTimeMillis()

        every { mockDao.getAllEntries() } returns flowOf(testEntries)
        coEvery { mockDao.getRecent(testLimit) } returns testEntries
        coJustRun { mockDao.insert(testEntry) }
        coJustRun { mockDao.deleteOlderThan(testCutoff) }

        // When: Calling various repository methods
        moodRepository.getAllMoodEntries().first()
        moodRepository.getRecentMoodEntries(testLimit)
        moodRepository.insertMoodEntry(testEntry)
        moodRepository.deleteOldMoodEntries(testCutoff)

        // Then: All corresponding DAO methods should be called
        verify { mockDao.getAllEntries() }
        coVerify { mockDao.getRecent(testLimit) }
        coVerify { mockDao.insert(testEntry) }
        coVerify { mockDao.deleteOlderThan(testCutoff) }
    }
}