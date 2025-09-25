package com.example.laboratoriodeldolor.testing

import com.example.laboratoriodeldolor.repository.*
import com.example.laboratoriodeldolor.*
import io.mockk.mockk
import io.mockk.coEvery
import io.mockk.every
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.Flow

/**
 * Provides fake repository implementations for testing.
 * These can be used as stubs with predefined behavior or mocks for verification.
 */
class FakeRepositoryProvider {
    
    // Mock repositories for testing
    val mockMoodRepository: MoodRepository = mockk()
    val mockPainPointRepository: PainPointRepository = mockk()
    val mockPainLogRepository: PainLogRepository = mockk()
    val mockExerciseRepository: ExerciseRepository = mockk()
    val mockTechniqueRepository: TechniqueRepository = mockk()
    val mockRoutineRepository: RoutineRepository = mockk()
    val mockRoutineStepRepository: RoutineStepRepository = mockk()

    /**
     * Sets up common mock behaviors for mood repository
     */
    fun setupMoodRepositoryMocks() {
        // Setup default flows and suspend functions based on actual MoodRepository interface
        every { mockMoodRepository.getAllMoodEntries() } returns flowOf(TestDataBuilder.createMoodEntries())
        coEvery { mockMoodRepository.getRecentMoodEntries(any()) } returns TestDataBuilder.createMoodEntries()
        coEvery { mockMoodRepository.insertMoodEntry(any()) } returns Unit
        coEvery { mockMoodRepository.deleteOldMoodEntries(any()) } returns Unit
    }

    /**
     * Sets up common mock behaviors for pain point repository
     */
    fun setupPainPointRepositoryMocks() {
        // Setup based on actual PainPointRepository interface  
        every { mockPainPointRepository.getAllPainPoints() } returns flowOf(TestDataBuilder.createPainPoints())
        coEvery { mockPainPointRepository.getAllPainPointsSnapshot() } returns TestDataBuilder.createPainPoints()
        coEvery { mockPainPointRepository.insertPainPoints(any()) } returns Unit
        coEvery { mockPainPointRepository.clearAllPainPoints() } returns Unit
        coEvery { mockPainPointRepository.deletePainPoint(any()) } returns Unit
        coEvery { mockPainPointRepository.getPainPointsByLogId(any()) } returns TestDataBuilder.createPainPoints()
        coEvery { mockPainPointRepository.deleteOldPainPoints(any()) } returns Unit
    }

    /**
     * Sets up common mock behaviors for exercise repository
     */
    fun setupExerciseRepositoryMocks() {
        // Setup based on actual ExerciseRepository interface
        every { mockExerciseRepository.getAllExerciseLogs() } returns flowOf(TestDataBuilder.createExerciseLogs())
        coEvery { mockExerciseRepository.insertExerciseLog(any()) } returns Unit
        coEvery { mockExerciseRepository.deleteMostRecentExerciseLog() } returns Unit
    }

    /**
     * Sets up all repository mocks with default behaviors
     */
    fun setupAllMocks() {
        setupMoodRepositoryMocks()
        setupPainPointRepositoryMocks()
        setupExerciseRepositoryMocks()
        
        // Setup other repositories with basic mocks (simplified for now)
        coEvery { mockPainLogRepository.insertPainLog(any()) } returns 0L
        every { mockTechniqueRepository.getAllTechniques() } returns flowOf(emptyList())
        every { mockRoutineRepository.getAllRoutines() } returns flowOf(emptyList())
        every { mockRoutineStepRepository.getStepsForRoutine(any()) } returns flowOf(emptyList())
    }

    /**
     * Resets all mocks to clean state
     */
    fun resetAllMocks() {
        io.mockk.clearAllMocks()
    }
}

/**
 * In-memory repository implementations for integration testing
 */
class InMemoryRepositoryProvider {
    private val moodEntries = mutableListOf<MoodEntry>()
    private val painPoints = mutableListOf<PainPoint>()
    private val exerciseLogs = mutableListOf<ExerciseLog>()

    val moodRepository = object : MoodRepository {
        override fun getAllMoodEntries(): Flow<List<MoodEntry>> = flowOf(moodEntries.toList())
        override suspend fun getRecentMoodEntries(limit: Int): List<MoodEntry> {
            return moodEntries.sortedByDescending { it.timestamp }.take(limit)
        }
        override suspend fun insertMoodEntry(moodEntry: MoodEntry) {
            moodEntries.add(moodEntry.copy(id = moodEntries.size + 1))
        }
        override suspend fun deleteOldMoodEntries(cutoffMillis: Long) {
            moodEntries.removeIf { it.timestamp < cutoffMillis }
        }
    }

    val painPointRepository = object : PainPointRepository {
        override fun getAllPainPoints(): Flow<List<PainPoint>> = flowOf(painPoints.toList())
        override suspend fun getAllPainPointsSnapshot(): List<PainPoint> = painPoints.toList()
        override suspend fun insertPainPoints(painPointList: List<PainPoint>) {
            painPointList.forEach { painPoint ->
                painPoints.add(painPoint.copy(id = painPoints.size + 1L))
            }
        }
        override suspend fun clearAllPainPoints() = painPoints.clear()
        override suspend fun deletePainPoint(id: Long) {
            painPoints.removeIf { it.id == id }
        }
        override suspend fun getPainPointsByLogId(logId: Long): List<PainPoint> {
            return painPoints.filter { it.logId == logId }
        }
        override suspend fun deleteOldPainPoints(cutoffMillis: Long) {
            painPoints.removeIf { it.timestamp < cutoffMillis }
        }
    }

    val exerciseRepository = object : ExerciseRepository {
        override suspend fun insertExerciseLog(exerciseLog: ExerciseLog) {
            exerciseLogs.add(exerciseLog.copy(id = exerciseLogs.size + 1L))
        }
        override fun getAllExerciseLogs(): Flow<List<ExerciseLog>> = flowOf(exerciseLogs.toList())
        override suspend fun deleteMostRecentExerciseLog() {
            exerciseLogs.maxByOrNull { it.timestamp }?.let { mostRecent ->
                exerciseLogs.remove(mostRecent)
            }
        }
    }

    fun reset() {
        moodEntries.clear()
        painPoints.clear()
        exerciseLogs.clear()
    }
}