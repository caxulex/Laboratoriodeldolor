package com.example.laboratoriodeldolor.repository.impl

import com.example.laboratoriodeldolor.MoodDao
import com.example.laboratoriodeldolor.MoodEntry
import com.example.laboratoriodeldolor.repository.MoodRepository
import kotlinx.coroutines.flow.Flow

/**
 * Implementation of MoodRepository that delegates to Room DAO.
 * This provides a clean abstraction over data access and allows for additional business logic.
 */
class MoodRepositoryImpl(
    private val moodDao: MoodDao
) : MoodRepository {

    override suspend fun insertMoodEntry(moodEntry: MoodEntry) {
        moodDao.insert(moodEntry)
    }

    override fun getAllMoodEntries(): Flow<List<MoodEntry>> {
        return moodDao.getAllEntries()
    }

    override suspend fun getRecentMoodEntries(limit: Int): List<MoodEntry> {
        return moodDao.getRecent(limit)
    }

    override suspend fun deleteOldMoodEntries(cutoffMillis: Long) {
        moodDao.deleteOlderThan(cutoffMillis)
    }
}