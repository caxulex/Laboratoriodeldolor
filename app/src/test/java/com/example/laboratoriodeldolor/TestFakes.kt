package com.example.laboratoriodeldolor

import kotlinx.coroutines.flow.flowOf

// Reusable test fakes for DAOs so tests don't need to reimplement common stubs.
fun fakePainPointDaoImpl(): PainPointDao = object : PainPointDao {
    override fun getAll() = flowOf(emptyList<PainPoint>())
    override suspend fun getAllSnapshot(): List<PainPoint> = emptyList()
    override suspend fun insertAll(points: List<PainPoint>) {}
    override suspend fun deleteAll() {}
    override suspend fun deleteById(id: Long) {}
    override suspend fun getByLogId(logId: Long): List<PainPoint> = emptyList()
    override suspend fun deleteOlderThan(cutoffMillis: Long) { /* no-op */ }
}

fun fakeMoodDaoImpl(): MoodDao = object : MoodDao {
    override suspend fun insert(moodEntry: MoodEntry) {}
    override fun getAllEntries() = flowOf(emptyList<MoodEntry>())
    override suspend fun getRecent(limit: Int): List<MoodEntry> = emptyList()
    override suspend fun deleteOlderThan(cutoffMillis: Long) { /* no-op */ }
}
